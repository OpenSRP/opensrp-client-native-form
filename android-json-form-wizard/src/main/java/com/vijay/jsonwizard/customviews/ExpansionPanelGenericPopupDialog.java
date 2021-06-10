package com.vijay.jsonwizard.customviews;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.ExpansionPanelValuesModel;
import com.vijay.jsonwizard.event.RefreshExpansionPanelEvent;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.SecondaryValueModel;
import com.vijay.jsonwizard.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

/**
 * Performs the expansion panel's {@link com.vijay.jsonwizard.widgets.ExpansionPanelFactory} functionality, which includes
 * Reading and assigning values on load
 * Creation of the sub forms widgets
 * Saving the new selected values to the expansion panel's widget `value` attribute
 */
public class ExpansionPanelGenericPopupDialog extends GenericPopupDialog {
    protected Toolbar mToolbar;
    protected String container;
    private Map<String, ExpansionPanelValuesModel> secondaryValuesMap = new HashMap<>();
    private FormUtils formUtils = new FormUtils();
    private Activity activity;
    private Context context;
    private String header;
    private LinearLayout linearLayout;
    private Utils utils = new Utils();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (Activity) context;
        setGenericPopUpDialog();
    }

    /**
     * Loads the values from the expansion panel
     *
     * @throws JSONException
     */
    @Override
    public void loadPartialSecondaryValues() throws JSONException {
        if (!TextUtils.isEmpty(getWidgetType()) && getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
            setSecondaryValues(formUtils.loadExpansionPanelValues(getMainFormFields(), getParentKey()));
        } else {
            super.loadPartialSecondaryValues();
        }
    }

    /**
     * Using the secondary values extracted from {@link ExpansionPanelGenericPopupDialog#loadPartialSecondaryValues()} it creates a map of the expansion panels values
     */
    @Override
    public void createSecondaryValuesMap() {
        if (!TextUtils.isEmpty(getWidgetType()) && getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
            if (getSecondaryValues() != null) {
                secondaryValuesMap = formUtils.createSecondaryValuesMap(getSecondaryValues());
            }
        } else {
            super.createSecondaryValuesMap();
        }

    }

    /**
     * Loads the sub from the sub form name declared on the expansion panel widget
     */
    @Override
    public void loadSubForms() {
        if (!TextUtils.isEmpty(getFormIdentity())) {
            JSONObject subForm = getSubForm();
            if (subForm != null) {
                try {
                    if (subForm.has(JsonFormConstants.CONTENT_FORM)) {
                        setSpecifyContent(subForm.getJSONArray(JsonFormConstants.CONTENT_FORM));
                        setSubFormsFields(addFormValues(getSpecifyContent()));
                    } else {
                        Utils.showToast(activity, activity.getApplicationContext().getResources()
                                .getString(com.vijay.jsonwizard.R.string.please_specify_content));
                        ExpansionPanelGenericPopupDialog.this.dismiss();
                    }
                } catch (JSONException e) {
                    Timber.e(e, "FullScreenGenericPopupDialog --> loadSubForms");
                }
            } else {
                ExpansionPanelGenericPopupDialog.this.dismiss();
            }
        }
    }

    /**
     * Adds the values from the map created by {@link ExpansionPanelGenericPopupDialog#createSecondaryValuesMap()} to the fields extracted from the sub form by {@link ExpansionPanelGenericPopupDialog#loadSubForms()}
     *
     * @param formValues {@link JSONArray} Form fields extracted by {@link ExpansionPanelGenericPopupDialog#loadSubForms()}
     * @return formFields {@link JSONArray} Form fields with values added.
     */
    @Override
    protected JSONArray addFormValues(JSONArray formValues) {
        if (!TextUtils.isEmpty(getWidgetType()) && getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
            return formUtils.addExpansionPanelFormValues(formValues, secondaryValuesMap);
        } else {
            super.addFormValues(formValues);
        }
        return formValues;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!TextUtils.isEmpty(getWidgetType()) && getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
            ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_generic_dialog, container, false);
            setDialogView(dialogView);
            attachToolBar(dialogView);
            attachDialogShowListener();
            attachCancelDialogButton(dialogView);
            attachOkDialogButton(dialogView);

            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }

            Utils.hideProgressDialog();
            return dialogView;
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void setStyle() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    private void attachToolBar(ViewGroup dialogView) {
        mToolbar = dialogView.findViewById(R.id.generic_toolbar);
        changeToolbarColor();

        TextView toolBar = mToolbar.findViewById(R.id.txt_title_label);
        if (!TextUtils.isEmpty(header)) {
            toolBar.setText(header);
        }
    }

    private void attachDialogShowListener() {
        new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager inputManager =
                        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(),
                        HIDE_NOT_ALWAYS);
            }
        };
    }

    private void attachCancelDialogButton(ViewGroup dialogView) {
        final AppCompatImageButton cancelButton;
        cancelButton = dialogView.findViewById(R.id.generic_dialog_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getJsonApi().updateGenericPopupSecondaryValues(new JSONArray(), getStepName());
                setFormFragment(null);
                setFormIdentity(null);
                setFormLocation(null);
                setContext(null);
                getJsonApi().setGenericPopup(null);
                ExpansionPanelGenericPopupDialog.this.dismissAllowingStateLoss();
                utils.enableExpansionPanelViews(linearLayout);
            }
        });
    }

    private void attachOkDialogButton(ViewGroup dialogView) {
        Button okButton;
        okButton = dialogView.findViewById(R.id.generic_dialog_done_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passData();
                getJsonApi().setGenericPopup(null);
                getJsonApi().updateGenericPopupSecondaryValues(new JSONArray(), getStepName());
                ExpansionPanelGenericPopupDialog.this.dismissAllowingStateLoss();
            }
        });
    }

    private void changeToolbarColor() {
        if (!TextUtils.isEmpty(getContainer())) {
            switch (getContainer()) {
                case JsonFormConstants.JsonFormConstantsUtils.ANC_TEST:
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.contact_tests_actionbar));
                    break;
                case JsonFormConstants.JsonFormConstantsUtils.ANC_COUNSELLING_TREATMENT:
                    mToolbar.setBackgroundColor(getResources().getColor(R.color.contact_counselling_actionbar));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * In the ANC case this is used to get the container the Expansion panel of loading to set its toolbar color.
     *
     * @return containerName - either C&T or Tests
     */
    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    /**
     * Receives the generic popup data from Generic Dialog fragment
     *
     * @param parentKey {@link String}
     * @param childKey  {@link String}
     */
    public void onDataPass(String parentKey, String childKey) {
        JSONObject mJSONObject = getJsonApi().getmJSONObject();
        if (mJSONObject != null) {
            try {
                if (getMainFormFields().length() > 0) {
                    for (int i = 0; i < getMainFormFields().length(); i++) {
                        JSONObject item = getMainFormFields().getJSONObject(i);
                        if (item != null && item.getString(JsonFormConstants.KEY).equals(parentKey)) {
                            addValues(getJsonObjectToUpdate(item, childKey));
                        }
                    }
                }
                getJsonApi().setmJSONObject(mJSONObject);
            } catch (JSONException e) {
                Timber.e(e, "FullScreenGenericPopupDialog --> onDataPass");
            }
        }
    }

    /**
     * Appends the expansion panel values formatted by {@link ExpansionPanelGenericPopupDialog#createValues()} to the widget in this case the {@link com.vijay.jsonwizard.widgets.ExpansionPanelFactory} Expansion panel
     *
     * @param item {@link JSONObject} the widget to update.
     */
    protected void addValues(JSONObject item) {
        JSONArray secondaryValuesArray = createValues();
        try {
            JSONArray orderedValues = orderExpansionPanelValues(secondaryValuesArray);
            item.remove(JsonFormConstants.VALUE);
            item.put(JsonFormConstants.VALUE, orderedValues);
            setNewSelectedValues(orderedValues);
            Utils.postEvent(new RefreshExpansionPanelEvent(orderedValues, linearLayout));
            addRequiredFields(item);
        } catch (Exception e) {
            Timber.e(e, "FullScreenGenericPopupDialog --> addValues");
        }
    }

    private JSONArray orderExpansionPanelValues(JSONArray expansionPanelValues) throws JSONException {
        JSONArray formattedArray = new JSONArray();
        if (expansionPanelValues != null && expansionPanelValues.length() > 0) {
            JSONArray sortedItemsWithNulls = new JSONArray();
            for (int i = 0; i < expansionPanelValues.length(); i++) {
                JSONObject valueItem = expansionPanelValues.getJSONObject(i);
                if (valueItem.has(JsonFormConstants.INDEX)) {
                    int itemIndex = valueItem.getInt(JsonFormConstants.INDEX);
                    sortedItemsWithNulls.put(itemIndex, valueItem);
                }
            }

            for (int k = 0; k < sortedItemsWithNulls.length(); k++) {
                if (!sortedItemsWithNulls.isNull(k)) {
                    formattedArray.put(sortedItemsWithNulls.getJSONObject(k));
                }
            }
        }

        return formattedArray;
    }

    /**
     * Adds a new attribute field called required_fields to the accordion object that it
     * has been passed. It does so by getting only visible that are required;
     *
     * @param theAccordion accordion/Expansion panel that is to be updated with the new attribute
     * @throws JSONException Exception thrown
     */
    private void addRequiredFields(JSONObject theAccordion) throws JSONException {
        //Clear the current required fields first
        if (theAccordion.has(JsonFormConstants.REQUIRED_FIELDS)) {
            theAccordion.remove(JsonFormConstants.REQUIRED_FIELDS);
        }

        JSONArray requiredFieldsList = new JSONArray();
        JSONArray formFields = getSubFormsFields();

        for (int index = 0; index < formFields.length(); index++) {
            JSONObject fieldObject = formFields.getJSONObject(index);
            boolean isFieldVisible = !fieldObject.has(JsonFormConstants.IS_VISIBLE) ||
                    fieldObject.getBoolean(JsonFormConstants.IS_VISIBLE);
            if (FormUtils.isFieldRequired(fieldObject)) {
                if (!isFieldVisible) {
                    continue;
                }
                requiredFieldsList.put(fieldObject.getString(JsonFormConstants.KEY));
            }
        }

        if (requiredFieldsList.length() > 0) {
            theAccordion.put(JsonFormConstants.REQUIRED_FIELDS, requiredFieldsList);
        }
    }

    @Override
    public void setContext(Context context) throws IllegalStateException {
        super.setContext(context);
        this.context = context;
    }

    @Override
    protected void passData() {
        if (!TextUtils.isEmpty(getWidgetType()) && getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
            onDataPass(getParentKey(), getChildKey());
        } else {
            onGenericDataPass(getParentKey(), getChildKey());
        }
    }

    /**
     * Finds the actual widget to be updated and secondary values added on
     *
     * @param jsonObject {@link JSONObject}
     * @param childKey   {@link String}
     * @return item {@link JSONObject}
     */
    @Override
    protected JSONObject getJsonObjectToUpdate(JSONObject jsonObject, String childKey) {
        JSONObject item = new JSONObject();
        try {
            if (jsonObject != null && jsonObject.has(JsonFormConstants.TYPE)) {
                if ((jsonObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.CHECK_BOX) || jsonObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.NATIVE_RADIO_BUTTON)) && childKey != null) {
                    JSONArray options = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
                    if (options != null) {
                        for (int i = 0; i < options.length(); i++) {
                            JSONObject childItem = options.getJSONObject(i);
                            if (childItem != null && childItem.has(JsonFormConstants.KEY) && childKey.equals(childItem.getString(JsonFormConstants.KEY))) {
                                item = childItem;
                            }
                        }
                    }
                } else {
                    item = jsonObject;
                }
            } else {
                item = jsonObject;
            }
        } catch (Exception e) {
            Timber.e(e, "FullScreenGenericPopupDialog --> getJsonObjectToUpdate");
        }

        return item;
    }

    /**
     * Formats the expansion panels popup values to a {@link JSONArray} to be attached to the main widget.
     *
     * @return selectedValues {@link JSONArray}
     */
    @Override
    protected JSONArray createValues() {
        JSONArray selectedValues = new JSONArray();
        try {
            if (getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
                selectedValues = formUtils.createExpansionPanelValues(getSubFormsFields());
            } else {
                selectedValues = super.createValues();
            }
        } catch (JSONException e) {
            Timber.e(e, " --> createValues");
        }
        return selectedValues;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyVariables();
        JsonApi ancJsonApi = (JsonApi) activity;
        ancJsonApi.setGenericPopup(null);
    }

    private void destroyVariables() {
        setSecondaryValuesMap(new HashMap<String, SecondaryValueModel>());
    }

    public void setGenericPopUpDialog() {
        JsonApi ancJsonApi = (JsonApi) activity;
        ancJsonApi.setGenericPopup(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
        // Utils.hideProgressDialog();
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setLinearLayout(LinearLayout linearLayout) {
        this.linearLayout = linearLayout;
    }

    @Override
    protected void createDialogWindow() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);    }
}
