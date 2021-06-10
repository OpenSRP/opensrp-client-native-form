package com.vijay.jsonwizard.widgets;

import android.app.FragmentManager;
import android.content.res.Resources;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.customviews.DatePickerDialog;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.shadow.ShadowDialogFragment;
import com.vijay.jsonwizard.utils.AppExecutors;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

public class DatePickerFactoryTest extends BaseTest {
    private DatePickerFactory factory;
    @Mock
    private JsonFormActivity formActivity;

    @Mock
    private JsonFormFragment formFragment;


    private Resources resources = RuntimeEnvironment.application.getResources();

    @Mock
    private CommonListener listener;

    @Mock
    private MaterialEditText editText;

    @Mock
    private RelativeLayout rootLayout;

    @Mock
    private TextView duration;

    @Mock
    private FragmentManager fragmentManager;

    @Captor
    private ArgumentCaptor<View> viewArgumentCaptor;

    @Captor
    private ArgumentCaptor<DatePickerDialog> datePickerDialogArgumentCaptor;

    @Before
    public void setUp() {
        AppExecutors appExecutors = new AppExecutors();
        when(formActivity.getAppExecutors()).thenReturn(appExecutors);
        factory = spy(new DatePickerFactory());
        Mockito.doReturn(Locale.ENGLISH).when(factory).getCurrentLocale(formActivity);
        Mockito.doReturn(Locale.ENGLISH).when(factory).getSetLanguage(formActivity);
        Mockito.doReturn(resources).when(formActivity).getResources();
        Mockito.doReturn("12 Age").when(factory).getDurationText(formActivity, "12-05-2010", Locale.ENGLISH);
    }

    @Test
    public void testDatePickerFactoryInstantiatesViewsCorrectly() throws Exception {
        Mockito.doReturn(resources).when(formActivity).getResources();

        formActivity.setTheme(R.style.NativeFormsAppTheme);
        Mockito.doReturn(rootLayout).when(factory).getRelativeLayout(formActivity);
        Mockito.doReturn(editText).when(rootLayout).findViewById(R.id.edit_text);
        Mockito.doReturn(duration).when(rootLayout).findViewById(R.id.duration);
        Mockito.doReturn(duration).when(rootLayout).findViewById(R.id.duration);
        String datePicker = "{\"key\":\"First_Health_Facility_Contact\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163260AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"text\",\"type\":\"date_picker\",\"hint\":\"Date first seen *\",\"expanded\":false,\"min_date\":\"today-5y\",\"max_date\":\"today\",\"v_required\":{\"value\":\"true\",\"err\":\"Enter the date that the child was first seen at a health facility for immunization services\"},\"constraints\":{\"type\":\"date\",\"ex\":\"greaterThanEqualTo(., step1:Date_Birth)\",\"err\":\"Date first seen can't occur before date of birth\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"sample-relevance-rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"sample-calculation-rules.yml\"}}},\"value\":\"12-05-2020\",\"read_only\":true,\"label_info_text\":\"Just testing\",\"label_info_title\":\"Just testing\",\"duration\":{\"label\":\"AGE\"}}";
        List<View> viewList = factory.getViewsFromJson("RandomStepName", formActivity, formFragment, new JSONObject(datePicker), listener);
        shadowOf(getMainLooper()).idle();
        Assert.assertNotNull(viewList);
        assertEquals(1, viewList.size());
    }

    @Test
    public void testGetCustomTranslatableWidgetFields() {
        Assert.assertNotNull(factory);
        DatePickerFactory factorySpy = spy(factory);
        Assert.assertNotNull(factorySpy);

        Set<String> editableProperties = factorySpy.getCustomTranslatableWidgetFields();
        assertEquals(3, editableProperties.size());
        assertEquals("duration.label", editableProperties.iterator().next());
    }


    @Config(shadows = {ShadowDialogFragment.class})
    @Test
    public void testShowDatePickerDialog() throws Exception {
        when(formActivity.getFragmentManager()).thenReturn(fragmentManager);
        AppCompatActivity appCompatActivity = Robolectric.buildActivity(AppCompatActivity.class).create().get();
        RelativeLayout view = (RelativeLayout) appCompatActivity.getLayoutInflater().inflate(factory.getLayout(), null);
        Mockito.doReturn(view).when(factory).getRelativeLayout(formActivity);
        String datePicker = "{\"key\":\"First_Health_Facility_Contact\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"163260AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"openmrs_data_type\":\"text\",\"type\":\"date_picker\",\"hint\":\"Date first seen *\",\"expanded\":false,\"min_date\":\"today-5y\",\"max_date\":\"today\",\"v_required\":{\"value\":\"true\",\"err\":\"Enter the date that the child was first seen at a health facility for immunization services\"},\"constraints\":{\"type\":\"date\",\"ex\":\"greaterThanEqualTo(., step1:Date_Birth)\",\"err\":\"Date first seen can't occur before date of birth\"},\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"sample-relevance-rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"sample-calculation-rules.yml\"}}},\"value\":\"12-05-2020\",\"read_only\":true,\"label_info_text\":\"Just testing\",\"label_info_title\":\"Just testing\",\"duration\":{\"label\":\"AGE\"}}";
        List<View> viewList = factory.getViewsFromJson("RandomStepName", formActivity, formFragment, new JSONObject(datePicker), listener);
        shadowOf(getMainLooper()).idle();
        assertEquals(1, viewList.size());
        verify(formActivity).addFormDataView(viewArgumentCaptor.capture());
        viewArgumentCaptor.getValue().performClick();
        verify(factory).showDatePickerDialog(eq(formActivity), datePickerDialogArgumentCaptor.capture(), any(MaterialEditText.class));
        verify(fragmentManager).beginTransaction();
        verify(fragmentManager).executePendingTransactions();
        DatePickerDialog datePickerDialog = datePickerDialogArgumentCaptor.getValue();
        assertEquals("12-05-2020", new SimpleDateFormat("dd-MM-yyyy").format(Whitebox.getInternalState(datePickerDialog, "date")));

    }

}
