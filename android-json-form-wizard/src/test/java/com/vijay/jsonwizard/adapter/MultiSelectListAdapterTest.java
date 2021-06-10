package com.vijay.jsonwizard.adapter;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.domain.MultiSelectItem;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MultiSelectListAdapterTest extends BaseTest {

    private MultiSelectListAdapter multiSelectListAdapter;

    @Before
    public void setUp() {
        multiSelectListAdapter = spy(new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()));
    }

    @Test
    public void testOnCreateViewHolder() {
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        RecyclerView.ViewHolder itemView = multiSelectListAdapter.onCreateViewHolder(linearLayout, 0);
        assertNotNull(itemView);
        assertTrue(itemView instanceof MultiSelectListAdapter.SectionViewHolder);


        itemView = multiSelectListAdapter.onCreateViewHolder(linearLayout, 1);
        assertNotNull(itemView);
        assertTrue(itemView instanceof MultiSelectListAdapter.SectionTitleViewHolder);
    }

    @Test
    public void testOnBindViewHolderShouldPopulateViews() {
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        MultiSelectItem viewSelectItem = new MultiSelectItem();
        viewSelectItem.setKey("key1");
        viewSelectItem.setText("text1");
        viewSelectItem.setValue("");

        MultiSelectItem headerSelectItem = new MultiSelectItem();
        headerSelectItem.setKey("key2");
        headerSelectItem.setText("text2");

        multiSelectListAdapter.getData().add(viewSelectItem);
        multiSelectListAdapter.getData().add(headerSelectItem);

        MultiSelectListAdapter.SectionViewHolder viewItemView = (MultiSelectListAdapter.SectionViewHolder) spy(multiSelectListAdapter.onCreateViewHolder(linearLayout, 0));

        multiSelectListAdapter.onBindViewHolder(viewItemView, 0);

        assertEquals(viewSelectItem.getText(), viewItemView.itemTextView().getText());

        MultiSelectListAdapter.SectionTitleViewHolder viewHeaderItemView = (MultiSelectListAdapter.SectionTitleViewHolder) spy(multiSelectListAdapter.onCreateViewHolder(linearLayout, 1));

        multiSelectListAdapter.onBindViewHolder(viewHeaderItemView, 1);

        assertEquals(headerSelectItem.getText(), viewHeaderItemView.itemHeaderView().getText());
    }

    @Test
    public void testGetFilterShouldFilterList() {
        MultiSelectItem viewSelectItem = new MultiSelectItem();
        viewSelectItem.setKey("key1");
        viewSelectItem.setText("text1");
        viewSelectItem.setValue("");

        MultiSelectItem viewSelectItem2 = new MultiSelectItem();
        viewSelectItem2.setKey("key2");
        viewSelectItem2.setText("chosen");
        viewSelectItem2.setValue("");

        MultiSelectItem headerSelectItem = new MultiSelectItem();
        headerSelectItem.setKey("A");
        headerSelectItem.setText("text2");

        multiSelectListAdapter.getData().add(viewSelectItem);
        multiSelectListAdapter.getData().add(headerSelectItem);
        multiSelectListAdapter.getData().add(viewSelectItem2);

        assertEquals(3, multiSelectListAdapter.getData().size());
        multiSelectListAdapter.getFilter().filter("cho");
        assertEquals(1, multiSelectListAdapter.getData().size());
        verify(multiSelectListAdapter, times(1))
                .notifyDataSetChanged();
    }
}