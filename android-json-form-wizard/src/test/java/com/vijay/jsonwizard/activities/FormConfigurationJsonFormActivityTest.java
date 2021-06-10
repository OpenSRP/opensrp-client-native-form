package com.vijay.jsonwizard.activities;


import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;

import com.vijay.jsonwizard.NativeFormLibrary;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interfaces.OnFormFetchedCallback;
import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.client.utils.contract.ClientFormContract;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 19-05-2020.
 */
public class FormConfigurationJsonFormActivityTest extends BaseActivityTest {

    private FormConfigurationJsonFormActivity formConfigurationJsonFormActivity;

    @Before
    public void setUp() throws Exception {
        formConfigurationJsonFormActivity = Robolectric.buildActivity(FormConfigurationJsonFormActivity.class)
                .get();
    }

    @Test
    public void getRulesShouldReturnCallFormUtils() throws Exception {
        FormUtils formUtils = Mockito.mock(FormUtils.class);

        String rulesFileIdentifier = "registration_calculation.yml";

        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        NativeFormLibrary.getInstance().setClientFormDao(clientFormRepository);
        ReflectionHelpers.setField(formConfigurationJsonFormActivity, "formUtils", formUtils);

        Mockito.doReturn(new BufferedReader(new StringReader(""))).when(formUtils).getRulesFromRepository(
                Mockito.eq(RuntimeEnvironment.application),
                Mockito.eq(clientFormRepository),
                Mockito.eq(rulesFileIdentifier));


        formConfigurationJsonFormActivity.getRules(RuntimeEnvironment.application, rulesFileIdentifier);
        Mockito.verify(formUtils).getRulesFromRepository(
                Mockito.eq(RuntimeEnvironment.application),
                Mockito.eq(clientFormRepository), Mockito.eq(rulesFileIdentifier));
    }

    @Test
    public void getSubFormShouldCallFormUtils() throws Exception {
        FormUtils formUtils = Mockito.mock(FormUtils.class);

        String subFormIdentifier = "tuberculosis_test";

        JSONObject jsonObject = new JSONObject();
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        NativeFormLibrary.getInstance().setClientFormDao(clientFormRepository);
        ReflectionHelpers.setField(formConfigurationJsonFormActivity, "formUtils", formUtils);

        Mockito.doReturn(jsonObject).when(formUtils).getSubFormJsonFromRepository(RuntimeEnvironment.application, clientFormRepository, subFormIdentifier, null, false);

        formConfigurationJsonFormActivity.getSubForm(subFormIdentifier, null, RuntimeEnvironment.application, false);
        Mockito.verify(formUtils).getSubFormJsonFromRepository(RuntimeEnvironment.application, clientFormRepository, subFormIdentifier, null, false);
    }

