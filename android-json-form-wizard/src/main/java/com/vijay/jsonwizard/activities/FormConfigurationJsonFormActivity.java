package com.vijay.jsonwizard.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.vijay.jsonwizard.NativeFormLibrary;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interfaces.OnFormFetchedCallback;
import com.vijay.jsonwizard.utils.AppExecutors;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.NativeFormLangUtils;
import com.vijay.jsonwizard.utils.NoLocaleFormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.client.utils.contract.ClientFormContract;

import java.io.BufferedReader;
import java.io.IOException;

import timber.log.Timber;


/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 29-06-2020.
 */
public class FormConfigurationJsonFormActivity extends JsonFormActivity {

    private FormUtils formUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formUtils = getFormUtils();
        JSONObject jsonObject = getmJSONObject();
        checkIfFormUpdate(jsonObject);
    }

    private FormUtils getFormUtils() {
        if (!this.supportsLocaleBasedForms()) {
            return new NoLocaleFormUtils();
        }
        return new FormUtils();
    }

    protected boolean supportsLocaleBasedForms() {
        return true;
    }

    private void checkIfFormUpdate(@NonNull JSONObject formJsonObject) {
        if (FormUtils.isFormNew(formJsonObject)) {
            showFormVersionUpdateDialog(formJsonObject, getString(R.string.form_update_title), getString(R.string.form_update_message));
        }
    }

    public void showFormVersionUpdateDialog(@NonNull JSONObject formJsonObject, @NonNull String title, @NonNull String message) {
        final int clientId = FormUtils.getClientFormId(formJsonObject);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        negateIsNewClientForm(clientId);
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        negateIsNewClientForm(clientId);
                    }
                })
                .show();
    }

    @VisibleForTesting
    protected void negateIsNewClientForm(final int clientFormId) {
        AppExecutors appExecutors = new AppExecutors();

        appExecutors.diskIO()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
                        if (clientFormRepository != null) {
                            clientFormRepository.setIsNew(false, clientFormId);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public BufferedReader getRules(@NonNull Context context, @NonNull String fileName) throws IOException {
        try {
            ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
            if (clientFormRepository != null) {
                BufferedReader bufferedReader = formUtils.getRulesFromRepository(context, clientFormRepository, fileName);
                if (bufferedReader != null) {
                    return bufferedReader;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return super.getRules(context, fileName);
    }

    @Override
    protected String getJsonForm() {
        String jsonForm = getIntent().getStringExtra(JsonFormConstants.JSON_FORM_KEY.JSON);
        if (translateForm) {
            jsonForm = NativeFormLangUtils.getTranslatedStringWithDBResourceBundle(this, jsonForm, null);
        }
        return jsonForm;
    }

    @Nullable
    @Override
    public JSONObject getSubForm(String formIdentity, String subFormsLocation, Context context, boolean translateSubForm) throws Exception {
        JSONObject dbForm = null;
        try {
            ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
            if (clientFormRepository != null) {
                dbForm = formUtils.getSubFormJsonFromRepository(context, clientFormRepository, formIdentity, subFormsLocation, translateSubForm);
            }
        } catch (JSONException ex) {
            Timber.e(ex);
            handleFormError(false, formIdentity);
            return null;
        }

        if (dbForm == null) {
            return super.getSubForm(formIdentity, subFormsLocation, context, translateSubForm);
        }

        return dbForm;
    }

    @Override
    public void handleFormError(boolean isRulesFile, @NonNull String formIdentifier) {
        ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
        if (clientFormRepository == null) {
            Timber.e(new Exception(), "Cannot handle form error because ClientFormRepository is null");
            return;
        }

        formUtils.handleJsonFormOrRulesError(this, clientFormRepository, isRulesFile, formIdentifier, new OnFormFetchedCallback<String>() {
            @Override
            public void onFormFetched(@Nullable String form) {
                if (form != null) {
                    Toast.makeText(FormConfigurationJsonFormActivity.this, R.string.form_changed_reopen_to_take_effect, Toast.LENGTH_LONG)
                            .show();
                }

                FormConfigurationJsonFormActivity.this.finish();
            }
        });
    }
}
