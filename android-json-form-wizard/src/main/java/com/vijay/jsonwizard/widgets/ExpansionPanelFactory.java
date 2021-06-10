package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.listeners.ExpansionPanelRecordButtonClickListener;
import com.vijay.jsonwizard.listeners.ExpansionPanelUndoButtonClickListener;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpansionPanelFactory implements FormWidgetFactory {
    private ExpansionPanelRecordButtonClickListener expansionPanelRecordButtonClickListener = new ExpansionPanelRecordButtonClickListener();
    private ExpansionPanelUndoButtonClickListener expansionPanelUndoButtonClickListener = new ExpansionPanelUndoButtonClickListener();
    private FormUtils formUtils = new FormUtils();
    private Utils utils = new Utils();

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment jsonFormFragment,
                                       JSONObject jsonObject, CommonListener commonListener, boolean popup)
            throws Exception {
        return attachJson(stepName, context, jsonFormFragment, jsonObject, commonListener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment jsonFormFragment,
                                       JSONObject jsonObject, CommonListener commonListener) throws Exception {
        return attachJson(stepName, context, jsonFormFragment, jsonObject, commonListener, false);
    }

    public List<View> attachJson(String stepName, Context context, JsonFormFragment jsonFormFragment, JSONObject jsonObject,
                                 CommonListener commonListener, boolean popup) throws JSONException {
        JSONArray canvasIds = new JSONArray();
        List<View> views = new ArrayList<>(1);

        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        LinearLayout.LayoutParams layoutParams =
                FormUtils.getLinearLayoutParams(FormUtils.MATCH_PARENT, FormUtils.MATCH_PARENT, 1, 2, 1, 2);
        LinearLayout rootLayout = getRootLayout(context);
        rootLayout.setLayoutParams(layoutParams);
        rootLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(rootLayout.getId());
        rootLayout.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());
        rootLayout.setTag(com.vijay.jsonwizard.R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_parent, openMrsEntityParent);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.openmrs_entity, openMrsEntity);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_id, openMrsEntityId);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.extraPopup, popup);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        rootLayout.setTag(com.vijay.jsonwizard.R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());

        attachRefreshLogic(context, relevance, constraints, calculation, rootLayout);
        attachLayout(stepName, context, jsonFormFragment, jsonObject, commonListener, rootLayout);

        views.add(rootLayout);
        return views;
    }

    public LinearLayout getRootLayout(Context context) {
        return (LinearLayout) LayoutInflater.from(context).inflate(R.layout.native_expansion_panel, null);
    }

    private void attachRefreshLogic(Context context, String relevance, String constraints, String calculation, LinearLayout rootLayout) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            rootLayout.setTag(com.vijay.jsonwizard.R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(rootLayout);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            rootLayout.setTag(com.vijay.jsonwizard.R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(rootLayout);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            rootLayout.setTag(com.vijay.jsonwizard.R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(rootLayout);
        }
    }

    private void attachLayout(String stepName, final Context context, JsonFormFragment jsonFormFragment,
                              JSONObject jsonObject, CommonListener commonListener, LinearLayout rootLayout)
            throws JSONException {
        String accordionText = jsonObject.optString(JsonFormConstants.TEXT, "");
        RelativeLayout expansionHeader = rootLayout.findViewById(R.id.expansionHeader);
        RelativeLayout expansionHeaderLayout = expansionHeader.findViewById(R.id.expansion_header_layout);
        addRecordViewTags(expansionHeaderLayout, jsonObject, stepName, commonListener, jsonFormFragment, context);
        expansionHeaderLayout.setOnClickListener(expansionPanelRecordButtonClickListener);

        ImageView statusImage = expansionHeaderLayout.findViewById(R.id.statusImageView);
        addRecordViewTags(statusImage, jsonObject, stepName, commonListener, jsonFormFragment, context);
        statusImage.setOnClickListener(expansionPanelRecordButtonClickListener);

        ImageView infoIcon = expansionHeader.findViewById(R.id.accordion_info_icon);
        addRecordViewTags(infoIcon, jsonObject, stepName, commonListener, jsonFormFragment, context);
        infoIcon.setOnClickListener(expansionPanelRecordButtonClickListener);

        CustomTextView headerText = expansionHeaderLayout.findViewById(R.id.topBarTextView);
        headerText.setText(accordionText);
        addRecordViewTags(headerText, jsonObject, stepName, commonListener, jsonFormFragment, context);
        headerText.setOnClickListener(expansionPanelRecordButtonClickListener);

        displayInfoIcon(jsonObject, commonListener, infoIcon);
        changeStatusIcon(statusImage, jsonObject, context);
        attachContent(rootLayout, context, jsonObject);
        addBottomSection(stepName, context, jsonFormFragment, jsonObject, commonListener, rootLayout);
    }


    private void addRecordViewTags(View recordView, JSONObject jsonObject, String stepName, CommonListener commonListener,
                                   JsonFormFragment jsonFormFragment, Context context) throws JSONException {
        recordView.setTag(R.id.specify_content, jsonObject.optString(JsonFormConstants.CONTENT_FORM, ""));
        recordView.setTag(R.id.specify_context, context);
        recordView.setTag(R.id.specify_content_form, jsonObject.optString(JsonFormConstants.CONTENT_FORM_LOCATION, ""));
        recordView.setTag(R.id.specify_step_name, stepName);
        recordView.setTag(R.id.specify_listener, commonListener);
        recordView.setTag(R.id.specify_fragment, jsonFormFragment);
        recordView.setTag(R.id.header, jsonObject.optString(JsonFormConstants.TEXT, ""));
        if (jsonObject.getString(JsonFormConstants.TYPE) != null) {
            recordView.setTag(R.id.secondaryValues,
                    formUtils.getSecondaryValues(jsonObject, jsonObject.getString(JsonFormConstants.TYPE)));
        }
        recordView.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        recordView.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        if (jsonObject.has(JsonFormConstants.JsonFormConstantsUtils.CONTACT_CONTAINER)) {
            String container = jsonObject.optString(JsonFormConstants.JsonFormConstantsUtils.CONTACT_CONTAINER, null);
            recordView.setTag(R.id.contact_container, container);
        }

    }

    private void displayInfoIcon(JSONObject jsonObject, CommonListener commonListener, ImageView accordionInfoWidget)
            throws JSONException {
        String accordionInfoText = jsonObject.optString(JsonFormConstants.ACCORDION_INFO_TEXT, null);
        String accordionInfoTitle = jsonObject.optString(JsonFormConstants.ACCORDION_INFO_TITLE, null);
        String accordionKey = jsonObject.getString(JsonFormConstants.KEY);
        String accordionType = jsonObject.getString(JsonFormConstants.TYPE);
        if (accordionInfoText != null) {
            accordionInfoWidget.setVisibility(View.VISIBLE);
            accordionInfoWidget.setTag(com.vijay.jsonwizard.R.id.key, accordionKey);
            accordionInfoWidget.setTag(com.vijay.jsonwizard.R.id.type, accordionType);
            accordionInfoWidget.setTag(com.vijay.jsonwizard.R.id.label_dialog_info, accordionInfoText);
            accordionInfoWidget.setTag(com.vijay.jsonwizard.R.id.label_dialog_title, accordionInfoTitle);
            accordionInfoWidget.setOnClickListener(commonListener);
        }
    }

    private void changeStatusIcon(ImageView imageView, JSONObject optionItem, Context context) throws JSONException {
        JSONArray value = getExpansionPanelValue(optionItem);
        for (int i = 0; i < value.length(); i++) {
            if (!value.isNull(i)) {
                JSONObject item = value.getJSONObject(i);
                if (item.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.EXTENDED_RADIO_BUTTON) ||
                        item.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.NATIVE_RADIO_BUTTON)) {

                    JSONArray jsonArray = item.getJSONArray(JsonFormConstants.VALUES);
                    for (int k = 0; k < jsonArray.length(); k++) {
                        String list = jsonArray.getString(k);
                        String[] stringValues = list.split(":");
                        if (getWidgetValueAndChangeIcon(imageView, context, list, stringValues))
                            break;
                    }
                }
            }
        }
    }

    private void attachContent(LinearLayout rootLayout, Context context, JSONObject jsonObject) throws JSONException {
        JSONArray values = new JSONArray();
        LinearLayout contentLayout = rootLayout.findViewById(R.id.contentLayout);
        if (jsonObject.has(JsonFormConstants.VALUE)) {
            values = jsonObject.getJSONArray(JsonFormConstants.VALUE);
            if (formUtils.checkValuesContent(values)) {
                contentLayout.setVisibility(View.VISIBLE);
            }
        }
        LinearLayout contentView = contentLayout.findViewById(R.id.contentView);
        formUtils.addValuesDisplay(utils.createExpansionPanelChildren(values), contentView, context);
    }

    private void addBottomSection(String stepName, Context context, JsonFormFragment jsonFormFragment, JSONObject jsonObject,
                                  CommonListener commonListener, LinearLayout rootLayout) throws JSONException {
        LinearLayout buttonSectionLayout = rootLayout.findViewById(R.id.accordion_bottom_navigation);
        JSONObject showBottomSection = jsonObject.optJSONObject(JsonFormConstants.BOTTOM_SECTION);
        boolean showButtons = true;
        boolean showRecordButton = true;
        if (showBottomSection != null) {
            showButtons = showBottomSection.optBoolean(JsonFormConstants.DISPLAY_BOTTOM_SECTION, true);
            showRecordButton = showBottomSection.optBoolean(JsonFormConstants.DISPLAY_RECORD_BUTTON, true);
        }

        Button recordButton = buttonSectionLayout.findViewById(R.id.ok_button);
        addRecordViewTags(recordButton, jsonObject, stepName, commonListener, jsonFormFragment, context);
        recordButton.setOnClickListener(expansionPanelRecordButtonClickListener);
        if (showRecordButton) {
            recordButton.setVisibility(View.VISIBLE);
        }

        Button undoButton = buttonSectionLayout.findViewById(R.id.undo_button);
        undoButton.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        undoButton.setTag(R.id.specify_context, context);
        undoButton.setTag(R.id.specify_step_name, stepName);
        undoButton.setTag(R.id.linearLayout, rootLayout);
        undoButton.setOnClickListener(expansionPanelUndoButtonClickListener);

        if (jsonObject.has(JsonFormConstants.VALUE) && jsonObject.getJSONArray(JsonFormConstants.VALUE).length() > 0) {
            JSONArray value = jsonObject.optJSONArray(JsonFormConstants.VALUE);
            if (formUtils.checkValuesContent(value)) {
                if (showButtons) {
                    buttonSectionLayout.setVisibility(View.VISIBLE);
                }

                if (jsonObject.has(JsonFormConstants.VALUE)) {
                    undoButton.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    /**
     * Return the Expansion Panel value
     *
     * @param optionItem {@link JSONObject}
     * @return value {@link JSONArray}
     * @throws JSONException
     */
    private JSONArray getExpansionPanelValue(JSONObject optionItem) throws JSONException {
        JSONArray value = new JSONArray();
        if (optionItem.has(JsonFormConstants.VALUE)) {
            value = optionItem.getJSONArray(JsonFormConstants.VALUE);
        }
        return value;
    }

    private boolean getWidgetValueAndChangeIcon(ImageView imageView, Context context, String list, String[] stringValues) {
        if (stringValues.length >= 2) {
            String valueDisplay = list.split(":")[1];

            if (valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE_TODAY) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_TODAY) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE_EARLIER) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_EARLIER) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.ORDERED) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.ORDERED) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.NOT_DONE) ||
                    valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.NOT_DONE)) {

                formUtils.changeIcon(imageView, valueDisplay, context);
                return true;
            }
        }
        return false;
    }

    @Override
    @NonNull
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> customTranslatableWidgetFields = new HashSet<>();
        customTranslatableWidgetFields.add(JsonFormConstants.ACCORDION_INFO_TEXT);
        customTranslatableWidgetFields.add(JsonFormConstants.ACCORDION_INFO_TITLE);
        return customTranslatableWidgetFields;
    }
}