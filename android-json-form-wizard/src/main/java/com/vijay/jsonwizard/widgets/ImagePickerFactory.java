package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ImageUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vijay on 24-05-2015.
 */
public class ImagePickerFactory implements FormWidgetFactory {

    public static int dp2px(Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return Math.round(px);
    }

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView,
                                            ImageView imageView) {
        if (!(imageView.getTag(R.id.v_required) instanceof String) || !(imageView.getTag(R.id.error) instanceof String)) {
            return new ValidationStatus(true, null, formFragmentView, imageView);
        }
        Boolean isRequired = Boolean.valueOf((String) imageView.getTag(R.id.v_required));
        if (!isRequired || !imageView.isEnabled()) {
            return new ValidationStatus(true, null, formFragmentView, imageView);
        }
        Object path = imageView.getTag(R.id.imagePath);
        if (path instanceof String && !TextUtils.isEmpty((String) path)) {
            return new ValidationStatus(true, null, formFragmentView, imageView);
        }
        return new ValidationStatus(false, (String) imageView.getTag(R.id.error), formFragmentView, imageView);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    private List<View> attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        JSONArray canvasIds = new JSONArray();

        List<View> views = new ArrayList<>(1);
        ImageView imageView = new ImageView(context);
        imageView.setId(ViewUtil.generateViewId());
        canvasIds.put(imageView.getId());
        imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.add_photo_background));
        imageView.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        imageView.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        imageView.setTag(R.id.extraPopup, popup);
        imageView.setTag(R.id.openmrs_entity, openMrsEntity);
        imageView.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        imageView.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        imageView.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        if (relevance != null && context instanceof JsonApi) {
            imageView.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(imageView);
        }

        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            String requiredValue = requiredObject.getString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(requiredValue)) {
                imageView.setTag(R.id.v_required, requiredValue);
                imageView.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR));
            }
        }

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int imageHeight = FormUtils.dpToPixels(context, context.getResources().getBoolean(R.bool.isTablet) ? 200 : 100);
        imageView.setLayoutParams(FormUtils.getLinearLayoutParams(FormUtils.MATCH_PARENT, imageHeight, 0, 0, 0, (int) context
                .getResources().getDimension(R.dimen.default_bottom_margin)));
        String imagePath = jsonObject.optString(JsonFormConstants.VALUE);
        Button uploadButton = new Button(context);
        if (!TextUtils.isEmpty(imagePath)) {
            imageView.setTag(R.id.imagePath, imagePath);
            imageView.setImageBitmap(ImageUtils.loadBitmapFromFile(context, imagePath, ImageUtils.getDeviceWidth(context), FormUtils.dpToPixels(context, 200)));
        }

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            boolean readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            uploadButton.setEnabled(!readOnly);
            uploadButton.setFocusable(!readOnly);
        }

        ((JsonApi) context).addFormDataView(imageView);
        imageView.setOnClickListener(listener);
        views.add(imageView);

        uploadButton.setText(jsonObject.getString(JsonFormConstants.UPLOAD_BUTTON_TEXT));
        uploadButton.setBackgroundColor(context.getResources().getColor(R.color.primary));
        uploadButton.setMinHeight(0);
        uploadButton.setMinimumHeight(0);
        uploadButton.setTextColor(context.getResources().getColor(android.R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            uploadButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        uploadButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.button_text_size));
        uploadButton.setPadding(
                context.getResources().getDimensionPixelSize(R.dimen.button_padding),
                context.getResources().getDimensionPixelSize(R.dimen.button_padding),
                context.getResources().getDimensionPixelSize(R.dimen.button_padding),
                context.getResources().getDimensionPixelSize(R.dimen.button_padding));
        uploadButton.setLayoutParams(FormUtils.getRelativeLayoutParams(FormUtils.MATCH_PARENT, FormUtils.WRAP_CONTENT, 0, 0, 0, (int) context
                .getResources().getDimension(R.dimen.default_bottom_margin)));
        uploadButton.setOnClickListener(listener);
        uploadButton.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        uploadButton.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        uploadButton.setTag(R.id.openmrs_entity, openMrsEntity);
        uploadButton.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        uploadButton.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp2px(context, 20));
        uploadButton.setLayoutParams(params);

        uploadButton.setId(ViewUtil.generateViewId());
        canvasIds.put(uploadButton.getId());
        uploadButton.setTag(R.id.canvas_ids, canvasIds.toString());

        views.add(uploadButton);
        if (relevance != null && context instanceof JsonApi) {
            uploadButton.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(uploadButton);
        }
        return views;
    }
}
