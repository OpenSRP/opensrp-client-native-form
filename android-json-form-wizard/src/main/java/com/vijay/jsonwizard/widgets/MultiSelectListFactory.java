package com.vijay.jsonwizard.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.adapter.MultiSelectListAdapter;
import com.vijay.jsonwizard.adapter.MultiSelectListSelectedAdapter;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.domain.MultiSelectListAccessory;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;
import com.vijay.jsonwizard.task.MultiSelectListLoadTask;
import com.vijay.jsonwizard.utils.MultiSelectListUtils;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class MultiSelectListFactory implements FormWidgetFactory {
    public JSONObject jsonObject;
    public String currentAdapterKey;
    private Context context;
    private JsonFormFragment jsonFormFragment;
    private static HashMap<String, MultiSelectListAccessory> multiSelectListAccessoryHashMap = new HashMap<>();

    @Override
    public List<View> getViewsFromJson(@NonNull String stepName, @NonNull Context context, @NonNull JsonFormFragment formFragment, @NonNull JSONObject jsonObject,
                                       @NonNull CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(@NonNull String stepName, @NonNull Context context, @NonNull JsonFormFragment formFragment, @NonNull JSONObject jsonObject,
                                       @NonNull CommonListener listener) throws Exception {
        return getViewsFromJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    private List<View> attachJson(@NonNull final String stepName, @NonNull final Context context, @NonNull JsonFormFragment formFragment, @NonNull JSONObject jsonObject,
                                  @NonNull CommonListener listener, final boolean popup) throws JSONException {
        Timber.i("stepName %s popup %s listener %s", stepName, popup, listener);
        this.jsonFormFragment = formFragment;
        this.jsonObject = jsonObject;
        this.currentAdapterKey = jsonObject.optString(JsonFormConstants.KEY);
        this.context = context;
        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID);

        prepareMultiSelectHashMap(stepName, popup, openMrsEntity, openMrsEntityParent, openMrsEntityId);

        formFragment.getJsonApi().getAppExecutors().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                setUpDialog(context);
            }
        });

        RelativeLayout actionView = createActionView(context);
        RecyclerView recyclerView = createSelectedRecyclerView(context);
        List<View> views = new ArrayList<View>(Arrays.asList(recyclerView, actionView));

        populateTags(actionView, stepName, popup, openMrsEntity, openMrsEntityParent, openMrsEntityId);

        prepareViewChecks(actionView, context);
        addRequiredValidator(actionView, jsonObject);
        ((JsonApi) context).addFormDataView(actionView);
        return views;
    }

    private void addRequiredValidator(RelativeLayout relativeLayout, JSONObject jsonObject) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(JsonFormConstants.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                relativeLayout.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR, null));
            }
        }
    }

    public static ValidationStatus validate(JsonFormFragmentView fragmentView, RelativeLayout multiselectLayout) {
        String error = (String)  multiselectLayout.getTag(R.id.error);
        if (multiselectLayout.isEnabled() && error != null) {
            boolean isValid = performValidation(multiselectLayout);
            if (!isValid) {
                return new ValidationStatus(false, error, fragmentView, multiselectLayout);
            }
        }

        return new ValidationStatus(true, error, fragmentView, multiselectLayout);
    }

    private static boolean performValidation(RelativeLayout relativeLayout) {

        boolean isSelected = false;
        String currentAdapterKey = (String) relativeLayout.getTag(R.id.key);
        MultiSelectListAccessory multiSelectListAccessory = getMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (multiSelectListAccessory != null) {
            List<MultiSelectItem> multiSelectItems = multiSelectListAccessory.getSelectedAdapter().getData();

            if (!multiSelectItems.isEmpty()) {
                isSelected = true;
            }
        }
        return isSelected;
    }


    private void prepareViewChecks(@NonNull RelativeLayout view, @NonNull Context context) {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);

        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            view.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(view);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            view.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(view);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            view.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(view);
        }
    }

    private void populateTags(@NonNull View view, @NonNull String stepName, boolean popUp, String openmrsEntity, String openmrsEntityParent, String openmrsEntityId) {
        JSONArray canvasIds = new JSONArray();
        view.setId(ViewUtil.generateViewId());
        canvasIds.put(view.getId());
        view.setTag(R.id.canvas_ids, canvasIds.toString());
        view.setTag(R.id.key, jsonObject.optString(JsonFormConstants.KEY));
        view.setTag(R.id.openmrs_entity_parent, openmrsEntityParent);
        view.setTag(R.id.openmrs_entity, openmrsEntity);
        view.setTag(R.id.openmrs_entity_id, openmrsEntityId);
        view.setTag(R.id.type, jsonObject.optString(JsonFormConstants.TYPE));
        view.setTag(R.id.extraPopup, popUp);
        view.setTag(R.id.address, stepName + ":" + jsonObject.optString(JsonFormConstants.KEY));
        view.setTag(R.id.is_multiselect_relative_layout, true);
    }

    private void prepareMultiSelectHashMap(@NonNull String stepName, boolean popup, String openmrsEntity, String openmrsEntityParent, String openmrsEntityId) {

        MultiSelectListAccessory multiSelectListAccessory = new MultiSelectListAccessory(
                new MultiSelectListSelectedAdapter(new ArrayList<MultiSelectItem>(), this),
                new MultiSelectListAdapter(prepareListData()),
                null,
                new ArrayList<MultiSelectItem>(),
                new ArrayList<MultiSelectItem>());
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JsonFormConstants.STEPNAME, stepName);
            jsonObject.put(JsonFormConstants.IS_POPUP, popup);
            jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, openmrsEntityParent);
            jsonObject.put(JsonFormConstants.OPENMRS_ENTITY, openmrsEntity);
            jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_ID, openmrsEntityId);
            multiSelectListAccessory.setFormAttributes(jsonObject);
        } catch (JSONException e) {
            Timber.e(e);
        }

        updateMultiSelectListAccessoryHashMap(multiSelectListAccessory);
    }

    protected List<MultiSelectItem> prepareSelectedData() {
        try {
            JSONArray jsonValueArray = jsonObject.has(JsonFormConstants.VALUE) ? jsonObject.optJSONArray(JsonFormConstants.VALUE) : null;
            if (jsonValueArray != null) {
                return MultiSelectListUtils.processOptionsJsonArray(jsonValueArray);
            }

            jsonValueArray = jsonObject.has(JsonFormConstants.VALUE) ? new JSONArray(jsonObject.optString(JsonFormConstants.VALUE)) : null;
            if (jsonValueArray != null) {
                return MultiSelectListUtils.processOptionsJsonArray(jsonValueArray);
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return new ArrayList<>();
    }

    protected List<MultiSelectItem> prepareListData() {
        new MultiSelectListLoadTask(this);
        return new ArrayList<>();
    }

    public List<MultiSelectItem> loadListItems(@Nullable String source) {
        if (StringUtils.isBlank(source)) {
            return MultiSelectListUtils.loadOptionsFromJsonForm(jsonObject);
        } else {
            try {
                String strRepositoryClass = jsonObject.optString(JsonFormConstants.MultiSelectUtils.REPOSITORY_CLASS);
                Class<?> aClass = Class.forName(strRepositoryClass);
                MultiSelectListRepository multiSelectListRepository = (MultiSelectListRepository) aClass.newInstance();
                List<MultiSelectItem> fetchedMultiSelectItems = multiSelectListRepository.fetchData();
                if (fetchedMultiSelectItems == null || fetchedMultiSelectItems.isEmpty()) {
                    Activity activity = jsonFormFragment.getActivity();
                    if (activity != null) {
                        jsonFormFragment.getJsonApi().getAppExecutors().mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast(context, context.getString(R.string.multi_select_list_msg_data_source_invalid));

                            }
                        });
                    }
                    return null;
                }
                return fetchedMultiSelectItems;
            } catch (IllegalAccessException e) {
                Timber.e(e);
            } catch (InstantiationException e) {
                Timber.e(e);
            } catch (ClassNotFoundException e) {
                Timber.e(e);
            }
            return null;
        }
    }

    public void updateSelectedData(@NonNull MultiSelectItem selectedData, boolean clearData) {
        if (clearData) {
            getMultiSelectListSelectedAdapter().getData().clear();
        }
        List<MultiSelectItem> multiSelectItems = getMultiSelectListSelectedAdapter().getData();
        if (multiSelectItems.contains(selectedData)) {
            Utils.showToast(context, String.format(context.getString(R.string.multiselect_already_added_msg), selectedData.getText()));
            return;
        }
        getMultiSelectListSelectedAdapter().getData().add(selectedData);
        Utils.showToast(context, selectedData.getText() + " " + context.getString(R.string.multiselect_msg_on_item_added));
        getMultiSelectListSelectedAdapter().notifyDataSetChanged();
    }

    public void updateListData(boolean clearData) {
        MultiSelectListAccessory multiSelectListAccessory = getMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (clearData) {
            getMultiSelectListAdapter().getData().clear();
        }
        getMultiSelectListAdapter().getData().addAll(multiSelectListAccessory.getItemList());
        getMultiSelectListAdapter().notifyDataSetChanged();
    }

    private void showListDataDialog() {
        if (getAlertDialog() != null) {
            getAlertDialog().show();
        }
    }

    private void setUpDialog(final Context context) {
        if (jsonFormFragment == null) {
            return;
        }
        LayoutInflater inflater = jsonFormFragment.getLayoutInflater();
        View view = inflater.inflate(R.layout.multiselectlistdialog, null);
        ImageView imgClose = view.findViewById(R.id.multiSelectListCloseDialog);
        TextView txtMultiSelectListDialogTitle = view.findViewById(R.id.multiSelectListDialogTitle);
        txtMultiSelectListDialogTitle.setText(jsonObject.optString(JsonFormConstants.MultiSelectUtils.DIALOG_TITLE));
        SearchView searchViewMultiSelect = view.findViewById(R.id.multiSelectListSearchView);
        searchViewMultiSelect.setQueryHint(jsonObject.optString(JsonFormConstants.MultiSelectUtils.SEARCH_HINT));
        final RecyclerView recyclerView = view.findViewById(R.id.multiSelectListRecyclerView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.FullScreenDialogStyle);
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog alertDialog = builder.create();
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        final MultiSelectListAdapter multiSelectListAdapter = getMultiSelectListAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(multiSelectListAdapter);
        searchViewMultiSelect.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                multiSelectListAdapter.getFilter().filter(newText);
                return true;
            }
        });

        multiSelectListAdapter.setOnClickListener(new MultiSelectListAdapter.ClickListener() {
            @Override
            public void onItemClick(View view) {
                int position = recyclerView.getChildLayoutPosition(view);
                handleClickEventOnListData(getMultiSelectListAdapter().getItemAt(position));
            }
        });

        MultiSelectListAccessory multiSelectListAccessory = getMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        multiSelectListAccessory.setAlertDialog(alertDialog);
        updateMultiSelectListAccessoryHashMap(multiSelectListAccessory);
    }

    public static HashMap<String, MultiSelectListAccessory> getMultiSelectListAccessoryHashMap() {
        return multiSelectListAccessoryHashMap;
    }

    private void updateMultiSelectListAccessoryHashMap(@NonNull MultiSelectListAccessory multiSelectListAccessory) {
        getMultiSelectListAccessoryHashMap().put(currentAdapterKey, multiSelectListAccessory);
    }

    protected void handleClickEventOnListData(@NonNull MultiSelectItem multiSelectItem) {
        updateSelectedData(multiSelectItem, false);
        writeToForm();
        getAlertDialog().dismiss();
    }

    public void writeToForm() {
        MultiSelectListUtils.writeToForm(currentAdapterKey, jsonFormFragment, getMultiSelectListAccessoryHashMap());
    }

    public MultiSelectListSelectedAdapter getMultiSelectListSelectedAdapter() {
        MultiSelectListAccessory multiSelectListAccessory = getMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (multiSelectListAccessory != null) {
            return multiSelectListAccessory.getSelectedAdapter();
        }
        return null;
    }

    public AlertDialog getAlertDialog() {
        MultiSelectListAccessory multiSelectListAccessory = getMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (multiSelectListAccessory != null) {
            return multiSelectListAccessory.getAlertDialog();
        }
        return null;
    }

    public MultiSelectListAdapter getMultiSelectListAdapter() {
        MultiSelectListAccessory multiSelectListAccessory = getMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (multiSelectListAccessory != null) {
            return multiSelectListAccessory.getListAdapter();
        }
        return null;
    }

    protected RecyclerView createSelectedRecyclerView(@NonNull Context context) {
        List<MultiSelectItem> multiSelectItems = prepareSelectedData();
        MultiSelectListSelectedAdapter multiSelectListSelectedAdapter = new MultiSelectListSelectedAdapter(multiSelectItems, this);

        MultiSelectListAccessory multiSelectListAccessory = getMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        multiSelectListAccessory.setSelectedAdapter(multiSelectListSelectedAdapter);
        updateMultiSelectListAccessoryHashMap(multiSelectListAccessory);

        writeToForm();

        final RecyclerView recyclerView = new RecyclerView(context);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(context.getResources().getDrawable(R.drawable.multi_select_list_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(multiSelectListSelectedAdapter);
        return recyclerView;
    }

    protected RelativeLayout createActionView(@NonNull Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final RelativeLayout relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.multi_select_list_action_layout, null);
        relativeLayout.setTag(R.id.key, currentAdapterKey);
        Button btn_multi_select_action = relativeLayout.findViewById(R.id.btn_multi_select_action);
        btn_multi_select_action.setText(jsonObject.optString(JsonFormConstants.MultiSelectUtils.BUTTON_TEXT));
        btn_multi_select_action.setTypeface(Typeface.DEFAULT);
        btn_multi_select_action.setTag(R.id.maxSelectable, jsonObject.optString(JsonFormConstants.MultiSelectUtils.MAX_SELECTABLE));
        btn_multi_select_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMaxSelectable = (String) v.getTag(R.id.maxSelectable);
                currentAdapterKey = (String) relativeLayout.getTag(R.id.key);
                int maxSelectable;
                if (!TextUtils.isEmpty(strMaxSelectable)) {
                    maxSelectable = Integer.parseInt(strMaxSelectable);
                    List<MultiSelectItem> multiSelectItems = getMultiSelectListSelectedAdapter().getData();
                    if ((multiSelectItems.size() >= maxSelectable) && !multiSelectItems.isEmpty()) {
                        return;
                    }
                }
                updateListData(true);
                showListDataDialog();
            }
        });

        return relativeLayout;
    }


    public Context getContext() {
        return context;
    }

    public JsonFormFragment getJsonFormFragment() {
        return jsonFormFragment;
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}