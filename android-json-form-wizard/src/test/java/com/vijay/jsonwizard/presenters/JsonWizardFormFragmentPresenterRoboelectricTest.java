package com.vijay.jsonwizard.presenters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.TestConstants;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.DatePickerDialog;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.interfaces.OnFieldsInvalid;
import com.vijay.jsonwizard.shadow.ShadowFileProvider;
import com.vijay.jsonwizard.shadow.ShadowIntent;
import com.vijay.jsonwizard.shadow.ShadowPermissionUtils;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.widgets.NativeRadioButtonFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.UUID;

import static com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter.RESULT_LOAD_IMG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by samuelgithengi on 9/8/20.
 */
public class JsonWizardFormFragmentPresenterRoboelectricTest extends BaseTest {

    @Mock
    private JsonWizardFormFragment formFragment;

    @Mock
    private JsonFormActivity formActivity;

    @Mock
    private JsonFormInteractor jsonFormInteractor;

    @Mock
    private OnFieldsInvalid onFieldsInvalid;

    @Mock
    private PackageManager packageManager;

    @Captor
    private ArgumentCaptor<Intent> intentArgumentCaptor;

    private JsonWizardFormFragmentPresenter formFragmentPresenter;

    private Context context = RuntimeEnvironment.application;

    @Before
    public void setUp() throws JSONException {
        when(formFragment.getJsonApi()).thenReturn(formActivity);
        when(formFragment.getOnFieldsInvalidCallback()).thenReturn(onFieldsInvalid);
        when(formActivity.getmJSONObject()).thenReturn(new JSONObject(TestConstants.BASIC_FORM));
        formFragmentPresenter = new JsonWizardFormFragmentPresenter(formFragment, jsonFormInteractor);
        formFragmentPresenter.attachView(formFragment);

    }

    @Test
    public void testOnNextClickShouldMoveToNextStep() throws JSONException {
        when(formActivity.nextStep()).thenReturn("step2");
        formFragmentPresenter.onNextClick(null);
        verify(jsonFormInteractor).fetchFormElements("step2", formFragment, formActivity.getmJSONObject().getJSONObject("step2"), null, false);
        verify(formActivity).initializeDependencyMaps();
        verify(formActivity).setNextStepRelevant(false);
        verify(formFragment).skipStepsOnNextPressed("step2");
    }

    @Test
    public void testOnClickShouldOpenSimPrintsRegistration() {
        View view = new View(context);
        view.setTag(R.id.type, JsonFormConstants.FINGER_PRINT);
        view.setTag(R.id.finger_print_option, JsonFormConstants.SIMPRINTS_OPTION_REGISTER);
        view.setTag(R.id.project_id, "test");
        view.setTag(R.id.module_id, "nf");
        view.setTag(R.id.user_id, "jdoe");
        formFragmentPresenter.onClick(view);
        verify(formFragment).startSimprintsRegistration("test", "jdoe", "nf");
    }

    @Test
    public void testOnClickShouldOpenSimPrintsVerification() {
        View view = new View(context);
        view.setTag(R.id.type, JsonFormConstants.FINGER_PRINT);
        view.setTag(R.id.finger_print_option, JsonFormConstants.SIMPRINTS_OPTION_VERIFY);
        view.setTag(R.id.project_id, "test");
        view.setTag(R.id.module_id, "nf");
        view.setTag(R.id.user_id, "jdoe");
        String guid = UUID.randomUUID().toString();
        view.setTag(R.id.guid, guid);
        formFragmentPresenter.onClick(view);
        verify(formFragment).startSimprintsVerification("test", "jdoe", "nf", guid);
    }

    @Test
    @Config(shadows = {ShadowPermissionUtils.class, ShadowIntent.class, ShadowFileProvider.class})
    public void testOnClickShouldSOpenPictureTakingActivity() {
        context = spy(context);
        when(context.getPackageManager()).thenReturn(packageManager);
        when(formFragment.getContext()).thenReturn(context);
        View view = new View(context);
        view.setTag(R.id.type, JsonFormConstants.CHOOSE_IMAGE);
        view.setTag(R.id.key, "profile_picture");
        formFragmentPresenter.onClick(view);
        verify(formFragment).startActivityForResult(intentArgumentCaptor.capture(), eq(RESULT_LOAD_IMG));
        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, intentArgumentCaptor.getValue().getAction());
        assertEquals(Uri.fromFile(new File("profile.jpg")), intentArgumentCaptor.getValue().getParcelableExtra(MediaStore.EXTRA_OUTPUT));
    }

    @Test
    public void testOnClickShouldDisplayDatePickerDialog() {
        LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.native_form_compound_button_parent, null);
        CustomTextView customTextView = new CustomTextView(context);
        Activity activity = Robolectric.buildActivity(AppCompatActivity.class).create().get();
        RadioButton radioButton = new RadioButton(context);
        view.setTag(R.id.specify_textview, customTextView);
        view.setTag(R.id.native_radio_button, radioButton);
        view.setTag(R.id.specify_context, activity);


        view.setTag(R.id.type, JsonFormConstants.NATIVE_RADIO_BUTTON);
        view.setTag(R.id.key, "date");
        view.setTag(R.id.specify_type, JsonFormConstants.CONTENT_INFO);
        view.setTag(R.id.specify_widget, JsonFormConstants.DATE_PICKER);
        view.setTag(R.id.option_json_object, new JSONObject());
        formFragmentPresenter.onClick(view);
        DatePickerDialog dialogFragment = (DatePickerDialog) activity.getFragmentManager()
                .findFragmentByTag(NativeRadioButtonFactory.class.getCanonicalName());
        assertNotNull(dialogFragment);
    }

}
