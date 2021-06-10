package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.task.AttachRepeatingGroupTask;
import com.vijay.jsonwizard.utils.AppExecutors;
import com.vijay.jsonwizard.utils.FormUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@LooperMode(PAUSED)
public class RepeatingGroupFactoryTest extends FactoryTest {

    private RepeatingGroupFactory factory;

    @Mock
    private JsonFormFragment jsonFormFragment;

    private final String REPEATING_GROUP_FORM = "{\"count\":\"1\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true,\"show_errors_on_submit\":true,\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"step1\":{\"title\":\"Basic Form One\",\"fields\":[{\"key\":\"dips\",\"type\":\"repeating_group\",\"reference_edit_text\":\"step1:larval_count\",\"reference_edit_text_hint\":\"# of dips\",\"repeating_group_label\":\"dip\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"dips_count\":\"2\",\"v_required\":{\"value\":true,\"err\":\"Please specify the # of dips\"},\"value\":[{\"key\":\"larvae_total\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of larvae collected\",\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"}}]}]}}";

    @Override
    @Before
    public void setUp() {
        super.setUp();
        factory = new RepeatingGroupFactory();
    }

    @Test
    public void testRepeatingGroupFactoryInstantiatesViewsCorrectly() throws Exception {
        List<View> viewList = invokeGetViewsFromJson();
        Assert.assertNotNull(viewList);
        Assert.assertEquals(1, viewList.size());

        // invoke repeating group generation when reference edit text loses focus
        MaterialEditText referenceEditText = viewList.get(0).findViewById(R.id.reference_edit_text);
        referenceEditText.requestFocus();
        referenceEditText.clearFocus();
    }

    @Test
    public void testRepeatingGroupCountIsSaved() throws Exception {
        doReturn(jsonFormActivity).when(jsonFormFragment).getContext();
        doReturn(jsonFormActivity).when(jsonFormFragment).getJsonApi();
        JsonFormFragmentPresenter jsonFormFragmentPresenter = spy(new JsonFormFragmentPresenter(jsonFormFragment));
        doReturn("Step title").when(jsonFormFragmentPresenter).getStepTitle();

        JSONObject stepJsonObject = new JSONObject();
        stepJsonObject.put(JsonFormConstants.FIELDS, new JSONArray());
        doReturn(stepJsonObject).when(jsonFormFragment).getStep(JsonFormConstants.STEP1);
        doReturn(jsonFormFragmentPresenter).when(jsonFormFragment).getPresenter();

        View rootLayout = invokeGetViewsFromJson().get(0);
        ((MaterialEditText) rootLayout.findViewById(R.id.reference_edit_text)).setText("2");


        (rootLayout.findViewById(R.id.btn_repeating_group_done)).performClick();
        shadowOf(getMainLooper()).idle();
        Thread.sleep(TIMEOUT);

        JSONObject repeatingGroupCountObj = stepJsonObject.getJSONArray(JsonFormConstants.FIELDS).getJSONObject(0);
        Assert.assertEquals("2", repeatingGroupCountObj.getString(JsonFormConstants.VALUE));
    }

    @Test
    public void testParseIntWithDefaultIntegerInput() {
        final String integerString = "1";
        final int integerFromString = 1;

        Assert.assertThat(RepeatingGroupFactory.parseIntWithDefault(integerString), is(integerFromString));
    }

    @Test
    public void testParseIntWithDefaultNullInput() {
        final String emptyString = "";
        final int defaultInteger = 0;

        Assert.assertThat(RepeatingGroupFactory.parseIntWithDefault(emptyString), is(defaultInteger));
    }

    @Test
    public void testSetRepeatingGroupNumLimits() {
        RepeatingGroupFactory factorySpy = spy(factory);
        String stepName = "step_name";
        Context context = mock(Context.class);
        JsonFormFragment formFragment = mock(JsonFormFragment.class);
        JSONObject jsonObject = new JSONObject();
        CommonListener listener = mock(CommonListener.class);
        boolean popup = false;

        WidgetArgs widgetArgs = new WidgetArgs();
        widgetArgs.withContext(context)
                .withFormFragment(formFragment)
                .withJsonObject(jsonObject)
                .withListener(listener)
                .withPopup(popup)
                .withStepName(stepName);

        ReflectionHelpers.callInstanceMethod(factorySpy, "setRepeatingGroupNumLimits",
                ReflectionHelpers.ClassParameter.from(WidgetArgs.class, widgetArgs));
        Assert.assertEquals(widgetArgs.getJsonObject().optInt("repeating_group_min", 0), factorySpy.MIN_NUM_REPEATING_GROUPS);
        Assert.assertEquals(widgetArgs.getJsonObject().optInt("repeating_group_max", 35), factorySpy.MAX_NUM_REPEATING_GROUPS);
    }

    @Test
    public void testGetCustomTranslatableWidgetFields() {
        RepeatingGroupFactory factorySpy = spy(factory);

        Set<String> editableProperties = factorySpy.getCustomTranslatableWidgetFields();
        Assert.assertEquals(1, editableProperties.size());
    }

    @Test
    public void testSetOnEditorActionListener() throws Exception {
        RepeatingGroupFactory repeatingGroupFactory = mock(RepeatingGroupFactory.class);
        MaterialEditText referenceEditText = mock(MaterialEditText.class);
        TextView.OnEditorActionListener mockOnEditorActionListener =
                mock(EditText.OnEditorActionListener.class);

        String stepName = "step_name";
        Context context = mock(Context.class);
        JsonFormFragment formFragment = mock(JsonFormFragment.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.KEY, JsonFormConstants.KEY);
        CommonListener listener = mock(CommonListener.class);
        boolean popup = false;

        WidgetArgs widgetArgs = new WidgetArgs();
        widgetArgs.withContext(context)
                .withFormFragment(formFragment)
                .withJsonObject(jsonObject)
                .withListener(listener)
                .withPopup(popup)
                .withStepName(stepName);

        ReflectionHelpers.callInstanceMethod(repeatingGroupFactory, "setUpReferenceEditText",
                ReflectionHelpers.ClassParameter.from(ImageButton.class, mock(ImageButton.class)),
                ReflectionHelpers.ClassParameter.from(MaterialEditText.class, referenceEditText),
                ReflectionHelpers.ClassParameter.from(String.class, "Hint"),
                ReflectionHelpers.ClassParameter.from(String.class, "Label"),
                ReflectionHelpers.ClassParameter.from(WidgetArgs.class, widgetArgs));

        referenceEditText.setOnEditorActionListener(mockOnEditorActionListener);
        referenceEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        mockOnEditorActionListener.onEditorAction(referenceEditText, EditorInfo.IME_ACTION_DONE, null);

        verify(mockOnEditorActionListener, times(1)).onEditorAction(referenceEditText,
                EditorInfo.IME_ACTION_DONE, null);
    }

    @Test
    public void testUniqueChildElementKeyGenerationShouldContainChildKeyAsComponent() throws JSONException {
        final String parentRepeatingGroupWidgetKey = "parent";
        final String childElementKey = "child";
        final String uniqueId = "unique";

        JSONObject parentRepeatingGroupWidget = new JSONObject();
        parentRepeatingGroupWidget.put(JsonFormConstants.KEY, parentRepeatingGroupWidgetKey);
        WidgetArgs widgetArgs = new WidgetArgs().withJsonObject(parentRepeatingGroupWidget);

        JSONObject childElement = new JSONObject();
        childElement.put(JsonFormConstants.KEY, childElementKey);

        AttachRepeatingGroupTask attachRepeatingGroupTask = new AttachRepeatingGroupTask(mock(LinearLayout.class),
                0, new HashMap<Integer, String>(), widgetArgs, mock(ImageButton.class));

        ReflectionHelpers.callInstanceMethod(attachRepeatingGroupTask,
                "addUniqueIdentifiers",
                ReflectionHelpers.ClassParameter.from(JSONObject.class, childElement),
                ReflectionHelpers.ClassParameter.from(String.class, uniqueId));

        String[] uniqueIdComponents = childElement.getString(JsonFormConstants.KEY).split("_");
        Assert.assertEquals(childElementKey, uniqueIdComponents[0]);
    }

    @Test
    public void testRepeatingGroupDefaultCount() throws Exception {
        JsonApi jsonApi = mock(JsonApi.class);
        doReturn(jsonApi).when(jsonFormFragment).getJsonApi();
        doReturn(new AppExecutors()).when(jsonApi).getAppExecutors();

        JSONObject stepJsonObject = new JSONObject();
        stepJsonObject.put(JsonFormConstants.FIELDS, new JSONArray());
        doReturn(stepJsonObject).when(jsonFormFragment).getStep(eq(JsonFormConstants.STEP1));

        invokeGetViewsFromJson().get(0);

        JSONObject repeatingGroupCountObj = stepJsonObject.getJSONArray(JsonFormConstants.FIELDS).getJSONObject(0);
        Assert.assertEquals("0", repeatingGroupCountObj.getString(JsonFormConstants.VALUE));
    }

    @Test
    public void testAddOnDoneActionShouldCreateRepeatingGroups() throws InterruptedException, JSONException {
        JSONObject jsonFormObj = new JSONObject(REPEATING_GROUP_FORM);
        JSONObject jsonField = FormUtils.getFieldFromForm(jsonFormObj, "dips");

        Assert.assertEquals(1, FormUtils.getMultiStepFormFields(jsonFormObj).length());

        String count = "2";
        LinearLayout linearLayout = prepareLinearLayout();
        TextView textView = prepareRepeatingGrpTextView(count);
        linearLayout.addView(textView);

        LinearLayout rootLayout = prepareLinearLayout();
        rootLayout.addView(linearLayout);

        createRepeatingGroup(jsonFormObj, jsonField, rootLayout, textView);

        backgroundThreadHandling();

        Assert.assertEquals(1 + Integer.parseInt(count), FormUtils.getMultiStepFormFields(jsonFormObj).length());
    }

    @Test
    public void testAddOnDoneActionShouldUpdateRepeatingGroups() throws InterruptedException, JSONException {

        JSONObject jsonFormObj = new JSONObject(REPEATING_GROUP_FORM);
        JSONObject jsonField = FormUtils.getFieldFromForm(jsonFormObj, "dips");

        JSONObject stepDetails = prepareStepDetails(jsonFormObj);
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.STEPNAME, JsonFormConstants.STEP1);
        doReturn(bundle).when(jsonFormFragment).getArguments();
        doReturn(stepDetails).when(jsonFormFragment).getStep(JsonFormConstants.STEP1);

        String initialCount = "2";
        Assert.assertEquals(1, FormUtils.getMultiStepFormFields(jsonFormObj).length());

        LinearLayout linearLayout = prepareLinearLayout();

        TextView textView = prepareRepeatingGrpTextView(initialCount);
        linearLayout.addView(textView);

        LinearLayout rootLayout = prepareLinearLayout();
        rootLayout.addView(linearLayout);

        createRepeatingGroup(jsonFormObj, jsonField, rootLayout, textView);

        backgroundThreadHandling();

        //invoke again the same method to remove excess methods
        String newCount = "1";
        Assert.assertEquals(3, FormUtils.getMultiStepFormFields(jsonFormObj).length());
        textView.setText(newCount);
        createRepeatingGroup(jsonFormObj, jsonField, rootLayout, textView);

        backgroundThreadHandling();

        Assert.assertEquals(1 + Integer.parseInt(newCount), FormUtils.getMultiStepFormFields(jsonFormObj).length());
    }

    @NotNull
    private JSONObject prepareStepDetails(JSONObject jsonFormObj) throws JSONException {
        JSONObject stepDetails = new JSONObject();
        stepDetails.put(JsonFormConstants.STEP_TITLE, "Test Title");
        stepDetails.put(JsonFormConstants.FIELDS, FormUtils.getMultiStepFormFields(jsonFormObj));
        return stepDetails;
    }

    @NotNull
    private TextView prepareRepeatingGrpTextView(String count) {
        TextView textView = new MaterialEditText(RuntimeEnvironment.application);
        textView.setText(count);
        textView.setTag(R.id.repeating_group_item_count, Integer.parseInt(count));
        textView.setTag(R.id.repeating_group_label, "sample");
        return textView;
    }

    private void backgroundThreadHandling() throws InterruptedException {
        Thread.sleep(TIMEOUT);
        ShadowLooper.runUiThreadTasks();
    }

    @NotNull
    private LinearLayout prepareLinearLayout() {
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        linearLayout.setId(ViewUtil.generateViewId());
        return linearLayout;
    }

    private void createRepeatingGroup(JSONObject jsonFormObject, JSONObject jsonField, LinearLayout rootLayout, TextView textView) {
        JsonFormInteractor jsonFormInteractor = new JsonFormInteractor();
        JsonFormFragmentPresenter mockJsonFormFragmentPresenter = mock(JsonFormFragmentPresenter.class);
        doReturn(jsonFormActivity).when(jsonFormFragment).getContext();
        doReturn(jsonFormObject).when(jsonFormActivity).getmJSONObject();
        doReturn(jsonFormActivity).when(jsonFormFragment).getJsonApi();
        doReturn(mockJsonFormFragmentPresenter).when(jsonFormFragment).getPresenter();
        doReturn(jsonFormInteractor).when(mockJsonFormFragmentPresenter).getInteractor();


        Map<Integer, String> repeatingGroupLayouts = new HashMap<>();
        repeatingGroupLayouts.put(rootLayout.getId(), jsonField.optJSONArray(JsonFormConstants.VALUE).toString());

        ImageButton imageButton = new ImageButton(RuntimeEnvironment.application);
        WidgetArgs widgetArgs = new WidgetArgs().withContext(jsonFormActivity)
                .withFormFragment(jsonFormFragment)
                .withPopup(false)
                .withStepName(JsonFormConstants.STEP1)
                .withJsonObject(jsonField);

        ReflectionHelpers.setField(factory, "repeatingGroupLayouts", repeatingGroupLayouts);

        factory.addOnDoneAction(textView, imageButton, widgetArgs);
    }

    private List<View> invokeGetViewsFromJson() throws Exception {
        JSONObject step = new JSONObject();
        JSONArray fields = new JSONArray();
        step.put(JsonFormConstants.FIELDS, fields);
        doReturn(step).when(jsonFormActivity).getStep(anyString());

        JSONObject repeatingGroupWidget = new JSONObject();
        repeatingGroupWidget.put(JsonFormConstants.KEY, "key");
        repeatingGroupWidget.put(JsonFormConstants.VALUE, new JSONArray());
        repeatingGroupWidget.put(RepeatingGroupFactory.REFERENCE_EDIT_TEXT_HINT, "text");
        return factory.getViewsFromJson("step1", jsonFormActivity, jsonFormFragment,
                repeatingGroupWidget, mock(CommonListener.class));
    }
}
