package com.vijay.jsonwizard.utils;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.Button;
import android.widget.LinearLayout;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.ExpansionPanelItemModel;
import com.vijay.jsonwizard.domain.ExpansionPanelValuesModel;
import com.vijay.jsonwizard.interfaces.OnFormFetchedCallback;
import com.vijay.jsonwizard.model.DynamicLabelInfo;
import com.vijay.jsonwizard.views.CustomTextView;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Facts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.client.utils.contract.ClientFormContract;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class FormUtilsTest extends BaseTest {
    private final String spinnerWithOptions = " {\n" +
            "        \"key\": \"response_spinner_with_options\",\n" +
            "        \"openmrs_entity_parent\": \"\",\n" +
            "        \"openmrs_entity\": \"\",\n" +
            "        \"openmrs_entity_id\": \"\",\n" +
            "        \"type\": \"spinner\",\n" +
            "        \"hint\": \"Response Spinners\",\n" +
            "        \"options\": [\n" +
            "          {\n" +
            "            \"key\": \"yes\",\n" +
            "            \"text\": \"Yes\",\n" +
            "            \"openmrs_entity\": \"\",\n" +
            "            \"openmrs_entity_id\": \"\",\n" +
            "            \"openmrs_entity_parent\": \"\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"no\",\n" +
            "            \"text\": \"No\",\n" +
            "            \"openmrs_entity\": \"openmrs_entity\",\n" +
            "            \"openmrs_entity_id\": \"openmrs_entity_id\",\n" +
            "            \"openmrs_entity_parent\": \"openmrs_entity_parent\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"maybe\",\n" +
            "            \"text\": \"Maybe\",\n" +
            "            \"openmrs_entity\": \"openmrs_entity_2\",\n" +
            "            \"openmrs_entity_id\": \"openmrs_entity_id_2\",\n" +
            "            \"openmrs_entity_parent\": \"openmrs_entity_parent_2\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"value\": \"maybe\",\n" +
            "        \"v_required\": {\n" +
            "          \"value\": \"true\",\n" +
            "          \"err\": \"Please enter response\"\n" +
            "        }\n" +
            "      }";
    private final String spinnerWithoutOptions = " {\n" +
            "        \"key\": \"user_spinner\",\n" +
            "        \"openmrs_entity\": \"openmrs_entity\",\n" +
            "        \"openmrs_entity_id\": \"openmrs_entity_id\",\n" +
            "        \"openmrs_entity_parent\": \"openmrs_entity_parent\",\n" +
            "        \"type\": \"spinner\",\n" +
            "        \"hint\": \"User Spinners\",\n" +
            "        \"values\": [\n" +
            "          \"User Option One\",\n" +
            "          \"User Option Two\"\n" +
            "        ],\n" +
            "        \"keys\": [\n" +
            "          \"user_option_one\",\n" +
            "          \"user_option_two\"\n" +
            "        ],\n" +
            "        \"v_required\": {\n" +
            "          \"value\": \"true\",\n" +
            "          \"err\": \"Please enter the sex\"\n" +
            "        },\n" +
            "        \"openmrs_choice_ids\": {\n" +
            "          \"user_one\": \"1107AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
            "          \"user_two\": \"1713AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
            "        },\n" +
            "        \"value\": \"user_one\"\n" +
            "      }";
    private FormUtils formUtils;
    private JSONObject jsonFormObject;
    private String jsonForm = "{\"count\":\"4\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true,\"show_errors_on_submit\":true,\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"step1\":{\"title\":\"Test\",\"next\":\"step2\",\"fields\":[{\"key\":\"delivery_complications\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"161641AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"select one\",\"type\":\"check_box\",\"label\":\"Any delivery complications?\",\"label_text_size\":\"18sp\",\"label_text_color\":\"#FF9800\",\"hint\":\"Any delivery complications?\",\"read_only\":true,\"editable\":true,\"exclusive\":[\"none\"],\"options\":[{\"key\":\"none\",\"text\":\"None\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"severe_bleeding\",\"text\":\"Severe bleeding/Hemorrhage\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"text_color\":\"#000000\"},{\"key\":\"placenta_previa\",\"text\":\"Placenta previa\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"text_size\":\"15sp\"},{\"key\":\"cord_prolapse\",\"text\":\"Cord prolapse\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"text_size\":\"10sp\"},{\"key\":\"prolonged_obstructed_labour\",\"text\":\"Prolonged/obstructed labour\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"abnormal_presentation\",\"text\":\"Abnormal presentation\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"text_color\":\"#FF9800\"},{\"key\":\"perineal_tear\",\"text\":\"Perineal tear (2, 3 or 4th degree)\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"Other\",\"text\":\"Other\",\"value\":false,\"openmrs_choice_id\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":\"false\"}},{\"key\":\"first_name\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"person\",\"openmrs_entity_id\":\"first_name\",\"type\":\"edit_text\",\"hidden\":true,\"hint\":\"First name\",\"edit_type\":\"name\",\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the first name\"},\"v_regex\":{\"value\":\"[A-Za-z\\\\s\\\\.\\\\-]*\",\"err\":\"Please enter a valid name\"},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"sample-calculation-rules.yml\"}}}}]},\"step2\":{\"title\":\"Test 2\",\"next\":\"step3\",\"fields\":[{\"key\":\"fetal_heartbeat\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"native_radio\",\"label\":\"Which medications is she still taking ? Which medications is she still taking ?\",\"label_text_style\":\"bold\",\"text_color\":\"#000000\",\"extra_rel\":true,\"has_extra_rel\":\"yes\",\"options\":[{\"key\":\"yes\",\"text\":\"Yes\",\"value\":false,\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"specify_info\":\"specify fetal heart rate (bpm)...\",\"specify_widget\":\"normal_edit_text\",\"specify_info_color\":\"#8C8C8C\",\"secondary_suffix\":\"bpm\",\"content_form\":\"fetal_heartbeat_sub_form\"},{\"key\":\"no\",\"text\":\"No\",\"value\":false,\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"specify_info\":\"specify fetal heart rate (bpm)...\",\"specify_widget\":\"check_box\",\"specify_info_color\":\"#8C8C8C\",\"content_form\":\"fetal_heartbeat_sub_form\"}],\"v_required\":{\"value\":true,\"err\":\"Please specify if fetal heartbeat is present.\"}},{\"key\":\"fetal_heart_beat_rate_value\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"normal_edit_text\",\"edit_text_style\":\"bordered\",\"edit_type\":\"number\"}]},\"step3\":{\"title\":\"Maternal Exam\",\"next\":\"step4\",\"fields\":[{\"key\":\"spacer\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"spacer\",\"type\":\"spacer\",\"spacer_height\":\"20dp\"},{\"key\":\"gravida_label\",\"type\":\"label\",\"label_text_style\":\"bold\",\"text\":\"No. of pregnancies (including this pregnancy)\",\"text_color\":\"#000000\",\"v_required\":{\"value\":true}}]}}";

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.initMocks(this);
        formUtils = new FormUtils();
        jsonFormObject = new JSONObject(jsonForm);
    }

    @Test
    public void testGetDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_MONTH, -1);
        Calendar res = FormUtils.getDate("today-1w");
        assertEquals(sdf.format(calendar.getTime()), sdf.format(res.getTime()));
    }

    @Test
    public void testGetFieldFromForm() throws JSONException {
        JSONObject field = FormUtils.getFieldFromForm(jsonFormObject, "gravida_label");
        Assert.assertNotNull(field);
        Assert.assertEquals("label", field.getString(JsonFormConstants.TYPE));
    }

    @Test
    public void testGetSpecifyText() throws JSONException {
        String secondaryValues = "[{\"key\":\"respiratory_exam_abnormal\",\"type\":\"check_box\",\"values\":[\"rapid_breathing:Rapid breathing:true\"]},{\"key\":\"respiratory_exam_radio_button\",\"type\":\"native_radio\",\"values\":[\"1:Not done\"]},{\"key\":\"respiratory_exam_abnormal_other\",\"type\":\"edit_text\",\"values\":[\"other:Respiratory exam answer\"]}]";
        JSONArray jsonArray = new JSONArray(secondaryValues);
        String specifyText = formUtils.getSpecifyText(jsonArray);
        Assert.assertNotNull(specifyText);
        Assert.assertEquals("Rapid breathing, Not done, Respiratory exam answer", specifyText);
    }

    @Test
    public void testCreateExpansionPanelValues() throws JSONException {
        String selectedValues = "[{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"label\":\"Blood type test\",\"label_text_style\":\"bold\",\"text_color\":\"#000000\",\"type\":\"extended_radio_button\",\"options\":[{\"key\":\"done_today\",\"text\":\"Done today\",\"type\":\"done_today\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"done_earlier\",\"text\":\"Done earlier\",\"type\":\"done_earlier\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165385AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"ordered\",\"text\":\"Ordered\",\"type\":\"ordered\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"not_done\",\"text\":\"Not done\",\"type\":\"not_done\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1118AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Blood type status is required\"},\"index\":\"0\",\"value\":\"done_today\",\"step\":\"step1\",\"is-rule-check\":true},{\"key\":\"spacer\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"spacer\",\"type\":\"spacer\",\"spacer_height\":\"10dp\",\"index\":\"1\"},{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"key\":\"blood_type_test_date_today_hidden\",\"type\":\"hidden\",\"label_text_style\":\"bold\",\"text_color\":\"#000000\",\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_calculation_rules.yml\"}}},\"index\":\"2\",\"value\":\"10-03-2020\"},{\"key\":\"blood_type_test_date\",\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"date_picker\",\"hint\":\"Blood type test date\",\"expanded\":\"false\",\"max_date\":\"today\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_calculation_rules.yml\"}}},\"v_required\":{\"value\":true,\"err\":\"Date that the blood test was done.\"},\"index\":\"3\",\"is_visible\":false},{\"key\":\"blood_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163126AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"native_radio\",\"label\":\"Blood type\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"a\",\"text\":\"A\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163115AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"b\",\"text\":\"B\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163116AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"ab\",\"text\":\"AB\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163117AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"o\",\"text\":\"O\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163118AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Please specify blood type\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}},\"index\":\"4\",\"is_visible\":true,\"value\":\"ab\"},{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"native_radio\",\"label\":\"Rh factor\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"positive\",\"text\":\"Positive\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"negative\",\"text\":\"Negative\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"664AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Rh factor is required\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}},\"index\":\"5\",\"is_visible\":true,\"step\":\"step1\",\"is-rule-check\":true,\"value\":\"negative\"},{\"key\":\"rh_factor_toaster\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"toaster_notes\",\"text\":\"Rh factor negative counseling\",\"toaster_info_text\":\"- Woman is at risk of alloimmunisation if the baby's father is Rh positive or unknown.\\n\\n- Proceed with local protocol to investigate sensitization and the need for referral.\\n\\n- If Rh negative and non-sensitized, woman should receive anti- D prophylaxis postnatally if the baby is Rh positive.\",\"toaster_info_title\":\"Rh factor negative counseling\",\"toaster_type\":\"warning\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}},\"index\":\"6\",\"is_visible\":true}]";
        JSONArray fields = formUtils.createExpansionPanelValues(new JSONArray(selectedValues));
        Assert.assertNotNull(fields);
        Assert.assertEquals(5, fields.length());
    }

    @Test
    public void testCheckValuesContent() throws JSONException {
        JSONArray value = new JSONArray("[\n" +
                "  {\n" +
                "    \"key\": \"blood_type_test_status\",\n" +
                "    \"type\": \"extended_radio_button\",\n" +
                "    \"label\": \"Blood type test\",\n" +
                "    \"index\": 0,\n" +
                "    \"values\": [\n" +
                "      \"done_today:Done today\"\n" +
                "    ],\n" +
                "    \"openmrs_attributes\": {\n" +
                "      \"openmrs_entity_parent\": \"\",\n" +
                "      \"openmrs_entity\": \"concept\",\n" +
                "      \"openmrs_entity_id\": \"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
                "    },\n" +
                "    \"value_openmrs_attributes\": [\n" +
                "      {\n" +
                "        \"key\": \"blood_type_test_status\",\n" +
                "        \"openmrs_entity_parent\": \"\",\n" +
                "        \"openmrs_entity\": \"concept\",\n" +
                "        \"openmrs_entity_id\": \"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]");
        boolean showHidden = formUtils.checkValuesContent(value);
        Assert.assertFalse(showHidden);
    }

    @Test
    public void testCheckValuesContentWithMultipleValues() throws JSONException {
        JSONArray value = new JSONArray("[{\"key\":\"blood_type_test_status\",\"type\":\"extended_radio_button\",\"label\":\"Blood type test\",\"index\":0,\"values\":[\"done_today:Done today\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"blood_type_test_date_today_hidden\",\"type\":\"hidden\",\"label\":\"\",\"index\":2,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"}},{\"key\":\"blood_type_test_date\",\"type\":\"date_picker\",\"label\":\"Blood type test date\",\"index\":3,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}}]");
        boolean showHidden = formUtils.checkValuesContent(value);
        Assert.assertTrue(showHidden);
    }

    @Test
    public void testAddFormDetails() {
        String form = "{\"count\":\"1\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true,\"show_errors_on_submit\":true,\"form_version\":\"0.0.1\",\"step1\":{\"title\":\"Basic Form One\",\"next\":\"step2\",\"fields\":[{\"key\":\"user_image\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"choose_image\",\"uploadButtonText\":\"Take a photo of the child\"}]}}";
        String updatedForm = formUtils.addFormDetails(form);
        Assert.assertNotNull(updatedForm);
        Assert.assertTrue(updatedForm.contains("appVersionName"));
    }

    @Test
    public void testAddFormDetailsWithNull() {
        String updatedForm = formUtils.addFormDetails(null);
        Assert.assertNotNull(updatedForm);
        Assert.assertTrue(StringUtils.isAllBlank(updatedForm));
        Assert.assertFalse(updatedForm.contains("appVersionName"));
    }

    @Test
    public void testLoadExpansionPanelValues() throws JSONException {
        JSONArray jsonArray = new JSONArray("[{\"key\":\"accordion_panel_demo\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"text\":\"Expansion Panel Demo\",\"type\":\"expansion_panel\",\"content_form\":\"expansion_panel_sub_form\",\"container\":\"anc_test\",\"value\":[{\"key\":\"blood_type_test_status\",\"type\":\"extended_radio_button\",\"label\":\"Blood type test\",\"index\":0,\"values\":[\"done_today:Done today\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"blood_type_test_date_today_hidden\",\"type\":\"hidden\",\"label\":\"\",\"index\":2,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"}},{\"key\":\"blood_type_test_date\",\"type\":\"date_picker\",\"label\":\"Blood type test date\",\"index\":3,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}},{\"key\":\"blood_type\",\"type\":\"native_radio\",\"label\":\"Blood type\",\"index\":4,\"values\":[\"ab:AB\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163126AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163117AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"rh_factor\",\"type\":\"native_radio\",\"label\":\"Rh factor\",\"index\":5,\"values\":[\"positive:Positive\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}],\"required_fields\":[\"blood_type_test_status\",\"blood_type\",\"rh_factor\"]}]");
        JSONArray values = formUtils.loadExpansionPanelValues(jsonArray, "accordion_panel_demo");
        Assert.assertNotNull(values);
        Assert.assertEquals(5, values.length());
    }

    @Test
    public void testCreateSecondaryValuesMap() throws JSONException {
        JSONArray jsonArray = new JSONArray("[{\"key\":\"accordion_panel_demo\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"text\":\"Expansion Panel Demo\",\"type\":\"expansion_panel\",\"content_form\":\"expansion_panel_sub_form\",\"container\":\"anc_test\",\"value\":[{\"key\":\"blood_type_test_status\",\"type\":\"extended_radio_button\",\"label\":\"Blood type test\",\"index\":0,\"values\":[\"done_today:Done today\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"blood_type_test_date_today_hidden\",\"type\":\"hidden\",\"label\":\"\",\"index\":2,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"}},{\"key\":\"blood_type_test_date\",\"type\":\"date_picker\",\"label\":\"Blood type test date\",\"index\":3,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}},{\"key\":\"blood_type\",\"type\":\"native_radio\",\"label\":\"Blood type\",\"index\":4,\"values\":[\"ab:AB\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163126AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163117AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"rh_factor\",\"type\":\"native_radio\",\"label\":\"Rh factor\",\"index\":5,\"values\":[\"positive:Positive\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}],\"required_fields\":[\"blood_type_test_status\",\"blood_type\",\"rh_factor\"]}]");
        JSONArray values = formUtils.loadExpansionPanelValues(jsonArray, "accordion_panel_demo");
        Assert.assertNotNull(values);

        Map<String, ExpansionPanelValuesModel> valuesModelMap = formUtils.createSecondaryValuesMap(values);
        Assert.assertNotNull(valuesModelMap);
        Assert.assertTrue(valuesModelMap.containsKey("blood_type_test_status"));

        ExpansionPanelValuesModel valuesModel = valuesModelMap.get("blood_type_test_status");
        Assert.assertNotNull(valuesModel);
        Assert.assertEquals("Blood type test", valuesModel.getLabel());
    }

    @Test
    public void testGetExpansionPanelItem() throws JSONException {
        JSONArray values = new JSONArray("[{\"key\":\"blood_type_test_status\",\"type\":\"extended_radio_button\",\"label\":\"Blood type test\",\"index\":0,\"values\":[\"done_today:Done today\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"blood_type_test_date_today_hidden\",\"type\":\"hidden\",\"label\":\"\",\"index\":2,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"}},{\"key\":\"blood_type_test_date\",\"type\":\"date_picker\",\"label\":\"Blood type test date\",\"index\":3,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}},{\"key\":\"blood_type\",\"type\":\"native_radio\",\"label\":\"Blood type\",\"index\":4,\"values\":[\"ab:AB\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163126AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163117AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"rh_factor\",\"type\":\"native_radio\",\"label\":\"Rh factor\",\"index\":5,\"values\":[\"positive:Positive\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"rh_factor_two\",\"type\":\"check_box\",\"label\":\"Rh factor\",\"index\":5,\"values\":[\"positive:Positive\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}]");
        Assert.assertNotNull(values);

        ExpansionPanelItemModel expansionPanelItemModel = FormUtils.getExpansionPanelItem("blood_type_test_status", values);
        Assert.assertNotNull(expansionPanelItemModel);
        Assert.assertEquals("Done today", expansionPanelItemModel.getSelectedValues());

        ExpansionPanelItemModel expansionPanelItemModelDatePicker = FormUtils.getExpansionPanelItem("blood_type_test_date", values);
        Assert.assertNotNull(expansionPanelItemModelDatePicker);
        Assert.assertEquals("10-03-2020", expansionPanelItemModelDatePicker.getSelectedValues());


        ExpansionPanelItemModel expansionPanelItemModelCheckBox = FormUtils.getExpansionPanelItem("rh_factor_two", values);
        Assert.assertNotNull(expansionPanelItemModelCheckBox);
        Assert.assertEquals("Positive", expansionPanelItemModelCheckBox.getSelectedValues());
    }

    @Test
    public void testAddExpansionPanelFormValues() throws JSONException {
        JSONArray values = new JSONArray("[{\"key\":\"blood_type_test_status\",\"type\":\"extended_radio_button\",\"label\":\"Blood type test\",\"index\":0,\"values\":[\"done_today:Done today\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"blood_type_test_date_today_hidden\",\"type\":\"hidden\",\"label\":\"\",\"index\":2,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\"}},{\"key\":\"blood_type_test_date\",\"type\":\"date_picker\",\"label\":\"Blood type test date\",\"index\":3,\"values\":[\"10-03-2020\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}},{\"key\":\"blood_type\",\"type\":\"native_radio\",\"label\":\"Blood type\",\"index\":4,\"values\":[\"ab:AB\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163126AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"blood_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163117AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"rh_factor\",\"type\":\"native_radio\",\"label\":\"Rh factor\",\"index\":5,\"values\":[\"positive:Positive\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]},{\"key\":\"rh_factor_two\",\"type\":\"check_box\",\"label\":\"Rh factor\",\"index\":5,\"values\":[\"positive:Positive\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}]");
        Assert.assertNotNull(values);

        Map<String, ExpansionPanelValuesModel> secondaryValuesMap = formUtils.createSecondaryValuesMap(values);
        Assert.assertNotNull(secondaryValuesMap);
        Assert.assertEquals(6, secondaryValuesMap.size());

        JSONArray fields = new JSONArray("[{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163725AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"label\":\"Blood type test\",\"label_text_style\":\"bold\",\"text_color\":\"#000000\",\"type\":\"extended_radio_button\",\"options\":[{\"key\":\"done_today\",\"text\":\"Done today\",\"type\":\"done_today\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"done_earlier\",\"text\":\"Done earlier\",\"type\":\"done_earlier\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165385AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"ordered\",\"text\":\"Ordered\",\"type\":\"ordered\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"not_done\",\"text\":\"Not done\",\"type\":\"not_done\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1118AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Blood type status is required\"}},{\"key\":\"spacer\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"spacer\",\"type\":\"spacer\",\"spacer_height\":\"10dp\"},{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"key\":\"blood_type_test_date_today_hidden\",\"type\":\"hidden\",\"label_text_style\":\"bold\",\"text_color\":\"#000000\",\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_calculation_rules.yml\"}}}},{\"key\":\"blood_type_test_date\",\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"date_picker\",\"hint\":\"Blood type test date\",\"expanded\":\"false\",\"max_date\":\"today\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_calculation_rules.yml\"}}},\"v_required\":{\"value\":true,\"err\":\"Date that the blood test was done.\"}},{\"key\":\"blood_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163126AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"native_radio\",\"label\":\"Blood type\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"a\",\"text\":\"A\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163115AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"b\",\"text\":\"B\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163116AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"ab\",\"text\":\"AB\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163117AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"o\",\"text\":\"O\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163118AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Please specify blood type\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}}},{\"key\":\"rh_factor\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"native_radio\",\"label\":\"Rh factor\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"positive\",\"text\":\"Positive\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"negative\",\"text\":\"Negative\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"664AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Rh factor is required\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}}},{\"key\":\"rh_factor_two\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160232AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"check_box\",\"label\":\"Rh factor\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"positive\",\"text\":\"Positive\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"negative\",\"text\":\"Negative\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"664AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Rh factor is required\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}}},{\"key\":\"rh_factor_toaster\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"toaster_notes\",\"text\":\"Rh factor negative counseling\",\"toaster_info_text\":\"- Woman is at risk of alloimmunisation if the baby's father is Rh positive or unknown.\\n\\n- Proceed with local protocol to investigate sensitization and the need for referral.\\n\\n- If Rh negative and non-sensitized, woman should receive anti- D prophylaxis postnatally if the baby is Rh positive.\",\"toaster_info_title\":\"Rh factor negative counseling\",\"toaster_type\":\"warning\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"expansion_panel_relevance_rules.yml\"}}}}]");
        Assert.assertNotNull(fields);

        JSONArray jsonArray = formUtils.addExpansionPanelFormValues(fields, secondaryValuesMap);
        Assert.assertNotNull(jsonArray);
        Assert.assertEquals("blood_type_test_status", jsonArray.getJSONObject(0).getString(JsonFormConstants.KEY));
        Assert.assertEquals("done_today", jsonArray.getJSONObject(0).getString(JsonFormConstants.VALUE));
    }

    @Test
    public void testGetSubFormJson() throws Exception {
        JSONObject form = FormUtils.getSubFormJson("expansion_panel_sub_form", null, RuntimeEnvironment.application, true);
        Assert.assertNotNull(form);
        Assert.assertTrue(form.has(JsonFormConstants.CONTENT_FORM));
        Assert.assertEquals(7, form.getJSONArray(JsonFormConstants.CONTENT_FORM).length());
    }

    public void testAddExpansionPanelFormValuesShouldPopulateValueAttributeOfJsonObj() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        String widgetRadioExpansionPanelValue = "{\"key\":\"ultrasound\",\"type\":\"extended_radio_button\",\"label\":\"Ultrasound test\",\"index\":0,\"values\":[\"done_today:Done today\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"ultrasound\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}";
        String widgetCheckBoxExpansionPanelValue = "{\"key\":\"ultrasound_notdone\",\"type\":\"check_box\",\"label\":\"Reason\",\"index\":4,\"values\":[\"expired_stock:Expired stock:true\"],\"openmrs_attributes\":{\"openmrs_entity_parent\":\"161476AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165182AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"value_openmrs_attributes\":[{\"key\":\"ultrasound_notdone\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165299AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}]}";

        String widgetRadio = "{\"key\":\"ultrasound\",\"openmrs_entity_parent\":\"159617AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163725AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"label\":\"Ultrasound test\",\"label_text_style\":\"bold\",\"text_color\":\"#000000\",\"type\":\"extended_radio_button\",\"options\":[{\"key\":\"done_today\",\"text\":\"Done today\",\"type\":\"done_today\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"done_earlier\",\"text\":\"Done earlier\",\"type\":\"done_earlier\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165385AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"ordered\",\"text\":\"Ordered\",\"type\":\"ordered\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"not_done\",\"text\":\"Not done\",\"type\":\"not_done\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1118AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true}}";
        String widgetCheckbox = "{\"key\":\"ultrasound_notdone\",\"openmrs_entity_parent\":\"161476AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165182AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"check_box\",\"label\":\"Reason\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"stock_out\",\"text\":\"Stock out\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165183AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"expired_stock\",\"text\":\"Expired stock\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165299AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"other\",\"text\":\"Other (specify)\",\"openmrs_entity_parent\":\"161476AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"tests_relevance_rules.yml\"}}},\"v_required\":{\"value\":true}}";
        JSONObject jsonObjectRadio = new JSONObject(widgetRadioExpansionPanelValue);
        JSONObject jsonObjectCheckBox = new JSONObject(widgetCheckBoxExpansionPanelValue);

        jsonArray.put(new JSONObject(widgetRadio)).put(new JSONObject(widgetCheckbox));
        Map<String, ExpansionPanelValuesModel> expansionPanelValuesModelMap = new HashMap<>();
        ExpansionPanelValuesModel expansionPanelValuesModel = new ExpansionPanelValuesModel(
                jsonObjectRadio.optString(JsonFormConstants.KEY),
                jsonObjectRadio.optString(JsonFormConstants.TYPE),
                jsonObjectRadio.optString(JsonFormConstants.LABEL),
                jsonObjectRadio.optInt(JsonFormConstants.INDEX),
                jsonObjectRadio.optJSONArray(JsonFormConstants.VALUES),
                jsonObjectRadio.optJSONObject(JsonFormConstants.OPENMRS_ATTRIBUTES),
                jsonObjectRadio.optJSONArray(JsonFormConstants.VALUE_OPENMRS_ATTRIBUTES));
        expansionPanelValuesModelMap.put("ultrasound", expansionPanelValuesModel);

        ExpansionPanelValuesModel expansionPanelValuesModelNotDone = new ExpansionPanelValuesModel(
                jsonObjectCheckBox.optString(JsonFormConstants.KEY),
                jsonObjectCheckBox.optString(JsonFormConstants.TYPE),
                jsonObjectCheckBox.optString(JsonFormConstants.LABEL),
                jsonObjectCheckBox.optInt(JsonFormConstants.INDEX),
                jsonObjectCheckBox.optJSONArray(JsonFormConstants.VALUES),
                jsonObjectCheckBox.optJSONObject(JsonFormConstants.OPENMRS_ATTRIBUTES),
                jsonObjectCheckBox.optJSONArray(JsonFormConstants.VALUE_OPENMRS_ATTRIBUTES));
        expansionPanelValuesModelMap.put("ultrasound_notdone", expansionPanelValuesModelNotDone);
        FormUtils formUtils = new FormUtils();
        JSONArray result = formUtils.addExpansionPanelFormValues(jsonArray, expansionPanelValuesModelMap);
        JSONObject objectRadio = result.optJSONObject(0);//expired_stock
        JSONObject objectCheckbox = result.optJSONObject(1);
        Assert.assertEquals("done_today", objectRadio.optString(JsonFormConstants.VALUE));
        JSONArray jsonArrayOptions = objectCheckbox.optJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        JSONObject expectedOption = null;
        for (int i = 0; i < jsonArrayOptions.length(); i++) {
            JSONObject option = jsonArrayOptions.optJSONObject(i);
            if (option.has(JsonFormConstants.VALUE) && option.optBoolean(JsonFormConstants.VALUE)) {
                expectedOption = option;
            }
        }
        Assert.assertNotNull(expectedOption);
        Assert.assertEquals("expired_stock", expectedOption.optString(JsonFormConstants.KEY));
    }


    @Test
    public void testGetSpinnerValueOpenMRSAttributesShouldCorrectlyExtractOpenMRSAttributes() throws JSONException {
        // spinner WITH options block
        JSONArray valueOpenMRSAttributes = new JSONArray();
        JSONObject spinnerWithOptionsObj = new JSONObject(spinnerWithOptions);
        formUtils.getSpinnerValueOpenMRSAttributes(spinnerWithOptionsObj, valueOpenMRSAttributes);
        JSONObject jsonObject = valueOpenMRSAttributes.getJSONObject(0);
        assertEquals(jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT), "openmrs_entity_parent_2");
        assertEquals(jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY), "openmrs_entity_2");
        assertEquals(jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID), "openmrs_entity_id_2");
        assertEquals(jsonObject.getString(JsonFormConstants.KEY), "response_spinner_with_options");

        // spinner WITHOUT options block
        JSONObject spinnerWithoutOptionsObj = new JSONObject(spinnerWithoutOptions);
        valueOpenMRSAttributes = new JSONArray();
        formUtils.getSpinnerValueOpenMRSAttributes(spinnerWithoutOptionsObj, valueOpenMRSAttributes);
        jsonObject = valueOpenMRSAttributes.getJSONObject(0);
        assertEquals(jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT), "openmrs_entity_parent");
        assertEquals(jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY), "openmrs_entity");
        assertEquals(jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID), "1107AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        assertEquals(jsonObject.getString(JsonFormConstants.KEY), "user_spinner");
    }


    @Test
    public void getFormJsonFromRepositoryOrAssets() throws Exception {
        formUtils = new FormUtils();

        Resources resources = Mockito.mock(Resources.class);
        Configuration configuration = Mockito.mock(Configuration.class);
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        ClientFormContract.Model clientForm = new TestClientForm();
        clientForm.setJson("{\"form\":\"Sick Child Referral\",\"count\":\"1\",\"encounter_type\":\" \",\"entity_id\":\"\",\"relational_id\":\"\",\"rules_file\":\"rule/general_neat_referral_form_rules.yml\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"steps\":[{\"title\":\"Sick child form\",\"fields\":[{\"name\":\"chw_referral_service\",\"type\":\"invisible\",\"properties\":{\"text\":\"Choose referral service\"},\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"09978\",\"openmrs_entity_parent\":\"\"},\"options\":[],\"required_status\":\"yes:Please specify referral service\"},{\"name\":\"problem\",\"type\":\"multi_choice_checkbox\",\"properties\":{\"text\":\"Pick condition/problem associated with the client.\"},\"meta_data\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163182AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"options\":[{\"name\":\"Fast_breathing_and_difficulty_with_breathing\",\"text\":\"Fast breathing and difficulty with breathing\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"142373AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Umbilical_cord_navel_bleeding\",\"text\":\"Umbilical cord/navel bleeding\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"123844AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Excessive_crying\",\"text\":\"Excessive crying\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"140944AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Convulsions\",\"text\":\"Convulsions\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"113054AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Unable_to_breastfeed_or_swallow\",\"text\":\"Unable to breastfeed or swallow\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"159861AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Neck_stiffness\",\"text\":\"Neck stiffness\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"112721AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Fever\",\"text\":\"Fever\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"140238AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Bloating\",\"text\":\"Bloating\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"147132AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Redness_around_the_umbilical_cord_foul_smelling_discharge_from_the_umbilical_cord\",\"text\":\"Redness around the umbilical cord, foul-smelling discharge from the umbilical cord\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"132407AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Bacterial_conjunctivitis\",\"text\":\"Bacterial conjunctivitis\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"148026AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Severe_anaemia\",\"text\":\"Severe anaemia\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"162044AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Severe_abdominal_pain\",\"text\":\"Severe abdominal pain\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165271AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Pale_or_jaundiced\",\"text\":\"Pale or jaundiced\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"136443AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Cyanosis_blueness_of_lips\",\"text\":\"Cyanosis (blueness of lips)\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"143050AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Skin_rash_pustules\",\"text\":\"Skin rash / pustules\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"512AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Diarrhea\",\"text\":\"Diarrhea\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"142412AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Vomiting\",\"text\":\"Vomiting\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"122983AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Disabilities\",\"text\":\"Disabilities\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"162558AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Premature_baby\",\"text\":\"Premature baby\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"159908AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Care_of_HIV_exposed_infant\",\"text\":\"Care of HIV-exposed infant\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"164818AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Immunisation\",\"text\":\"Immunisation\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1914AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Other_symptom\",\"text\":\"Other symptom\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}}],\"required_status\":\"yes:Please specify client's problems\"},{\"name\":\"problem_other\",\"type\":\"text_input_edit_text\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160632AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"163182AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"properties\":{\"hint\":\"Other symptoms\",\"type\":\"name\"},\"required_status\":\"true:Please specify other symptoms\",\"subjects\":\"problem:map\"},{\"name\":\"service_before_referral\",\"meta_data\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"164378AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"type\":\"multi_choice_checkbox\",\"properties\":{\"text\":\"Pre-referral management given.\"},\"options\":[{\"name\":\"ORS\",\"text\":\"ORS\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"351AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Panadol\",\"text\":\"Panadol\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"70116AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"Other_treatment\",\"text\":\"Other treatment\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}},{\"name\":\"None\",\"text\":\"None\",\"is_exclusive\":true,\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"164369AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"\"}}],\"required_status\":\"Pre-referral management field is required\"},{\"name\":\"service_before_referral_other\",\"type\":\"text_input_edit_text\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160632AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity_parent\":\"164378AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"properties\":{\"hint\":\"Other Treatment\",\"type\":\"name\"},\"required_status\":\"true:Please specify other treatment given\",\"subjects\":\"service_before_referral:map\"},{\"name\":\"chw_referral_hf\",\"type\":\"spinner\",\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"chw_referral_hf\",\"openmrs_entity_parent\":\"\"},\"properties\":{\"text\":\"Choose referral facility\",\"searchable\":\"Choose referral facility\"},\"options\":[],\"required_status\":\"yes:Please specify referral facility\"},{\"name\":\"referral_appointment_date\",\"type\":\"datetime_picker\",\"properties\":{\"hint\":\"Please select the appointment date\",\"type\":\"date_picker\",\"display_format\":\"dd/MM/yyyy\"},\"meta_data\":{\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"referral_appointment_date\",\"openmrs_entity_parent\":\"\"},\"required_status\":\"true:Please specify the appointment date\"},{\"name\":\"referral_date\",\"meta_data\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163181AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"type\":\"hidden\"},{\"name\":\"referral_time\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"referral_time\",\"type\":\"hidden\"},{\"name\":\"referral_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"referral_type\",\"type\":\"hidden\"},{\"name\":\"referral_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"referral_status\",\"type\":\"hidden\"}]}]}");

        configuration.locale = new Locale("en");

        Context context = Mockito.spy(RuntimeEnvironment.application);

        Mockito.when(context.getResources()).thenReturn(resources);
        Mockito.when(resources.getConfiguration()).thenReturn(configuration);
        Mockito.when(clientFormRepository.getActiveClientFormByIdentifier("sick_child_referral_form")).thenReturn(clientForm);

        JSONObject form = formUtils.getFormJsonFromRepositoryOrAssets(context, clientFormRepository, "sick_child_referral_form");

        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier("sick_child_referral_form");
        Assert.assertNotNull(form);
    }

    @Test
    public void extractFormNameWithoutExtensionShouldReturnNameWithoutExtension() {
        String expectedAns = "registration_form";

        Assert.assertEquals(expectedAns, formUtils.extractFormNameWithoutExtension("registration_form.json"));
    }

    @Test
    public void getRulesFromRepositoryShouldCallRepositoryQueryingClientForm() {
        String rulesFileIdentifier = "registration_calculation.yml";
        Context context = Mockito.spy(RuntimeEnvironment.application);
        ClientFormContract.Model clientForm = new TestClientForm();

        clientForm.setJson("");
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        Mockito.doReturn(clientForm).when(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(rulesFileIdentifier));

        Assert.assertNotNull(formUtils.getRulesFromRepository(context, clientFormRepository, rulesFileIdentifier));

        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(rulesFileIdentifier));
    }

    @Test
    public void getRulesFromRepositoryShouldRetryRepositoryQueryingClientFormWhenFilenameWithFilePathDoesNotExist() {
        String rulesFileIdentifierWithFilePath = "rest/registration_calculation.yml";
        String rulesFileIdentifier = "registration_calculation.yml";
        Context context = Mockito.spy(RuntimeEnvironment.application);
        ClientFormContract.Model clientForm = new TestClientForm();

        clientForm.setJson("");
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        Mockito.doReturn(null).when(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(rulesFileIdentifierWithFilePath));
        Mockito.doReturn(clientForm).when(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(rulesFileIdentifier));

        BufferedReader bufferedReader = formUtils.getRulesFromRepository(context, clientFormRepository, rulesFileIdentifierWithFilePath);
        Assert.assertNotNull(bufferedReader);
        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(rulesFileIdentifierWithFilePath));
        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(rulesFileIdentifier));
    }

    @Test
    public void getSubFormFromRepository() throws JSONException {
        Context context = Mockito.spy(RuntimeEnvironment.application);
        String subFormIdentifier = "some_tests";
        ClientFormContract.Model clientForm = new TestClientForm();
        clientForm.setJson("{}");
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);
        Mockito.doReturn(clientForm).when(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq("json.form/sub_form/" + subFormIdentifier));

        JSONObject jsonObject = formUtils.getSubFormJsonFromRepository(context, clientFormRepository, subFormIdentifier, null, false);

        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(subFormIdentifier));
        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq(subFormIdentifier + ".json"));
        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(Mockito.eq("json.form/sub_form/" + subFormIdentifier));

        Assert.assertEquals(0, jsonObject.length());
    }

    @Test
    public void injectFormStatusShouldAddClientFormDetailsToJsonObject() throws JSONException {
        //TODO: Fix the below line
        //ClientFormContract.Model clientForm = new TestClientForm();
        TestClientForm clientForm = new TestClientForm();
        clientForm.setId(3);
        clientForm.setNew(true);
        clientForm.setVersion("0.0.1");
        JSONObject jsonObject = new JSONObject();
        formUtils.injectFormStatus(jsonObject, clientForm);

        Assert.assertEquals(3, jsonObject.getInt(JsonFormConstants.Properties.CLIENT_FORM_ID));
        Assert.assertTrue(jsonObject.getBoolean(JsonFormConstants.Properties.IS_NEW));
        Assert.assertEquals("0.0.1", jsonObject.getString(JsonFormConstants.Properties.FORM_VERSION));
    }

    @Test
    public void getClientFormIdShouldReturnClientFormIdPropertyOnJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.Properties.CLIENT_FORM_ID, 3);
        Assert.assertEquals(3, FormUtils.getClientFormId(jsonObject));
    }

    @Test
    public void getClientFormIdShouldReturn0WhenJSONObjectDoesNotHaveClientFormId() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Assert.assertEquals(0, FormUtils.getClientFormId(jsonObject));
    }

    @Test
    public void isFormNewShouldReturnFalseWhenJSONObjectDoesNotHaveIsNewProperty() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Assert.assertFalse(FormUtils.isFormNew(jsonObject));
    }

    @Test
    public void isFormNewShouldReturnTrueWhenJSONObjectIsNewPropertyIsTrue() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.Properties.IS_NEW, true);
        Assert.assertTrue(FormUtils.isFormNew(jsonObject));
    }

    @Test
    public void getFormJsonShouldReturnCorrectFormWithSameLength() {
        Assert.assertEquals(10011, formUtils.getFormJson(RuntimeEnvironment.application, "test_basic_form").toString().length());
    }

    @Test(expected = JSONException.class)
    public void getFormJsonFromRepositoryOrAssetsShouldThrowExceptionWhenJsonIsSyntacticallyIncorrect() throws JSONException {
        String formIdentity = "reg.json";
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);

        TestClientForm clientForm = new TestClientForm();
        clientForm.setJson("{\"sonic");

        Mockito.doReturn(clientForm).when(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);

        formUtils.getFormJsonFromRepositoryOrAssets(RuntimeEnvironment.application, clientFormRepository, formIdentity);
    }

    @Test
    public void getFormJsonFromRepositoryOrAssetsShouldReturnCorrectJsonFromDb() throws JSONException {
        String formIdentity = "reg.json";
        String jsonText = "{\"count\":\"3\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true}";
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);

        TestClientForm clientForm = new TestClientForm();
        clientForm.setJson(jsonText);

        Mockito.doReturn(clientForm).when(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);

        JSONObject retrievedJson = formUtils.getFormJsonFromRepositoryOrAssets(RuntimeEnvironment.application, clientFormRepository, formIdentity);
        Assert.assertEquals(jsonText, retrievedJson.toString());
        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);
    }


    @Test
    public void getFormJsonFromRepositoryOrAssetsShouldCallCallbackWithCorrectClientFormFromDb() throws JSONException {
        String formIdentity = "reg.json";
        String jsonText = "{\"count\":\"3\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true}";
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);

        TestClientForm clientForm = new TestClientForm();
        clientForm.setJson(jsonText);

        OnFormFetchedCallback<JSONObject> onFormFetchedCallback = (OnFormFetchedCallback<JSONObject>) Mockito.mock(OnFormFetchedCallback.class);

        Mockito.doReturn(clientForm).when(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);

        formUtils.getFormJsonFromRepositoryOrAssets(RuntimeEnvironment.application, clientFormRepository, formIdentity, onFormFetchedCallback);
        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);

        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        Mockito.verify(onFormFetchedCallback).onFormFetched(jsonObjectArgumentCaptor.capture());

        Assert.assertEquals(jsonText, jsonObjectArgumentCaptor.getValue().toString());
    }

    @Test
    public void getFormJsonFromRepositoryOrAssetsShouldRetrieveFormFromAssetsWhenNotAvailableOnRepository() throws JSONException {
        formUtils = Mockito.spy(formUtils);

        String formIdentity = "reg.json";
        String jsonText = "{\"count\":\"3\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true}";
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);

        TestClientForm clientForm = new TestClientForm();
        clientForm.setJson(jsonText);

        OnFormFetchedCallback<JSONObject> onFormFetchedCallback = (OnFormFetchedCallback<JSONObject>) Mockito.mock(OnFormFetchedCallback.class);

        Mockito.doReturn(new JSONObject(jsonText)).when(formUtils).getFormJson(RuntimeEnvironment.application, formIdentity);

        formUtils.getFormJsonFromRepositoryOrAssets(RuntimeEnvironment.application, clientFormRepository, formIdentity, onFormFetchedCallback);
        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);
        Mockito.verify(formUtils).getFormJson(RuntimeEnvironment.application, formIdentity);

        ArgumentCaptor<JSONObject> jsonObjectArgumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        Mockito.verify(onFormFetchedCallback).onFormFetched(jsonObjectArgumentCaptor.capture());

        Assert.assertEquals(jsonText, jsonObjectArgumentCaptor.getValue().toString());
    }


    @Test
    public void getFormJsonFromRepositoryOrAssetsShouldRetrieveFormFromAssetsAndReturnCorrectJsonWhenNotAvailableOnRepository() throws JSONException {
        formUtils = Mockito.spy(formUtils);

        String formIdentity = "reg.json";
        String jsonText = "{\"count\":\"3\",\"encounter_type\":\"Test\",\"entity_id\":\"\",\"relational_id\":\"\",\"validate_on_submit\":true}";
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);

        TestClientForm clientForm = new TestClientForm();
        clientForm.setJson(jsonText);

        Mockito.doReturn(new JSONObject(jsonText)).when(formUtils).getFormJson(RuntimeEnvironment.application, formIdentity);

        JSONObject jsonObject = formUtils.getFormJsonFromRepositoryOrAssets(RuntimeEnvironment.application, clientFormRepository, formIdentity);

        Mockito.verify(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);
        Mockito.verify(formUtils).getFormJson(RuntimeEnvironment.application, formIdentity);
        Assert.assertEquals(jsonText, jsonObject.toString());
    }


    @Test
    public void getFormJsonFromRepositoryOrAssetsShouldCallHandleJsonFormOrRulesErrorWhenCallbackIsProvidedAndJsonIsIncorrect() throws JSONException {
        formUtils = Mockito.spy(formUtils);

        String formIdentity = "reg.json";
        String jsonText = "{\"count\":\"3\"";
        ClientFormContract.Dao clientFormRepository = Mockito.mock(ClientFormContract.Dao.class);

        TestClientForm clientForm = new TestClientForm();
        clientForm.setJson(jsonText);

        OnFormFetchedCallback<JSONObject> onFormFetchedCallback = (OnFormFetchedCallback<JSONObject>) Mockito.mock(OnFormFetchedCallback.class);

        Mockito.doReturn(clientForm).when(clientFormRepository).getActiveClientFormByIdentifier(formIdentity);

        formUtils.getFormJsonFromRepositoryOrAssets(RuntimeEnvironment.application, clientFormRepository, formIdentity, onFormFetchedCallback);

        Mockito.verify(clientFormRepository, Mockito.times(2)).getActiveClientFormByIdentifier(formIdentity);
        Mockito.verify(formUtils).getFormJson(RuntimeEnvironment.application, formIdentity);
        Mockito.verify(formUtils).handleJsonFormOrRulesError(Mockito.eq(RuntimeEnvironment.application), Mockito.eq(clientFormRepository), Mockito.eq(false), Mockito.eq(formIdentity), Mockito.any(OnFormFetchedCallback.class));

    }

    @Test
    public void testCreateSecondaryValueObject() throws JSONException {
        formUtils = Mockito.spy(formUtils);
        String key = "test_key";
        String type = "check_box";
        JSONArray jsonArray = new JSONArray("[\n" +
                "                  \"3:Abnormal\"\n" +
                "                ]");
        JSONObject openMrsEntities = new JSONObject("{\"openmrs_entity_parent\": \"test\",\n" +
                "        \"openmrs_entity\": \"test_1\",\n" +
                "        \"openmrs_entity_id\": \"test_2\"\n" +
                "      }");
        JSONArray valueOpenmrsEntities = new JSONArray("        [\n" +
                "          {\"openmrs_entity_parent\": \"test\",\n" +
                "            \"openmrs_entity\": \"test_1\",\n" +
                "            \"openmrs_entity_id\": \"test_2\"\n" +
                "          }\n" +
                "        ]");

        JSONObject jsonObject = formUtils.createSecondaryValueObject(key, type, jsonArray, openMrsEntities, valueOpenmrsEntities);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals("test_key", jsonObject.getString("key"));
        Assert.assertTrue(jsonObject.has("value_openmrs_attributes"));
        Assert.assertTrue(jsonObject.has("openmrs_attributes"));
    }

    @Test
    public void testGetSecondaryValuesWithTypeExpansionPanel() throws JSONException {
        formUtils = Mockito.spy(formUtils);
        String type = JsonFormConstants.EXPANSION_PANEL;
        String expansionPanelString = "{\"key\":\"resTwo3\",\"text\":\"Abnormal\",\"type\":\"expansion_panel\",\"content_form\":\"child_enrollment_two_sub_form\",\"content_form_location\":\"\",\"value\":[{\"key\":\"respiratory_exam_radio_button\",\"type\":\"native_radio\",\"values\":[\"3:Abnormal\"]},{\"key\":\"respiratory_exam_abnormal_other\",\"type\":\"edit_text\",\"values\":[\"other:Respiratory exam answer two\"]}]}";
        JSONObject jsonObject = new JSONObject(expansionPanelString);
        JSONArray array = formUtils.getSecondaryValues(jsonObject, type);
        Assert.assertNotNull(array);
        Assert.assertTrue(array.length() > 0);
        Assert.assertEquals(2, array.length());
    }

    @Test
    public void testGetSecondaryValuesWithTypeRadioButton() throws JSONException {
        formUtils = Mockito.spy(formUtils);
        String type = JsonFormConstants.NATIVE_RADIO_BUTTON;
        String expansionPanelString = "{\"key\":\"resTwo3\",\"text\":\"Abnormal\",\"specify_info\":\"Specify\",\"specify_info_color\":\"#b5b5b5\",\"specify_widget\":\"check_box\",\"content_form\":\"child_enrollment_two_sub_form\",\"content_form_location\":\"\",\"secondary_suffix\":\"bpm\",\"secondary_value\":[{\"key\":\"respiratory_exam_radio_button\",\"type\":\"native_radio\",\"values\":[\"3:Abnormal\"]},{\"key\":\"respiratory_exam_abnormal_other\",\"type\":\"edit_text\",\"values\":[\"other:Respiratory exam answer two\"]}]}";
        JSONObject jsonObject = new JSONObject(expansionPanelString);
        JSONArray array = formUtils.getSecondaryValues(jsonObject, type);
        Assert.assertNotNull(array);
        Assert.assertTrue(array.length() > 0);
        Assert.assertEquals(2, array.length());
    }

    @Test
    public void testGetCheckBoxResultsWithIsRuleCheckTrue() throws JSONException {
        formUtils = Mockito.spy(formUtils);
        String checkBoxString = "{\"key\":\"user_check_box\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"select one\",\"type\":\"check_box\",\"label\":\"Do want to select any checkbox?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"None\",\"text\":\"None\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"yes\",\"text\":\"Yes\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"no\",\"text\":\"No\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"other\",\"text\":\"Other\",\"value\":true,\"openmrs_choice_id\":\"\"}],\"v_required\":{\"value\":\"false\"},\"value\":\"[yes]\",\"is-rule-check\":true}";
        JSONObject jsonObject = new JSONObject(checkBoxString);
        Facts facts = formUtils.getCheckBoxResults(jsonObject);
        Assert.assertNotNull(facts);
        Assert.assertEquals(4, facts.asMap().size());
    }

    @Test
    public void testGetCheckBoxResultsWithNoValueInOptionsIsRuleCheckTrue() throws JSONException {
        formUtils = Mockito.spy(formUtils);
        String checkBoxString = "{\"key\":\"user_check_box\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"select one\",\"type\":\"check_box\",\"label\":\"Do want to select any checkbox?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"None\",\"text\":\"None\",\"openmrs_choice_id\":\"\"},{\"key\":\"yes\",\"text\":\"Yes\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"no\",\"text\":\"No\",\"openmrs_choice_id\":\"\"},{\"key\":\"other\",\"text\":\"Other\",\"openmrs_choice_id\":\"\"}],\"v_required\":{\"value\":\"false\"},\"value\":\"[yes]\",\"is-rule-check\":true}";
        JSONObject jsonObject = new JSONObject(checkBoxString);
        Facts facts = formUtils.getCheckBoxResults(jsonObject);
        Assert.assertNotNull(facts);
        Assert.assertEquals(1, facts.asMap().size());
    }

    @Test
    public void testGetCheckBoxResultsWithIsRuleCheckFalse() throws JSONException {
        formUtils = Mockito.spy(formUtils);
        String checkBoxString = "{\"key\":\"user_check_box\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"select one\",\"type\":\"check_box\",\"label\":\"Do want to select any checkbox?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"None\",\"text\":\"None\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"yes\",\"text\":\"Yes\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"no\",\"text\":\"No\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"other\",\"text\":\"Other\",\"value\":true,\"openmrs_choice_id\":\"\"}],\"v_required\":{\"value\":\"false\"},\"value\":\"[yes]\",\"is-rule-check\":false}";
        JSONObject jsonObject = new JSONObject(checkBoxString);
        Facts facts = formUtils.getCheckBoxResults(jsonObject);
        Assert.assertNotNull(facts);
        Assert.assertEquals(5, facts.asMap().size());
    }

    @Test
    public void testGetCheckBoxResultsWithNoIsRuleCheck() throws JSONException {
        formUtils = Mockito.spy(formUtils);
        String checkBoxString = "{\"key\":\"user_check_box\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_data_type\":\"select one\",\"type\":\"check_box\",\"label\":\"Do want to select any checkbox?\",\"label_text_style\":\"bold\",\"options\":[{\"key\":\"None\",\"text\":\"None\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"yes\",\"text\":\"Yes\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"no\",\"text\":\"No\",\"value\":true,\"openmrs_choice_id\":\"\"},{\"key\":\"other\",\"text\":\"Other\",\"value\":true,\"openmrs_choice_id\":\"\"}],\"v_required\":{\"value\":\"false\"},\"value\":\"[yes]\"}";
        JSONObject jsonObject = new JSONObject(checkBoxString);
        Facts facts = formUtils.getCheckBoxResults(jsonObject);
        Assert.assertNotNull(facts);
        Assert.assertEquals(4, facts.asMap().size());
    }

    @Test
    public void testShowGenericDialogShouldInvokeExpectedMethods() {
        formUtils = Mockito.spy(formUtils);

        FragmentTransaction mockFragmentTransaction = Mockito.mock(FragmentTransaction.class);
        Utils utils = Mockito.spy(new Utils());
        ReflectionHelpers.setField(formUtils, "utils", utils);
        Mockito.doReturn(mockFragmentTransaction).when(utils).getFragmentTransaction(ArgumentMatchers.any(Activity.class));

        Activity mockActivity = Mockito.mock(Activity.class);
        LinearLayout mainLayout = Mockito.spy(new LinearLayout(RuntimeEnvironment.application));
        Mockito.doReturn(mainLayout).when(mockActivity).findViewById(R.id.main_layout);

        Button button = new Button(RuntimeEnvironment.application);
        button.setTag(R.id.specify_context, mockActivity);
        button.setTag(R.id.type, JsonFormConstants.EXPANSION_PANEL);
        button.setTag(R.id.specify_content, "user_native_sub_form");
        button.setTag(R.id.specify_content_form, "");
        button.setTag(R.id.specify_textview, new CustomTextView(button.getContext()));
        button.setTag(R.id.specify_reasons_textview, new CustomTextView(button.getContext()));

        formUtils.showGenericDialog(button);

        Mockito.verify(mainLayout, Mockito.only()).clearFocus();
        Mockito.verify(mockFragmentTransaction).add(ArgumentMatchers.any(DialogFragment.class), ArgumentMatchers.eq("GenericPopup"));
    }
  
    @Test
    public void testGetDynamicLabelInfoList() throws JSONException {
        JSONArray jsonArray = new JSONArray("[{\"dynamic_label_title\": \"sample title\",\"dynamic_label_text\": \"sample text\",\"dynamic_label_image_src\": \"img/img.png\"}]");
        ArrayList<DynamicLabelInfo> expectedList = new ArrayList<>();
        expectedList.add(new DynamicLabelInfo("sample title", "sample text", "img/img.png"));
        ArrayList<DynamicLabelInfo> actualList =  FormUtils.getDynamicLabelInfoList(jsonArray);
        Assert.assertEquals(expectedList.get(0).getDynamicLabelText(), actualList.get(0).getDynamicLabelText());
        Assert.assertEquals(expectedList.get(0).getDynamicLabelTitle(), actualList.get(0).getDynamicLabelTitle());
        Assert.assertEquals(expectedList.get(0).getDynamicLabelImageSrc(), actualList.get(0).getDynamicLabelImageSrc());
    }

    @Test
    public void testCreateOptiBPDataObject() throws JSONException {
        JSONObject inputJson = FormUtils.createOptiBPDataObject("clientId", "clientOpenSRPId");

        Assert.assertEquals(inputJson.toString(), "{\"clientId\":\"clientId\",\"clientOpenSRPId\":\"clientOpenSRPId\"}");
    }
}
