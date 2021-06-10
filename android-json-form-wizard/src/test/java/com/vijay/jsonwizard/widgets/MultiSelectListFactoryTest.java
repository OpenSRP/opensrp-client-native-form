package com.vijay.jsonwizard.widgets;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.adapter.MultiSelectListAdapter;
import com.vijay.jsonwizard.adapter.MultiSelectListSelectedAdapter;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.domain.MultiSelectListAccessory;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.repository.TestMultiSelectListRepository;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@LooperMode(PAUSED)
@PrepareForTest({ViewUtil.class})
public class MultiSelectListFactoryTest extends FactoryTest {

    private MultiSelectListFactory multiSelectListFactory;

    @Mock
    private JsonFormFragment jsonFormFragment;

    private String strJsonObject = "{\"key\":\"user_dummy\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"buttonText\":\"+ Add disease code\",\"sort\":true,\"groupings\":\"[A,B]\",\"dialogTitle\":\"Add disease code\",\"value\":[{\"key\":\"abortion\",\"text\":\"Abortion\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}}],\"searchHint\":\"Type Disease Name\",\"options\":[{\"key\":\"Bbcess\",\"text\":\"BAbcess\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"bacterial_meningitis\",\"text\":\"Bacterial Meningitis\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"abortion\",\"text\":\"Abortion\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"bronchitis\",\"text\":\"Bronchitis\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"arucellosis\",\"text\":\"arucellosis\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}}],\"type\":\"multi_select_list\"}";

    @Before
    public void setUp() {
        super.setUp();
        multiSelectListFactory = Mockito.spy(new MultiSelectListFactory());
    }

    @Test
    public void testShouldInitializeFactoryCorrectly() throws Exception {
        String key = "user_dummy";
        JSONObject jsonObject = new JSONObject(strJsonObject);
        Mockito.doReturn(jsonFormActivity).when(jsonFormFragment).getJsonApi();
        Mockito.doReturn(LayoutInflater.from(jsonFormActivity)).when(jsonFormFragment).getLayoutInflater();
        Assert.assertNull(multiSelectListFactory.
                getMultiSelectListAccessoryHashMap().get(key));

        List<View> views = multiSelectListFactory.getViewsFromJson("step1", jsonFormActivity, jsonFormFragment,
                jsonObject, null);

        shadowOf(getMainLooper()).idle();
        Thread.sleep(TIMEOUT);

        Mockito.verify(multiSelectListFactory, Mockito.times(1))
                .createActionView(Mockito.eq(jsonFormActivity));

        Mockito.verify(multiSelectListFactory, Mockito.times(1))
                .createSelectedRecyclerView(Mockito.eq(jsonFormActivity));

        Mockito.verify(multiSelectListFactory, Mockito.times(1))
                .prepareListData();

        Mockito.verify(multiSelectListFactory, Mockito.times(1))
                .prepareSelectedData();

        Assert.assertNotNull(multiSelectListFactory.
                getMultiSelectListAccessoryHashMap().get(key).getAlertDialog());

        Assert.assertEquals(key, views.get(1).getTag(R.id.key));

        Assert.assertEquals(2, views.size());
    }

    @Test
    public void prepareSelectedDataShouldReturnEmptyArrayList() {
        multiSelectListFactory.jsonObject = new JSONObject();
        Assert.assertEquals(new ArrayList<>(), multiSelectListFactory.prepareSelectedData());
    }

