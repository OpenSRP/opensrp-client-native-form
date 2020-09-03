package com.vijay.jsonwizard.widgets;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

public class RepeatingGroupFactoryTest extends BaseTest {
    private RepeatingGroupFactory factory;
    @Mock
    private JsonFormActivity context;
    @Mock
    private JsonFormFragment formFragment;
    @Mock
    private CommonListener listener;
    @Mock
    private LinearLayout rootLayout;
    @Mock
    private Resources resources;
    @Mock
    private ImageButton doneButton;
    @Mock
    private MaterialEditText referenceEditText;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        factory = new RepeatingGroupFactory();
    }

    @Test
    public void testRepeatingGroupFactoryInstantiatesViewsCorrectly() throws Exception {
        String repeatingGroupFactoryString = "{\"key\":\"dips\",\"type\":\"repeating_group\",\"reference_edit_text\":\"step1:larval_count\",\"reference_edit_text_hint\":\"# of dips\",\"repeating_group_label\":\"dip\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"v_required\":{\"value\":true,\"err\":\"Please specify the # of dips\"},\"value\":[{\"key\":\"larvae_total\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of larvae collected\",\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"}},{\"key\":\"date_larvae_collection\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"Date of larvae collection\",\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-dynamic\":\"repeating_groups_calculation_rules.yml\"}}}},{\"key\":\"larvae_1_total\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of stage 1-2 larvae collected\",\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"}},{\"key\":\"larvae_3_total\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of stage 3-4 larvae collected\",\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_relative_max\":{\"value\":\"larvae_total\",\"err\":\"# of stage 3-4 larvae cannot be greater than the total # of larvae collected\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-dynamic\":\"repeating-groups-rules.yml\"}}}},{\"key\":\"moz_type\",\"label\":\"Larvae species found\",\"type\":\"check_box\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"options\":[{\"key\":\"An. gambiae\",\"text\":\"An. gambiae\",\"value\":false},{\"key\":\"An. funestus\",\"text\":\"An. funestus\",\"value\":false},{\"key\":\"An. minimus\",\"text\":\"An. minimus\",\"value\":false},{\"key\":\"An. dirus\",\"text\":\"An. dirus\",\"value\":false},{\"key\":\"An. maximus\",\"text\":\"An. maximus\",\"value\":false},{\"key\":\"An. other\",\"text\":\"An. other\",\"value\":false},{\"key\":\"Culex\",\"text\":\"Culex\",\"value\":false}],\"v_required\":{\"value\":true,\"err\":\"Please specify the larvae species collected\"}},{\"key\":\"An. gambiae\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. gambiae larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. gambiae\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. funestus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. funestus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. funestus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. minimus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. minimus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. minimus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. dirus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. dirus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. dirus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. maximus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. maximus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. maximus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. other\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. other larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. other\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"Culex\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of Culex larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"Culex\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"comment\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"Add a comment\"}],\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"repeating_group_relevance_rules.yml\"}}},\"constraints\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"repeating_group_constraints_rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"repeating_group_calculation_rules.yml\"}}}}";
        JSONObject repeatingGroupObject = new JSONObject(repeatingGroupFactoryString);
        Assert.assertNotNull(repeatingGroupFactoryString);
        RepeatingGroupFactory factorySpy = Mockito.spy(factory);
        Assert.assertNotNull(factorySpy);

        Mockito.doReturn(rootLayout).when(factorySpy).getRootLayout(context);
        String formString = "{\"count\":\"2\",\"encounter_type\":\"larval_dipping\",\"entity_id\":\"\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\"},\"step1\":{\"title\":\"Larval Details\",\"next\":\"step2\",\"display_back_button\":\"true\",\"fields\":[{\"key\":\"larval_count\",\"hint\":\"Optional Larval count used in repeating groups\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"value\":\"2\"}]},\"step2\":{\"title\":\"Larval Dipping Details\",\"display_back_button\":\"true\",\"fields\":[{\"key\":\"task_business_status\",\"label\":\"Status\",\"type\":\"native_radio\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"options\":[{\"key\":\"In Progress\",\"text\":\"In Progress\"},{\"key\":\"Incomplete\",\"text\":\"Incomplete\"},{\"key\":\"Not Eligible\",\"text\":\"Not Eligible\"},{\"key\":\"Complete\",\"text\":\"Complete\"}],\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. funestus\"]}]}},\"v_required\":{\"value\":true,\"err\":\"Please specify the task status\"}},{\"key\":\"occurred_date\",\"type\":\"date_picker\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"Date larval dipping was done\",\"max_date\":\"today\",\"v_required\":{\"value\":true,\"err\":\"Please specify the date larval dipping was done\"}},{\"key\":\"dips\",\"type\":\"repeating_group\",\"reference_edit_text\":\"step1:larval_count\",\"reference_edit_text_hint\":\"# of dips\",\"repeating_group_label\":\"dip\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"v_required\":{\"value\":true,\"err\":\"Please specify the # of dips\"},\"value\":[{\"key\":\"larvae_total\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of larvae collected\",\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"}},{\"key\":\"date_larvae_collection\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"Date of larvae collection\",\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-dynamic\":\"repeating_groups_calculation_rules.yml\"}}}},{\"key\":\"larvae_1_total\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of stage 1-2 larvae collected\",\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"}},{\"key\":\"larvae_3_total\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of stage 3-4 larvae collected\",\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_relative_max\":{\"value\":\"larvae_total\",\"err\":\"# of stage 3-4 larvae cannot be greater than the total # of larvae collected\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-dynamic\":\"repeating-groups-rules.yml\"}}}},{\"key\":\"moz_type\",\"label\":\"Larvae species found\",\"type\":\"check_box\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"options\":[{\"key\":\"An. gambiae\",\"text\":\"An. gambiae\",\"value\":false},{\"key\":\"An. funestus\",\"text\":\"An. funestus\",\"value\":false},{\"key\":\"An. minimus\",\"text\":\"An. minimus\",\"value\":false},{\"key\":\"An. dirus\",\"text\":\"An. dirus\",\"value\":false},{\"key\":\"An. maximus\",\"text\":\"An. maximus\",\"value\":false},{\"key\":\"An. other\",\"text\":\"An. other\",\"value\":false},{\"key\":\"Culex\",\"text\":\"Culex\",\"value\":false}],\"v_required\":{\"value\":true,\"err\":\"Please specify the larvae species collected\"}},{\"key\":\"An. gambiae\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. gambiae larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. gambiae\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. funestus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. funestus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. funestus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. minimus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. minimus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. minimus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. dirus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. dirus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. dirus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. maximus\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. maximus larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. maximus\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"An. other\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of An. other larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"An. other\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"Culex\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"# of Culex larvae collected\",\"relevance\":{\"step2:moz_type\":{\"ex-checkbox\":[{\"or\":[\"Culex\"]}]}},\"v_numeric_integer\":{\"value\":\"true\",\"err\":\"Must be a rounded number\"},\"v_required\":{\"value\":true,\"err\":\"Please specify the number of larvae of this species collected\"}},{\"key\":\"comment\",\"type\":\"edit_text\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"hint\":\"Add a comment\"}],\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"repeating_group_relevance_rules.yml\"}}}}]}}";
        Mockito.doReturn(new JSONObject(formString)).when(context).getmJSONObject();
        Mockito.doReturn(doneButton).when(rootLayout).findViewById(R.id.btn_repeating_group_done);
        Mockito.doReturn(referenceEditText).when(rootLayout).findViewById(R.id.reference_edit_text);
        Mockito.doNothing().when(factorySpy).setGlobalLayoutListener(ArgumentMatchers.eq(rootLayout), ArgumentMatchers.eq(referenceEditText), ArgumentMatchers.anyInt(), ArgumentMatchers.eq(doneButton));
        Mockito.doReturn(resources).when(context).getResources();

        JSONObject step = new JSONObject();
        JSONArray fields = new JSONArray();
        step.put(JsonFormConstants.FIELDS, fields);
        Mockito.doReturn(step).when(context).getStep(ArgumentMatchers.anyString());

        List<View> viewList = factorySpy.getViewsFromJson("step1", context, formFragment, repeatingGroupObject, listener);
        Assert.assertNotNull(viewList);
        Assert.assertEquals(1, viewList.size());
    }

    @Test
    public void testGetCustomTranslatableWidgetFields() {
        RepeatingGroupFactory factorySpy = Mockito.spy(factory);

        Set<String> editableProperties = factorySpy.getCustomTranslatableWidgetFields();
        Assert.assertEquals(0, editableProperties.size());
    }
}
