package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nipun on 30/05/15.
 */
public class SpinnerFactory extends BaseFactory {
    private FormUtils formUtils = new FormUtils();

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView, MaterialSpinner spinner) {
        if (spinner.getTag(R.id.v_required) == null) {
            return new ValidationStatus(true, null, formFragmentView, spinner);
        }
        boolean isRequired = (boolean) spinner.getTag(R.id.v_required);
        String error = (String) spinner.getTag(R.id.error);
        int selectedItemPosition = spinner.getSelectedItemPosition();

        if (isRequired && selectedItemPosition == 0 && spinner.isEnabled()) {
            return new ValidationStatus(false, error, formFragmentView, spinner);
        }
        return new ValidationStatus(true, null, formFragmentView, spinner);
    }

    private static void setRequiredOnHint(MaterialSpinner spinner) {
        if (!TextUtils.isEmpty(spinner.getHint())) {
            SpannableString hint = new SpannableString(spinner.getHint() + " *");
            hint.setSpan(new ForegroundColorSpan(Color.RED), hint.length() - 1, hint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spinner.setHint(hint);
        }
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, formFragment, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, formFragment, false);
    }

    private List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener listener, JsonFormFragment formFragment,
                                  boolean popup) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();
        RelativeLayout spinnerRelativeLayout = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.native_form_item_spinner, null);

        setViewTags(jsonObject, canvasIds, stepName, popup, openMrsEntityParent, openMrsEntity, openMrsEntityId,
                spinnerRelativeLayout);
        spinnerRelativeLayout.setTag(R.id.canvas_ids, canvasIds.toString());

        addSpinner(jsonObject, spinnerRelativeLayout, listener, formFragment, canvasIds, stepName, popup, context);

        genericWidgetLayoutHookback(spinnerRelativeLayout, jsonObject, formFragment);

        views.add(spinnerRelativeLayout);
        return views;
    }

    private void addSpinner(JSONObject jsonObject, RelativeLayout spinnerRelativeLayout, CommonListener listener, JsonFormFragment jsonFormFragment,
                            JSONArray canvasIds, String stepName, boolean popup, Context context) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String labelInfoText = jsonObject.optString(JsonFormConstants.LABEL_INFO_TEXT, "");
        String labelInfoTitle = jsonObject.optString(JsonFormConstants.LABEL_INFO_TITLE, "");
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculations = jsonObject.optString(JsonFormConstants.CALCULATION);

        MaterialSpinner spinner = spinnerRelativeLayout.findViewById(R.id.material_spinner);
        ImageView spinnerInfoIconImageView = spinnerRelativeLayout.findViewById(R.id.spinner_info_icon);
        ImageView editButton = spinnerRelativeLayout.findViewById(R.id.spinner_edit_button);
        FormUtils.setEditButtonAttributes(jsonObject, spinner, editButton, listener);
        String hint = jsonObject.optString(JsonFormConstants.HINT);
        if (!TextUtils.isEmpty(hint)) {
            spinner.setHint(jsonObject.getString(JsonFormConstants.HINT));
            spinner.setFloatingLabelText(jsonObject.getString(JsonFormConstants.HINT));
        }

        // Support defining key-value pairs as part of an options field
        // or as separate key and value JSON arrays
        Pair<JSONArray, JSONArray> optionsKeyValPairs = null;
        JSONArray keysJson = null;
        JSONArray options = jsonObject.optJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        if (options != null) {
            optionsKeyValPairs = getOptionsKeyValPairs(options);
            keysJson = optionsKeyValPairs.first;
            spinner.setTag(R.id.keys, keysJson);
        } else if (jsonObject.has(JsonFormConstants.KEYS)) {
            keysJson = jsonObject.getJSONArray(JsonFormConstants.KEYS);
            spinner.setTag(R.id.keys, keysJson);
        }

        setViewTags(jsonObject, canvasIds, stepName, popup, openMrsEntityParent, openMrsEntity, openMrsEntityId, spinner);

        addSkipLogicTags(context, relevance, constraints, calculations, spinner);

        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.optBoolean(JsonFormConstants.VALUE, false);
            if (Boolean.TRUE.equals(requiredValue)) {
                setRequiredOnHint(spinner);
            }
            spinner.setTag(R.id.v_required, requiredValue);
            spinner.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR, null));
        }

        String valueToSelect = "";
        int indexToSelect = -1;
        if (!TextUtils.isEmpty(jsonObject.optString(JsonFormConstants.VALUE))) {
            valueToSelect = jsonObject.optString(JsonFormConstants.VALUE);
        }

        FormUtils.setEditMode(jsonObject, spinner, editButton);

        String[] values = null;
        JSONArray valuesJson = optionsKeyValPairs == null ? jsonObject.optJSONArray(JsonFormConstants.VALUES) : optionsKeyValPairs.second;
        if (valuesJson != null && valuesJson.length() > 0) {
            values = new String[valuesJson.length()];
            for (int i = 0; i < valuesJson.length(); i++) {
                values[i] = valuesJson.optString(i);
                if (keysJson == null && valueToSelect.equals(values[i])) {
                    indexToSelect = i;
                } else if (keysJson != null && valueToSelect.equals(keysJson.optString(i))) {
                    indexToSelect = i;
                }
            }
        }

        if (values != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.native_form_simple_list_item_1, values);
            spinner.setAdapter(adapter);
            spinner.setSelection(indexToSelect + 1, true);
            spinner.setOnItemSelectedListener(listener);
        }
        ((JsonApi) context).addFormDataView(spinner);

        formUtils.showInfoIcon(stepName, jsonObject, listener, FormUtils.getInfoDialogAttributes(jsonObject), spinnerInfoIconImageView,
                canvasIds);
        spinner.setTag(R.id.canvas_ids, canvasIds.toString());
    }


    private Pair<JSONArray, JSONArray> getOptionsKeyValPairs(JSONArray options) throws JSONException {
        JSONArray optionKeys = new JSONArray();
        JSONArray optionValues = new JSONArray();
        for (int i = 0; i < options.length(); i++) {
            optionKeys.put(options.getJSONObject(i).optString(JsonFormConstants.KEY));
            optionValues.put(options.getJSONObject(i).optString(JsonFormConstants.TEXT));
        }
        return new Pair<>(optionKeys, optionValues);
    }

    private void setViewTags(JSONObject jsonObject, JSONArray canvasIds, String stepName, boolean popup,
                             String openMrsEntityParent, String openMrsEntity, String openMrsEntityId,
                             View view) throws JSONException {
        view.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        view.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        view.setTag(R.id.openmrs_entity, openMrsEntity);
        view.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        view.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        view.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        view.setTag(R.id.extraPopup, popup);
        view.setId(ViewUtil.generateViewId());
        canvasIds.put(view.getId());
    }

    private void addSkipLogicTags(Context context, String relevance, String constraints, String calculations,
                                  MaterialSpinner spinner) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            spinner.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(spinner);
        }
        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            spinner.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(spinner);
        }

        if (!TextUtils.isEmpty(calculations) && context instanceof JsonApi) {
            spinner.setTag(R.id.calculation, calculations);
            ((JsonApi) context).addCalculationLogicView(spinner);
        }
    }

    @Override
    @NonNull
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> customTranslatableWidgetFields = new HashSet<>();
        customTranslatableWidgetFields.add(JsonFormConstants.OPTIONS_FIELD_NAME + "." + JsonFormConstants.TEXT);
        customTranslatableWidgetFields.add(JsonFormConstants.VALUES);
        customTranslatableWidgetFields.add(JsonFormConstants.LABEL_INFO_TITLE);
        customTranslatableWidgetFields.add(JsonFormConstants.LABEL_INFO_TEXT);
        customTranslatableWidgetFields.add(JsonFormConstants.DYNAMIC_LABEL_INFO);
        return customTranslatableWidgetFields;
    }
}
