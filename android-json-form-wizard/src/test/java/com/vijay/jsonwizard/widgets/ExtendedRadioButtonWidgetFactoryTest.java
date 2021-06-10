package com.vijay.jsonwizard.widgets;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class ExtendedRadioButtonWidgetFactoryTest extends FactoryTest {

    private ExtendedRadioButtonWidgetFactory extendedRadioButtonWidgetFactory;

    @Mock
    private JsonFormFragment jsonFormFragment;

    @Mock
    private CommonListener commonListener;

    @Before
    public void setUp() {
        super.setUp();
        extendedRadioButtonWidgetFactory = spy(new ExtendedRadioButtonWidgetFactory());
    }

    @Test
    public void testFactoryShouldInitializeCorrectly() throws JSONException {
        String strJson = "{\"key\":\"blood_type_test_status\",\"openmrs_entity_parent\":\"300AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163725AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"label\":\"Blood type test\",\"label_text_style\":\"bold\",\"text_color\":\"#000000\",\"type\":\"extended_radio_button\",\"options\":[{\"key\":\"done_today\",\"text\":\"Done today\",\"type\":\"done_today\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165383AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"done_earlier\",\"text\":\"Done earlier\",\"type\":\"done_earlier\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165385AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"ordered\",\"text\":\"Ordered\",\"type\":\"ordered\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},{\"key\":\"not_done\",\"text\":\"Not done\",\"type\":\"not_done\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1118AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}],\"v_required\":{\"value\":true,\"err\":\"Blood type status is required\"}}";
        JSONObject fieldJsonObject = new JSONObject(strJson);
        String stepName = JsonFormConstants.STEP1;
        List<View> views = extendedRadioButtonWidgetFactory.attachJson(stepName, jsonFormActivity, jsonFormFragment, fieldJsonObject, commonListener, true);

        assertNotNull(views);
        assertTrue(views.get(0) instanceof LinearLayout);

        LinearLayout linearLayout = (LinearLayout) views.get(0);
        assertTrue(linearLayout.getChildAt(0) instanceof ConstraintLayout);
        assertTrue(linearLayout.getChildAt(1) instanceof RadioGroup);
        assertEquals(4, ((RadioGroup) linearLayout.getChildAt(1)).getChildCount());
    }
}