package com.vijay.jsonwizard.widgets;

import android.content.res.Resources;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.views.CustomTextView;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;

import java.util.List;

public class LabelFactoryTest extends BaseTest {
    private LabelFactory factory;
    @Mock
    private JsonFormActivity context;

    @Mock
    private JsonFormFragment formFragment;

    @Mock
    private Resources resources;

    @Mock
    private CommonListener listener;

    @Mock
    private ConstraintLayout constraintLayout;

    @Mock
    private CustomTextView labelText;

    @Mock
    private CustomTextView numberText;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        factory = new LabelFactory();
    }

    @Test
    public void testLabelWidgetFactoryInstantiatesViewsCorrectly() throws Exception {
        Assert.assertNotNull(factory);
        LabelFactory factorySpy = Mockito.spy(factory);
        Assert.assertNotNull(factorySpy);

        FormUtils formUtils = new FormUtils();
        FormUtils formUtilsSpy = Mockito.spy(formUtils);
        Assert.assertNotNull(formUtilsSpy);

        Whitebox.setInternalState(factorySpy, "formUtils", formUtilsSpy);

        Mockito.doReturn(constraintLayout).when(formUtilsSpy).getRootConstraintLayout(context);
        Assert.assertNotNull(constraintLayout);

        Mockito.doReturn(numberText).when(constraintLayout).findViewById(R.id.label_text_number);
        Assert.assertNotNull(numberText);

        Mockito.doReturn(resources).when(context).getResources();
        Assert.assertNotNull(resources);

        Mockito.doReturn(labelText).when(constraintLayout).findViewById(R.id.label_text);
        Assert.assertNotNull(labelText);

        Mockito.doReturn(0).when(factorySpy).getValueFromSpOrDpOrPx(ArgumentMatchers.eq(context), ArgumentMatchers.anyString());

        String labelString = "{\"key\":\"enabled_label\",\"type\":\"label\",\"text\":\"This is enabled\",\"hint_on_text\":false,\"text_color\":\"#FFC100\",\"text_size\":\"17sp\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"label_info_text\":\"Checking out the display oof the info icon needed to display this infomation\",\"label_info_title\":\"Highest Level of School\",\"label_number\":\"1\",\"has_bg\":true,\"v_required\":{\"value\":true,\"err\":\"The required error\"}}";
        List<View> viewList = factorySpy.getViewsFromJson("RandomStepName", context, formFragment, new JSONObject(labelString), listener);
        Assert.assertNotNull(viewList);
        Assert.assertTrue(viewList.size() > 0);
    }
}
