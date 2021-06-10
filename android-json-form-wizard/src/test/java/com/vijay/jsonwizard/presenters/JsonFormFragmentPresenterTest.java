package com.vijay.jsonwizard.presenters;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.widgets.CountDownTimerFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Vincent Karuri on 18/02/2020
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonFormFragment.class, CountDownTimerFactory.class})
public class JsonFormFragmentPresenterTest {

    private JsonFormFragmentPresenter jsonFormFragmentPresenter;

    @Mock
    private JsonFormFragment jsonFormFragment;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.initMocks(this);
        setUpJsonFormFragment(true);
        jsonFormFragmentPresenter = new JsonFormFragmentPresenter(jsonFormFragment);
    }

    private void setUpJsonFormFragment(boolean validationStatus) throws JSONException {
        // bootstrap jsonApi
        JsonApi jsonApi = mock(JsonApi.class);
        JSONObject mJsonObject = new JSONObject();
        mJsonObject.put(JsonFormConstants.VALIDATE_ON_SUBMIT, validationStatus);
        doReturn(jsonApi).when(jsonFormFragment).getJsonApi();
        doReturn(mJsonObject).when(jsonApi).getmJSONObject();
    }

    @Test
    public void testValidateOnSubmitShouldReturnCorrectValidationStatus() throws JSONException {
        assertTrue(jsonFormFragmentPresenter.validateOnSubmit());
        setUpJsonFormFragment(false);
        assertFalse(jsonFormFragmentPresenter.validateOnSubmit());
    }

    @Test
    public void testMoveToNextStepShouldMoveToNextStepIfExists() throws Exception {
        JsonApi jsonApi = jsonFormFragment.getJsonApi();
        doReturn("step1").when(jsonApi).nextStep();
        mockStaticClasses();
        JsonFormFragmentView view = mock(JsonFormFragmentView.class);
        jsonFormFragmentPresenter.attachView(view);

        boolean movedToNext = Whitebox.invokeMethod(jsonFormFragmentPresenter, "moveToNextStep");
        assertTrue(movedToNext);
        verify(view).hideKeyBoard();
        verify(view).transactThis(eq(jsonFormFragment));

        doReturn("").when(jsonApi).nextStep();
        movedToNext = Whitebox.invokeMethod(jsonFormFragmentPresenter, "moveToNextStep");
        assertFalse(movedToNext);
    }

/*    private void addMockStepDetails(String stepName) throws JSONException {
        JSONObject mStepDetails = new JSONObject();
        mStepDetails.put("next", stepName);
        Whitebox.setInternalState(jsonFormFragmentPresenter, "mStepDetails", mStepDetails);
    }*/

    private void mockStaticClasses() {
        mockStatic(JsonFormFragment.class);
        when(JsonFormFragment.getFormFragment(anyString())).thenReturn(jsonFormFragment);
    }

    @Test
    public void testCheckAndStopCountdownAlarmShouldStopAlarm() throws JSONException {
        bootStrapCurrentJsonState();
        mockStatic(CountDownTimerFactory.class);
        jsonFormFragmentPresenter.checkAndStopCountdownAlarm();
        verifyStatic(CountDownTimerFactory.class);
        CountDownTimerFactory.stopAlarm();
    }

    private void bootStrapCurrentJsonState() throws JSONException {
        // bootstrap currentJsonState
        JSONObject currentJsonState = new JSONObject();
        JSONArray fields = new JSONArray();
        JSONObject step1 = new JSONObject();
        step1.put("fields", fields);
        currentJsonState.put("step1", step1);
        Whitebox.setInternalState(jsonFormFragmentPresenter, "mStepName", "step1");

        // add timer object
        JSONObject timerObj = new JSONObject();
        timerObj.put(JsonFormConstants.COUNTDOWN_TIME_VALUE, 12);
        fields.put(timerObj);

        doReturn(currentJsonState.toString()).when(jsonFormFragment).getCurrentJsonState();
    }
}
