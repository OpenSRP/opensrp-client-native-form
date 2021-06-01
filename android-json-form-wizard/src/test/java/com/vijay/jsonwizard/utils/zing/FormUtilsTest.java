package com.vijay.jsonwizard.utils.zing;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

@RunWith(PowerMockRunner.class)
public class FormUtilsTest extends BaseTest {

    private String optionKey = "";
    private String keyValue = "Tests";
    private String itemText = "Tim Apple";
    private String itemKey = "my_test";

    @Mock
    private Context context;

    private FormUtils formUtils;

    @Mock
    private Resources resources;
    @Mock
    private DisplayMetrics displayMetrics;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        formUtils = new FormUtils();
    }

    @PrepareForTest({TypedValue.class})
    @Test
    public void testSpToPx() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();
        Mockito.doReturn(resources).when(context).getResources();
        Mockito.doReturn(displayMetrics).when(resources).getDisplayMetrics();

        float spString = 30.0f;
        int expected = 30;
        PowerMockito.mockStatic(TypedValue.class);
        PowerMockito.when(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, expected, displayMetrics)).thenReturn(Float
                .valueOf(expected));

        int px = FormUtils.spToPx(context, spString);
        Assert.assertEquals(expected, px);
    }

    @PrepareForTest({TypedValue.class})
    @Test
    public void testDpToPx() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();
        Mockito.doReturn(resources).when(context).getResources();
        Mockito.doReturn(displayMetrics).when(resources).getDisplayMetrics();

        int expected = 150;
        float dpString = 30.0f;
        PowerMockito.mockStatic(TypedValue.class);
        PowerMockito.when(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpString, displayMetrics)).thenReturn(Float
                .valueOf(expected));

        int px = FormUtils.dpToPixels(context, dpString);
        Assert.assertEquals(expected, px);
    }

    @PrepareForTest({TextUtils.class, TypedValue.class})
    @Test
    public void testGetValueFromSpOrDpOrPxWithAnSpInput() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();
        Mockito.doReturn(resources).when(context).getResources();
        Mockito.doReturn(displayMetrics).when(resources).getDisplayMetrics();

        String spString = "30sp";
        int expected = 30;
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.mockStatic(TypedValue.class);
        PowerMockito.when(!TextUtils.isEmpty(spString)).thenReturn(false);
        PowerMockito.when(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, expected, displayMetrics)).thenReturn(Float
                .valueOf(expected));

        int px = FormUtils.getValueFromSpOrDpOrPx(spString, context);
        Assert.assertEquals(expected, px);
    }

    @Test
    public void showInfoIconLabelHasImage() throws JSONException {
        HashMap<String, String> imageAttributes = new HashMap<>(2);
        imageAttributes.put(JsonFormConstants.LABEL_INFO_HAS_IMAGE, "true");
        imageAttributes.put(JsonFormConstants.LABEL_INFO_IMAGE_SRC, "random_image_src");
        ImageView testImageView = PowerMockito.mock(ImageView.class);
        CommonListener listener = Mockito.mock(CommonListener.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.KEY, "key");
        jsonObject.put(JsonFormConstants.TYPE, "type");
        String stepName = "step_name_test";
        JSONArray canvasIds = new JSONArray();

        formUtils.showInfoIcon(stepName, jsonObject, listener, imageAttributes, testImageView, canvasIds);

        Mockito.verify(testImageView).setTag(eq(R.id.label_dialog_image_src), eq(imageAttributes.get(JsonFormConstants.LABEL_INFO_IMAGE_SRC)));
        Mockito.verify(testImageView).setTag(eq(R.id.key), eq(jsonObject.getString(JsonFormConstants.KEY)));
        Mockito.verify(testImageView).setTag(eq(R.id.type), eq(jsonObject.getString(JsonFormConstants.TYPE)));
        Mockito.verify(testImageView).setTag(eq(R.id.address), eq(stepName + ":" + jsonObject.getString(JsonFormConstants.KEY)));
        Mockito.verify(testImageView).setTag(eq(R.id.canvas_ids), eq(canvasIds.toString()));
        Mockito.verify(testImageView).setOnClickListener(eq(listener));
        Mockito.verify(testImageView).setVisibility(eq(View.VISIBLE));
    }

    @Test
    public void showInfoIconLabelHasText() throws JSONException {
        HashMap<String, String> imageAttributes = new HashMap<>(2);
        imageAttributes.put(JsonFormConstants.LABEL_INFO_TEXT, "test_text");
        imageAttributes.put(JsonFormConstants.LABEL_INFO_TITLE, "test_title");
        ImageView testImageView = PowerMockito.mock(ImageView.class);
        CommonListener listener = Mockito.mock(CommonListener.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.KEY, "key");
        jsonObject.put(JsonFormConstants.TYPE, "type");
        String stepName = "step_name_test";
        JSONArray canvasIds = new JSONArray();

        formUtils.showInfoIcon(stepName, jsonObject, listener, imageAttributes, testImageView, canvasIds);

        Mockito.verify(testImageView).setTag(eq(R.id.label_dialog_info), eq(imageAttributes.get(JsonFormConstants.LABEL_INFO_TEXT)));
        Mockito.verify(testImageView).setTag(eq(R.id.label_dialog_title), eq(imageAttributes.get(JsonFormConstants.LABEL_INFO_TITLE)));
        Mockito.verify(testImageView).setTag(eq(R.id.key), eq(jsonObject.getString(JsonFormConstants.KEY)));
        Mockito.verify(testImageView).setTag(eq(R.id.type), eq(jsonObject.getString(JsonFormConstants.TYPE)));
        Mockito.verify(testImageView).setTag(eq(R.id.address), eq(stepName + ":" + jsonObject.getString(JsonFormConstants.KEY)));
        Mockito.verify(testImageView).setTag(eq(R.id.canvas_ids), eq(canvasIds.toString()));
        Mockito.verify(testImageView).setOnClickListener(eq(listener));
        Mockito.verify(testImageView).setVisibility(eq(View.VISIBLE));
    }

    @Test
    public void showInfoIconLabelIsDynamic() throws JSONException {
        HashMap<String, String> imageAttributes = new HashMap<>(2);
        imageAttributes.put(JsonFormConstants.LABEL_IS_DYNAMIC, "true");
        imageAttributes.put(JsonFormConstants.LABEL_INFO_TITLE, "test_title");
        ImageView testImageView = PowerMockito.mock(ImageView.class);
        CommonListener listener = Mockito.mock(CommonListener.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.KEY, "key");
        jsonObject.put(JsonFormConstants.TYPE, "type");
        jsonObject.put(JsonFormConstants.DYNAMIC_LABEL_INFO, new JSONArray());
        String stepName = "step_name_test";
        JSONArray canvasIds = new JSONArray();

        formUtils.showInfoIcon(stepName, jsonObject, listener, imageAttributes, testImageView, canvasIds);

        Mockito.verify(testImageView).setTag(eq(R.id.dynamic_label_info), eq(jsonObject.getJSONArray(JsonFormConstants.DYNAMIC_LABEL_INFO)));
        Mockito.verify(testImageView).setTag(eq(R.id.label_dialog_title), eq(imageAttributes.get(JsonFormConstants.LABEL_INFO_TITLE)));
        Mockito.verify(testImageView).setTag(eq(R.id.key), eq(jsonObject.getString(JsonFormConstants.KEY)));
        Mockito.verify(testImageView).setTag(eq(R.id.type), eq(jsonObject.getString(JsonFormConstants.TYPE)));
        Mockito.verify(testImageView).setTag(eq(R.id.address), eq(stepName + ":" + jsonObject.getString(JsonFormConstants.KEY)));
        Mockito.verify(testImageView).setTag(eq(R.id.canvas_ids), eq(canvasIds.toString()));
        Mockito.verify(testImageView).setOnClickListener(eq(listener));
        Mockito.verify(testImageView).setVisibility(eq(View.VISIBLE));
    }


    @PrepareForTest({TextUtils.class, TypedValue.class, FormUtils.class})
    @Test
    public void testGetValueFromSpOrDpOrPxWithADpInput() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();
        Mockito.doReturn(resources).when(context).getResources();
        Mockito.doReturn(displayMetrics).when(resources).getDisplayMetrics();
        String dpString = "30dp";
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(dpString)).thenReturn(false);

        Float dp = 30f;
        int expected = 150;
        PowerMockito.mockStatic(TypedValue.class);
        PowerMockito.when(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)).thenReturn(Float
                .valueOf(expected));


        int px = FormUtils.getValueFromSpOrDpOrPx(dpString, context);
        Assert.assertEquals(expected, px);
    }

    @PrepareForTest({TextUtils.class})
    @Test
    public void testGetValueFromSpOrDpOrPxWithAPxInput() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();
        Mockito.doReturn(resources).when(context).getResources();
        Mockito.doReturn(20.0f).when(resources).getDimension(R.dimen.default_label_text_size);

        String pxString = "30px";
        int expected = 30;
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(pxString)).thenReturn(false);

        int px = FormUtils.getValueFromSpOrDpOrPx(pxString, context);
        Assert.assertEquals(expected, px);
    }

    @PrepareForTest({TextUtils.class})
    @Test
    public void testGetValueFromSpOrDpOrPxWithAnyString() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();
        Mockito.doReturn(resources).when(context).getResources();
        Mockito.doReturn(20.0f).when(resources).getDimension(R.dimen.default_label_text_size);

        String string = "String";
        int expected = 20;
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(string)).thenReturn(false);

        int px = FormUtils.getValueFromSpOrDpOrPx(string, context);
        Assert.assertEquals(expected, px);
    }

    @PrepareForTest({TextUtils.class})
    @Test
    public void testGetValueFromSpOrDpOrPxWithEmptyString() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();

        String string = "";
        int expected = 0;
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(string)).thenReturn(true);

        int px = FormUtils.getValueFromSpOrDpOrPx(string, context);
        Assert.assertEquals(expected, px);
    }

    @PrepareForTest({TextUtils.class})
    @Test
    public void testGetValueFromSpOrDPOrPxwithNull() {
        Application application = Mockito.spy(Application.class);
        Mockito.doReturn(context).when(application).getApplicationContext();

        int expected = 0;
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(null)).thenReturn(true);

        int px = FormUtils.getValueFromSpOrDpOrPx(null, context);
        Assert.assertEquals(expected, px);

    }

    @PrepareForTest({TextUtils.class})
    @Test
    public void testAddAssignedValueForCheckBox() {
        String itemType = "check_box";

        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(null)).thenReturn(true);
        Map<String, String> value = formUtils.addAssignedValue(itemKey, optionKey, keyValue, itemType, itemText);
        Assert.assertNotNull(value);

        Map<String, String> expectedValue = new HashMap<>();
        expectedValue.put(itemKey, optionKey + ":" + itemText + ":" + keyValue + ";" + itemType);

        Assert.assertEquals(expectedValue, value);

    }

    @PrepareForTest({TextUtils.class})
    @Test
    public void testAddAssignedValueForNativeRadio() {
        String itemType = "native_radio";

        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(null)).thenReturn(true);
        Map<String, String> value = formUtils.addAssignedValue(itemKey, optionKey, keyValue, itemType, itemText);
        Assert.assertNotNull(value);

        Map<String, String> expectedValue = new HashMap<>();
        expectedValue.put(itemKey, keyValue + ":" + itemText + ";" + itemType);

        Assert.assertEquals(expectedValue, value);

    }

    @PrepareForTest({TextUtils.class})
    @Test
    public void testAddAssignedValueForOtherWidget() {
        String itemType = "date_picker";

        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(!TextUtils.isEmpty(null)).thenReturn(true);
        Map<String, String> value = formUtils.addAssignedValue(itemKey, optionKey, keyValue, itemType, itemText);
        Assert.assertNotNull(value);

        Map<String, String> expectedValue = new HashMap<>();
        expectedValue.put(itemKey, keyValue + ";" + itemType);

        Assert.assertEquals(expectedValue, value);

    }

    @Test
    public void testExtractOptionOpenMRSAttributes() throws Exception {
        String optionItem = "\n" +
                "        {\n" +
                "          \"key\": \"1\",\n" +
                "          \"text\": \"Not done\",\n" +
                "          \"openmrs_entity_parent\": \"\",\n" +
                "          \"openmrs_entity\": \"concept\",\n" +
                "          \"openmrs_entity_id\": \"165269AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
                "        },";
        JSONObject optionItemJson = new JSONObject(optionItem);
        JSONArray valuesArray = new JSONArray();
        String itemKey = "respiratory_exam_radio_button";

        Whitebox.invokeMethod(formUtils, "extractOptionOpenMRSAttributes", valuesArray, optionItemJson, itemKey);
        Assert.assertEquals(valuesArray.length(), 1);

    }
}
