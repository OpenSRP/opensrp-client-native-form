package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.VisibleForTesting;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.OnActivityResultListener;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ImageUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.simprint.SimPrintsConstantHelper;
import org.smartregister.simprint.SimPrintsRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Created by vijay on 24-05-2015.
 */
public class FingerPrintFactory implements FormWidgetFactory {

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
        return attachJson(stepName, context, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, false);
    }

    private void addFingerprintResultsListener(final Context context, final ImageView imageView) {
        if (context instanceof JsonApi) {
            JsonApi jsonApi = (JsonApi) context;
            jsonApi.addOnActivityResultListener(JsonFormConstants.ACTIVITY_REQUEST_CODE.REQUEST_CODE_REGISTER,
                    new OnActivityResultListener() {
                        @Override
                        public void onActivityResult(int requestCode,
                                                     int resultCode, Intent data) {
                            if (requestCode == JsonFormConstants.ACTIVITY_REQUEST_CODE.REQUEST_CODE_REGISTER && resultCode == RESULT_OK) {
                                if (data != null) {

                                    SimPrintsRegistration registration = (SimPrintsRegistration) data.getSerializableExtra(SimPrintsConstantHelper.INTENT_DATA);
                                    imageView.setTag(R.id.simprints_guid, registration.getGuid());
                                    setFingerprintDrawable(context, imageView, registration.getGuid(), true);
                                    Timber.d("Scanned Fingerprint GUID %s ", registration.getGuid());
                                } else {
                                    Timber.i("NO RESULT FOR FINGERPRINT");
                                    setFingerprintDrawable(context, imageView, "", true);
                                }
                            }
                        }
                    });
        }
    }

    private void setFingerprintDrawable(final Context context, final ImageView imageView,
                                        String fingerprintValue, boolean isFromScan) {


        if (isFromScan && TextUtils.isEmpty(fingerprintValue)) {
            //From scanning fingerprint and no result has not been received
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.finger_print_failed));
        } else if (isFromScan && !TextUtils.isEmpty(fingerprintValue)
                || (!isFromScan && !TextUtils.isEmpty(fingerprintValue))) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.finger_print_done));
        } else {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.finger_print));
        }

    }

    private List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener listener, boolean popup) throws JSONException {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        JSONArray canvasIds = new JSONArray();

        List<View> views = new ArrayList<>(1);
        createImageView(context, canvasIds, jsonObject, popup, stepName, listener, views);
        Button uploadButton = new Button(context);
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
        setViewTags(jsonObject, stepName, uploadButton, popup);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp2px(context, 20));
        uploadButton.setLayoutParams(params);

        uploadButton.setId(ViewUtil.generateViewId());
        canvasIds.put(uploadButton.getId());
        uploadButton.setTag(R.id.canvas_ids, canvasIds.toString());

        views.add(uploadButton);

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            boolean readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            uploadButton.setEnabled(!readOnly);
            uploadButton.setFocusable(!readOnly);
        }

        setRefreshLOgic(context, relevance, constraints, calculation, uploadButton);

        return views;
    }

    @VisibleForTesting
    protected void setViewTags(JSONObject jsonObject, String stepName, View view, boolean popup) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        view.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        view.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        view.setTag(R.id.openmrs_entity, openMrsEntity);
        view.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        view.setTag(R.id.extraPopup, popup);
        view.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        view.setTag(R.id.project_id, jsonObject.optString(JsonFormConstants.SIMPRINTS_PROJECT_ID));
        view.setTag(R.id.user_id, jsonObject.optString(JsonFormConstants.SIMPRINTS_USER_ID));
        view.setTag(R.id.module_id, jsonObject.optString(JsonFormConstants.SIMPRINTS_MODULE_ID));
        view.setTag(R.id.guid, jsonObject.optString(JsonFormConstants.SIMPRINTS_MODULE_ID));
        view.setTag(R.id.finger_print_option, jsonObject.optString(JsonFormConstants.SIMPRINTS_OPTION));
        view.setTag(com.vijay.jsonwizard.R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
    }

    private void createImageView(Context context, JSONArray canvasIds, JSONObject jsonObject, boolean popup, String stepName, CommonListener listener, List<View> views) throws JSONException {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        ImageView imageView = new ImageView(context);
        imageView.setId(ViewUtil.generateViewId());
        canvasIds.put(imageView.getId());
        imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.finger_print));
        setViewTags(jsonObject, stepName, imageView, popup);

        imageView.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        setRefreshLOgic(context, relevance, constraints, calculation, imageView);

        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            String requiredValue = requiredObject.getString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(requiredValue)) {
                imageView.setTag(R.id.v_required, requiredValue);
                imageView.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR));
            }
        }

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int imageHeight = FormUtils.dpToPixels(context, context.getResources().getBoolean(R.bool.isTablet) ? 50 : 50);
        imageView.setLayoutParams(FormUtils.getLinearLayoutParams(FormUtils.MATCH_PARENT, imageHeight, 0, 0, 0, (int) context
                .getResources().getDimension(R.dimen.default_bottom_margin)));

        String imagePath = jsonObject.optString(JsonFormConstants.VALUE);
        if (!TextUtils.isEmpty(imagePath)) {
            imageView.setTag(R.id.imagePath, imagePath);
            Bitmap bitmap = ImageUtils.loadBitmapFromFile(context, imagePath, ImageUtils.getDeviceWidth(context), FormUtils.dpToPixels(context, 200));
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
        }
        setFingerprintDrawable(context, imageView, imagePath, false);

        ((JsonApi) context).addFormDataView(imageView);
        imageView.setOnClickListener(listener);
        addFingerprintResultsListener(context, imageView);
        views.add(imageView);
    }

    private void setRefreshLOgic(Context context, String relevance, String constraints, String calculation, View view) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            view.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(view);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            view.setTag(com.vijay.jsonwizard.R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(view);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            view.setTag(com.vijay.jsonwizard.R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(view);
        }
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}