    @Test
    public void prepareSelectedDataShouldReturnFilledArrayList() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.VALUE,
                new JSONArray("[\n" +
                        "          {\n" +
                        "            \"key\": \"abortion\",\n" +
                        "            \"text\": \"Abortion\",\n" +
                        "            \"property\": {\n" +
                        "              \"presumed-id\": \"er\",\n" +
                        "              \"confirmed-id\": \"er\"\n" +
                        "            }\n" +
                        "          }\n" +
                        "        ]"));

        Whitebox.setInternalState(multiSelectListFactory, "jsonObject", jsonObject);

        Assert.assertEquals(1, multiSelectListFactory.prepareSelectedData().size());
    }

    @Test
    public void updateSelectedData() throws Exception {

        HashMap<String, MultiSelectListAccessory> accessoryHashMap = new HashMap<>();
        Whitebox.setInternalState(MultiSelectListFactory.class, "multiSelectListAccessoryHashMap", accessoryHashMap);
        MultiSelectListSelectedAdapter multiSelectListAdapter = new MultiSelectListSelectedAdapter(new ArrayList<MultiSelectItem>(), multiSelectListFactory);

        MultiSelectListAccessory multiSelectListAccessory = new MultiSelectListAccessory(
                multiSelectListAdapter,
                new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()),
                null,
                new ArrayList<MultiSelectItem>(),
                new ArrayList<MultiSelectItem>());
        multiSelectListFactory.currentAdapterKey = "test";

        Whitebox.invokeMethod(multiSelectListFactory, "updateMultiSelectListAccessoryHashMap", multiSelectListAccessory);
        try {
            multiSelectListFactory.updateSelectedData(new MultiSelectItem(), true);
        } catch (NullPointerException e) {
            //this exception catches call on notifyDataSetChanged since no recylclerview has been attached to the adapter;
        }
        Assert.assertEquals(1, multiSelectListFactory.getMultiSelectListSelectedAdapter().getData().size());

    }

    @Test
    public void updateListData() throws Exception {
        HashMap<String, MultiSelectListAccessory> accessoryHashMap = new HashMap<>();
        Whitebox.setInternalState(MultiSelectListFactory.class, "multiSelectListAccessoryHashMap", accessoryHashMap);
        MultiSelectListSelectedAdapter multiSelectListAdapter = new MultiSelectListSelectedAdapter(new ArrayList<MultiSelectItem>(), multiSelectListFactory);

        MultiSelectListAccessory multiSelectListAccessory = new MultiSelectListAccessory(
                multiSelectListAdapter,
                new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()),
                null,
                new ArrayList<MultiSelectItem>(),
                new ArrayList<MultiSelectItem>());
        multiSelectListFactory.currentAdapterKey = "test";

        List<MultiSelectItem> multiSelectItems = new ArrayList<>();
        multiSelectItems.add(new MultiSelectItem());
        multiSelectListAccessory.setItemList(multiSelectItems);

        Whitebox.invokeMethod(multiSelectListFactory, "updateMultiSelectListAccessoryHashMap", multiSelectListAccessory);
        try {

            multiSelectListFactory.updateListData(true);
        } catch (NullPointerException e) {
            //this exception catches call on notifyDataSetChanged since no recylclerview has been attached to the adapter;
        }
        Assert.assertEquals(1, multiSelectListFactory.getMultiSelectListAdapter().getData().size());
    }

    @Test
    public void testLoadListItemsWithExternalSource() throws JSONException {
        String strJsonObject =
                "{\n" +
                        "        \"source\": \"csv\",\n" +
                        "        \"repositoryClass\": \"com.vijay.jsonwizard.repository.TestMultiSelectListRepository\"" +
                        "}";
        JSONObject jsonObject = new JSONObject(strJsonObject);
        Whitebox.setInternalState(multiSelectListFactory, "jsonObject", jsonObject);
        List<MultiSelectItem> selectItems = multiSelectListFactory.loadListItems("settings");

        Assert.assertTrue(new TestMultiSelectListRepository().fetchData().containsAll(selectItems));
    }

    @Test
    public void testLoadListItemsWithOptionsAsDataSource() throws JSONException {
        String strJsonObject =
                "{" +
                        "        \"options\": [\n" +
                        "          {\n" +
                        "            \"key\": \"Bbcess\",\n" +
                        "            \"text\": \"BAbcess\",\n" +
                        "            \"property\": {\n" +
                        "              \"presumed-id\": \"er\",\n" +
                        "              \"confirmed-id\": \"er\"\n" +
                        "            }\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"key\": \"bacterial_meningitis\",\n" +
                        "            \"text\": \"Bacterial Meningitis\",\n" +
                        "            \"property\": {\n" +
                        "              \"presumed-id\": \"er\",\n" +
                        "              \"confirmed-id\": \"er\"\n" +
                        "            }\n" +
                        "          }]"
                        + "}";
        JSONObject jsonObject = new JSONObject(strJsonObject);
        Whitebox.setInternalState(multiSelectListFactory, jsonObject, jsonObject);
        List<MultiSelectItem> selectItems = multiSelectListFactory.loadListItems(null);
        Assert.assertEquals("Bbcess", selectItems.get(0).getKey());
        Assert.assertEquals("BAbcess", selectItems.get(0).getText());

        Assert.assertEquals("bacterial_meningitis", selectItems.get(1).getKey());
        Assert.assertEquals("Bacterial Meningitis", selectItems.get(1).getText());


        Assert.assertEquals("{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}", selectItems.get(0).getValue());
        Assert.assertEquals("{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}", selectItems.get(1).getValue());

    }

    @Test
    public void testPrepareListDataShouldPopulateTheFactoryListItems() throws JSONException, InterruptedException {
        JSONObject jsonObject = new JSONObject(strJsonObject);
        JsonFormFragment jsonFormFragment = Mockito.spy(new JsonFormFragment());
        Mockito.doReturn(jsonFormActivity).when(jsonFormFragment).getJsonApi();
        Mockito.doReturn(jsonFormFragment).when(multiSelectListFactory).getJsonFormFragment();
        Mockito.doReturn(RuntimeEnvironment.application).when(multiSelectListFactory).getContext();
        multiSelectListFactory.jsonObject = jsonObject;
        multiSelectListFactory.currentAdapterKey = "user_dummy";

        MultiSelectListAdapter multiSelectListAdapterSpy = Mockito.spy(new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()));

        HashMap<String, MultiSelectListAccessory> listAccessoryHashMap = new HashMap<>();
        listAccessoryHashMap.put(multiSelectListFactory.currentAdapterKey,
                new MultiSelectListAccessory(Mockito.mock(MultiSelectListSelectedAdapter.class), multiSelectListAdapterSpy, Mockito.mock(AlertDialog.class), new ArrayList<MultiSelectItem>(), new ArrayList<MultiSelectItem>()));

        Whitebox.setInternalState(MultiSelectListFactory.class, "multiSelectListAccessoryHashMap", listAccessoryHashMap);

        List<MultiSelectItem> multiSelectItems = multiSelectListFactory.prepareListData();
        shadowOf(getMainLooper()).idle();
        Thread.sleep(TIMEOUT);

        Assert.assertTrue(multiSelectItems.isEmpty());

        Assert.assertFalse(listAccessoryHashMap.get(multiSelectListFactory.currentAdapterKey).getItemList().isEmpty());

        Assert.assertFalse(multiSelectListAdapterSpy.getData().isEmpty());

        Assert.assertEquals(7, multiSelectListAdapterSpy.getData().size());

        Mockito.verify(multiSelectListFactory, Mockito.timeout(2)).updateListData(Mockito.eq(true));
    }

    @Test
    public void testAddRequiredValidator() throws Exception {
        Method addRequiredValidator = MultiSelectListFactory.class.getDeclaredMethod("addRequiredValidator", RelativeLayout.class, JSONObject.class);
        addRequiredValidator.setAccessible(true);

        RelativeLayout relativeLayout = Mockito.mock(RelativeLayout.class);
        JSONObject jsonObject = Mockito.mock(JSONObject.class);

        Mockito.doReturn(jsonObject).when(jsonObject).optJSONObject(JsonFormConstants.V_REQUIRED);
        Mockito.doReturn(true).when(jsonObject).getBoolean(JsonFormConstants.VALUE);
        Mockito.doReturn("kassim").when(jsonObject).optString(JsonFormConstants.ERR, null);

        addRequiredValidator.invoke(multiSelectListFactory, relativeLayout, jsonObject);

        Mockito.verify(jsonObject).optJSONObject(JsonFormConstants.V_REQUIRED);
        Mockito.verify(jsonObject).getBoolean(JsonFormConstants.VALUE);
        Mockito.verify(relativeLayout).setTag(R.id.error, "kassim");
    }

    @Test
    public void testValidateWhenNoError() {

        JsonFormFragmentView jsonFormFragmentView = Mockito.mock(JsonFormFragmentView.class);
        RelativeLayout relativeLayout = Mockito.mock(RelativeLayout.class);

        ValidationStatus validationStatus = MultiSelectListFactory.validate(jsonFormFragmentView, relativeLayout);

        Mockito.verify(relativeLayout).getTag(R.id.error);
        Mockito.verify(relativeLayout).isEnabled();

        Assert.assertEquals(true, validationStatus.isValid());
    }

    @Test
    public void testValidateWhenError() throws Exception {

        JsonFormFragmentView jsonFormFragmentView = Mockito.mock(JsonFormFragmentView.class);
        RelativeLayout relativeLayout = Mockito.mock(RelativeLayout.class);

        Mockito.doReturn("Error").when(relativeLayout).getTag(R.id.error);
        Mockito.doReturn(true).when(relativeLayout).isEnabled();

        HashMap<String, MultiSelectListAccessory> accessoryHashMap = new HashMap<>();
        Whitebox.setInternalState(MultiSelectListFactory.class, "multiSelectListAccessoryHashMap", accessoryHashMap);
        MultiSelectListSelectedAdapter multiSelectListAdapter = new MultiSelectListSelectedAdapter(new ArrayList<MultiSelectItem>(), multiSelectListFactory);

        MultiSelectListAccessory multiSelectListAccessory = new MultiSelectListAccessory(
                multiSelectListAdapter,
                new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()),
                null,
                new ArrayList<MultiSelectItem>(),
                new ArrayList<MultiSelectItem>());
        multiSelectListFactory.currentAdapterKey = "test";

        Whitebox.invokeMethod(multiSelectListFactory, "updateMultiSelectListAccessoryHashMap", multiSelectListAccessory);
        try {
            multiSelectListFactory.updateSelectedData(new MultiSelectItem(), true);
        } catch (NullPointerException e) {
            //this exception catches call on notifyDataSetChanged since no recylclerview has been attached to the adapter;
        }

        Mockito.doReturn("test").when(relativeLayout).getTag(R.id.key);
        ValidationStatus validationStatus = MultiSelectListFactory.validate(jsonFormFragmentView, relativeLayout);

        Mockito.verify(relativeLayout).getTag(R.id.error);
        Mockito.verify(relativeLayout).getTag(R.id.key);
        Mockito.verify(relativeLayout).isEnabled();

        Assert.assertEquals(true, validationStatus.isValid());
    }
}