package com.vijay.jsonwizard.widgets;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interfaces.JsonApi;

import org.json.JSONException;

import edu.washington.cs.ubicomplab.rdt_reader.activity.RDTCaptureActivity;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;
import static com.vijay.jsonwizard.utils.Utils.hideProgressDialog;
import static com.vijay.jsonwizard.utils.Utils.showProgressDialog;
import static edu.washington.cs.ubicomplab.rdt_reader.core.Constants.SAVED_IMAGE_FILE_PATH;

/**
 * Created by Vincent Karuri on 17/06/2020
 */
public class BasicRDTCaptureFactory extends RDTCaptureFactory {
    private static final String TAG = BasicRDTCaptureFactory.class.getName();

    private class LaunchRDTCameraTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Activity activity = (Activity) widgetArgs.getContext();
            Intent intent = new Intent(activity, RDTCaptureActivity.class);
            activity.startActivityForResult(intent, JsonFormConstants.RDT_CAPTURE_CODE);
            return null;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog(R.string.please_wait_title, R.string.launching_rdt_capture_message, widgetArgs.getContext());
        }
    }

    protected void launchRDTCaptureActivity() {
        if (isPermissionGiven()) {
            new LaunchRDTCameraTask().execute();
        }
    }

    protected boolean isPermissionGiven() {
        return ContextCompat.checkSelfPermission(widgetArgs.getContext(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        hideProgressDialog();
        final JsonApi jsonApi = (JsonApi) widgetArgs.getContext();
        if (requestCode == JsonFormConstants.RDT_CAPTURE_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    String imageFilePath = data.getExtras().getString(SAVED_IMAGE_FILE_PATH);
                    widgetArgs.getJsonObject().put(VALUE, imageFilePath);
                    // write image path as widget value
                    String key = (String) rootLayout.getTag(R.id.key);
                    String openMrsEntityParent = (String) rootLayout.getTag(R.id.openmrs_entity_parent);
                    String openMrsEntity = (String) rootLayout.getTag(R.id.openmrs_entity);
                    String openMrsEntityId = (String) rootLayout.getTag(R.id.openmrs_entity_id);
                    jsonApi.writeValue(widgetArgs.getStepName(), key, imageFilePath, openMrsEntityParent,
                            openMrsEntity, openMrsEntityId, widgetArgs.isPopup());
                } catch (JSONException e) {
                    Log.e(TAG, e.getStackTrace().toString());
                }
            } else {
                Log.i(TAG, "No result data for RDT capture!");
            }
            if (!widgetArgs.getFormFragment().next()) {
                widgetArgs.getFormFragment().save(true);
            }
        } else if (resultCode == RESULT_CANCELED) {
            ((Activity) widgetArgs.getContext()).finish();
        }
    }
}