    @Test
    public void getSubFormShouldCallHandleFormErrorWhenFormReturnedIsCorrupted() throws Exception {
        FormUtils formUtils = Mockito.mock(FormUtils.class);
        FormConfigurationJsonFormActivity spiedActivity = Mockito.spy(formConfigurationJsonFormActivity);

        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        String subFormIdentifier = "tuberculosis_test.json";
        NativeFormLibrary.getInstance().setClientFormDao(clientFormRepository);
        ReflectionHelpers.setField(spiedActivity, "formUtils", formUtils);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new JSONException("Test exception");
            }
        }).when(formUtils).getSubFormJsonFromRepository(RuntimeEnvironment.application, clientFormRepository, subFormIdentifier, null, false);


        Assert.assertNull(spiedActivity.getSubForm(subFormIdentifier, null, RuntimeEnvironment.application, false));
        Mockito.verify(spiedActivity).handleFormError(false, subFormIdentifier);
    }

    @Test
    public void handleFormErrorShouldCallFormUtilsHandleError() {
        String formIdentifier = "tuberculosis_test.json";
        FormUtils formUtils = Mockito.mock(FormUtils.class);
        //formConfigurationJsonFormActivity.contex
        FormConfigurationJsonFormActivity spiedActivity = Mockito.spy(formConfigurationJsonFormActivity);
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        NativeFormLibrary.getInstance().setClientFormDao(clientFormRepository);
        ReflectionHelpers.setField(spiedActivity, "formUtils", formUtils);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                OnFormFetchedCallback<String> onFormFetchedCallback = invocation.getArgument(4);
                onFormFetchedCallback.onFormFetched("");
                return null;
            }
        }).when(formUtils).handleJsonFormOrRulesError(Mockito.eq(spiedActivity), Mockito.eq(clientFormRepository), Mockito.eq(false), Mockito.eq(formIdentifier), Mockito.any(OnFormFetchedCallback.class));

        spiedActivity.handleFormError(false, formIdentifier);
        Mockito.verify(formUtils).handleJsonFormOrRulesError(Mockito.eq(spiedActivity), Mockito.eq(clientFormRepository), Mockito.eq(false), Mockito.eq(formIdentifier), Mockito.any(OnFormFetchedCallback.class));
        Mockito.verify(spiedActivity).finish();
    }

    @Test
    public void showFormVersionUpdateDialogShouldCreateAlertDialogWithTitleAndMessage() throws JSONException {
        String title = "This is the title";
        String message = "This is the message";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.Properties.CLIENT_FORM_ID, 3);

        FormConfigurationJsonFormActivity spiedActivity = Mockito.spy(formConfigurationJsonFormActivity);
        spiedActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spiedActivity.setTheme(R.style.Theme_AppCompat_Light_Dialog_Alert);

        Mockito.doReturn(jsonObject).when(spiedActivity).getmJSONObject();

        spiedActivity.showFormVersionUpdateDialog(jsonObject, title, message);

        AlertDialog alertDialog = (AlertDialog) ShadowDialog.getLatestDialog();
        Object alertDialogController = ReflectionHelpers.getField(alertDialog, "mAlert");
        Assert.assertNotNull(alertDialog);
        Assert.assertEquals(title, ReflectionHelpers.getField(alertDialogController, "mTitle"));
        Assert.assertEquals(message, ReflectionHelpers.getField(alertDialogController, "mMessage"));
    }


    @Test
    public void formUpdateAlertDialogShouldCallNegateIsNewClientForm() throws JSONException {
        String title = "This is the title";
        String message = "This is the message";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.Properties.CLIENT_FORM_ID, 3);

        FormConfigurationJsonFormActivity spiedActivity = Mockito.spy(formConfigurationJsonFormActivity);
        spiedActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spiedActivity.setTheme(R.style.Theme_AppCompat_Light_Dialog_Alert);

        Mockito.doReturn(jsonObject).when(spiedActivity).getmJSONObject();

        spiedActivity.showFormVersionUpdateDialog(jsonObject, title, message);
        AlertDialog alertDialog = (AlertDialog) ShadowDialog.getLatestDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).callOnClick();

        Mockito.verify(spiedActivity).negateIsNewClientForm(3);
    }

    @Test
    public void onCreateShouldCallShowFormVersionUpdateDialog() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.Properties.CLIENT_FORM_ID, 3);
        jsonObject.put(JsonFormConstants.Properties.IS_NEW, true);
        jsonObject.put(JsonFormConstants.Properties.FORM_VERSION, "0.0.1");

        FormConfigurationJsonFormActivity spiedActivity = Mockito.spy(formConfigurationJsonFormActivity);
        spiedActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spiedActivity.setTheme(R.style.Theme_AppCompat_Light_Dialog_Alert);

        Mockito.doReturn(jsonObject).when(spiedActivity).getmJSONObject();
        Mockito.doNothing().when(spiedActivity).init(Mockito.anyString());
        Mockito.doNothing().when(spiedActivity).showFormVersionUpdateDialog(jsonObject, getString(R.string.form_update_title), getString(R.string.form_update_message));

        Bundle bundle = new Bundle();
        bundle.putString(JsonFormBaseActivity.JSON_STATE, "{}");
        spiedActivity.onCreate(bundle);

        Mockito.verify(spiedActivity).showFormVersionUpdateDialog(jsonObject, getString(R.string.form_update_title), getString(R.string.form_update_message));
    }

    protected String getString(int stringId) {
        return RuntimeEnvironment.application.getString(stringId);
    }
}