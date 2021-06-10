package com.vijay.jsonwizard.adapter;

import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.model.DynamicLabelInfo;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class DynamicLabelAdapterTest extends BaseTest {

    private DynamicLabelAdapter dynamicLabelAdapter;

    @Before
    public void setUp() {
        dynamicLabelAdapter = spy(new DynamicLabelAdapter(RuntimeEnvironment.application, new ArrayList<DynamicLabelInfo>()));
    }

    @Test
    public void testOnCreateViewHolder() {
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        RecyclerView.ViewHolder itemView = dynamicLabelAdapter.onCreateViewHolder(linearLayout, 0);
        assertNotNull(itemView);
        assertTrue(itemView instanceof DynamicLabelAdapter.RecyclerViewHolder);
    }

    @Test
    public void testOnBindViewHolderShouldShowNonBlankViews() {
        DynamicLabelInfo dynamicLabelInfo = new DynamicLabelInfo("sampleTitle", "sampleText", "avatar_woman.png");
        ArrayList<DynamicLabelInfo> dynamicLabelInfos = new ArrayList<>();
        dynamicLabelInfos.add(dynamicLabelInfo);
        ReflectionHelpers.setField(dynamicLabelAdapter, "dynamicLabelInfoList", dynamicLabelInfos);
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        RecyclerView.ViewHolder viewHolder = dynamicLabelAdapter.onCreateViewHolder(linearLayout, 0);

        dynamicLabelAdapter.onBindViewHolder(viewHolder, 0);

        assertEquals(View.VISIBLE, viewHolder.itemView.findViewById(R.id.descriptionText).getVisibility());
        assertEquals(View.VISIBLE, viewHolder.itemView.findViewById(R.id.labelTitle).getVisibility());
        assertEquals(View.VISIBLE, viewHolder.itemView.findViewById(R.id.imageViewLabel).getVisibility());
    }

    @Test
    public void testOnBindViewHolderShouldHideBlankViews() {
        DynamicLabelInfo dynamicLabelInfo = new DynamicLabelInfo("", "", "");
        ArrayList<DynamicLabelInfo> dynamicLabelInfos = new ArrayList<>();
        dynamicLabelInfos.add(dynamicLabelInfo);
        ReflectionHelpers.setField(dynamicLabelAdapter, "dynamicLabelInfoList", dynamicLabelInfos);
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        RecyclerView.ViewHolder viewHolder = dynamicLabelAdapter.onCreateViewHolder(linearLayout, 0);

        dynamicLabelAdapter.onBindViewHolder(viewHolder, 0);

        assertEquals(View.GONE, viewHolder.itemView.findViewById(R.id.descriptionText).getVisibility());
        assertEquals(View.GONE, viewHolder.itemView.findViewById(R.id.labelTitle).getVisibility());
        assertEquals(View.GONE, viewHolder.itemView.findViewById(R.id.imageViewLabel).getVisibility());
    }
}