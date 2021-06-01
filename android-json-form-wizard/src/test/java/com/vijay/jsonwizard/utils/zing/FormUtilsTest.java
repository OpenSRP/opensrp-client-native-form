package com.vijay.jsonwizard.utils.zing;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
    public void testSetTextStyleBold(){
        AppCompatTextView mockTextView = PowerMockito.mock(AppCompatTextView.class);
        FormUtils.setTextStyle(JsonFormConstants.BOLD, mockTextView);
        Mockito.verify(mockTextView).setTypeface(ArgumentMatchers.<Typeface>isNull(), eq(Typeface.BOLD));
    }

    @Test
    public void testSetTextStyleItalic(){
        AppCompatTextView mockTextView = PowerMockito.mock(AppCompatTextView.class);
        FormUtils.setTextStyle(JsonFormConstants.ITALIC, mockTextView);
        Mockito.verify(mockTextView).setTypeface(ArgumentMatchers.<Typeface>isNull(), eq(Typeface.ITALIC));
    }

    @Test
    public void testSetTextStyleBoldItalic(){
        AppCompatTextView mockTextView = PowerMockito.mock(AppCompatTextView.class);
        FormUtils.setTextStyle(JsonFormConstants.BOLD_ITALIC, mockTextView);
        Mockito.verify(mockTextView).setTypeface(ArgumentMatchers.<Typeface>isNull(), eq(Typeface.BOLD_ITALIC));
    }

    @Test
    public void testSetTextStyleNormal(){
        AppCompatTextView mockTextView = PowerMockito.mock(AppCompatTextView.class);
        FormUtils.setTextStyle(JsonFormConstants.NORMAL, mockTextView);
        Mockito.verify(mockTextView).setTypeface(ArgumentMatchers.<Typeface>isNull(), eq(Typeface.NORMAL));
    }

    @Test
    public void testSetTextStyleUnknown(){
        AppCompatTextView mockTextView = PowerMockito.mock(AppCompatTextView.class);
        FormUtils.setTextStyle("normal", mockTextView);
        Mockito.verify(mockTextView).setTypeface(ArgumentMatchers.<Typeface>isNull(), eq(Typeface.NORMAL));
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
