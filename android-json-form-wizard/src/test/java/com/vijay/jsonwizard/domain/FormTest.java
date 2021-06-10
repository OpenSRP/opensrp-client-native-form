package com.vijay.jsonwizard.domain;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Vincent Karuri on 14/07/2020
 */
public class FormTest {

    @Test
    public void testFormOperationsShouldCorrectlyManipulateFormState() {
        Form form = new Form();

        assertEquals(0, form.getActionBarBackground());
        assertEquals(0, form.getBackIcon());
        assertNull(form.getDisabledFields());
        assertNull(form.getHiddenFields());
        assertEquals(0, form.getHomeAsUpIndicator());
        assertNull(form.getName());
        assertEquals(0, form.getNavigationBackground());
        assertNull(form.getNextLabel());
        assertNull(form.getPreviousLabel());
        assertNull(form.getSaveLabel());
        assertFalse(form.isHideNextButton());
        assertFalse(form.isHidePreviousButton());
        assertFalse(form.isHideSaveLabel());
        assertTrue(form.isWizard());
        assertNull(Form.getDatePickerDisplayFormat());

        form.setActionBarBackground(1);
        assertEquals(1, form.getActionBarBackground());

        form.setBackIcon(2);
        assertEquals(2, form.getBackIcon());

        Set<String> disabledFields = new HashSet<>();
        form.setDisabledFields(disabledFields);
        assertEquals(disabledFields, form.getDisabledFields());

        Set<String> hiddenFields = new HashSet<>();
        form.setHiddenFields(hiddenFields);
        assertEquals(hiddenFields, form.getHiddenFields());

        form.setHomeAsUpIndicator(3);
        assertEquals(3, form.getHomeAsUpIndicator());

        form.setName("form_name");
        assertEquals("form_name", form.getName());

        form.setNavigationBackground(4);
        assertEquals(4, form.getNavigationBackground());

        form.setNextLabel("next_label");
        assertEquals("next_label", form.getNextLabel());

        form.setPreviousLabel("prev_label");
        assertEquals("prev_label", form.getPreviousLabel());

        form.setSaveLabel("save_label");
        assertEquals("save_label", form.getSaveLabel());

        form.setHideNextButton(true);
        assertTrue(form.isHideNextButton());

        form.setHidePreviousButton(true);
        assertTrue(form.isHidePreviousButton());

        form.setHideSaveLabel(true);
        assertTrue(form.isHideSaveLabel());

        form.setWizard(false);
        assertFalse(form.isWizard());

        form.setDatePickerDisplayFormat("display_format");
        assertEquals("display_format", Form.getDatePickerDisplayFormat());
    }
}
