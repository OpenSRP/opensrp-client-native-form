package com.vijay.jsonwizard.utils;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.domain.MultiSelectListAccessory;
import com.vijay.jsonwizard.fragments.JsonFormFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class MultiSelectListUtils {

    public static List<MultiSelectItem> loadOptionsFromJsonForm(@NonNull JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.has(JsonFormConstants.OPTIONS_FIELD_NAME) ? jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME) : null;
            if (jsonArray != null) {
                return processOptionsJsonArray(jsonArray);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    public static List<MultiSelectItem> processOptionsJsonArray(@NonNull JSONArray jsonArray) throws JSONException {
        List<MultiSelectItem> multiSelectItems = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonDataObject = jsonArray.getJSONObject(i);
            multiSelectItems.add(new MultiSelectItem(
                    jsonDataObject.getString(JsonFormConstants.KEY),
                    jsonDataObject.getString(JsonFormConstants.TEXT),
                    jsonDataObject.has(JsonFormConstants.MultiSelectUtils.PROPERTY) ? jsonDataObject.getJSONObject(JsonFormConstants.MultiSelectUtils.PROPERTY).toString() : null,
                    jsonDataObject.optString(JsonFormConstants.OPENMRS_ENTITY),
                    jsonDataObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID),
                    jsonDataObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT)));
        }
        return multiSelectItems;
    }

    public static void addGroupings(@NonNull List<MultiSelectItem> unsortedMultiSelectItems, @NonNull JSONArray jsonGroupingsArray) {
        for (int i = 0; i < jsonGroupingsArray.length(); i++) {
            MultiSelectItem multiSelectItem = new MultiSelectItem(jsonGroupingsArray.optString(i), jsonGroupingsArray.optString(i), null, null, null, null);
            unsortedMultiSelectItems.add(multiSelectItem);
        }
    }

    public static void writeToForm(String currentAdapterKey, JsonFormFragment jsonFormFragment, HashMap<String, MultiSelectListAccessory> multiSelectListAccessoryHashMap) {
        try {
            MultiSelectListAccessory multiSelectListAccessory = multiSelectListAccessoryHashMap.get(currentAdapterKey);
            jsonFormFragment.getJsonApi().writeValue(
                    multiSelectListAccessory.getFormAttributes().optString(JsonFormConstants.STEPNAME), currentAdapterKey,
                    MultiSelectListUtils.toJson(multiSelectListAccessory.getSelectedAdapter().getData()).toString(),
                    multiSelectListAccessory.getFormAttributes().optString(JsonFormConstants.OPENMRS_ENTITY_PARENT),
                    multiSelectListAccessory.getFormAttributes().optString(JsonFormConstants.OPENMRS_ENTITY),
                    multiSelectListAccessory.getFormAttributes().optString(JsonFormConstants.OPENMRS_ENTITY_ID),
                    multiSelectListAccessory.getFormAttributes().optBoolean(JsonFormConstants.IS_POPUP));
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public static JSONArray toJson(List<MultiSelectItem> multiSelectItems) {
        JSONArray jsonArray = new JSONArray();
        try {
            if (multiSelectItems.isEmpty()) {
                return new JSONArray();
            }
            for (MultiSelectItem multiSelectItem : multiSelectItems) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonFormConstants.KEY, multiSelectItem.getKey());
                jsonObject.put(JsonFormConstants.MultiSelectUtils.TEXT, multiSelectItem.getText());
                jsonObject.put(JsonFormConstants.OPENMRS_ENTITY, multiSelectItem.getOpenmrsEntity());
                jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_ID, multiSelectItem.getOpenmrsEntityId());
                jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, multiSelectItem.getOpenmrsEntityParent());
                jsonObject.put(JsonFormConstants.MultiSelectUtils.PROPERTY, new JSONObject(multiSelectItem.getValue()));
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return jsonArray;
    }

}
