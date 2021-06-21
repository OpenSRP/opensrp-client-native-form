package com.vijay.jsonwizard.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static com.vijay.jsonwizard.utils.FormUtils.MATCH_PARENT;
import static com.vijay.jsonwizard.utils.FormUtils.WRAP_CONTENT;
import static com.vijay.jsonwizard.utils.FormUtils.getCurrentCheckboxValues;
import static com.vijay.jsonwizard.utils.FormUtils.getLinearLayoutParams;
import static com.vijay.jsonwizard.utils.FormUtils.getValueFromSpOrDpOrPx;

/**
 * Created by vijay on 24-05-2015.
 */
public class CheckBoxFactory extends BaseFactory {
    private FormUtils formUtils = new FormUtils();

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView, LinearLayout checkboxLinearLayout) {
        String error = (String) checkboxLinearLayout.getTag(R.id.error);
        if (checkboxLinearLayout.isEnabled() && error != null) {
            boolean isValid = performValidation(checkboxLinearLayout);
            if (!isValid) {
                return new ValidationStatus(false, error, formFragmentView, checkboxLinearLayout);
            }
        }
        return new ValidationStatus(true, null, formFragmentView, checkboxLinearLayout);
    }

    private static boolean performValidation(LinearLayout checkboxLinearLayout) {
        //Iterate through child layouts skipping first which is the label for the checkbox factory
        boolean isChecked = false;
        for (int i = 0; i < checkboxLinearLayout.getChildCount(); i++) {
            if (i == 0) {
                continue;
            }
            LinearLayout checkboxOptionLayout = (LinearLayout) checkboxLinearLayout.getChildAt(i);
            CheckBox currentCheckbox = (CheckBox) checkboxOptionLayout.getChildAt(0);
            if (currentCheckbox.isChecked()) {
                isChecked = true;
                break;
            }

        }
        return isChecked;
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, formFragment, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, formFragment, false);
    }

    private List<View> attachJson(final String stepName, final Context context, final JSONObject jsonObject, final CommonListener listener, final JsonFormFragment formFragment,
                                  final boolean popup) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        boolean readOnly = false;
        boolean editable = false;

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
        }
        if (jsonObject.has(JsonFormConstants.EDITABLE)) {
            editable = jsonObject.getBoolean(JsonFormConstants.EDITABLE);
        }

        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();
        ImageView editButton;
        LinearLayout rootLayout = getLinearLayout(context);

        rootLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setId(ViewUtil.generateViewId());
        rootLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        rootLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        rootLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        rootLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        rootLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setTag(R.id.extraPopup, popup);
        rootLayout.setTag(R.id.is_checkbox_linear_layout, true);
        canvasIds.put(rootLayout.getId());
        addRequiredValidator(rootLayout, jsonObject);

        Map<String, View> labelViews = formUtils.createRadioButtonAndCheckBoxLabel(stepName, rootLayout, jsonObject, context, canvasIds, readOnly, listener, popup);

        final WidgetArgs widgetArgs = new WidgetArgs()
                .withStepName(stepName)
                .withContext(context)
                .withFormFragment(formFragment)
                .withJsonObject(jsonObject)
                .withListener(listener)
                .withPopup(popup);

        ArrayList<View> editableCheckBoxes = addCheckBoxOptionsElements(readOnly, canvasIds,
                rootLayout, widgetArgs);

        if (labelViews != null && labelViews.size() > 0) {
            editButton = (ImageView) labelViews.get(JsonFormConstants.EDIT_BUTTON);
            if (editButton != null) {
                showEditButton(jsonObject, editableCheckBoxes, editButton, listener);
                editButton.setTag(R.id.extraPopup, popup);
                if (editable) {
                    editButton.setVisibility(View.VISIBLE);
                }
            }

        }
        formUtils.updateValueToJSONArray(jsonObject, jsonObject.optString(JsonFormConstants.VALUE, ""));
        attachRefreshLogic(jsonObject, context, rootLayout);
        rootLayout.setTag(R.id.canvas_ids, canvasIds.toString());

        genericWidgetLayoutHookback(rootLayout, jsonObject, formFragment);

        views.add(rootLayout);
        return views;
    }

    @VisibleForTesting
    protected LinearLayout getLinearLayout(Context context) {
        return (LinearLayout) LayoutInflater.from(context).inflate(R.layout.native_form_compound_button_parent, null);
    }

    private void addRequiredValidator(LinearLayout rootLayout, JSONObject jsonObject) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(JsonFormConstants.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                rootLayout.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR, null));
            }
        }
    }

    private ArrayList<View> addCheckBoxOptionsElements(Boolean readOnly,
                                                       JSONArray canvasIds,
                                                       LinearLayout linearLayout,
                                                       final WidgetArgs widgetArgs) throws JSONException {

        JSONArray checkBoxValues = null;

        if (widgetArgs.getJsonObject().has(JsonFormConstants.VALUE)) {
            String checkBoxValue = widgetArgs.getJsonObject().optString(JsonFormConstants.VALUE, "");
            if (StringUtils.isNotEmpty(checkBoxValue)) {
                checkBoxValues = new JSONArray(checkBoxValue);
            }
        }

        JSONArray options = widgetArgs.getJsonObject().getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        ArrayList<CheckBox> checkBoxes = new ArrayList<>();
        ArrayList<View> checkboxLayouts = new ArrayList<>();
        for (int i = 0; i < options.length(); i++) {
            final JSONObject item = options.getJSONObject(i);

            String openMrsEntityParent = item.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
            String openMrsEntity = item.optString(JsonFormConstants.OPENMRS_ENTITY);
            String openMrsEntityId = item.optString(JsonFormConstants.OPENMRS_ENTITY_ID);

            LinearLayout checkboxLayout = getCheckboxLayout(widgetArgs.getContext());

            final CheckBox checkBox = checkboxLayout.findViewById(R.id.checkbox);
            createCheckBoxText(checkBox, item, widgetArgs.getContext(), readOnly);

            checkBoxes.add(checkBox);
            checkBox.setTag(widgetArgs.getJsonObject().getString(JsonFormConstants.TYPE));
            checkBox.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
            checkBox.setTag(R.id.openmrs_entity, openMrsEntity);
            checkBox.setTag(R.id.openmrs_entity_id, openMrsEntityId);
            checkBox.setTag(R.id.raw_value, item.getString(JsonFormConstants.TEXT));
            checkBox.setTag(R.id.key, widgetArgs.getJsonObject().getString(JsonFormConstants.KEY));
            checkBox.setTag(R.id.type, widgetArgs.getJsonObject().getString(JsonFormConstants.TYPE));
            checkBox.setTag(R.id.childKey, item.getString(JsonFormConstants.KEY));
            checkBox.setTag(R.id.extraPopup, widgetArgs.isPopup());

            checkBox.setOnCheckedChangeListener(widgetArgs.getListener());
            checkBox.setId(ViewUtil.generateViewId());
            checkboxLayout.setId(ViewUtil.generateViewId());
            checkboxLayout.setTag(R.id.type, widgetArgs.getJsonObject().getString(JsonFormConstants.TYPE) + JsonFormConstants.SUFFIX.PARENT);
            canvasIds.put(checkboxLayout.getId());

            final JSONArray finalCheckBoxValues = checkBoxValues;
            ((Activity) widgetArgs.getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (StringUtils.isNotEmpty(item.optString(JsonFormConstants.VALUE))) {
                        widgetArgs.getFormFragment().getJsonApi().getAppExecutors().mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                checkBox.setChecked(Boolean.parseBoolean(item.optString(JsonFormConstants.VALUE)));
                            }
                        });
                    }
                    //Preselect values if they exist
                    try {
                        if (finalCheckBoxValues != null && getCurrentCheckboxValues(finalCheckBoxValues).contains(item.getString(JsonFormConstants.KEY))) {
                            checkBox.setChecked(true);
                        }
                    } catch (JSONException e) {
                        Timber.e(e);
                    }
                }
            });

            checkBox.setEnabled(!readOnly);
            if (i == options.length() - 1) {
                checkboxLayout.setLayoutParams(
                        getLinearLayoutParams(MATCH_PARENT, WRAP_CONTENT, 0, 0, 0, (int)
                                widgetArgs.getContext()
                                        .getResources().getDimension(R.dimen.extra_bottom_margin)));
            }
            //Displaying optional info alert dialog
            ImageView imageView = checkboxLayout.findViewById(R.id.checkbox_info_icon);
            formUtils.showInfoIcon(widgetArgs.getStepName(), widgetArgs.getJsonObject(), widgetArgs.getListener(), FormUtils.getInfoDialogAttributes(item), imageView, canvasIds);

            checkboxLayout.setTag(R.id.canvas_ids, canvasIds.toString());
            checkboxLayouts.add(checkboxLayout);
            linearLayout.addView(checkboxLayout);
        }

        return checkboxLayouts;
    }

    @VisibleForTesting
    protected LinearLayout getCheckboxLayout(Context context) {
        return (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.native_form_item_checkbox, null);
    }

    private void showEditButton(JSONObject jsonObject, List<View> editableViews, ImageView editButton,
                                CommonListener listener) throws JSONException {
        editButton.setTag(R.id.editable_view, editableViews);
        editButton.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        editButton.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        editButton.setOnClickListener(listener);
    }

    private void attachRefreshLogic(JSONObject jsonObject, Context context, LinearLayout rootLayout) {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);

        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(rootLayout);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(rootLayout);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(rootLayout);
        }

        ((JsonApi) context).addFormDataView(rootLayout);
    }

    /**
     * Inflates and set the checkbox text attributes.
     *
     * @param item
     * @param context
     * @param readOnly
     * @throws JSONException
     */
    private void createCheckBoxText(CheckBox checkBox, JSONObject item, Context context, Boolean readOnly)
            throws JSONException {
        String optionTextColor = JsonFormConstants.DEFAULT_TEXT_COLOR;
        String optionTextSize = String.valueOf(context.getResources().getDimension(R.dimen.options_default_text_size));
        if (item.has(JsonFormConstants.TEXT_COLOR)) {
            optionTextColor = item.getString(JsonFormConstants.TEXT_COLOR);
        }
        if (item.has(JsonFormConstants.TEXT_SIZE)) {
            optionTextSize = item.getString(JsonFormConstants.TEXT_SIZE);
        }

        checkBox.setText(item.getString(JsonFormConstants.TEXT));
        checkBox.setTextColor(Color.parseColor(optionTextColor));
        checkBox.setTextSize(getValueFromSpOrDpOrPx(optionTextSize, context));
        checkBox.setEnabled(!readOnly);
        checkBox.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }

    @Override
    @NonNull
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> customTranslatableWidgetFields = new HashSet<>();
        customTranslatableWidgetFields.add(JsonFormConstants.OPTIONS_FIELD_NAME + "." + JsonFormConstants.TEXT);
        return customTranslatableWidgetFields;
    }
}
