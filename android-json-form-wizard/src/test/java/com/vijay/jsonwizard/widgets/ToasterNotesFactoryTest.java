package com.vijay.jsonwizard.widgets;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.customviews.ToasterLinearLayout;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class ToasterNotesFactoryTest extends BaseTest {

    private ToasterNotesFactory factory;
    @Mock
    private JsonFormActivity context;

    @Mock
    private JsonFormFragment formFragment;

    @Mock
    private CommonListener listener;

    @Mock
    private ToasterLinearLayout rootLayout;

    @Mock
    private Resources resources;

    @Mock
    private RelativeLayout toasterRelativeLayout;

    @Mock
    private ImageView toasterNoteImageView;

    @Mock
    private ImageView toasterNoteInfo;

    @Mock
    private TextView toasterNotesTextView;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        factory = new ToasterNotesFactory();
    }

    @Test
    public void testInfoToasterInstantiatesViewsCorrectly() throws Exception {
        Assert.assertNotNull(factory);
        ToasterNotesFactory factorySpy = Mockito.spy(factory);
        Assert.assertNotNull(factorySpy);

        Mockito.doReturn(rootLayout).when(factorySpy).getToasterLinearLayout(context);
        Assert.assertNotNull(rootLayout);

        Mockito.doReturn(toasterRelativeLayout).when(rootLayout).findViewById(R.id.toaster_notes_layout);
        Assert.assertNotNull(toasterRelativeLayout);

        Mockito.doReturn(resources).when(context).getResources();
        Assert.assertNotNull(resources);

        Mockito.doReturn(toasterNoteImageView).when(rootLayout).findViewById(R.id.toaster_notes_image);
        Assert.assertNotNull(toasterNoteImageView);

        Mockito.doReturn(toasterNoteInfo).when(rootLayout).findViewById(R.id.toaster_notes_info);
        Assert.assertNotNull(toasterNoteInfo);

        Mockito.doReturn(toasterNotesTextView).when(rootLayout).findViewById(R.id.toaster_notes_text);
        Assert.assertNotNull(toasterNotesTextView);

        String labelString = "{\"key\":\"ultrasound_info_toaster\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"toaster_notes\",\"text\":\"Test toaster notes\",\"toaster_type\":\"info\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_relevance_rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_calculation_rules.yml\"}}},\"constraints\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_constraints_rules.yml\"}}}}";
        List<View> viewList = factorySpy.getViewsFromJson("RandomStepName", context, formFragment, new JSONObject(labelString), listener);
        Assert.assertNotNull(viewList);
        Assert.assertEquals(1, viewList.size());
    }

    @Test
    public void testWarningToasterInstantiatesViewsCorrectly() throws Exception {
        Assert.assertNotNull(factory);
        ToasterNotesFactory factorySpy = Mockito.spy(factory);
        Assert.assertNotNull(factorySpy);

        Mockito.doReturn(rootLayout).when(factorySpy).getToasterLinearLayout(context);
        Assert.assertNotNull(rootLayout);

        Mockito.doReturn(toasterRelativeLayout).when(rootLayout).findViewById(R.id.toaster_notes_layout);
        Assert.assertNotNull(toasterRelativeLayout);

        Mockito.doReturn(resources).when(context).getResources();
        Assert.assertNotNull(resources);

        Mockito.doReturn(toasterNoteImageView).when(rootLayout).findViewById(R.id.toaster_notes_image);
        Assert.assertNotNull(toasterNoteImageView);

        Mockito.doReturn(toasterNoteInfo).when(rootLayout).findViewById(R.id.toaster_notes_info);
        Assert.assertNotNull(toasterNoteInfo);

        Mockito.doReturn(toasterNotesTextView).when(rootLayout).findViewById(R.id.toaster_notes_text);
        Assert.assertNotNull(toasterNotesTextView);

        String labelString = "{\"key\":\"ultrasound_info_toaster\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"toaster_notes\",\"text\":\"Test toaster notes\",\"toaster_type\":\"warning\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_relevance_rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_calculation_rules.yml\"}}},\"constraints\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_constraints_rules.yml\"}}}}";
        List<View> viewList = factorySpy.getViewsFromJson("RandomStepName", context, formFragment, new JSONObject(labelString), listener);
        Assert.assertNotNull(viewList);
        Assert.assertEquals(1, viewList.size());
    }

    @Test
    public void testProblemToasterInstantiatesViewsCorrectly() throws Exception {
        Assert.assertNotNull(factory);
        ToasterNotesFactory factorySpy = Mockito.spy(factory);
        Assert.assertNotNull(factorySpy);

        Mockito.doReturn(rootLayout).when(factorySpy).getToasterLinearLayout(context);
        Assert.assertNotNull(rootLayout);

        Mockito.doReturn(toasterRelativeLayout).when(rootLayout).findViewById(R.id.toaster_notes_layout);
        Assert.assertNotNull(toasterRelativeLayout);

        Mockito.doReturn(resources).when(context).getResources();
        Assert.assertNotNull(resources);

        Mockito.doReturn(toasterNoteImageView).when(rootLayout).findViewById(R.id.toaster_notes_image);
        Assert.assertNotNull(toasterNoteImageView);

        Mockito.doReturn(toasterNoteInfo).when(rootLayout).findViewById(R.id.toaster_notes_info);
        Assert.assertNotNull(toasterNoteInfo);

        Mockito.doReturn(toasterNotesTextView).when(rootLayout).findViewById(R.id.toaster_notes_text);
        Assert.assertNotNull(toasterNotesTextView);

        String labelString = "{\"key\":\"ultrasound_info_toaster\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"toaster_notes\",\"text\":\"Test toaster notes\",\"toaster_type\":\"problem\",\"toaster_info_text\":\"Toaster text\",\"toaster_info_title\":\"Toaster titles\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_relevance_rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_calculation_rules.yml\"}}},\"constraints\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_constraints_rules.yml\"}}}}";
        List<View> viewList = factorySpy.getViewsFromJson("RandomStepName", context, formFragment, new JSONObject(labelString), listener);
        Assert.assertNotNull(viewList);
        Assert.assertEquals(1, viewList.size());
    }

    @Test
    public void testPositiveToasterInstantiatesViewsCorrectly() throws Exception {
        Assert.assertNotNull(factory);
        ToasterNotesFactory factorySpy = Mockito.spy(factory);
        Assert.assertNotNull(factorySpy);

        Mockito.doReturn(rootLayout).when(factorySpy).getToasterLinearLayout(context);
        Assert.assertNotNull(rootLayout);

        Mockito.doReturn(toasterRelativeLayout).when(rootLayout).findViewById(R.id.toaster_notes_layout);
        Assert.assertNotNull(toasterRelativeLayout);

        Mockito.doReturn(resources).when(context).getResources();
        Assert.assertNotNull(resources);

        Mockito.doReturn(toasterNoteImageView).when(rootLayout).findViewById(R.id.toaster_notes_image);
        Assert.assertNotNull(toasterNoteImageView);

        Mockito.doReturn(toasterNoteInfo).when(rootLayout).findViewById(R.id.toaster_notes_info);
        Assert.assertNotNull(toasterNoteInfo);

        Mockito.doReturn(toasterNotesTextView).when(rootLayout).findViewById(R.id.toaster_notes_text);
        Assert.assertNotNull(toasterNotesTextView);

        String labelString = "{\"key\":\"ultrasound_info_toaster\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"toaster_notes\",\"text\":\"Test toaster notes\",\"toaster_type\":\"positive\",\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_relevance_rules.yml\"}}},\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_calculation_rules.yml\"}}},\"constraints\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"ultrasound_sub_form_constraints_rules.yml\"}}}}";
        List<View> viewList = factorySpy.getViewsFromJson("RandomStepName", context, formFragment, new JSONObject(labelString), listener);
        Assert.assertNotNull(viewList);
        Assert.assertEquals(1, viewList.size());
    }
}
