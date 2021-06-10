package com.vijay.jsonwizard.presenters;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatSpinner;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.TestConstants;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormErrorFragment;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.OnFieldsInvalid;
import com.vijay.jsonwizard.rules.RulesEngineFactory;
import com.vijay.jsonwizard.shadow.ShadowContextCompat;
import com.vijay.jsonwizard.shadow.ShadowPermissionUtils;
import com.vijay.jsonwizard.utils.AppExecutors;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.widgets.NumberSelectorFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ReflectionHelpers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter.RESULT_LOAD_IMG;
import static com.vijay.jsonwizard.utils.PermissionUtils.CAMERA_PERMISSION_REQUEST_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

/**
 * Created by samuelgithengi on 3/3/20.
 */
@LooperMode(PAUSED)
public class JsonFormFragmentPresenterRoboElectricTest extends BaseTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private JsonFormFragment formFragment;

    @Mock
    private JsonFormInteractor jsonFormInteractor;
    @Mock
    private JsonFormActivity jsonFormActivity;

    @Captor
    private ArgumentCaptor<JSONObject> jsonArgumentCaptor;

    @Captor
    private ArgumentCaptor<Intent> intentArgumentCaptor;

    @Mock
    private JsonFormErrorFragment errorFragment;

    @Mock
    private OnFieldsInvalid onFieldsInvalid;

    @Mock
    private RulesEngineFactory rulesEngineFactory;

    private JsonFormFragmentPresenter presenter;

    private JSONObject mStepDetails;

    private View textView;

    private Context context = RuntimeEnvironment.application;

    private AppExecutors appExecutors;


    @Before
    public void setUp() throws JSONException {
        setUp(formFragment);
    }

    public void setUp(JsonFormFragment formFragment) throws JSONException {
        when(formFragment.getJsonApi()).thenReturn(jsonFormActivity);
        formFragment.onFieldsInvalid = onFieldsInvalid;
        presenter = new JsonFormFragmentPresenter(formFragment, jsonFormInteractor);
        Whitebox.setInternalState(presenter, "viewRef", new WeakReference<>(formFragment));
        textView = new TextView(context);
        JSONObject jsonForm = new JSONObject(TestConstants.PAOT_TEST_FORM);
        mStepDetails = jsonForm.getJSONObject(STEP1);
        when(jsonFormActivity.getmJSONObject()).thenReturn(jsonForm);
        when(formFragment.getContext()).thenReturn(context);
        AppExecutors myAppExecutors = new AppExecutors();
        appExecutors = new AppExecutors(myAppExecutors.mainThread(), myAppExecutors.mainThread(), myAppExecutors.mainThread());
        when(jsonFormActivity.getAppExecutors()).thenReturn(appExecutors);
    }


    private void initWithActualForm() throws InterruptedException {
        Intent intent = new Intent();
        intent.putExtra("json", TestConstants.BASIC_FORM);
        jsonFormActivity = spy(Robolectric.buildActivity(JsonFormActivity.class, intent).create().resume().get());
        when(jsonFormActivity.getAppExecutors()).thenReturn(appExecutors);
        formFragment = spy(JsonFormFragment.getFormFragment("step1"));
        jsonFormActivity.getSupportFragmentManager().beginTransaction().add(formFragment, null).commit();
        shadowOf(getMainLooper()).idle();
        formFragment.onFieldsInvalid = this.onFieldsInvalid;
        presenter = formFragment.getPresenter();
        shadowOf(getMainLooper()).idle();
        Thread.sleep(TIMEOUT);
    }

    @Test
    public void testAddFormElements() {
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, STEP1);
        when(formFragment.getArguments()).thenReturn(bundle);
        when(formFragment.getStep(STEP1)).thenReturn(mStepDetails);
        List<View> views = Collections.singletonList(textView);
        when(jsonFormInteractor.fetchFormElements(anyString(), any(JsonFormFragment.class), any(JSONObject.class), isNull(CommonListener.class), anyBoolean())).thenReturn(views);
        presenter.addFormElements();
        shadowOf(getMainLooper()).idle();
        verify(jsonFormInteractor, timeout(TIMEOUT)).fetchFormElements(eq(STEP1), eq(formFragment), jsonArgumentCaptor.capture(), isNull(CommonListener.class), eq(false));
        assertEquals(mStepDetails.toString(), jsonArgumentCaptor.getValue().toString());
        verify(formFragment, timeout(TIMEOUT)).addFormElements(views);
    }


    @Test
    public void testAddFormElementsWillNullStepShouldNotAddFormElements() {
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, STEP1);
        when(formFragment.getArguments()).thenReturn(bundle);
        when(formFragment.getStep(STEP1)).thenReturn(null);
        presenter.addFormElements();
        verify(formFragment, never()).addFormElements(any(List.class));
    }


    @Test
    public void testAddFormElementsForWizardShouldSetNextStep() throws JSONException {
        formFragment = mock(JsonWizardFormFragment.class);
        setUp(formFragment);
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, STEP1);
        when(formFragment.getArguments()).thenReturn(bundle);
        when(formFragment.getStep(STEP1)).thenReturn(mStepDetails);
        List<View> views = Collections.singletonList(textView);
        when(jsonFormInteractor.fetchFormElements(anyString(), any(JsonFormFragment.class), any(JSONObject.class), isNull(CommonListener.class), anyBoolean())).thenReturn(views);
        presenter.addFormElements();
        shadowOf(getMainLooper()).idle();
        verify(jsonFormInteractor, timeout(TIMEOUT)).fetchFormElements(eq(STEP1), eq(formFragment), jsonArgumentCaptor.capture(), isNull(CommonListener.class), eq(false));
        assertEquals(mStepDetails.toString(), jsonArgumentCaptor.getValue().toString());
        verify(formFragment, timeout(TIMEOUT)).addFormElements(views);
        verify(jsonFormActivity).setNextStep(STEP1);
    }


    @Test
    public void testAddFormElementsForWizardShouldSkipSteps() throws JSONException {
        formFragment = mock(JsonWizardFormFragment.class);
        setUp(formFragment);
        when(jsonFormActivity.skipBlankSteps()).thenReturn(true);
        when(jsonFormActivity.nextStep()).thenReturn(STEP1);
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, STEP1);
        when(formFragment.getArguments()).thenReturn(bundle);
        when(formFragment.getStep(STEP1)).thenReturn(mStepDetails);
        List<View> views = Collections.singletonList(textView);
        when(jsonFormInteractor.fetchFormElements(anyString(), any(JsonFormFragment.class), any(JSONObject.class), isNull(CommonListener.class), anyBoolean())).thenReturn(views);
        presenter.addFormElements();
        shadowOf(getMainLooper()).idle();
        verify(jsonFormInteractor, timeout(TIMEOUT)).fetchFormElements(eq(STEP1), eq(formFragment), jsonArgumentCaptor.capture(), isNull(CommonListener.class), eq(false));
        assertEquals(mStepDetails.toString(), jsonArgumentCaptor.getValue().toString());
        verify(formFragment, timeout(TIMEOUT)).addFormElements(views);
        verify(jsonFormActivity).setNextStep(STEP1);
        JsonWizardFormFragment wizardFormFragment = (JsonWizardFormFragment) formFragment;
        verify(wizardFormFragment).skipLoadedStepsOnNextPressed();
    }


    @Test
    public void testAddFormElementsShouldDismissDialog() {
        Whitebox.setInternalState(presenter, "cleanupAndExit", true);
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, STEP1);
        when(formFragment.getArguments()).thenReturn(bundle);
        when(formFragment.getStep(STEP1)).thenReturn(mStepDetails);
        presenter.addFormElements();
        verify(formFragment, never()).addFormElements(any(List.class));
    }

    @Test
    public void testSetUpToolBarForBottomNavigation() throws JSONException {
        Whitebox.setInternalState(presenter, "mStepDetails", mStepDetails);
        mStepDetails.put("bottom_navigation", true);
        presenter.setUpToolBar();
        verify(formFragment).updateVisibilityOfNextAndSave(false, false);

    }

    @Test
    public void testSetUpToolBarForStep2AndMore() {
        Whitebox.setInternalState(presenter, "mStepDetails", mStepDetails);
        Whitebox.setInternalState(presenter, "mStepName", "step2");
        presenter.setUpToolBar();
        verify(formFragment).setUpBackButton();

    }

    @Test
    public void testSetUpToolBarIfStepHasNext() throws JSONException {
        Whitebox.setInternalState(presenter, "mStepDetails", mStepDetails);
        Whitebox.setInternalState(presenter, "mStepName", "step2");
        mStepDetails.put("next", true);
        presenter.setUpToolBar();
        verify(formFragment).updateVisibilityOfNextAndSave(true, false);

    }

    @Test
    public void testSetUpToolBarForLastLastStep() {
        Whitebox.setInternalState(presenter, "mStepDetails", mStepDetails);
        Whitebox.setInternalState(presenter, "mStepName", "step2");
        presenter.setUpToolBar();
        verify(formFragment).updateVisibilityOfNextAndSave(false, true);

    }

    @Test
    public void testOnBackClick() {
        presenter.onBackClick();
        verify(formFragment).hideKeyBoard();
        verify(formFragment).backClick();

    }

    @Test
    public void testGetIncorrectlyFormattedFields() {
        assertEquals(new ArrayList<>(), presenter.getIncorrectlyFormattedFields());
        Stack<String> stack = new Stack<>();
        stack.push("field1");
        Whitebox.setInternalState(presenter, "incorrectlyFormattedFields", stack);
        assertEquals(stack, presenter.getIncorrectlyFormattedFields());
    }

    @Test
    public void testSetErrorFragment() {
        presenter.setErrorFragment(errorFragment);
        assertEquals(errorFragment, presenter.getErrorFragment());
    }

    @Test
    public void testOnNextClickReturnsFalseIfFormIsInvalid() {
        Whitebox.setInternalState(presenter, "mStepDetails", mStepDetails);
        doReturn(onFieldsInvalid).when(formFragment).getOnFieldsInvalidCallback();
        assertFalse(presenter.onNextClick(null));
    }

    @Test
    public void testValidateAndWriteValuesWithInvalidFields() throws InterruptedException {
        initWithActualForm();
        presenter.validateAndWriteValues();
        shadowOf(getMainLooper()).idle();
        assertEquals(2, presenter.getInvalidFields().size());
        assertEquals("Please enter the last name", presenter.getInvalidFields().get("step1#Basic Form One:user_last_name").getErrorMessage());
        assertEquals("Please enter the sex", presenter.getInvalidFields().get("step1#Basic Form One:user_spinner").getErrorMessage());
        shadowOf(getMainLooper()).idle();
        verify(formFragment, times(1)).writeValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyBoolean());
        verify(onFieldsInvalid, times(1)).passInvalidFields(presenter.getInvalidFields());
    }

    @Test
    public void testValidateAndWriteValues() throws InterruptedException {
        initWithActualForm();
        presenter.validateAndWriteValues();
        shadowOf(getMainLooper()).idle();
        assertEquals(2, presenter.getInvalidFields().size());


        setTextValue("step1:user_last_name", "Doe");
        setTextValue("step1:user_first_name", "John");
        setTextValue("step1:user_age", "21");
        ((AppCompatSpinner) formFragment.getJsonApi().getFormDataView("step1:user_spinner")).setSelection(1, false);
        presenter.validateAndWriteValues();
        shadowOf(getMainLooper()).idle();
        assertEquals(0, presenter.getInvalidFields().size());
        verify(formFragment, times(3)).writeValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyBoolean());
        verify(onFieldsInvalid, times(2)).passInvalidFields(presenter.getInvalidFields());
    }

    private void setTextValue(String address, String value) {
        TextView view = (TextView) formFragment.getJsonApi().getFormDataView(address);
        view.setTag(R.id.raw_value, value);
        view.setText(value);

    }


    @Test
    public void testOnSaveClickDisplaysErrorFragmentAndDisplaysToast() throws InterruptedException {
        initWithActualForm();
        formFragment.getMainView().setTag(R.id.skip_validation, false);
        doNothing().when(formFragment).addFormElements(ArgumentMatchers.<View>anyList());
        presenter.onSaveClick(formFragment.getMainView());
        shadowOf(getMainLooper()).idle();
        assertEquals(2, presenter.getInvalidFields().size());
        assertEquals("Please enter the last name", presenter.getInvalidFields().get("step1#Basic Form One:user_last_name").getErrorMessage());
        assertEquals("Please enter the sex", presenter.getInvalidFields().get("step1#Basic Form One:user_spinner").getErrorMessage());
        verify(formFragment, times(1)).writeValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyBoolean());
        assertTrue(presenter.getErrorFragment().isVisible());
        Toast toast = ShadowToast.getLatestToast();
        assertEquals(Toast.LENGTH_SHORT, toast.getDuration());
        assertEquals(context.getString(R.string.json_form_error_msg, 2), ShadowToast.getTextOfLatestToast());
    }


    @Test
    public void testOnSaveClickFinishesForm() throws JSONException, InterruptedException {
        initWithActualForm();
        formFragment.getMainView().setTag(R.id.skip_validation, false);
        setTextValue("step1:user_last_name", "Doe");
        setTextValue("step1:user_first_name", "John");
        setTextValue("step1:user_age", "21");
        ((AppCompatSpinner) formFragment.getJsonApi().getFormDataView("step1:user_spinner")).setSelection(1, false);
        presenter.onSaveClick(formFragment.getMainView());
        shadowOf(getMainLooper()).idle();
        assertEquals(0, presenter.getInvalidFields().size());
        verify(formFragment, times(1)).writeValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyBoolean());
        assertNull(presenter.getErrorFragment());
        assertNull(ShadowToast.getLatestToast());
        verify(formFragment).onFormFinish();
        verify(formFragment).finishWithResult(intentArgumentCaptor.capture());
        assertFalse(intentArgumentCaptor.getValue().getBooleanExtra(JsonFormConstants.SKIP_VALIDATION, true));
        JSONObject json = new JSONObject(intentArgumentCaptor.getValue().getStringExtra("json"));
        assertNotNull(json);
        assertEquals("Doe", FormUtils.getFieldFromForm(json, "user_last_name").getString(JsonFormConstants.VALUE));
        assertEquals("John", FormUtils.getFieldFromForm(json, "user_first_name").getString(JsonFormConstants.VALUE));
        assertEquals("21", FormUtils.getFieldFromForm(json, "user_age").getString(JsonFormConstants.VALUE));

    }


    @Test
    public void testOnSaveClickErrorFragmentDisabledAndDisplaysSnackbar() throws JSONException, InterruptedException {
        initWithActualForm();
        formFragment.getMainView().setTag(R.id.skip_validation, false);
        formFragment.getJsonApi().getmJSONObject().put(JsonFormConstants.SHOW_ERRORS_ON_SUBMIT, false);
        formFragment = spy(formFragment);
        doNothing().when(formFragment).showSnackBar(anyString());
        Whitebox.setInternalState(presenter, "viewRef", new WeakReference<>(formFragment));
        presenter.onSaveClick(formFragment.getMainView());
        assertEquals(2, presenter.getInvalidFields().size());
        assertEquals("Please enter the last name", presenter.getInvalidFields().get("step1#Basic Form One:user_last_name").getErrorMessage());
        assertEquals("Please enter the sex", presenter.getInvalidFields().get("step1#Basic Form One:user_spinner").getErrorMessage());
        verify(formFragment, times(1)).writeValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyBoolean());
        assertNull(presenter.getErrorFragment());
        assertNull(ShadowToast.getLatestToast());
        verify(formFragment, never()).onFormFinish();
        verify(formFragment).showSnackBar(context.getString(R.string.json_form_error_msg, 2));
    }

    @Test
    public void testOnActivityResult() {
        when(formFragment.getContext()).thenReturn(context);
        presenter.onActivityResult(RESULT_LOAD_IMG, RESULT_CANCELED, null);
        verify(formFragment).getJsonApi();
        verifyNoMoreInteractions(formFragment);
        presenter.onActivityResult(RESULT_LOAD_IMG, RESULT_OK, null);
        verify(formFragment).updateRelevantImageView(null, null, null);
    }

    @Test
    @Config(shadows = {ShadowContextCompat.class, ShadowPermissionUtils.class})
    public void testOnRequestPermissionsResultDisplaysCameraIntent() {
        Whitebox.setInternalState(presenter, "key", "user_image");
        Whitebox.setInternalState(presenter, "type", "choose_image");
        when(formFragment.getContext()).thenReturn(context);
        presenter.onRequestPermissionsResult(CAMERA_PERMISSION_REQUEST_CODE, new String[]{permission.CAMERA, permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE}, new int[5]);
        verify(formFragment).hideKeyBoard();
        assertEquals("user_image", Whitebox.getInternalState(presenter, "mCurrentKey"));
    }

    @Test
    public void testGetInteractor() {
        assertNotNull(presenter.getmJsonFormInteractor());
        assertNotNull(presenter.getInteractor());
    }

    @Test
    public void testPreLoadRulesShouldInitializeRules() throws JSONException {
        when(jsonFormActivity.getRulesEngineFactory()).thenReturn(rulesEngineFactory);
        presenter.preLoadRules(new JSONObject(TestConstants.BASIC_FORM_WITH_RULES), STEP1);
        shadowOf(getMainLooper()).idle();
        verify(rulesEngineFactory).getRulesFromAsset("sample-relevance-rules.yml");
        verify(rulesEngineFactory).getRulesFromAsset("sample-calculation-rules.yml");
    }

    @Test
    public void testOnCheckedChangedShouldWriteValueToForm() throws InterruptedException {
        initWithActualForm();
        CheckBox checkBox = new CheckBox(RuntimeEnvironment.application);
        checkBox.setTag(R.id.key, "user_check_box");
        checkBox.setTag(R.id.childKey, "no");
        checkBox.setChecked(true);

        presenter.onCheckedChanged(checkBox, true);

        verify(formFragment, times(1))
                .writeValue(eq("step1"), eq("user_check_box"),
                        eq(JsonFormConstants.OPTIONS_FIELD_NAME), eq("no"), eq(String.valueOf(true)),
                        nullable(String.class), nullable(String.class), nullable(String.class), eq(false));
    }

    @Test
    public void testOnCheckedChangedWithExclusiveShouldUncheckAllExceptChildKeyThenWriteValueToForm() throws InterruptedException, JSONException {
        initWithActualForm();
        CheckBox checkBox = new CheckBox(RuntimeEnvironment.application);
        checkBox.setTag(R.id.key, "user_check_box");
        checkBox.setTag(R.id.childKey, "none");
        checkBox.setChecked(true);

        JSONObject form = jsonFormActivity.getmJSONObject();

        JSONObject jsonObjectField = FormUtils.getFieldFromForm(form, "user_check_box");
        jsonObjectField.put("exclusive", new JSONArray("[\"none\"]"));

        presenter.onCheckedChanged(checkBox, true);

        verify(formFragment, times(1))
                .unCheckAllExcept(eq("user_check_box"), eq("none"), eq(checkBox));

        verify(formFragment, times(1))
                .writeValue(eq("step1"), eq("user_check_box"),
                        eq(JsonFormConstants.OPTIONS_FIELD_NAME), eq("none"), eq(String.valueOf(true)),
                        nullable(String.class), nullable(String.class), nullable(String.class), eq(false));
    }


    @Test
    public void testOnCheckedChangedWithExclusiveShouldUncheckThenWriteValueToForm() throws InterruptedException, JSONException {
        initWithActualForm();
        CheckBox checkBox = new CheckBox(RuntimeEnvironment.application);
        checkBox.setTag(R.id.key, "user_check_box");
        checkBox.setTag(R.id.childKey, "none");
        checkBox.setChecked(true);

        JSONObject form = jsonFormActivity.getmJSONObject();

        JSONObject jsonObjectField = FormUtils.getFieldFromForm(form, "user_check_box");
        jsonObjectField.put("exclusive", new JSONArray("[\"dont_know\"]"));

        presenter.onCheckedChanged(checkBox, true);

        verify(formFragment, times(1))
                .unCheck(eq("user_check_box"), eq("dont_know"), eq(checkBox));

        verify(formFragment, times(1))
                .writeValue(eq("step1"), eq("user_check_box"),
                        eq(JsonFormConstants.OPTIONS_FIELD_NAME), eq("none"), eq(String.valueOf(true)),
                        nullable(String.class), nullable(String.class), nullable(String.class), eq(false));
    }

    @Test
    public void testOnItemSelectedShouldWriteSelectedValueToForm() throws JSONException {
        Spinner adapterView = mock(Spinner.class);
        doReturn("user_location").when(adapterView).getTag(R.id.key);
        JSONArray jsonArray = new JSONArray("[\"user_option_one\",\"user_option_two\"]");
        doReturn(jsonArray).when(adapterView).getTag(R.id.keys);
        doReturn("user_option_one").when(adapterView).getItemAtPosition(0);

        JsonFormFragmentPresenter spyPresenter = spy(presenter);

        ReflectionHelpers.setField(spyPresenter, "mStepName", "step1");

        spyPresenter.onItemSelected(adapterView, mock(View.class), 0, 0);

        verify(formFragment, times(1))
                .writeValue(eq("step1"), eq("user_location"),
                        eq(jsonArray.getString(0)),
                        nullable(String.class), nullable(String.class), nullable(String.class), eq(false));
    }


    @Test
    public void testShowInformationDialogShouldShowCustomDialog() {
        View view = new View(RuntimeEnvironment.application);
        view.setTag(R.id.label_dialog_image_src, "label");
        view.setTag(R.id.label_dialog_title, "title");
        view.setTag(R.id.label_dialog_info, "info");

        JsonFormFragmentPresenter spyPresenter = spy(presenter);
        Dialog dialogSpy = spy(new Dialog(view.getContext()));
        doReturn(dialogSpy).when(spyPresenter).getCustomDialog(view);
        spyPresenter.showInformationDialog(view);

        verify(dialogSpy, times(1)).show();

        assertTrue(dialogSpy.findViewById(R.id.dialogText).isShown());

        assertTrue(dialogSpy.findViewById(R.id.dialogTitle).isShown());

        assertTrue(dialogSpy.findViewById(R.id.dialogImage).isShown());

        dialogSpy.findViewById(R.id.dialogButton).performClick();

        verify(dialogSpy, times(1)).dismiss();
    }

    @Test
    public void testShowInformationDialogShouldShowDynamicDialog() {
        View view = new View(RuntimeEnvironment.application);
        try {
            view.setTag(R.id.dynamic_label_info, new JSONArray("[{\"dynamic_label_title\": \"1\",\"dynamic_label_text\": \"1- A maximum of up to 3 weekly doses may be required.\",\"dynamic_label_image_src\":\"img/first_img.png\"}]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        view.setTag(R.id.label_dialog_title, "title");
        view.setTag(R.id.label_dialog_info, "info");

        JsonFormFragmentPresenter spyPresenter = spy(presenter);
        Dialog dialogSpy = spy(new Dialog(view.getContext()));
        doReturn(dialogSpy).when(spyPresenter).getCustomDialog(view);
        spyPresenter.showInformationDialog(view);

        verify(dialogSpy, times(1)).show();

        assertTrue(dialogSpy.findViewById(R.id.dialogRecyclerView).isShown());

        assertTrue(dialogSpy.findViewById(R.id.dialogTitle).isShown());

        dialogSpy.findViewById(R.id.dialogButton).performClick();

        verify(dialogSpy, times(1)).dismiss();
    }

    @Test
    public void testShowInformationDialogShouldShowAlertDialog() {
        View view = new View(RuntimeEnvironment.application);
        view.setTag(R.id.label_dialog_title, "title");
        view.setTag(R.id.label_dialog_info, "info");

        JsonFormFragmentPresenter spyPresenter = spy(presenter);

        AlertDialog.Builder spyBuilder = spy(new AlertDialog.Builder(view.getContext(),
                R.style.AppThemeAlertDialog));

        doReturn(spyBuilder).when(spyPresenter).getAlertDialogBuilder();

        spyPresenter.showInformationDialog(view);

        verify(spyBuilder, times(1)).show();
    }

    @Test
    public void testOnClickOnExpandNumberSelectorViewShouldSaveClickedViewText() {
        LinearLayout view = new LinearLayout(RuntimeEnvironment.application);
        view.setTag(R.id.key, "test");

        CustomTextView customTextView = new CustomTextView(RuntimeEnvironment.application);
        customTextView.setText("+2");
        customTextView.setTag(R.id.type, JsonFormConstants.NUMBER_SELECTOR);
        customTextView.setTag(R.id.is_number_selector_dialog_textview, true);
        customTextView.setTag(R.id.number_selector_dialog_numbers, Arrays.asList("1", "2", "3"));
        customTextView.setTag(R.id.number_selector_item, 1);
        customTextView.setTag(R.id.number_selector_number_of_selectors, 4);
        customTextView.setTag(R.id.number_selector_default_text_color, "#ffffff");
        customTextView.setTag(R.id.number_selector_selected_text_color, "#000000");
        customTextView.setTag(R.id.openmrs_entity_parent, "");
        customTextView.setTag(R.id.openmrs_entity, "");
        customTextView.setTag(R.id.openmrs_entity_id, "");

        view.addView(customTextView);

        JsonFormFragmentPresenter spyPresenter = spy(presenter);
        ReflectionHelpers.setField(spyPresenter, "mStepName", "step1");
        JsonFormFragmentView jsonFormFragmentView = mock(JsonFormFragmentView.class);
        spyPresenter.attachView(jsonFormFragmentView);
        spyPresenter.onClick(customTextView);

        verify(jsonFormFragmentView, only())
                .writeValue(eq(STEP1), anyString(), eq("2"), anyString(),
                        anyString(), anyString(), eq(false));
    }

    @Test
    public void testOnClickOnNormalNumberSelectorViewShouldSaveClickedViewText() {
        LinearLayout view = new LinearLayout(RuntimeEnvironment.application);
        view.setTag(R.id.key, "test");

        CustomTextView customTextView = new CustomTextView(RuntimeEnvironment.application);
        customTextView.setText("1");
        customTextView.setTag(R.id.type, JsonFormConstants.NUMBER_SELECTOR);
        customTextView.setTag(R.id.is_number_selector_dialog_textview, false);
        customTextView.setTag(R.id.number_selector_dialog_numbers, Arrays.asList("1", "2", "3"));
        customTextView.setTag(R.id.number_selector_item, 1);
        customTextView.setTag(R.id.number_selector_number_of_selectors, 4);
        customTextView.setTag(R.id.number_selector_default_text_color, "#ffffff");
        customTextView.setTag(R.id.number_selector_selected_text_color, "#000000");
        customTextView.setTag(R.id.openmrs_entity_parent, "");
        customTextView.setTag(R.id.openmrs_entity, "");
        customTextView.setTag(R.id.openmrs_entity_id, "");

        view.addView(customTextView);

        JsonFormFragmentPresenter spyPresenter = spy(presenter);
        ReflectionHelpers.setField(spyPresenter, "mStepName", "step1");
        JsonFormFragmentView jsonFormFragmentView = mock(JsonFormFragmentView.class);
        spyPresenter.attachView(jsonFormFragmentView);
        spyPresenter.onClick(customTextView);

        verify(jsonFormFragmentView, only())
                .writeValue(eq(STEP1), anyString(), eq("1"), anyString(),
                        anyString(), anyString(), eq(false));
    }

    @Test
    public void testOnMenuItemClickOnNumberSelectorMenuShouldWriteValue() {
        ReflectionHelpers.setStaticField(NumberSelectorFactory.class, "selectedTextView", mock(CustomTextView.class));
        ReflectionHelpers.setField(presenter, "mStepName", STEP1);
        Intent intent = new Intent();
        MenuItem menuItem = mock(MenuItem.class);
        doReturn("Test").when(menuItem).getTitle();
        doReturn(intent).when(menuItem).getIntent();
        presenter.onMenuItemClick(menuItem);
        verify(formFragment).writeValue(eq(STEP1), nullable(String.class), eq("Test"), nullable(String.class), nullable(String.class), nullable(String.class), eq(false));
    }

    @Test
    public void testOnSpinnerEditBtnClickShouldResetWidgetReadOnly() throws JSONException {
        String formWithReadOnlySpinner = "{\"count\":\"1\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true,\"show_errors_on_submit\":true,\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"step1\":{\"title\":\"Basic Form One\",\"fields\":[{\"key\":\"user_spinner\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"User Spinners\",\"values\":[\"User Option One\",\"User Option Two\"],\"keys\":[\"user_option_one\",\"user_option_two\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the sex\"},\"read_only\":true,\"openmrs_choice_ids\":{\"user_one\":\"1107AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"user_two\":\"1713AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}}]}}";
        JSONObject jsonForm = new JSONObject(formWithReadOnlySpinner);
        when(jsonFormActivity.getmJSONObject()).thenReturn(jsonForm);

        View view = spy(new View(RuntimeEnvironment.application));
        view.setTag(R.id.key, "user_spinner");
        view.setId(R.id.spinner_edit_button);
        view.setTag(R.id.type, JsonFormConstants.SPINNER);

        JSONObject expectedField = FormUtils.getFieldFromForm(jsonForm, (String) view.getTag(R.id.key));
        assertTrue(expectedField.optBoolean(JsonFormConstants.READ_ONLY));

        View editableView = spy(new View(RuntimeEnvironment.application));
        view.setTag(R.id.editable_view, editableView);
        presenter.onClick(view);

        expectedField = FormUtils.getFieldFromForm(jsonForm, (String) view.getTag(R.id.key));
        assertFalse(expectedField.optBoolean(JsonFormConstants.READ_ONLY));
    }
}
