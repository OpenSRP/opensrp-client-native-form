package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.ToasterLinearLayout;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToasterNotesFactory implements FormWidgetFactory {
    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener
                                               listener, boolean popup) throws JSONException {
        return attachJson(stepName, context, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, false);
    }

    @Override
    @NonNull
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> customTranslatableWidgetFields = new HashSet<>();
        customTranslatableWidgetFields.add(JsonFormConstants.TOASTER_INFO_TITLE);
        customTranslatableWidgetFields.add(JsonFormConstants.TOASTER_INFO_TEXT);
        return customTranslatableWidgetFields;
    }

    private List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener
            listener, boolean popup) throws JSONException {
        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);

        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();

        ToasterLinearLayout linearLayout = getToasterLinearLayout(context);
        linearLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(linearLayout.getId());
        linearLayout.setTag(R.id.canvas_ids, canvasIds.toString());
        linearLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        linearLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        linearLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        linearLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        linearLayout.setTag(R.id.extraPopup, popup);
        linearLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        linearLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));

        attachRefreshLogic(context, relevance, calculation, linearLayout);
        attachLayout(views, context, jsonObject, linearLayout, listener);
        return views;
    }

    @VisibleForTesting
    protected ToasterLinearLayout getToasterLinearLayout(Context context) {
        return (ToasterLinearLayout) LayoutInflater.from(context).inflate(R.layout.native_form_toaster_notes, null);
    }

    private void attachRefreshLogic(Context context, String relevance, String calculation, ToasterLinearLayout linearLayout) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            linearLayout.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(linearLayout);
        }
        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            linearLayout.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(linearLayout);
        }
    }

    private void attachLayout(List<View> views, Context context, JSONObject jsonObject, LinearLayout linearLayout,
                              CommonListener listener)
            throws JSONException {
        String type = jsonObject.optString(JsonFormConstants.TOASTER_TYPE, JsonFormConstants.TOASTER_INFO);
        String text = jsonObject.optString(JsonFormConstants.TEXT, "");
        String textColor = jsonObject.optString(JsonFormConstants.TEXT_COLOR, JsonFormConstants.DEFAULT_TEXT_COLOR);
        String infoText = jsonObject.optString(JsonFormConstants.TOASTER_INFO_TEXT, null);

        RelativeLayout toasterRelativeLayout = linearLayout.findViewById(R.id.toaster_notes_layout);
        ImageView toasterNoteImageView = linearLayout.findViewById(R.id.toaster_notes_image);
        ImageView toasterNoteInfo = linearLayout.findViewById(R.id.toaster_notes_info);
        TextView toasterNotesTextView = linearLayout.findViewById(R.id.toaster_notes_text);

        switch (type) {
            case JsonFormConstants.TOASTER_INFO:
                toasterRelativeLayout.setBackgroundResource(R.drawable.toaster_notes_info);
                toasterNoteImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_info));
                toasterNoteInfo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_info_info));
                break;
            case JsonFormConstants.TOASTER_POSITIVE:
                toasterRelativeLayout.setBackgroundResource(R.drawable.toaster_notes_positive);
                toasterNoteImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_positive));
                toasterNoteInfo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_info_positive));
                break;
            case JsonFormConstants.TOASTER_PROBLEM:
                toasterRelativeLayout.setBackgroundResource(R.drawable.toaster_notes_danger);
                toasterNoteImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_danger));
                toasterNoteInfo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_info_danger));
                break;
            case JsonFormConstants.TOASTER_WARNING:
                toasterRelativeLayout.setBackgroundResource(R.drawable.toaster_notes_warning);
                toasterNoteImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_warning));
                toasterNoteInfo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_info_warning));
                break;
            default:
                break;
        }
        toasterNotesTextView.setText(text);
        linearLayout.setTag(R.id.original_text, text);
        toasterNotesTextView.setTextColor(Color.parseColor(textColor));

        if (infoText != null) {
            addToasterInfo(views, jsonObject, linearLayout, listener);
        } else {
            views.add(linearLayout);
        }
    }

    private void addToasterInfo(List<View> views, JSONObject jsonObject, LinearLayout linearLayout, CommonListener listener)
            throws
            JSONException {
        String infoTitle = jsonObject.optString(JsonFormConstants.TOASTER_INFO_TITLE, "");
        String infoText = jsonObject.optString(JsonFormConstants.TOASTER_INFO_TEXT, "");

        ImageView toasterNoteInfo = linearLayout.findViewById(R.id.toaster_notes_info);
        toasterNoteInfo.setVisibility(View.VISIBLE);
        toasterNoteInfo.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        toasterNoteInfo.setTag(R.id.type, jsonObject.getString("type"));
        toasterNoteInfo.setTag(R.id.label_dialog_info, infoText);
        toasterNoteInfo.setTag(R.id.label_dialog_title, infoTitle);
        toasterNoteInfo.setOnClickListener(listener);
        views.add(linearLayout);
    }
}
