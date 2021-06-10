package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.VisibleForTesting;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.OnActivityResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vijay.jsonwizard.constants.JsonFormConstants.RDT_CAPTURE_CODE;

/**
 * Created by Vincent Karuri on 24/05/2019
 */
public abstract class RDTCaptureFactory implements FormWidgetFactory, OnActivityResultListener {

    protected WidgetArgs widgetArgs;
    protected View rootLayout;

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        widgetArgs = new WidgetArgs();
        widgetArgs.withStepName(stepName).withContext(context).withFormFragment(formFragment)
                .withJsonObject(jsonObject).withListener(listener).withPopup(popup);
        rootLayout = getRootLayout(context);

        addWidgetTags(jsonObject);
        setUpRDTCaptureActivity();
        launchRDTCaptureActivity();

        List<View> views = new ArrayList<>(1);
        views.add(rootLayout);

        return views;
    }

    @VisibleForTesting
    protected View getRootLayout(Context context) {
        return LayoutInflater.from(context).inflate(getLayout(), null);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return getViewsFromJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    private void addWidgetTags(JSONObject jsonObject) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String key = jsonObject.getString(JsonFormConstants.KEY);

        rootLayout.setTag(R.id.key, key);
        rootLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        rootLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        rootLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
    }

    protected abstract void launchRDTCaptureActivity() throws Exception;

    protected int getLayout() {
        return R.layout.native_form_rdt_capture;
    }

    public void setUpRDTCaptureActivity() {
        Context context = widgetArgs.getContext();
        if (context instanceof JsonApi) {
            final JsonApi jsonApi = (JsonApi) context;
            jsonApi.addOnActivityResultListener(RDT_CAPTURE_CODE, this);
        }
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}
