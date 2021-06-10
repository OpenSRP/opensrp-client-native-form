package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatRadioButton;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtendedRadioButtonWidgetFactory extends NativeRadioButtonFactory {

    private FormUtils formUtils = new FormUtils();

    @Override
    protected List<View> attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject,
                                    CommonListener commonListener, boolean popup) throws JSONException {
        String widgetType = jsonObject.optString(JsonFormConstants.TYPE, "");
        JSONArray canvasIds = new JSONArray();
        List<View> views = new ArrayList<>(1);
        ImageView editButton;
        if (widgetType.equals(JsonFormConstants.EXTENDED_RADIO_BUTTON)) {
            boolean readOnly = false;
            if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
                readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            }
            LinearLayout rootLayout = getLinearRootLayout(context);
            Map<String, View> labelViews = formUtils.createRadioButtonAndCheckBoxLabel(stepName, rootLayout, jsonObject, context, canvasIds, readOnly,
                            commonListener, popup);
            String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
            String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY);
            String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID);
            String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
            String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
            String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
            LinearLayout.LayoutParams layoutParams =
                    FormUtils.getLinearLayoutParams(FormUtils.MATCH_PARENT, FormUtils.WRAP_CONTENT, 1, 3, 1, 3);
            RadioGroup radioGroup = getRootLayout(context);
            radioGroup.setLayoutParams(layoutParams);
            radioGroup.setId(ViewUtil.generateViewId());
            canvasIds.put(radioGroup.getId());
            radioGroup.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());
            radioGroup.setTag(com.vijay.jsonwizard.R.id.key, jsonObject.getString(JsonFormConstants.KEY));
            radioGroup.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_parent, openMrsEntityParent);
            radioGroup.setTag(com.vijay.jsonwizard.R.id.openmrs_entity, openMrsEntity);
            radioGroup.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_id, openMrsEntityId);
            radioGroup.setTag(com.vijay.jsonwizard.R.id.extraPopup, popup);
            radioGroup.setTag(com.vijay.jsonwizard.R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
            radioGroup.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());
            radioGroup
                    .setTag(com.vijay.jsonwizard.R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));

            attachRefreshLogic(context, relevance, constraints, calculation, radioGroup);
            addRadioButtons(stepName, context, jsonObject, commonListener, popup, radioGroup, readOnly, canvasIds);
            rootLayout.addView(radioGroup);
            views.add(rootLayout);
            if (labelViews.size() > 0) {
                editButton = (ImageView) labelViews.get(JsonFormConstants.EDIT_BUTTON);
                if (editButton != null) {
                    FormUtils.setEditButtonAttributes(jsonObject, radioGroup, editButton, commonListener);
                }

            }
        } else {
            return super.attachJson(stepName, context, formFragment, jsonObject, commonListener, popup);
        }

        return views;
    }

    private RadioGroup getRootLayout(Context context) {
        return (RadioGroup) LayoutInflater.from(context).inflate(R.layout.extended_radio_button, null);
    }

    private void attachRefreshLogic(Context context, String relevance, String constraints, String calculation, RadioGroup radioGroup) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            radioGroup.setTag(com.vijay.jsonwizard.R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(radioGroup);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            radioGroup.setTag(com.vijay.jsonwizard.R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(radioGroup);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            radioGroup.setTag(com.vijay.jsonwizard.R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(radioGroup);
        }
    }

    private void addRadioButtons(String stepName, Context context, JSONObject jsonObject, CommonListener commonListener,
                                 boolean popup, RadioGroup rootLayout, boolean readOnly, JSONArray canvasIds)
            throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        String optionTextSize =
                String.valueOf(context.getResources().getDimension(com.vijay.jsonwizard.R.dimen.options_default_text_size));
        String optionTextColor = JsonFormConstants.DEFAULT_TEXT_COLOR;
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (item.has(JsonFormConstants.TEXT_SIZE)) {
                    optionTextSize = item.getString(JsonFormConstants.TEXT_SIZE);
                }
                if (item.has(JsonFormConstants.TEXT_COLOR)) {
                    optionTextColor = item.getString(JsonFormConstants.TEXT_COLOR);
                }
                String openMrsEntityParent = item.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
                String openMrsEntity = item.optString(JsonFormConstants.OPENMRS_ENTITY);
                String openMrsEntityId = item.optString(JsonFormConstants.OPENMRS_ENTITY_ID);
                LinearLayout.LayoutParams layoutParams =
                        FormUtils.getLinearLayoutParams(FormUtils.MATCH_PARENT, FormUtils.WRAP_CONTENT, 1, 3, 1, 3);
                layoutParams.setMargins(0, 5, 0, 5);
                AppCompatRadioButton radioButton = new AppCompatRadioButton(context);
                radioButton.setLayoutParams(layoutParams);
                radioButton.setId(ViewUtil.generateViewId());
                radioButton.setText(item.getString(JsonFormConstants.TEXT));
                radioButton.setTextColor(Color.parseColor(optionTextColor));
                radioButton.setTextSize(FormUtils.getValueFromSpOrDpOrPx(optionTextSize, context));
                radioButton.setTag(com.vijay.jsonwizard.R.id.key, jsonObject.getString(JsonFormConstants.KEY));
                radioButton.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_parent, openMrsEntityParent);
                radioButton.setTag(com.vijay.jsonwizard.R.id.openmrs_entity, openMrsEntity);
                radioButton.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_id, openMrsEntityId);
                radioButton.setTag(com.vijay.jsonwizard.R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
                radioButton.setTag(com.vijay.jsonwizard.R.id.childKey, item.getString(JsonFormConstants.KEY));
                radioButton.setTag(com.vijay.jsonwizard.R.id.address,
                        stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
                radioButton.setTag(com.vijay.jsonwizard.R.id.extraPopup, popup);
                radioButton.setOnCheckedChangeListener(commonListener);
                radioButton.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());
                if (!TextUtils.isEmpty(jsonObject.optString(JsonFormConstants.VALUE)) &&
                        jsonObject.optString(JsonFormConstants.VALUE).equals(item.getString(JsonFormConstants.KEY))) {
                    radioButton.setChecked(true);
                }
                radioButton.setTag(com.vijay.jsonwizard.R.id.relevance, relevance);
                radioButton.setTag(com.vijay.jsonwizard.R.id.constraints, constraints);
                radioButton.setTag(com.vijay.jsonwizard.R.id.calculation, calculation);
                radioButton.setEnabled(!readOnly);
                //setRadioButtonIcon(radioButton, item, context);
                rootLayout.addView(radioButton);
            }
        } else {
            Toast.makeText(context, R.string.extended_radio_info_text_ensure_options_set, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}
