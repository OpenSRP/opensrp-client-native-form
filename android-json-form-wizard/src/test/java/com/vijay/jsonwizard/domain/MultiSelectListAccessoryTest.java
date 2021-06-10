package com.vijay.jsonwizard.domain;

import androidx.appcompat.app.AlertDialog;

import com.vijay.jsonwizard.adapter.MultiSelectListAdapter;
import com.vijay.jsonwizard.adapter.MultiSelectListSelectedAdapter;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 01-10-2020.
 */
public class MultiSelectListAccessoryTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private MultiSelectListAccessory multiSelectListAccessory;

    @Mock
    private MultiSelectListAdapter adapter;

    @Mock
    private MultiSelectListSelectedAdapter multiSelectListSelectedAdapter;

    @Mock
    private AlertDialog alertDialog;

    private List<MultiSelectItem> itemList;
    private List<MultiSelectItem> selectedItemList;

    @Before
    public void setUp() throws Exception {
        multiSelectListAccessory = new MultiSelectListAccessory(multiSelectListSelectedAdapter, adapter, alertDialog, selectedItemList, itemList);
    }

    @Test
    public void getSelectedAdapter() {
        Assert.assertEquals(multiSelectListSelectedAdapter, multiSelectListAccessory.getSelectedAdapter());
    }

    @Test
    public void setSelectedAdapter() {
        Assert.assertEquals(this.multiSelectListSelectedAdapter, multiSelectListAccessory.getSelectedAdapter());
        MultiSelectListSelectedAdapter multiSelectListSelectedAdapter = Mockito.mock(MultiSelectListSelectedAdapter.class);

        multiSelectListAccessory.setSelectedAdapter(multiSelectListSelectedAdapter);

        Assert.assertEquals(multiSelectListSelectedAdapter, multiSelectListAccessory.getSelectedAdapter());
    }

    @Test
    public void getListAdapter() {
        Assert.assertEquals(adapter, multiSelectListAccessory.getListAdapter());
    }

    @Test
    public void setListAdapter() {
        MultiSelectListAdapter adapter = Mockito.mock(MultiSelectListAdapter.class);

        multiSelectListAccessory.setListAdapter(adapter);

        Assert.assertEquals(adapter, ReflectionHelpers.getField(multiSelectListAccessory, "listAdapter"));

    }

    @Test
    public void getAlertDialog() {
        Assert.assertEquals(alertDialog, multiSelectListAccessory.getAlertDialog());
    }

    @Test
    public void setAlertDialog() {
        AlertDialog alertDialog = Mockito.mock(AlertDialog.class);

        multiSelectListAccessory.setAlertDialog(alertDialog);
        Assert.assertEquals(alertDialog, ReflectionHelpers.getField(multiSelectListAccessory, "alertDialog"));
    }

    @Test
    public void getItemList() {
        Assert.assertEquals(itemList, multiSelectListAccessory.getItemList());
    }

    @Test
    public void getFormAttributes() {
        JSONObject formAttributes = new JSONObject();
        ReflectionHelpers.setField(multiSelectListAccessory, "formAttributes", formAttributes);

        Assert.assertEquals(formAttributes, multiSelectListAccessory.getFormAttributes());
    }

    @Test
    public void setFormAttributes() {
        JSONObject formAttributes = new JSONObject();
        multiSelectListAccessory.setFormAttributes(formAttributes);

        Assert.assertEquals(formAttributes, ReflectionHelpers.getField(multiSelectListAccessory, "formAttributes"));
    }

    @Test
    public void setItemList() {
        List<MultiSelectItem> itemList = new ArrayList<>();

        multiSelectListAccessory.setItemList(itemList);

        Assert.assertTrue(itemList == ReflectionHelpers.getField(multiSelectListAccessory, "itemList"));
    }

    @Test
    public void getSelectedItemList() {
        Assert.assertEquals(selectedItemList, multiSelectListAccessory.getSelectedItemList());
    }
}