package com.vijay.jsonwizard.widgets;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rey.material.util.ViewUtil;
import com.rey.material.widget.Button;
import com.rey.material.widget.TextView;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.GpsDialog;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.domain.WidgetMetadata;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.OnActivityRequestPermissionResultListener;
import com.vijay.jsonwizard.utils.PermissionUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Captures GPS locations
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 11/24/17.
 */

public class GpsFactory implements FormWidgetFactory {

    protected GpsDialog gpsDialog;

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView,
                                            Button recordButton) {
        if (!(recordButton.getTag(R.id.v_required) instanceof String) || !(recordButton.getTag(R.id.error) instanceof String)) {
            return new ValidationStatus(true, null, formFragmentView, recordButton);
        }
        Boolean isRequired = Boolean.valueOf((String) recordButton.getTag(R.id.v_required));
        if (!isRequired || !recordButton.isEnabled()) {
            return new ValidationStatus(true, null, formFragmentView, recordButton);
        }

        return new ValidationStatus(false, (String) recordButton.getTag(R.id.error), formFragmentView, recordButton);
    }

    public static String constructString(Location location) {
        if (location != null) {
            return constructString(new Object[]{location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy()});
        }
        return null;
    }

    public static String constructString(String latitude, String longitude) {
        return constructString(new Object[]{latitude, longitude});
    }

    @Override
    public List<View> getViewsFromJson(String stepName, final Context context,
                                       JsonFormFragment formFragment, JSONObject jsonObject,
                                       CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return getViewsFromJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    private List<View> attachJson(String stepName, final Context context, final JsonFormFragment formFragment, JSONObject jsonObject,
                                  boolean popup) throws JSONException {

        List<View> views = new ArrayList<>();
        final View rootLayout = getRootLayout(context);
        final int canvasId = ViewUtil.generateViewId();
        rootLayout.setId(canvasId);

        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);

        final Button recordButton = rootLayout.findViewById(R.id.record_button);

        final WidgetMetadata metadata = new WidgetMetadata();
        metadata.withOpenMrsEntityParent(openMrsEntityParent)
                .withOpenMrsEntity(openMrsEntity)
                .withOpenMrsEntityId(openMrsEntityId)
                .withRelevance(relevance);

        final WidgetArgs widgetArgs = new WidgetArgs();
        widgetArgs.withStepName(stepName)
                .withContext(context)
                .withJsonObject(jsonObject)
                .withPopup(popup);


        formFragment.getJsonApi().getAppExecutors().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    setUpViews(recordButton, widgetArgs, rootLayout, metadata, formFragment);
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        });

        ((JsonApi) context).addFormDataView(recordButton);
        views.add(rootLayout);

        return views;
    }

    public View getRootLayout(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.item_gps, null);
    }

    protected void setUpViews(final Button recordButton, WidgetArgs widgetArgs, View rootLayout,
                              WidgetMetadata metadata, JsonFormFragment formFragment) throws JSONException {

        final Context context = widgetArgs.getContext();
        final JSONObject jsonObject = widgetArgs.getJsonObject();

        addViewTags(recordButton, widgetArgs, metadata, rootLayout);

        if (jsonObject.has(JsonFormConstants.HINT)) {
            recordButton.setText(jsonObject.getString(JsonFormConstants.HINT));
        }

        String relevance = metadata.getRelevance();
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            recordButton.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(recordButton);
        }

        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            String requiredValue = requiredObject.getString(JsonFormConstants.VALUE);
            if (!TextUtils.isEmpty(requiredValue)) {
                recordButton.setTag(R.id.v_required, requiredValue);
                recordButton.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR));
            }
        }

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            boolean readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            recordButton.setEnabled(!readOnly);
            recordButton.setFocusable(!readOnly);
        }

        final TextView latitudeTV = rootLayout.findViewById(R.id.latitude);
        final TextView longitudeTV = rootLayout.findViewById(R.id.longitude);
        final TextView altitudeTV = rootLayout.findViewById(R.id.altitude);
        final TextView accuracyTV = rootLayout.findViewById(R.id.accuracy);

        attachLayout(context, jsonObject, recordButton, latitudeTV, longitudeTV, altitudeTV, accuracyTV);

        gpsDialog = getGpsDialog(recordButton, context, latitudeTV, longitudeTV, altitudeTV, accuracyTV);

        customizeViews(recordButton, context);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissionsForLocation(context);
            }
        });

        recordButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
    }

    @NotNull
    public GpsDialog getGpsDialog(Button recordButton, Context context, TextView latitudeTV, TextView longitudeTV, TextView altitudeTV, TextView accuracyTV) {
        return new GpsDialog(context, recordButton, latitudeTV, longitudeTV, altitudeTV, accuracyTV);
    }

    protected void customizeViews(Button recordButton, Context context) {
        recordButton.setBackgroundColor(context.getResources().getColor(R.color.primary));
        recordButton.setMinHeight(0);
        recordButton.setMinimumHeight(0);
        recordButton.setId(ViewUtil.generateViewId());
    }

    protected void addViewTags(Button recordButton, WidgetArgs widgetArgs, WidgetMetadata metadata, View rootLayout) throws JSONException {

        final JSONObject jsonObject = widgetArgs.getJsonObject();
        JSONArray canvasIdsArray = new JSONArray();

        canvasIdsArray.put(rootLayout.getId());
        recordButton.setTag(R.id.canvas_ids, canvasIdsArray.toString());
        recordButton.setTag(R.id.address, widgetArgs.getStepName() + ":" + jsonObject.getString(JsonFormConstants.KEY));
        recordButton.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        recordButton.setTag(R.id.openmrs_entity_parent, metadata.getOpenMrsEntityParent());
        recordButton.setTag(R.id.openmrs_entity, metadata.getOpenMrsEntity());
        recordButton.setTag(R.id.openmrs_entity_id, metadata.getOpenMrsEntityId());
        recordButton.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        recordButton.setTag(R.id.extraPopup, widgetArgs.isPopup());
    }

    public void attachLayout(Context context, @NonNull JSONObject jsonObject, @NonNull View dataView,
                             @NonNull TextView latitudeTv, @NonNull TextView longitudeTv,
                             @NonNull TextView altitudeTv, @NonNull TextView accuracyTv) {
        TextView[] views = new TextView[]{latitudeTv, longitudeTv, altitudeTv, accuracyTv};

        int[] stringIds = new int[]{R.string.latitude, R.string.longitude, R.string.altitude, R.string.accuracy};

        //loops to fill all textViews with empty string
        for (int i = 0; i < stringIds.length; i++) {
            views[i].setText(getText(context, "", stringIds[i]));
        }

        dataView.setTag(R.id.raw_value, "");

        if (jsonObject.has(JsonFormConstants.VALUE)) {
            String coordinateData = jsonObject.optString(JsonFormConstants.VALUE);

            String[] coordinateElements = coordinateData.split(" ");

            //loops to fill textViews with values supplied
            for (int i = 0; i < coordinateElements.length; i++) {
                views[i].setText(getText(context, coordinateElements[i], stringIds[i]));
            }

            dataView.setTag(R.id.raw_value, constructString(coordinateElements));
        }
    }

    private static String constructString(Object[] coordinateElements) {
        return StringUtils.join(coordinateElements, " ");
    }

    @NotNull
    public String getText(Context context, String latitude, int p) {
        return String.format(context.getResources().getString(p), latitude);
    }

    public void requestPermissionsForLocation(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Register the RequestPermissionResult listener
                if (activity instanceof JsonApi) {
                    final JsonApi jsonApi = (JsonApi) activity;
                    jsonApi.addOnActivityRequestPermissionResultListener(PermissionUtils.FINE_LOCATION_PERMISSION_REQUEST_CODE, new OnActivityRequestPermissionResultListener() {
                        @Override
                        public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
                            if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                showGpsDialog();
                            } else {
                                jsonApi.removeOnActivityRequestPermissionResultListener(PermissionUtils.FINE_LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        }
                    });
                }

                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermissionUtils.FINE_LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                showGpsDialog();
            }
        }
    }

    protected void showGpsDialog() {
        gpsDialog.show();
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}
