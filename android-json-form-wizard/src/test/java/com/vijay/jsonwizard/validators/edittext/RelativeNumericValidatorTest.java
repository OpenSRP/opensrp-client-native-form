package com.vijay.jsonwizard.validators.edittext;

import com.vijay.jsonwizard.fragments.JsonFormFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * Created by Vincent Karuri on 16/06/2020
 */
public class RelativeNumericValidatorTest {

    private final String form = "{\n" +
            "  \"count\": \"1\",\n" +
            "  \"encounter_type\": \"patient_registration\",\n" +
            "  \"entity_id\": \"\",\n" +
            "  \"metadata\": {\n" +
            "    \"start\": {\n" +
            "      \"openmrs_entity_parent\": \"\",\n" +
            "      \"openmrs_entity\": \"concept\",\n" +
            "      \"openmrs_data_type\": \"start\",\n" +
            "      \"openmrs_entity_id\": \"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
            "    },\n" +
            "    \"end\": {\n" +
            "      \"openmrs_entity_parent\": \"\",\n" +
            "      \"openmrs_entity\": \"concept\",\n" +
            "      \"openmrs_data_type\": \"end\",\n" +
            "      \"openmrs_entity_id\": \"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
            "    },\n" +
            "    \"today\": {\n" +
            "      \"openmrs_entity_parent\": \"\",\n" +
            "      \"openmrs_entity\": \"encounter\",\n" +
            "      \"openmrs_entity_id\": \"encounter_date\"\n" +
            "    },\n" +
            "    \"deviceid\": {\n" +
            "      \"openmrs_entity_parent\": \"\",\n" +
            "      \"openmrs_entity\": \"concept\",\n" +
            "      \"openmrs_data_type\": \"deviceid\",\n" +
            "      \"openmrs_entity_id\": \"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
            "    },\n" +
            "    \"subscriberid\": {\n" +
            "      \"openmrs_entity_parent\": \"\",\n" +
            "      \"openmrs_entity\": \"concept\",\n" +
            "      \"openmrs_data_type\": \"subscriberid\",\n" +
            "      \"openmrs_entity_id\": \"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
            "    },\n" +
            "    \"simserial\": {\n" +
            "      \"openmrs_entity_parent\": \"\",\n" +
            "      \"openmrs_entity\": \"concept\",\n" +
            "      \"openmrs_data_type\": \"simserial\",\n" +
            "      \"openmrs_entity_id\": \"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
            "    },\n" +
            "    \"phonenumber\": {\n" +
            "      \"openmrs_entity_parent\": \"\",\n" +
            "      \"openmrs_entity\": \"concept\",\n" +
            "      \"openmrs_data_type\": \"phonenumber\",\n" +
            "      \"openmrs_entity_id\": \"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"\n" +
            "    },\n" +
            "    \"encounter_location\": \"\"\n" +
            "  },\n" +
            "  \"step1\": {\n" +
            "    \"title\": \"New client record\",\n" +
            "    \"display_back_button\": \"true\",\n" +
            "    \"bottom_navigation\": \"true\",\n" +
            "    \"bottom_navigation_orientation\": \"vertical\",\n" +
            "    \"next_type\": \"submit\",\n" +
            "    \"submit_label\": \"SUBMIT\",\n" +
            "    \"fields\": [\n" +
            "      {\n" +
            "        \"key\": \"key\",\n" +
            "        \"type\": \"edit_text\",\n" +
            "        \"text_color\": \"#000000\",\n" +
            "        \"value\": 2\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    @Mock
    private JsonFormFragment formFragment;

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.initMocks(this);
        JSONObject currentJsonState = new JSONObject(form);
        doReturn(currentJsonState.toString()).when(formFragment).getCurrentJsonState();
    }

    @Test
    public void testIsMaxValidReturnsCorrectStatus() {
        RelativeNumericValidator relativeNumericValidator
                = new RelativeNumericValidator("", formFragment, "key", 0, "step1", true);
        assertTrue(relativeNumericValidator.isValid("1", false));
        assertFalse(relativeNumericValidator.isValid("3", false));
    }

    @Test
    public void testIsMinValidReturnsCorrectStatus() {
        RelativeNumericValidator relativeNumericValidator
                = new RelativeNumericValidator("", formFragment, "key", 0, "step1", false);
        assertFalse(relativeNumericValidator.isValid("1", false));
        assertTrue(relativeNumericValidator.isValid("3", false));
    }
}
