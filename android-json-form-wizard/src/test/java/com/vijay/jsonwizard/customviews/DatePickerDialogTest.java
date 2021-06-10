package com.vijay.jsonwizard.customviews;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.vijay.jsonwizard.BaseTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;

import java.util.Calendar;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by samuelgithengi on 9/8/20.
 */
public class DatePickerDialogTest extends BaseTest {

    @Mock
    private DialogInterface.OnShowListener onShowListener;

    @Mock
    private DialogInterface dialog;

    @Mock
    private Activity activity;

    @Mock
    private InputMethodManager inputManager;

    private DatePickerDialog datePickerDialog;

    @Before
    public void setUp() {
        datePickerDialog = new DatePickerDialog();
        datePickerDialog.setContext(RuntimeEnvironment.application);
    }

    @Test
    public void testOnCreateShouldNotThrowErrorAndSetsTheme() {
        datePickerDialog.onCreate(null);
        assertEquals(android.R.style.Theme_Holo_Light_Dialog, datePickerDialog.getTheme());
    }

    @Test(expected = IllegalStateException.class)
    public void testOnCreateShoulThrowErrorIfContextIsNotSet() {
        datePickerDialog = new DatePickerDialog();
        datePickerDialog.onCreate(null);
    }

    public void testSetOnShowListenerShouldSetListener() {
        datePickerDialog.setOnShowListener(onShowListener);
        assertEquals(onShowListener, Whitebox.getInternalState(datePickerDialog, "onShowListener"));
    }

    @Test
    public void testOnCreateViewShouldShowDatePicker() {
        datePickerDialog.onCreateView(LayoutInflater.from(RuntimeEnvironment.application), null, null);
        assertNotNull(datePickerDialog.getDatePicker());
        assertEquals(View.VISIBLE, datePickerDialog.getDatePicker().getVisibility());

    }

    @Test
    public void testOnCreateViewShouldSetMinAndMaxDates() {
        long max = System.currentTimeMillis();
        datePickerDialog.setMaxDate(max);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        datePickerDialog.setMinDate(cal.getTimeInMillis());
        datePickerDialog.onCreateView(LayoutInflater.from(RuntimeEnvironment.application), null, null);
        assertNotNull(datePickerDialog.getDatePicker());
        assertEquals(View.VISIBLE, datePickerDialog.getDatePicker().getVisibility());
        assertEquals(cal.getTimeInMillis(), datePickerDialog.getDatePicker().getMinDate());
        assertEquals(max, datePickerDialog.getDatePicker().getMaxDate());
    }

    @Test
    public void testOnCreateViewShouldSetTimeBasedOnInputs() {
        long max = System.currentTimeMillis();
        datePickerDialog.setMaxDate(max);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MONTH, 1);
        Calendar calMin = Calendar.getInstance();
        cal.set(Calendar.YEAR, -1);
        datePickerDialog.setMinDate(calMin.getTimeInMillis());
        datePickerDialog.setDate(cal.getTime());
        datePickerDialog.onCreateView(LayoutInflater.from(RuntimeEnvironment.application), null, null);
        assertNotNull(datePickerDialog.getDatePicker());
        assertEquals(View.VISIBLE, datePickerDialog.getDatePicker().getVisibility());
        assertEquals(calMin.getTimeInMillis(), datePickerDialog.getDatePicker().getMinDate());
        assertEquals(1, datePickerDialog.getDatePicker().getDayOfMonth());
        assertEquals(1, datePickerDialog.getDatePicker().getMonth());
        assertEquals(cal.get(Calendar.YEAR), datePickerDialog.getDatePicker().getYear());
    }

    @Test
    public void testOnShowListenerShouldHideSoftInputFromWindow() {
        datePickerDialog.setContext(activity);
        when(activity.getSystemService(Context.INPUT_METHOD_SERVICE)).thenReturn(inputManager);
        when(activity.getCurrentFocus()).thenReturn(mock(View.class));
        datePickerDialog.onCreateView(LayoutInflater.from(RuntimeEnvironment.application), null, null);
        onShowListener = spy(Whitebox.getInternalState(datePickerDialog, "onShowListener"));
        onShowListener.onShow(dialog);
        verify(activity).getSystemService(Context.INPUT_METHOD_SERVICE);
        verify(activity).getCurrentFocus();
        verify(inputManager).hideSoftInputFromWindow(nullable(IBinder.class), eq(HIDE_NOT_ALWAYS));
    }
}
