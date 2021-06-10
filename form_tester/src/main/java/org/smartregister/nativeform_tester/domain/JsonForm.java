package org.smartregister.nativeform_tester.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.utils.Utils;

import org.json.JSONObject;
import org.smartregister.nativeform_tester.contract.FormTesterContract;

import java.io.File;

import timber.log.Timber;


/***
 * This is an Immutable class.
 * The class constructor is heavy and should be created in a back ground thread
 */
public class JsonForm implements FormTesterContract.NativeForm {

    private String formName;
    private String fileName;
    private boolean isValid = false;
    private JSONObject jsonObject;
    private Form form = null;

    @WorkerThread
    public JsonForm(@NonNull File sourceFile, Form form) {
        fileName = sourceFile.getName();
        this.form = form;
        try {
            jsonObject = new JSONObject(Utils.getFileContentsAsString(sourceFile));
            if (jsonObject.has(JsonFormConstants.ENCOUNTER_TYPE)) {
                formName = jsonObject.getString(JsonFormConstants.ENCOUNTER_TYPE);
                isValid = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Nullable
    @Override
    public String getFormName() {
        return formName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Nullable
    @Override
    public JSONObject getJsonForm() {
        return jsonObject;
    }

    @Nullable
    @Override
    public Form getFormDetails() {
        return form;
    }
}
