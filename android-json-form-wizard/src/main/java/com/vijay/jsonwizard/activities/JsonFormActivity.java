package com.vijay.jsonwizard.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.Icon;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.comparisons.Comparison;
import com.vijay.jsonwizard.comparisons.EqualToComparison;
import com.vijay.jsonwizard.comparisons.GreaterThanComparison;
import com.vijay.jsonwizard.comparisons.GreaterThanEqualToComparison;
import com.vijay.jsonwizard.comparisons.LessThanComparison;
import com.vijay.jsonwizard.comparisons.LessThanEqualToComparison;
import com.vijay.jsonwizard.comparisons.NotEqualToComparison;
import com.vijay.jsonwizard.comparisons.RegexComparison;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.GenericPopupDialog;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.customviews.TextableView;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.event.RefreshExpansionPanelEvent;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.GenericDialogInterface;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.LifeCycleListener;
import com.vijay.jsonwizard.interfaces.OnActivityRequestPermissionResultListener;
import com.vijay.jsonwizard.interfaces.OnActivityResultListener;
import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.utils.AppExecutors;
import com.vijay.jsonwizard.utils.ExObjectResult;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.PermissionUtils;
import com.vijay.jsonwizard.utils.PropertyManager;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.widgets.CountDownTimerFactory;
import com.vijay.jsonwizard.widgets.NumberSelectorFactory;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jeasy.rules.api.Facts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.vijay.jsonwizard.utils.FormUtils.getCheckboxValueJsonArray;
import static com.vijay.jsonwizard.utils.FormUtils.getCurrentCheckboxValues;

public class JsonFormActivity extends JsonFormBaseActivity implements JsonApi {

    private FormUtils formUtils = new FormUtils();
    private Map<String, View> formDataViews = new ConcurrentHashMap<>();
    private Map<String, JSONObject> formFields = new ConcurrentHashMap<>();
    private Set<String> popupFormFields = new ConcurrentSkipListSet<>();
    private String functionRegex;
    private HashMap<String, Comparison> comparisons;
    private Map<String, List<String>> ruleKeys = new HashMap<>();
    private GenericDialogInterface genericDialogInterface;
    private JSONArray extraFieldsWithValues;
    private Map<String, String> formValuesCacheMap = new HashMap<>();
    private TextView selectedTextView = null;
    private Utils utils = new Utils();
    private HashMap<String, String[]> addressMap = new HashMap<>();

    private Map<String, Set<String>> calculationDependencyMap = new HashMap<>();
    private Map<String, Set<String>> skipLogicDependencyMap = new HashMap<>();

    private Map<String, Boolean> stepSkipLogicPresenceMap = new ConcurrentHashMap<>();

    private boolean isNextStepRelevant;

    private String nextStep = "";

    private AppExecutors appExecutors = new AppExecutors();

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messageType = intent.getStringExtra(JsonFormConstants.INTENT_KEY.MESSAGE_TYPE);
            if (JsonFormConstants.MESSAGE_TYPE.GLOBAL_VALUES.equals(messageType)) {
                Map<String, String> map =
                        (Map<String, String>) intent.getSerializableExtra(JsonFormConstants.INTENT_KEY.MESSAGE);
                globalValues.putAll(map);
                String stepName = intent.getStringExtra(JsonFormConstants.STEPNAME);
                if (StringUtils.isNotBlank(stepName))
                    performActionOnReceived(stepName);
            }
        }
    };

    public void performActionOnReceived(String stepName) {
        try {
            invokeRefreshLogic(null, false, null, null, stepName, false);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public synchronized JSONObject getStep(final String name) {
        synchronized (getmJSONObject()) {
            try {
                return getmJSONObject().getJSONObject(name);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return null;
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity,
                           String openMrsEntityId, boolean popup) throws JSONException {
        if (invokeRefreshLogic(stepName, null, key, value)) {
            if (!popup) {
                cacheFormMapValues(stepName, null, key, value);
            }
            widgetsWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
        }
    }

    @Override
    public void writeValue(String stepName, String parentKey, String childObjectKey, String childKey, String value,
                           String openMrsEntityParent, String openMrsEntity, String openMrsEntityId, boolean popup)
            throws JSONException {
        if (invokeRefreshLogic(stepName, parentKey, childKey, value)) {
            if (!popup) {
                cacheFormMapValues(stepName, parentKey, childKey, value);
            }
            checkBoxWriteValue(stepName, parentKey, childObjectKey, childKey, value, popup);

        }
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity,
                           String openMrsEntityId) throws JSONException {
        if (invokeRefreshLogic(stepName, null, key, value)) {
            cacheFormMapValues(stepName, null, key, value);
            widgetsWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId, false);
        }
    }

    @Override
    public void writeValue(String stepName, String parentKey, String childObjectKey, String childKey, String value,
                           String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        if (invokeRefreshLogic(stepName, parentKey, childKey, value)) {
            cacheFormMapValues(stepName, parentKey, childKey, value);
            checkBoxWriteValue(stepName, parentKey, childObjectKey, childKey, value, false);

        }
    }

    @Override
    public void writeMetaDataValue(String metaDataKey, Map<String, String> values) throws JSONException {
        synchronized (getmJSONObject()) {
            if (mJSONObject.has(FormUtils.METADATA_PROPERTY) && !values.isEmpty() &&
                    (mJSONObject.getJSONObject(FormUtils.METADATA_PROPERTY).has(metaDataKey))) {
                JSONObject metaData = mJSONObject.getJSONObject(FormUtils.METADATA_PROPERTY).getJSONObject(metaDataKey);
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value == null) value = "";
                    metaData.put(key, value);
                }
            }

        }
    }

    @Override
    public String currentJsonState() {
        synchronized (getmJSONObject()) {
            return getmJSONObject().toString();
        }
    }

    @Override
    public String getCount() {
        synchronized (getmJSONObject()) {
            return getmJSONObject().optString("count");
        }
    }

    @Override
    public void onFormFinish() {
        try {
            if (propertyManager == null) {
                propertyManager = new PropertyManager(this);
            }
            FormUtils.updateEndProperties(propertyManager, mJSONObject);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void clearSkipLogicViews() {
        skipLogicViews.clear();
    }

    @Override
    public void clearCalculationLogicViews() {
        calculationLogicViews.clear();
    }

    @Override
    public void clearConstrainedViews() {
        constrainedViews = new ConcurrentHashMap<>();
    }

    @Override
    public void clearFormDataViews() {
        formDataViews = new ConcurrentHashMap<>();
        clearSkipLogicViews();
        clearConstrainedViews();
        clearCalculationLogicViews();
    }

    @Override
    public void addSkipLogicView(View view) {
        skipLogicViews.put((String) view.getTag(R.id.address), view);
    }

    @Override
    public void addCalculationLogicView(View view) {
        calculationLogicViews.put((String) view.getTag(R.id.address), view);
    }

    @Override
    public void addConstrainedView(View view) {
        constrainedViews.put(getViewKey(view), view);
    }

    @Override
    public void refreshHiddenViews(boolean popup) {
        for (View curView : getFormDataViews()) {
            String addressString = (String) curView.getTag(R.id.address);
            if (StringUtils.isNotBlank(addressString)) {
                String[] address = addressString.split(":");
                try {
                    JSONObject viewData = getObjectUsingAddress(address, popup);
                    if (viewData.has(JsonFormConstants.HIDDEN) && viewData.getBoolean(JsonFormConstants.HIDDEN)) {
                        toggleViewVisibility(curView, false, popup);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }
    }

    @Override
    public void refreshSkipLogic(String parentKey, String childKey, boolean popup, String stepName, boolean isForNextStep) {
        initComparisons();
        Set<String> viewsIds = skipLogicDependencyMap.get(stepName + "_" + parentKey);
        if (parentKey == null || childKey == null) {
            for (View curView : skipLogicViews.values()) {
                if (isForNextStep && isNextStepRelevant()) {
                    break;
                }
                addRelevance(curView, popup, isForNextStep);
            }
        } else if (viewsIds == null) {
            for (String curViewId : skipLogicViews.keySet()) {
                //skip any relevance by rules engine since the these components are not affected either way.
                // Run relevance for native relevance functions as these are first and not optimized currently
                if (isForNextStep && isNextStepRelevant()) {
                    break;
                }
                if (!skipLogicDependencyMap.containsKey(curViewId)) {
                    addRelevance(skipLogicViews.get(curViewId), popup, isForNextStep);
                }
            }
        } else {
            for (String viewId : viewsIds) {
                if (isForNextStep && isNextStepRelevant()) {
                    break;
                }
                addRelevance(skipLogicViews.get(viewId), popup, isForNextStep);
            }
        }
    }


    public Pair<String[], JSONObject> getCalculationAddressAndValue(View view) throws JSONException {
        String calculationTag = (String) view.getTag(R.id.calculation);
        String widgetKey = (String) view.getTag(R.id.key);
        String stepName = ((String) view.getTag(R.id.address)).split(":")[0];
        if (calculationTag != null && calculationTag.length() > 0) {
            JSONObject calculation = new JSONObject(calculationTag);
            Iterator<String> keys = calculation.keys();

            while (keys.hasNext()) {
                String curKey = keys.next();

                JSONObject curCalculation = calculation.getJSONObject(curKey);
                JSONObject valueSource = new JSONObject();
                if (calculation.has(JsonFormConstants.SRC)) {
                    valueSource = calculation.getJSONObject(JsonFormConstants.SRC);
                }
                String[] address = getAddressFromMap(widgetKey, stepName, JsonFormConstants.CALCULATION);
                if (address == null && curCalculation.has(JsonFormConstants.JSON_FORM_KEY.EX_RULES)) {
                    JSONObject exRulesObject = curCalculation.getJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES);
                    if (exRulesObject.has(RuleConstant.RULES_DYNAMIC)) {
                        address = getDynamicRulesEngineAddress(curKey, curCalculation, view, JsonFormConstants.CALCULATION);
                    } else {
                        address = getRulesEngineAddress(curKey, curCalculation, view, JsonFormConstants.CALCULATION);
                    }
                }
                return new Pair<>(address, valueSource);
            }
        }
        return null;
    }

    @Override
    public void refreshCalculationLogic(String parentKey, String childKey, boolean popup, String stepName, boolean isForNextStep) {
        Set<String> viewsIds = calculationDependencyMap.get(stepName + "_" + parentKey);
        if (parentKey == null || viewsIds == null)
            viewsIds = calculationLogicViews.keySet();
        for (String viewId : viewsIds) {
            try {
                View curView = calculationLogicViews.get(viewId);
                if (curView == null) {
                    Timber.w("calculationLogicViews Missing %s", viewId);
                    continue;
                }
                Pair<String[], JSONObject> addressAndValue = getCalculationAddressAndValue(curView);
                if (addressAndValue != null && addressAndValue.first != null) {
                    String[] address = addressAndValue.first;
                    JSONObject valueSource = addressAndValue.second;
                    Facts curValueMap;
                    if (valueSource.length() > 0) {
                        curValueMap = getValueFromAddress(address, popup, valueSource);
                    } else {
                        curValueMap = getValueFromAddress(address, popup);
                    }
                    //update ui
                    updateCalculation(curValueMap, curView, address, isForNextStep);
                }

            } catch (Exception e) {
                Timber.e(e, "%s refreshCalculationLogic()", this.getClass().getCanonicalName());

            }
        }
    }

    @Override
    public void initializeDependencyMaps() {
        populateDependencyMap(calculationLogicViews, calculationDependencyMap, true);
        populateDependencyMap(skipLogicViews, skipLogicDependencyMap, false);
    }

    @Override
    public void invokeRefreshLogic(String value, boolean popup, String parentKey, String childKey, String stepName, boolean isForNextStep) {
        refreshCalculationLogic(parentKey, childKey, popup, stepName, isForNextStep);
        refreshSkipLogic(parentKey, childKey, popup, stepName, isForNextStep);

        if (!isForNextStep) {
            refreshConstraints(parentKey, childKey, popup);
            refreshMediaLogic(parentKey, value, stepName);
        }
    }

    private void populateDependencyMap(Map<String, View> formViews, Map<String, Set<String>> dependencyMap, boolean calculation) {
        for (View view : formViews.values()) {
            try {
                boolean isPopup = false;
                if (view.getTag(R.id.extraPopup) != null) {
                    isPopup = (boolean) view.getTag(R.id.extraPopup);
                }
                Pair<String[], JSONObject> addressAndValue = calculation ? getCalculationAddressAndValue(view) :
                        getRelevanceAddress(view, isPopup);
                if (addressAndValue != null) {
                    String[] address = addressAndValue.first;
                    List<String> widgets = null;
                    if (address.length > 2) {
                        if (RuleConstant.RULES_DYNAMIC.equals(address[0])) {
                            widgets = getDynamicRules(address);
                        } else {
                            widgets = getRules(address[1], address[2], true);
                        }
                    } else if (address.length == 2) {
                        widgets = Arrays.asList(address[0] + "_" + address[1]);
                    }

                    if (widgets == null)
                        continue;
                    for (String widget : widgets) {
                        if (!widget.startsWith(RuleConstant.STEP)) {
                            continue;
                        }
                        String key = (String) view.getTag(R.id.address);
                        if (!dependencyMap.containsKey(widget)) {
                            Set<String> views = new HashSet<>();
                            views.add(key);
                            dependencyMap.put(widget, views);
                        } else {
                            dependencyMap.get(widget).add(key);
                        }
                    }
                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

    }

    @Override
    public void addFormDataView(View view) {
        String address = String.valueOf(view.getTag(R.id.address));
        formDataViews.put(address, view);
    }

    @Override
    public Collection<View> getFormDataViews() {
        return formDataViews.values();
    }

    @Override
    public View getFormDataView(String address) {
        return formDataViews.get(address);
    }

    @Override
    public JSONObject getObjectUsingAddress(String[] address, boolean popup) throws JSONException {
        if (address != null && address.length > 1) {
            if (RuleConstant.RULES_DYNAMIC.equals(address[0])) {
                List<String> rulesList = getDynamicRules(address);
                return fillFieldsWithValues(rulesList, popup);
            } else if (RuleConstant.RULES_ENGINE.equals(address[0])) {
                String fieldKey = address[2];
                List<String> rulesList = getRules(address[1], fieldKey, false);
                if (rulesList != null) {
                    return fillFieldsWithValues(rulesList, popup);
                }
            } else {
                return getRelevanceReferencedObject(address[0], address[1]);
            }
        }

        return null;
    }

    private List<String> getDynamicRules(@NonNull String[] address) {
        List<String> keysList = new ArrayList<>();

        JSONArray jsonArray = null;
        if (address.length > 1 && StringUtils.isNotBlank(address[1])) {
            try {
                jsonArray = new JSONArray(address[1]);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    if (!jsonObject.has(JsonFormConstants.KEY)) {
                        String condition = jsonObject.optString(RuleConstant.CONDITION);
                        if (StringUtils.isNotBlank(condition)) {
                            keysList.addAll(Utils.getConditionKeys(condition));
                        }

                        String action = jsonObject.optString(RuleConstant.ACTIONS);
                        if (StringUtils.isNotBlank(action) && !action.trim().startsWith(RuleConstant.IS_RELEVANT)) {
                            keysList.addAll(Utils.getConditionKeys(action));
                        }
                    }
                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return keysList;
    }

    private JSONObject fillFieldsWithValues(List<String> rulesList, boolean popup) throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray rulesArray = new JSONArray();
        for (int h = 1; h < getmJSONObject().getInt(JsonFormConstants.COUNT) + 1; h++) {
            JSONArray fields = fetchFields(getmJSONObject().optJSONObject(RuleConstant.STEP + h), popup);
            for (int i = 0; i < fields.length(); i++) {
                if (rulesList.contains(RuleConstant.STEP + h + "_" +
                        fields.getJSONObject(i).getString(JsonFormConstants.KEY))) {

                    JSONObject fieldObject = fields.getJSONObject(i);
                    fieldObject.put(RuleConstant.STEP, RuleConstant.STEP + h);
                    rulesArray.put(fieldObject);
                }
            }
        }
        result.put(RuleConstant.RESULT, rulesArray);
        return result;
    }

    @Override
    public JSONObject getObjectUsingAddress(String[] address, boolean popup, JSONObject valueSource) throws JSONException {
        if (valueSource != null && valueSource.has(JsonFormConstants.KEY) && valueSource.has(JsonFormConstants.STEPNAME) &&
                valueSource.has(JsonFormConstants.OPTION_KEY)) {

            String key = valueSource.getString(JsonFormConstants.KEY);
            String stepName = valueSource.getString(JsonFormConstants.STEPNAME);
            String optionKey = valueSource.getString(JsonFormConstants.OPTION_KEY);

            try {
                if (address != null && address.length > 1) {
                    if (RuleConstant.RULES_ENGINE.equals(address[0])) {
                        String fieldKey = address[2];

                        List<String> rulesList = getRules(address[1], fieldKey, false);
                        if (rulesList != null) {
                            JSONObject result = new JSONObject();
                            JSONArray rulesArray = new JSONArray();

                            JSONObject mainWidget = FormUtils.getFieldFromForm(mJSONObject, key);
                            if (mainWidget.has(JsonFormConstants.OPTIONS_FIELD_NAME)) {
                                JSONArray options = mainWidget.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
                                for (int i = 0; i < options.length(); i++) {
                                    JSONObject option = options.getJSONObject(i);

                                    if (option != null && option.has(JsonFormConstants.KEY) &&
                                            optionKey.equals(option.getString(JsonFormConstants.KEY)) &&
                                            option.has(JsonFormConstants.CONTENT_FORM)) {
                                        String formName = option.getString(JsonFormConstants.CONTENT_FORM);
                                        String popupFormName = "";
                                        if (genericDialogInterface != null) {
                                            popupFormName = genericDialogInterface.getFormIdentity();
                                        }

                                        if (genericDialogInterface != null && formName.equals(popupFormName)) {
                                            JSONArray subFormField = genericDialogInterface.getPopUpFields();
                                            getFieldObject(stepName, rulesList, rulesArray, subFormField);
                                        } else if (option.has(JsonFormConstants.SECONDARY_VALUE)) {
                                            JSONArray secondaryValue =
                                                    option.getJSONArray(JsonFormConstants.SECONDARY_VALUE);
                                            getFieldObject(stepName, rulesList, rulesArray, secondaryValue);
                                        }
                                    }
                                }
                            }

                            result.put(RuleConstant.RESULT, rulesArray);
                            return result;
                        }
                    } else {
                        return getRelevanceReferencedObject(address[0], address[1]);
                    }
                }
            } catch (Exception e) {
                Timber.e(e, "%s getObjectUsingAddress()", this.getClass().getCanonicalName());
            }
        } else {
            getObjectUsingAddress(address, popup);
        }

        return null;
    }

    /**
     * This method checks if all views being watched for constraints enforce those constraints This library currently only
     * supports constraints on views that store the value in {@link MaterialEditText} (ie TreeViews, DatePickers, and
     * EditTexts), and {@link CheckBox}
     *
     * @param parentKey {@link String}
     * @param childKey  {@link String}
     * @param popup     {@link Boolean}
     */
    @Override
    public void refreshConstraints(String parentKey, String childKey, boolean popup) {
        initComparisons();

        // Priorities constraints on the view that has just been changed
        String changedViewKey = parentKey;
        if (changedViewKey != null && childKey != null) {
            changedViewKey = changedViewKey + ":" + childKey;
        }

        if (changedViewKey != null && (constrainedViews != null && constrainedViews.containsKey(changedViewKey))) {
            checkViewConstraints(constrainedViews.get(changedViewKey), popup);
        }

        for (View curView : constrainedViews.values()) {
            String viewKey = getViewKey(curView);
            if (changedViewKey == null || (!TextUtils.isEmpty(viewKey) && !viewKey.equals(changedViewKey))) {
                checkViewConstraints(curView, popup);
            }
        }
    }

    @Override
    public void addOnActivityResultListener(final Integer requestCode, OnActivityResultListener onActivityResultListener) {
        onActivityResultListeners.put(requestCode, onActivityResultListener);
    }

    @Override
    public void addOnActivityRequestPermissionResultListener(Integer requestCode,
                                                             OnActivityRequestPermissionResultListener onActivityRequestPermissionResultListener) {
        onActivityRequestPermissionResultListeners.put(requestCode, onActivityRequestPermissionResultListener);
    }

    @Override
    public void removeOnActivityRequestPermissionResultListener(Integer requestCode) {
        onActivityRequestPermissionResultListeners.remove(requestCode);
    }

    @Override
    public void resetFocus() {
        EditText defaultFocusView = findViewById(R.id.default_focus_view);
        defaultFocusView.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputManager != null && getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public JSONObject getmJSONObject() {
        return mJSONObject;
    }

    @Override
    public void setmJSONObject(JSONObject mJSONObject) {
        super.setmJSONObject(mJSONObject);
        initializeFormFieldsMap();
    }

    private void initializeFormFieldsMap() {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                JSONObject formJsonObject = getmJSONObject();
                int count = formJsonObject.optInt(JsonFormConstants.COUNT);
                try {
                    for (int i = 1; i <= count; i++) {
                        String step = JsonFormConstants.STEP + i;
                        if (!formJsonObject.has(step)) {
                            Timber.w("Missing step %s", step);
                            continue;
                        }
                        JSONArray fields = fetchFields(formJsonObject.getJSONObject(step), false);
                        for (int j = 0; j < fields.length(); j++) {
                            JSONObject field = fields.getJSONObject(j);
                            formFields.put(step + "_" + field.getString(JsonFormConstants.KEY), field);
                        }
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        });
    }

    @Override
    protected void initiateFormUpdate(JSONObject json) {
        if (getForm() != null && ((getForm().getHiddenFields() != null && !getForm().getHiddenFields().isEmpty()) || (getForm().getDisabledFields() != null && !getForm().getDisabledFields().isEmpty()))) {
            JSONArray fieldsJsonObject = FormUtils.getMultiStepFormFields(json);
            for (int k = 0; k < fieldsJsonObject.length(); k++) {
                Utils.handleFieldBehaviour(fieldsJsonObject.optJSONObject(k), getForm());
            }
        }
    }


    @Override
    public void updateGenericPopupSecondaryValues(JSONArray jsonArray, String stepName) {
        if (jsonArray == null || jsonArray.length() == 0) {
            for (String key : popupFormFields) {
                popupFormFields.remove(key);
            }
        }
        setExtraFieldsWithValues(jsonArray);
        if (jsonArray != null) {
            for (int j = 0; j < jsonArray.length(); j++) {
                try {
                    JSONObject field = jsonArray.getJSONObject(j);
                    String key = stepName + "_" + field.getString(JsonFormConstants.KEY);
                    formFields.put(key, field);
                    popupFormFields.add(key);
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }
    }

    @Override
    public void registerLifecycleListener(LifeCycleListener lifeCycleListener) {
        lifeCycleListeners.add(lifeCycleListener);
    }

    @Override
    public void unregisterLifecycleListener(LifeCycleListener lifeCycleListener) {
        lifeCycleListeners.remove(lifeCycleListener);
    }

    @Override
    public void setGenericPopup(GenericPopupDialog context) {
        genericDialogInterface = context;
    }

    @Override
    public String getConfirmCloseMessage() {
        return confirmCloseMessage;
    }

    @Override
    public void setConfirmCloseMessage(String confirmCloseMessage) {
        this.confirmCloseMessage = confirmCloseMessage;
    }

    @Override
    public String getConfirmCloseTitle() {
        return confirmCloseTitle;
    }

    @Override
    public void setConfirmCloseTitle(String confirmCloseTitle) {
        this.confirmCloseTitle = confirmCloseTitle;
    }

    @Override
    public void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_denied_title))
                .setMessage(getString(R.string.permission_messege))
                .setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(JsonFormActivity.this, new String[]{
                                Manifest.permission.READ_PHONE_STATE}, PermissionUtils.PHONE_STATE_PERMISSION);
                    }
                })
                .setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * if the display scroll bars attribute is set to true then the form shows scroll bars
     *
     * @return true/false {@link Boolean}
     */
    @Override
    public boolean displayScrollBars() {
        synchronized (getmJSONObject()) {
            return getmJSONObject().optBoolean(JsonFormConstants.DISPLAY_SCROLL_BARS, false);
        }
    }

    @Override
    public boolean skipBlankSteps() {
        synchronized (getmJSONObject()) {
            return getmJSONObject().optBoolean(JsonFormConstants.SKIP_BLANK_STEPS, false);
        }
    }

    @Override
    public Form form() {
        return getForm();
    }

    private String getViewKey(View view) {
        String key = (String) view.getTag(R.id.key);
        if (view.getTag(R.id.childKey) != null) {
            key = key + ":" + view.getTag(R.id.childKey);
        }

        return key;
    }

    private void addToAddressMap(String key, String stepName, String type, String[] address) {
        addressMap.put(stepName + "_" + key + "_" + type, address);
    }

    private String[] getAddressFromMap(String key, String stepName, String type) {
        return addressMap.get(stepName + "_" + key + "_" + type);
    }

    private String[] getAddress(View view, String curKey, JSONObject curRelevance, String type) {
        String[] address;
        if (curKey.contains(":")) {
            address = curKey.split(":");
            String[] viewAddress = view.getTag(R.id.address).toString().split(":");
            addToAddressMap(viewAddress[1], viewAddress[0], type, address);
        } else {
            try {
                if (curRelevance.getJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES).has(RuleConstant.RULES_DYNAMIC)) {
                    return getDynamicRulesEngineAddress(curKey, curRelevance, view, type);
                } else {
                    address = getRulesEngineAddress(curKey, curRelevance, view, type);
                }
            } catch (JSONException e) {
                Timber.e(e);
                return null;
            }
        }
        return address;
    }

    private String[] getRulesEngineAddress(String curKey, JSONObject curRelevance, View view, String type) {
        return getRulesEngineAddress(curKey, curRelevance, view, type, RuleConstant.RULES_FILE);
    }

    private String[] getDynamicRulesEngineAddress(String curKey, JSONObject curRelevance, View view, String type) {
        return getRulesEngineAddress(curKey, curRelevance, view, type, RuleConstant.RULES_DYNAMIC);
    }

    private String[] getRulesEngineAddress(String curKey, JSONObject curRelevance, View view, String type, String ruleType) {
        String[] address = new String[0];
        try {
            String currentKey = RuleConstant.RULES_DYNAMIC.equals(ruleType) ? ruleType : curKey;
            address = new String[]{currentKey,
                    curRelevance.getJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES).getString(ruleType),
                    view.getTag(R.id.address).toString().replace(':', '_')};

            String[] viewAddress = view.getTag(R.id.address).toString().split(":");
            addToAddressMap(viewAddress[0], viewAddress[1], type, address);
        } catch (JSONException e) {
            Timber.e(e, "%s getRulesEngineAddress()", this.getClass().getCanonicalName());
        }
        return address;
    }

    private JSONObject getRelevanceReferencedObject(String stepName, String key) {
        return formFields.get(stepName + "_" + key);
    }

    private void getFieldObject(String stepName, List<String> rulesList, JSONArray rulesArray, JSONArray fields)
            throws JSONException {
        if (fields.length() > 0) {
            for (int j = 0; j < fields.length(); j++) {
                JSONObject fieldObject = fields.getJSONObject(j);
                if (rulesList.contains(stepName + "_" + fieldObject.getString(JsonFormConstants.KEY)) &&
                        !JsonFormConstants.LABEL.equals(fieldObject.getString(JsonFormConstants.TYPE))) {
                    if (fieldObject.has(JsonFormConstants.VALUES)) {
                        String value;
                        if (JsonFormConstants.CHECK_BOX.equals(fieldObject.getString(JsonFormConstants.TYPE))) {
                            value = String.valueOf(fieldObject.getJSONArray(JsonFormConstants.VALUES));
                            fieldObject.put(JsonFormConstants.VALUE, value);
                        } else {
                            value = fieldObject.getJSONArray(JsonFormConstants.VALUES).getString(0);
                            fieldObject.put(JsonFormConstants.VALUE, value);
                        }
                    }

                    fieldObject.put(RuleConstant.STEP, stepName);
                    rulesArray.put(fieldObject);

                }
            }
        }
    }

    protected void widgetsWriteValue(String stepName, String key, String value, String openMrsEntityParent,
                                     String openMrsEntity, String openMrsEntityId, boolean popup) throws JSONException {

        JSONObject item = formFields.get(stepName + "_" + key);
        if (item != null) {
            String keyAtIndex = item.getString(JsonFormConstants.KEY);
            String itemType = item.has(JsonFormConstants.TYPE) ? item.getString(JsonFormConstants.TYPE) : "";
            boolean isSpecialWidget = isSpecialWidget(itemType);
            String cleanKey = isSpecialWidget ? cleanWidgetKey(key, itemType) : key;

            if (cleanKey.equals(keyAtIndex)) {
                if (item.has(JsonFormConstants.TEXT)) {
                    item.put(JsonFormConstants.TEXT, value);
                } else {
                    widgetWriteItemValue(value, item, itemType);
                }
                addOpenMrsAttributes(openMrsEntityParent, openMrsEntity, openMrsEntityId, item);
                invokeRefreshLogic(value, popup, cleanKey, null, stepName, false);
            }
        }
    }

    private void addOpenMrsAttributes(String openMrsEntityParent, String openMrsEntity, String openMrsEntityId,
                                      JSONObject item) throws JSONException {
        item.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, openMrsEntityParent);
        item.put(JsonFormConstants.OPENMRS_ENTITY, openMrsEntity);
        item.put(JsonFormConstants.OPENMRS_ENTITY_ID, openMrsEntityId);
    }

    private void widgetWriteItemValue(String value, JSONObject item, String itemType) throws JSONException {
        if (!TextUtils.isEmpty(value)) {
            value = value.trim();
        }
        item.put(JsonFormConstants.VALUE, itemType.equals(JsonFormConstants.HIDDEN) && TextUtils.isEmpty(value) ?
                item.has(JsonFormConstants.VALUE) && !TextUtils.isEmpty(item.getString(JsonFormConstants.VALUE)) ?
                        item.getString(JsonFormConstants.VALUE) : value : value);
    }

    private boolean checkPopUpValidity(String[] curKey, boolean popup) throws JSONException {
        boolean validity = false;
        if (popup) {
            String parentKey = "";
            if (curKey.length == 2) {
                parentKey = curKey[1];
            } else if (curKey.length == 3) {
                parentKey = curKey[2].substring(curKey[2].indexOf("_") + 1);
            }

            if (extraFieldsWithValues != null && extraFieldsWithValues.length() > 0) {
                for (int i = 0; i < extraFieldsWithValues.length(); i++) {
                    JSONObject jsonObject = extraFieldsWithValues.getJSONObject(i);
                    if (jsonObject.has(JsonFormConstants.KEY) &&
                            jsonObject.getString(JsonFormConstants.KEY).equals(parentKey)) {
                        validity = true;
                        break;
                    }
                }
            }
        }
        return validity;
    }

    protected boolean isSpecialWidget(String itemType) {
        return isNumberSelector(itemType);
    }

    protected String cleanWidgetKey(String itemKey, String itemType) {
        String key = itemKey;

        if (isNumberSelector(itemType) && itemKey.endsWith(JsonFormConstants.SUFFIX.TEXT_VIEW) ||
                itemKey.endsWith(JsonFormConstants.SUFFIX.SPINNER)) {
            key = itemKey.endsWith(JsonFormConstants.SUFFIX.TEXT_VIEW) ?
                    itemKey.substring(0, itemKey.indexOf(JsonFormConstants.SUFFIX.TEXT_VIEW)) :
                    itemKey.substring(0, itemKey.indexOf(JsonFormConstants.SUFFIX.SPINNER));
        }

        return key;
    }

    private boolean isNumberSelector(String itemType) {
        return itemType.equals(JsonFormConstants.NUMBER_SELECTOR);
    }

    protected void checkBoxWriteValue(String stepName, String parentKey, String childObjectKey, String childKey,
                                      String value, boolean popup) throws JSONException {

        synchronized (getmJSONObject()) {
            JSONObject checkboxObject = formFields.get(stepName + "_" + parentKey);
            JSONArray checkboxOptions = checkboxObject.getJSONArray(childObjectKey);
            HashSet<String> currentValues = new HashSet<>();
            //Get current values
            if (checkboxObject.has(JsonFormConstants.VALUE)) {
                formUtils.updateValueToJSONArray(checkboxObject, checkboxObject.optString(JsonFormConstants.VALUE, ""));
            }

            if (checkboxObject != null && checkboxOptions != null) {
                if (checkboxObject.has(JsonFormConstants.VALUE) && StringUtils.isNotEmpty(checkboxObject.getString(JsonFormConstants.VALUE))) {
                    currentValues.addAll(getCurrentCheckboxValues(checkboxObject.getJSONArray(JsonFormConstants.VALUE)));
                }

                for (int index = 0; index < checkboxOptions.length(); index++) {
                    JSONObject option = checkboxOptions.getJSONObject(index);
                    if (option.has(JsonFormConstants.KEY) &&
                            childKey.equals(option.getString(JsonFormConstants.KEY))) {
                        option.put(JsonFormConstants.VALUE, Boolean.parseBoolean(value));
                        if (Boolean.parseBoolean(value)) {
                            currentValues.add(childKey);
                        } else {
                            currentValues.remove(childKey);
                        }
                    }
                }
                checkboxObject.put(JsonFormConstants.VALUE, getCheckboxValueJsonArray(currentValues));
            }
            invokeRefreshLogic(value, popup, parentKey, childKey, stepName, false);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppThemeAlertDialog).setTitle(confirmCloseTitle)
                .setMessage(confirmCloseMessage).setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JsonFormActivity.this.finish();
                        CountDownTimerFactory.stopAlarm();
                    }
                }).setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("No button on dialog in %s", JsonFormActivity.class.getCanonicalName());
                    }
                }).create();

        dialog.show();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            lifeCycleListener.onLowMemory();
        }
    }

    @Override
    protected void onPause() {
        localBroadcastManager.unregisterReceiver(NumberSelectorFactory.getNumberSelectorFactory().getNumberSelectorsReceiver());
        localBroadcastManager.unregisterReceiver(messageReceiver);
        super.onPause();
        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            lifeCycleListener.onPause();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        localBroadcastManager
                .registerReceiver(messageReceiver, new IntentFilter(JsonFormConstants.INTENT_ACTION.JSON_FORM_ACTIVITY));
        localBroadcastManager.registerReceiver(NumberSelectorFactory.getNumberSelectorFactory().getNumberSelectorsReceiver(),
                new IntentFilter(JsonFormConstants.INTENT_ACTION.NUMBER_SELECTOR_FACTORY));

        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            lifeCycleListener.onResume();
        }
        if (!getmJSONObject().has(JsonFormConstants.INVISIBLE_REQUIRED_FIELDS)) {
            try {
                getmJSONObject().put(JsonFormConstants.INVISIBLE_REQUIRED_FIELDS, invisibleRequiredFields);
            } catch (JSONException e) {
                Timber.e(e, "JsonFormActivity --> onResume");
            }
        }
        EventBus.getDefault().register(this);
    }


    protected Pair<String[], JSONObject> getRelevanceAddress(View view, boolean popup) throws JSONException {
        if (view != null) {
            String relevanceTag = (String) view.getTag(R.id.relevance);
            String widgetKey = (String) view.getTag(R.id.key);
            String stepName = ((String) view.getTag(R.id.address)).split(":")[0];
            boolean widgetDisplay = (boolean) view.getTag(R.id.extraPopup);
            if ((relevanceTag != null && relevanceTag.length() > 0) && (widgetDisplay == popup)) {
                JSONObject relevance = new JSONObject(relevanceTag);
                Iterator<String> keys = relevance.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    JSONObject curRelevance = relevance.has(curKey) ? relevance.getJSONObject(curKey) : null;

                    String[] address = getAddressFromMap(widgetKey, stepName, JsonFormConstants.RELEVANCE);
                    if (address == null) {
                        address = getAddress(view, curKey, curRelevance, JsonFormConstants.RELEVANCE);
                    }
                    return new Pair<>(address, curRelevance);
                }
            }
        }
        return null;
    }


    protected void addRelevance(View view, boolean popup, boolean isForNextStep) {
        try {
            Pair<String[], JSONObject> addressPair = getRelevanceAddress(view, popup);
            boolean comparison = true;
            if (addressPair != null) {
                String[] address = addressPair.first;
                JSONObject curRelevance = addressPair.second;
                boolean isPopup = checkPopUpValidity(address, popup);
                if (address.length > 1) {

                    Facts curValueMap = getValueFromAddress(address, isPopup);
                    try {
                        comparison = isRelevant(curValueMap, curRelevance);
                    } catch (Exception e) {
                        Timber.e(e, "JsonFormActivity --> addRelevance --> comparison");
                    }

                }

                if (isForNextStep) {
                    if (((address.length == 2 && address[0].equals(nextStep())) || (address.length == 3 && address[2].contains(nextStep()))) && comparison) {
                        setNextStepRelevant(true);
                    }
                } else {
                    if (Utils.isRunningOnUiThread()) {
                        toggleViewVisibility(view, comparison, isPopup);
                    }
                }
            }


        } catch (Exception e) {
            Timber.e(e);
        }
    }

    protected void toggleViewVisibility(View view, boolean visible, boolean popup) {
        try {
            JSONArray canvasViewIds = new JSONArray((String) view.getTag(R.id.canvas_ids));
            String addressString = (String) view.getTag(R.id.address);
            String[] address = addressString.split(":");
            JSONObject object = getObjectUsingAddress(address, popup);
            boolean enabled = visible;
            if (object != null && object.has(JsonFormConstants.READ_ONLY) &&
                    object.getBoolean(JsonFormConstants.READ_ONLY) && visible) {
                enabled = false;
            }

            view.setEnabled(enabled);
            if (view instanceof MaterialEditText || view instanceof RelativeLayout || view instanceof LinearLayout) {
                view.setFocusable(enabled);
                if (view instanceof MaterialEditText) {
                    view.setFocusableInTouchMode(enabled);
                }
            }

            updateCanvas(view, visible, canvasViewIds, addressString, object);
            setReadOnlyAndFocus(view, visible, popup);

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private void setReadOnlyAndFocus(View view, boolean visible, boolean popup) {
        try {
            String addressString = (String) view.getTag(R.id.address);
            String widgetType = (String) view.getTag(R.id.type);
            String[] address = addressString.split(":");
            JSONObject object = getObjectUsingAddress(address, popup);

            boolean enabled = visible;
            if (object != null && object.has(JsonFormConstants.READ_ONLY) &&
                    object.getBoolean(JsonFormConstants.READ_ONLY) && visible) {
                enabled = false;
            }

            view.setEnabled(enabled);
            if (StringUtils.isNotBlank(widgetType) && JsonFormConstants.NATIVE_RADIO_BUTTON.equals(widgetType) && view instanceof RadioGroup) {
                setReadOnlyRadioButtonOptions(view, enabled);
            }
            if (view instanceof MaterialEditText || view instanceof RelativeLayout || view instanceof LinearLayout) {
                view.setFocusable(enabled);
                if (view instanceof MaterialEditText) {
                    view.setFocusableInTouchMode(enabled);
                }
            }
        } catch (JSONException e) {
            Timber.e(e, "JsonFormActivity --> setReadOnlyAndFocus");
        }
    }

    /**
     * Gets the {@link AppCompatRadioButton} views on the whole {@link com.vijay.jsonwizard.widgets.NativeRadioButtonFactory} and updates the enabled status
     *
     * @param view    {@link View}
     * @param enabled {@link Boolean}
     */
    private void setReadOnlyRadioButtonOptions(View view, boolean enabled) {
        if (view != null) {
            try {
                int viewChildrenCount = ((RadioGroup) view).getChildCount();
                for (int i = 0; i < viewChildrenCount; i++) {
                    RelativeLayout radioGroupChildLayout = (RelativeLayout) ((RadioGroup) view).getChildAt(i);
                    LinearLayout linearLayout = (LinearLayout) (radioGroupChildLayout).getChildAt(0);
                    LinearLayout radioButtonMainLayout = (LinearLayout) (linearLayout).getChildAt(0);
                    AppCompatRadioButton appCompatRadioButton = (AppCompatRadioButton) (radioButtonMainLayout).getChildAt(0);
                    appCompatRadioButton.setEnabled(enabled);
                }
            } catch (ClassCastException e) {
                Timber.e(e, " --> setReadOnlyRadioButtonOptions");
            }
        }
    }

    private void checkViewConstraints(View curView, boolean popup) {
        String constraintTag = (String) curView.getTag(R.id.constraints);
        String widgetKey = (String) curView.getTag(R.id.key);
        String stepName = ((String) curView.getTag(R.id.address)).split(":")[0];
        if (constraintTag != null && constraintTag.length() > 0) {
            try {

                String errorMessage = null;
                String[] address = null;

                if (constraintTag.charAt(0) == '[') {
                    String addressString = (String) curView.getTag(R.id.address);
                    address = addressString.split(":");

                    JSONArray constraint = new JSONArray(constraintTag);
                    for (int i = 0; i < constraint.length(); i++) {
                        JSONObject curConstraint = constraint.getJSONObject(i);
                        if (address.length == 2) {
                            String value = String.valueOf(getValueFromAddress(address, popup).get(JsonFormConstants.VALUE));
                            errorMessage = enforceConstraint(value, curView, curConstraint);
                            if (errorMessage != null) break;
                        }
                    }

                } else {
                    //Rules Engine
                    JSONObject constraint = new JSONObject(constraintTag);
                    Iterator<String> keys = constraint.keys();
                    while (keys.hasNext()) {
                        String curKey = keys.next();
                        JSONObject curConstraint = constraint.getJSONObject(curKey);


                        address = getAddressFromMap(widgetKey, stepName, JsonFormConstants.CONSTRAINTS);
                        if (address == null) {
                            address = getAddress(curView, curKey, curConstraint, JsonFormConstants.CONSTRAINTS);
                        }
                        Facts curValueMap = getValueFromAddress(address, popup);
                        errorMessage = enforceConstraint(curValueMap, curConstraint);
                        if (errorMessage != null) break;
                    }
                }

                updateUiByConstraints(curView, popup, errorMessage);

            } catch (Exception e) {
                Timber.e(e, "JsonFormActivity --> checkViewConstraints");
            }
        }
    }

    private void updateUiByConstraints(View curView, boolean popup, String errorMessage) throws JSONException {
        String[] address = ((String) curView.getTag(R.id.address)).split(":");
        if (errorMessage != null) {
            if (curView instanceof MaterialEditText) {
                ((MaterialEditText) curView).setText(null);
                ((MaterialEditText) curView).setError(errorMessage);
            } else if (curView instanceof CheckBox) {
                ((CheckBox) curView).setChecked(false);
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                String checkBoxKey = (String) curView.getTag(R.id.childKey);

                JSONObject questionObject = getObjectUsingAddress(address, popup);
                for (int i = 0;
                     i < questionObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME).length(); i++) {
                    JSONObject curOption =
                            questionObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME).getJSONObject(i);
                    if (curOption.getString(JsonFormConstants.KEY).equals(checkBoxKey)) {
                        curOption.put(JsonFormConstants.VALUE, "false");
                        break;
                    }
                }
            } else if (curView.getTag(R.id.type).toString().equals(JsonFormConstants.NUMBER_SELECTOR) &&
                    !TextUtils.isEmpty(errorMessage) &&
                    (curView.getTag(R.id.previous) == null || !curView.getTag(R.id.previous).equals(errorMessage))) {

                if (!"false".equals(errorMessage)) {
                    Intent localIntent = new Intent(JsonFormConstants.INTENT_ACTION.NUMBER_SELECTOR_FACTORY);
                    localIntent.putExtra(JsonFormConstants.MAX_SELECTION_VALUE, Integer.valueOf(errorMessage));
                    localIntent.putExtra(JsonFormConstants.JSON_OBJECT_KEY, curView.getTag(R.id.key).toString());
                    localIntent.putExtra(JsonFormConstants.STEPNAME, address[0]);
                    localIntent.putExtra(JsonFormConstants.IS_POPUP, popup);
                    localBroadcastManager.sendBroadcast(localIntent);
                    curView.setTag(R.id.previous, errorMessage); //Store value to avoid re-fires
                }


            } else if (curView instanceof RadioGroup &&
                    curView.getTag(R.id.type).toString().equals(JsonFormConstants.NATIVE_RADIO_BUTTON) &&
                    !TextUtils.isEmpty(errorMessage) &&
                    (curView.getTag(R.id.previous) == null || !curView.getTag(R.id.previous).equals(errorMessage))) {

                JSONObject jsonObject = (JSONObject) curView.getTag(R.id.json_object);
                JSONObject jsonObjectNew = new JSONObject(errorMessage);
                Iterator<String> keys = jsonObjectNew.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    jsonObject.put(key, jsonObjectNew.getString(key));
                }

            } else if (curView instanceof LinearLayout) {
                LinearLayout linearLayout = (LinearLayout) curView;
                try {
                    View viewRadioGroup = linearLayout.getChildAt(0);
                    if (viewRadioGroup instanceof RadioGroup && curView.getTag(R.id.type).toString().equals(JsonFormConstants.NATIVE_RADIO_BUTTON) &&
                            !TextUtils.isEmpty(errorMessage) &&
                            (curView.getTag(R.id.previous) == null || !curView.getTag(R.id.previous).equals(errorMessage))) {
                        JSONObject jsonObject = (JSONObject) curView.getTag(R.id.json_object);
                        JSONObject jsonObjectNew = new JSONObject(errorMessage);
                        Iterator<String> keys = jsonObjectNew.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            jsonObject.put(key, jsonObjectNew.getString(key));
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    Timber.e(e);
                }
            }
        }
    }

    private Facts getValueFromAddress(String[] address, boolean popup, JSONObject valueSource) throws
            Exception {
        JSONObject object = getObjectUsingAddress(address, popup, valueSource);
        return getEntries(address, object);
    }

    private Facts getValueFromAddress(String[] address, boolean popup) throws Exception {
        JSONObject object = getObjectUsingAddress(address, popup);
        return getEntries(address, object);
    }

    private Facts getEntries(String[] address, JSONObject object) throws JSONException {
        Facts result = new Facts();
        if (object != null) {
            //reset the rules check value
            object.put(RuleConstant.IS_RULE_CHECK, false);
            if (object.has(RuleConstant.RESULT)) {
                JSONArray jsonArray = object.getJSONArray(RuleConstant.RESULT);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject formObject = jsonArray.getJSONObject(i);

                    formObject.put(RuleConstant.IS_RULE_CHECK, true);
                    formObject.put(RuleConstant.STEP, formObject.getString(RuleConstant.STEP));

                    result.asMap().putAll(getValueFromAddressCore(formObject).asMap());
                }

                result.put(RuleConstant.SELECTED_RULE, address[2]);
            } else {
                result = getValueFromAddressCore(object);
            }
        }
        return result;
    }

    protected Facts getValueFromAddressCore(JSONObject object) throws JSONException {
        Facts result = new Facts();

        if (object != null && object.has(JsonFormConstants.TYPE)) {
            switch (object.getString(JsonFormConstants.TYPE)) {
                case JsonFormConstants.CHECK_BOX:
                    result = formUtils.getCheckBoxResults(object);
                    break;
                case JsonFormConstants.NATIVE_RADIO_BUTTON:
                case JsonFormConstants.EXTENDED_RADIO_BUTTON:
                    boolean multiRelevance = object.optBoolean(JsonFormConstants.NATIVE_RADIO_BUTTON_MULTI_RELEVANCE, false);
                    result = formUtils.getRadioButtonResults(multiRelevance, object);
                    break;
                default:
                    result.put(getKey(object), getValue(object));
                    break;
            }

            if (object.has(RuleConstant.IS_RULE_CHECK) && object.getBoolean(RuleConstant.IS_RULE_CHECK) &&
                    (object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.CHECK_BOX) ||
                            (object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.NATIVE_RADIO_BUTTON) &&
                                    object.optBoolean(JsonFormConstants.NATIVE_RADIO_BUTTON_MULTI_RELEVANCE, false)))) {
                List<String> selectedValues = new ArrayList<>(result.asMap().keySet());
                result = new Facts();
                result.put(getKey(object), selectedValues);
            }
        }
        return result;
    }

    private void initComparisons() {
        if (comparisons == null) {
            functionRegex = "";
            comparisons = new HashMap<>();

            LessThanComparison lessThanComparison = new LessThanComparison();
            functionRegex += lessThanComparison.getFunctionName();
            comparisons.put(lessThanComparison.getFunctionName(), lessThanComparison);

            LessThanEqualToComparison lessThanEqualToComparison = new LessThanEqualToComparison();
            functionRegex += "|" + lessThanEqualToComparison.getFunctionName();
            comparisons.put(lessThanEqualToComparison.getFunctionName(), lessThanEqualToComparison);

            EqualToComparison equalToComparison = new EqualToComparison();
            functionRegex += "|" + equalToComparison.getFunctionName();
            comparisons.put(equalToComparison.getFunctionName(), equalToComparison);

            NotEqualToComparison notEqualToComparer = new NotEqualToComparison();
            functionRegex += "|" + notEqualToComparer.getFunctionName();
            comparisons.put(notEqualToComparer.getFunctionName(), notEqualToComparer);

            GreaterThanComparison greaterThanComparison = new GreaterThanComparison();
            functionRegex += "|" + greaterThanComparison.getFunctionName();
            comparisons.put(greaterThanComparison.getFunctionName(), greaterThanComparison);

            GreaterThanEqualToComparison greaterThanEqualToComparison = new GreaterThanEqualToComparison();
            functionRegex += "|" + greaterThanEqualToComparison.getFunctionName();
            comparisons.put(greaterThanEqualToComparison.getFunctionName(), greaterThanEqualToComparison);

            RegexComparison regexComparison = new RegexComparison();
            functionRegex += "|" + regexComparison.getFunctionName();
            comparisons.put(regexComparison.getFunctionName(), regexComparison);
        }
    }

    private boolean doComparison(String value, JSONObject comparison) throws Exception {
        String type = comparison.getString(JsonFormConstants.TYPE).toLowerCase();
        String ex = comparison.getString(JsonFormConstants.EX);

        Pattern pattern = Pattern.compile("(" + functionRegex + ")\\((.*)\\)");
        Matcher matcher = pattern.matcher(ex);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            String b = matcher.group(
                    2);//functions arguments should be two, and should either be addresses or values (enclosed using "")
            String[] args = getFunctionArgs(b, value);
            return comparisons.get(functionName).compare(args[0], type, args[1]);
        }

        return false;
    }

    private String[] getFunctionArgs(String functionArgs, String value) {
        String[] args = new String[2];
        String[] splitArgs = functionArgs.split(",");
        if (splitArgs.length == 2) {
            Pattern valueRegex = Pattern.compile("\"(.*)\"");
            for (int i = 0; i < splitArgs.length; i++) {
                String curArg = splitArgs[i].trim();

                if (".".equals(curArg)) {
                    args[i] = value;
                } else {
                    Matcher valueMatcher = valueRegex.matcher(curArg);
                    if (valueMatcher.find()) {
                        args[i] = valueMatcher.group(1);
                    } else {
                        try {
                            args[i] = String.valueOf(
                                    getValueFromAddress(curArg.split(":"), false).get(JsonFormConstants.VALUE));
                        } catch (Exception e) {
                            Timber.e(e, "JsonFormActivity --> getFunctionArgs");
                        }
                    }
                }
            }
        }

        return args;
    }

    protected void refreshMediaLogic(String key, String value, String stepName) {
        if (StringUtils.isBlank(key))
            return;
        try {
            JSONObject questionGroup = formFields.get(stepName + "_" + key);
            if (questionGroup == null) {
                Timber.d("refreshMediaLogic field %s is missing", key);
            } else if ((questionGroup.has("key") && questionGroup.has("has_media_content")) &&
                    (questionGroup.getString("key").equalsIgnoreCase(key)) &&
                    (questionGroup.getBoolean("has_media_content"))) {
                JSONArray medias = questionGroup.getJSONArray("media");
                for (int j = 0; j < medias.length(); j++) {
                    JSONObject media = medias.getJSONObject(j);
                    mediaDialog(media, value);
                }
            }
        } catch (Exception e) {
            Timber.e(e, "JsonFormActivity --> refreshMediaLogic");
        }
    }

    public void mediaDialog(JSONObject media, String value) {
        try {
            if (media.getString("media_trigger_value").equalsIgnoreCase(value)) {
                String mediatype = media.getString("media_type");
                String medialink = media.getString("media_link");
                String mediatext = media.getString("media_text");

                infoDialog(mediatype, medialink, mediatext);
            }
        } catch (Exception e) {
            Timber.e(e, "JsonFormActivity --> mediaDialog");
        }
    }

    private void infoDialog(String mediatype, String medialink, String mediatext) {
        final FancyAlertDialog.Builder builder = new FancyAlertDialog.Builder(this);
        builder.setTitle("Info");
        builder.setBackgroundColor(Color.parseColor("#208CC5"))
                .setPositiveBtnBackground(Color.parseColor("#208CC5"))  //Don't pass R
                // .color.colorvalue
                .setPositiveBtnText("OK").setAnimation(Animation.SLIDE).isCancellable(true)
                .setIcon(com.shashank.sony.fancydialoglib.R.drawable.ic_person_black_24dp, Icon.Visible);
        builder.setMessage(mediatext);
        if (mediatype.equalsIgnoreCase("image")) {
            builder.setImagetoshow(medialink);
        } else if (mediatype.equalsIgnoreCase("video")) {
            builder.setVideopath(medialink);
        }
        builder.build();
    }

    /**
     * This method checks whether a constraint has been enforced and returns an error message if not The error message should
     * be displayable to the user
     *
     * @param value      {@link String} The value to be checked
     * @param view       {@link View} The value to be checked
     * @param constraint {@link JSONObject} The constraint expression to use
     * @return An error message if constraint has not been enforced or NULL if constraint enforced
     * @throws Exception
     */
    private String enforceConstraint(String value, View view, JSONObject constraint) throws
            Exception {

        String type = constraint.getString("type").toLowerCase();
        String ex = constraint.getString(JsonFormConstants.EX);
        String errorMessage = type.equals(JsonFormConstants.NUMBER_SELECTOR) ? constraint.optString(JsonFormConstants.ERR) :
                constraint.getString(JsonFormConstants.ERR);
        Pattern pattern = Pattern.compile("(" + functionRegex + ")\\((.*)\\)");
        Matcher matcher = pattern.matcher(ex);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            String b = matcher.group(2);
            String[] args = getFunctionArgs(b, value);

            boolean viewDoesNotHaveValue = TextUtils.isEmpty(value);
            if (view instanceof CheckBox) {
                viewDoesNotHaveValue = !((CheckBox) view).isChecked();
            } else if (isNumberSelectorConstraint(view) || isDatePickerNativeRadio(view)) {
                return args.length > 1 ? args[1] : "";//clever fix to pass back the max value for number selectors

            }

            if (checkViewValues(type, functionName, args, viewDoesNotHaveValue)) return null;
        } else {
            Timber.d("Matcher didn't work with function");
        }

        return errorMessage;
    }

    private boolean checkViewValues(String type, String functionName, String[] args,
                                    boolean viewDoesNotHaveValue) {
        return viewDoesNotHaveValue || TextUtils.isEmpty(args[0]) || TextUtils.isEmpty(args[1]) ||
                comparisons.get(functionName).compare(args[0], type, args[1]);
    }

    private String enforceConstraint(Facts curValueMap, JSONObject constraint) throws Exception {
        return curValueMap.asMap().size() == 0 ? "0" : getRulesEngineFactory().getConstraint(curValueMap,
                constraint.getJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES).getString(RuleConstant.RULES_FILE));
    }

    private boolean isNumberSelectorConstraint(View view) {
        return JsonFormConstants.NUMBER_SELECTOR.equals(view.getTag(R.id.type));
    }

    private boolean isDatePickerNativeRadio(View view) {
        return JsonFormConstants.NATIVE_RADIO_BUTTON.equals(view.getTag(R.id.type));
    }

    protected JSONArray fetchFields(JSONObject parentJson, boolean popup) {
        JSONArray fields = new JSONArray();
        if (Utils.isEmptyJsonObject(parentJson)) {
            return fields;
        }
        try {
            if (parentJson.has(JsonFormConstants.SECTIONS) &&
                    parentJson.get(JsonFormConstants.SECTIONS) instanceof JSONArray) {
                JSONArray sections = parentJson.getJSONArray(JsonFormConstants.SECTIONS);
                for (int i = 0; i < sections.length(); i++) {
                    JSONObject sectionJson = sections.getJSONObject(i);
                    fields = returnFormWithSectionFields(sectionJson, popup);
                }
            } else if (parentJson.has(JsonFormConstants.FIELDS) &&
                    parentJson.get(JsonFormConstants.FIELDS) instanceof JSONArray) {
                fields = returnWithFormFields(parentJson, popup);
            }
        } catch (JSONException e) {
            Timber.e(e, "JsonFormActivity --> fetchFields");
        }

        return fields;
    }

    /**
     * Get form fields from JSON forms that have sections in the form steps. The JSONObject {@link JSONObject} argument is
     * the object after getting the section in the specified step name The popup {@link Boolean} argument is a boolean value
     * to let the function know that the form is being executed on a popup and not the main android view.
     * <p>
     * This function returns a JSONArray {@link JSONArray} of the fields contained in the section for the given step
     *
     * @param sectionJson
     * @param popup
     * @return
     * @throws JSONException
     * @author dubdabasoduba
     */
    protected JSONArray returnFormWithSectionFields(JSONObject sectionJson, boolean popup) throws
            JSONException {
        JSONArray fields = new JSONArray();
        if (sectionJson.has(JsonFormConstants.FIELDS)) {
            if (popup) {
                JSONArray jsonArray = sectionJson.getJSONArray(JsonFormConstants.FIELDS);
                for (int k = 0; k < jsonArray.length(); k++) {
                    JSONObject item = jsonArray.getJSONObject(k);
                    if (genericDialogInterface != null &&
                            item.getString(JsonFormConstants.KEY).equals(genericDialogInterface.getParentKey())) {
                        fields = formUtils.concatArray(fields, specifyFields(item));
                    }
                }
            } else {
                fields = formUtils.concatArray(fields, sectionJson.getJSONArray(JsonFormConstants.FIELDS));
            }
        }
        return fields;
    }

    /**
     * Get the form fields for the JSON forms that do not use the sections in the steps The JSONObject {@link JSONObject}
     * argument is the object after getting the step name The popup {@link boolean} argument is a boolean value to let the
     * function know that the form is being executed on a popup and not the main android view.
     * <p>
     * This function returns a JSONArray {@link JSONArray} of the fields contained in the step
     *
     * @param parentJson
     * @param popup
     * @return fields
     * @throws JSONException
     * @author dubdabasoduba
     */
    protected JSONArray returnWithFormFields(JSONObject parentJson, boolean popup) throws
            JSONException {
        JSONArray fields = new JSONArray();
        if (popup) {
            JSONArray jsonArray = parentJson.getJSONArray(JsonFormConstants.FIELDS);
            for (int k = 0; k < jsonArray.length(); k++) {
                JSONObject item = jsonArray.getJSONObject(k);
                if (genericDialogInterface != null &&
                        item.getString(JsonFormConstants.KEY).equals(genericDialogInterface.getParentKey())) {
                    fields = specifyFields(item);
                }
            }
        } else {
            fields = parentJson.getJSONArray(JsonFormConstants.FIELDS);
        }

        return fields;
    }

    protected JSONArray specifyFields(JSONObject parentJson) {
        JSONArray fields = new JSONArray();
        if (genericDialogInterface != null && genericDialogInterface.getWidgetType() != null &&
                genericDialogInterface.getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
            if (parentJson.has(JsonFormConstants.CONTENT_FORM)) {
                fields = returnFields(parentJson);
            }
        } else {
            if (parentJson.has(JsonFormConstants.HAS_EXTRA_REL)) {
                String optionKey;
                try {
                    optionKey = (String) parentJson.get(JsonFormConstants.HAS_EXTRA_REL);
                    JSONArray options = parentJson.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
                    if (options.length() > 0) {
                        for (int j = 0; j < options.length(); j++) {
                            JSONObject jsonObject = options.getJSONObject(j);
                            String objectKey = (String) jsonObject.get(JsonFormConstants.KEY);
                            if (objectKey.equals(optionKey) && jsonObject.has(JsonFormConstants.CONTENT_FORM)) {
                                fields = returnFields(jsonObject);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Timber.e(e, "JsonFormActivity --> specifyFields");
                }
            }
        }
        return fields;
    }

    private JSONArray returnFields(JSONObject jsonObject) {
        JSONArray fields = new JSONArray();
        try {
            if (getExtraFieldsWithValues() != null) {
                fields = getExtraFieldsWithValues();
            } else {
                String formLocation = jsonObject.has(JsonFormConstants.CONTENT_FORM_LOCATION) ? jsonObject.getString(JsonFormConstants.CONTENT_FORM_LOCATION) : "";
                fields = getSubFormFields(jsonObject.get(JsonFormConstants.CONTENT_FORM).toString(), formLocation, fields);
            }
        } catch (JSONException e) {
            Timber.e(e, "JsonFormActivity --> returnFields");
        }
        return fields;
    }

    protected JSONArray getSubFormFields(String subFormName, String subFormLocation, JSONArray
            fields) {
        JSONArray fieldArray = new JSONArray();
        JSONObject jsonObject = null;
        try {
            jsonObject = getSubForm(subFormName, subFormLocation, this, translateForm);
        } catch (Exception e) {
            Timber.e(e);
        }

        if (jsonObject != null) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(JsonFormConstants.CONTENT_FORM);
                if (jsonArray != null && jsonArray.length() > 0) {
                    fieldArray = formUtils.concatArray(fields, jsonArray);
                }
            } catch (JSONException e) {
                Timber.e(e, "JsonFormActivity --> getSubFormFields");
            }
        }

        return fieldArray;
    }

    public Form getForm() {
        return form;
    }

    private boolean isRelevant(Facts curValueMap, JSONObject curRelevance) throws Exception {
        if (curRelevance != null) {
            if (curRelevance.has(JsonFormConstants.JSON_FORM_KEY.EX_RULES)) {

                JSONObject exRulesObject = curRelevance.getJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES);

                if (exRulesObject.has(RuleConstant.RULES_FILE)) {

                    return curValueMap.asMap().size() != 0 && getRulesEngineFactory().getRelevance(curValueMap,
                            exRulesObject.getString(RuleConstant.RULES_FILE));

                } else if (exRulesObject.has(RuleConstant.RULES_DYNAMIC)) {

                    return curValueMap.asMap().size() != 0 && getRulesEngineFactory()
                            .getDynamicRelevance(curValueMap, exRulesObject.optJSONArray(RuleConstant.RULES_DYNAMIC));

                }

            } else if (curRelevance.has(JsonFormConstants.JSON_FORM_KEY.EX_CHECKBOX)) {
                JSONArray exArray = curRelevance.getJSONArray(JsonFormConstants.JSON_FORM_KEY.EX_CHECKBOX);

                for (int i = 0; i < exArray.length(); i++) {
                    ExObjectResult exObjectResult = isExObjectRelevant(curValueMap, exArray.getJSONObject(i));
                    if (exObjectResult.isRelevant()) {
                        return true;
                    } else if (!exObjectResult.isRelevant() && exObjectResult.isFinal()) {
                        return false;
                    }

                }
                return false;
            } else {
                String curValue = String.valueOf(curValueMap.get(JsonFormConstants.VALUE));
                return doComparison(curValue != null ? curValue : "", curRelevance);
            }
        }
        return false;
    }

    private ExObjectResult isExObjectRelevant(Facts curValueMap, JSONObject object) throws
            Exception {
        if (object.has(JsonFormConstants.JSON_FORM_KEY.NOT)) {
            JSONArray orArray = object.getJSONArray(JsonFormConstants.JSON_FORM_KEY.NOT);

            for (int i = 0; i < orArray.length(); i++) {
                String curValue = curValueMap.get(orArray.getString(i));

                if (curValue != null && !Boolean.valueOf(curValue)) {
                    return new ExObjectResult(true, false);
                } else {
                    return new ExObjectResult(false, true);
                }
            }
        }

        if (object.has(JsonFormConstants.JSON_FORM_KEY.OR)) {
            JSONArray orArray = object.getJSONArray(JsonFormConstants.JSON_FORM_KEY.OR);

            for (int i = 0; i < orArray.length(); i++) {
                String curValue = curValueMap.get(orArray.getString(i));
                if (curValue != null && Boolean.valueOf(curValue)) {
                    return new ExObjectResult(true, true);
                }

            }

        }

        if (object.has(JsonFormConstants.JSON_FORM_KEY.AND)) {
            JSONArray andArray = object.getJSONArray(JsonFormConstants.JSON_FORM_KEY.AND);

            for (int i = 0; i < andArray.length(); i++) {
                String curValue = curValueMap.get(andArray.getString(i));
                if (curValue != null && !Boolean.valueOf(curValue)) {
                    return new ExObjectResult(false, false);
                }
            }
            return new ExObjectResult(true, false);

        }

        return new ExObjectResult(false, false);
    }

    private List<String> getRules(String filename, String fieldKey, boolean readAllRules) {
        List<String> rules = ruleKeys.get(filename + ":" + fieldKey);

        if (rules == null) {
            try {
                Yaml yaml = new Yaml();
                BufferedReader inputStreamReader = getRules(getApplicationContext(), getRulesEngineFactory().getRulesFolderPath() + filename);
                Iterable<Object> ruleObjects = yaml.loadAll(inputStreamReader);

                for (Object object : ruleObjects) {

                    Map<String, Object> map = ((Map<String, Object>) object);

                    String name = map.get(RuleConstant.NAME).toString();
                    if (ruleKeys.containsKey(filename + ":" + name)) {
                        continue;
                    }

                    List<String> actions = new ArrayList<>();

                    StringBuilder conditionString = new StringBuilder();
                    conditionString.append(map.get(RuleConstant.CONDITION).toString());

                    List<String> fields = (List<String>) map.get(RuleConstant.ACTIONS);
                    if (fields != null) {
                        for (String field : fields) {
                            if (field.trim().startsWith(RuleConstant.CALCULATION) ||
                                    field.trim().startsWith(RuleConstant.CONSTRAINT)) {
                                conditionString.append(" " + field);
                            }
                        }

                    }

                    actions.addAll(getConditionKeys(conditionString.toString()));
                    ruleKeys.put(filename + ":" + name, actions);

                    if (!readAllRules && name.equals(fieldKey)) {
                        break;
                    }
                }

            } catch (Exception e) {
                Timber.e(e, "JsonFormActivity --> getRules");
            }

            return ruleKeys.get(filename + ":" + fieldKey);
        } else {
            return rules;
        }
    }

    private List<String> getConditionKeys(String condition) {
        return Utils.getConditionKeys(condition);
    }

    private void updateUiByCalculation(@NonNull String calculationValue, final View view) {
        String calculation = calculationValue;
        if (view instanceof CheckBox) {
            //For now were only handling checkbox titles only
            TextView checkboxLabel = ((View) view.getParent().getParent()).findViewById(R.id.label_text);
            if (checkboxLabel != null) {
                checkboxLabel.setText(getRenderText(calculation, checkboxLabel.getTag(R.id.original_text).toString(), false));
            }

        } else if (view instanceof TextableView) {
            TextableView textView = ((TextableView) view);
            if (!TextUtils.isEmpty(calculation)) {
                CharSequence spanned = calculation.charAt(0) == '{' ? getRenderText(calculation, textView.getTag(R.id.original_text).toString(), true) :
                        (textView.getTag(R.id.original_text) != null && "0".equals(calculation)) ? textView.getTag(R.id.original_text).toString() : calculation;
                textView.setText(spanned);
            }
        } else if (view instanceof EditText) {
            String type = (String) view.getTag(R.id.type);
            if (JsonFormConstants.HIDDEN.equals(type) && TextUtils.isEmpty(calculation)) {
                calculation = "0";
            }

            if (!TextUtils.isEmpty(calculation)) {
                final String finalCalculation = calculation;
                getAppExecutors().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        ((EditText) view).setText(finalCalculation);
                    }
                });
            }

        } else if (view instanceof RadioGroup) {
            setRadioButtonCalculation((RadioGroup) view, calculation);

        } else if (view instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) view;
            String type = (String) linearLayout.getTag(R.id.type);
            if (JsonFormConstants.NUMBER_SELECTOR.equals(type)) {
                setNumberSelectorCalculation(calculation, linearLayout);
            }
            try {
                View viewRadioGroup = linearLayout.getChildAt(0);
                if (viewRadioGroup instanceof RadioGroup) {
                    setRadioButtonCalculation((RadioGroup) viewRadioGroup, calculation);
                }
            } catch (IndexOutOfBoundsException e) {
                Timber.e(e);
            }
        } else {
            ((TextView) view).setText(calculation);
        }

    }

    private void updateCalculation(Facts valueMap, View view, String[] address, boolean isForNextStep) {
        String calculation;
        try {
            if (address[0].equals(RuleConstant.RULES_DYNAMIC)) {
                calculation = getRulesEngineFactory().getDynamicCalculation(valueMap, new JSONArray(address[1]));
            } else {
                calculation = getRulesEngineFactory().getCalculation(valueMap, address[1]);
            }

            if (!isForNextStep) {
                updateUiByCalculation(calculation, view);
            }

        } catch (Exception e) {
            Timber.e(e, "calling updateCalculation on Non TextView or Text View decendant");
        }

    }

    private void setRadioButtonCalculation(final RadioGroup view, final String calculation) {
        int count = view.getChildCount();
        for (int i = 0; i < count; i++) {
            final int childPosition = i;
            getAppExecutors().mainThread().execute(new Runnable() {
                @Override
                public void run() {
                    addRadioButtonCalculation(calculation, view, childPosition);
                }
            });
        }
    }

    private void addRadioButtonCalculation(String calculation, RadioGroup view,
                                           int childPosition) {
        if (!TextUtils.isEmpty(calculation)) {
            RelativeLayout radioButtonLayout = (RelativeLayout) view.getChildAt(childPosition);
            int radioButtonViewId = (int) radioButtonLayout.getTag(R.id.native_radio_button_view_id);
            RadioButton radioButton = radioButtonLayout.findViewById(radioButtonViewId);
            boolean showExtraInfo = (boolean) radioButton.getTag(R.id.native_radio_button_extra_info);
            String radioButtonKey = (String) radioButton.getTag(R.id.childKey);

            if (!TextUtils.isEmpty(radioButtonKey) && calculation.equals(radioButtonKey)) {
                radioButton.setChecked(true);
                radioButton.performClick();
            }

            if (showExtraInfo) {
                CustomTextView renderView = view.getChildAt(childPosition).findViewById(R.id.extraInfoTextView);

                if (renderView.getTag(R.id.original_text) == null) {
                    renderView.setTag(R.id.original_text, renderView.getText());
                }


                if (!TextUtils.isEmpty(calculation)) {
                    renderView.setText(calculation.charAt(0) == '{' ? getRenderText(calculation, renderView.getTag(R.id.original_text).toString(), false) : calculation);
                }

                renderView.setVisibility(renderView.getText().toString().contains("{") ||
                        renderView.getText().toString().equals("0") ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void setNumberSelectorCalculation(String calculation, LinearLayout linearLayout) {
        if (!TextUtils.isEmpty(calculation)) {
            int childCount = linearLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (linearLayout.getChildAt(i) instanceof TextView) {
                    TextView textView = (TextView) linearLayout.getChildAt(i);
                    String text = textView.getText().toString();
                    CommonListener commonListener =
                            (CommonListener) textView.getTag(R.id.number_selector_listener);

                    String selectedNumber = "";
                    if (selectedTextView != null) {
                        selectedNumber = selectedTextView.getText().toString();
                    }

                    if (i + 1 == childCount) {
                        String[] splitValue = text.split("");
                        if (splitValue.length > 2) {
                            String value = splitValue[1];

                            if (Integer.valueOf(calculation) > Integer.valueOf(value)) {
                                if (!calculation.equals(selectedNumber)) {
                                    textView.setText(calculation);
                                    textView.setOnClickListener(commonListener);
                                    textView.performClick();
                                }
                            } else {
                                selectNumber(calculation, textView, value, commonListener);
                            }
                        }
                    } else {
                        selectNumber(calculation, textView, text, commonListener);
                    }
                }
            }
        }
    }

    private void selectNumber(String calculation, TextView textView, String
            text, CommonListener commonListener) {
        if (calculation.equals(text) && !textView.equals(selectedTextView)) {
            selectedTextView = textView;
            textView.setOnClickListener(commonListener);
            textView.performClick();
        }
    }

    private CharSequence getRenderText(String calculation, String textTemplate, boolean makeBold) {
        Map<String, Object> valueMap = new Gson().fromJson(calculation, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        return stringFormat(textTemplate, valueMap, makeBold);
    }

    public Spanned stringFormat(String string, Map<String, Object> valueMap, boolean makeBold) {
        String resString = string;
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String templateValue = getTemplateValue(entry.getValue());
            if (makeBold) {
                templateValue = "<b>" + getTemplateValue(entry.getValue()) + "</b>";
            }
            resString = resString.replace("{" + entry.getKey() + "}", templateValue);
        }

        return Html.fromHtml(resString);
    }

    protected Object getValue(JSONObject object) throws JSONException {
        Object value;

        if (object.has(JsonFormConstants.VALUE)) {
            value = object.opt(JsonFormConstants.VALUE);

            if (isNumberWidget(object)) {
                value = TextUtils.isEmpty(object.optString(JsonFormConstants.VALUE)) ? 0 : processNumberValues(object.optString(JsonFormConstants.VALUE));
            } else if (value != null && !TextUtils.isEmpty(object.getString(JsonFormConstants.VALUE)) && canHaveNumber(object)) {
                value = processNumberValues(value);
            }

        } else {
            value = isNumberWidget(object) ? 0 : "";
        }

        return value;
    }

    protected Object processNumberValues(Object object) {
        Object jsonObject = object;
        try {
            if (jsonObject.toString().contains(".")) {
                jsonObject = String.valueOf((float) Math.round(Float.valueOf(jsonObject.toString()) * 100) / 100);
            } else {
                jsonObject = Integer.valueOf(jsonObject.toString());
            }
        } catch (NumberFormatException e) {
            //Log.e(TAG, "Error trying to convert " + object + " to a number ", e);
        }
        return jsonObject;
    }

    protected String getKey(JSONObject object) throws JSONException {
        return object.has(RuleConstant.IS_RULE_CHECK) && object.getBoolean(RuleConstant.IS_RULE_CHECK) ?
                object.get(RuleConstant.STEP) + "_" + object.get(JsonFormConstants.KEY) : JsonFormConstants.VALUE;
    }

    private void clearHiddenViewsValues(JSONObject object, String addressString) {
        if (object != null) {
            String objectKey = addressString.replace(":", "_");
            formValuesCacheMap.remove(objectKey);
            formValuesCacheMap.put(objectKey, "");
            if (object.has(JsonFormConstants.VALUE)) {
                object.remove(JsonFormConstants.VALUE);
            }
        }
    }

    private void updateCanvas(View view, boolean visible, JSONArray canvasViewIds, String
            addressString, JSONObject object)
            throws JSONException {
        for (int i = 0; i < canvasViewIds.length(); i++) {
            int curId = canvasViewIds.getInt(i);
            View curCanvasView = view.getRootView().findViewById(curId);

            if (curCanvasView == null) {
                continue;
            }

            if (visible) {
                curCanvasView.setEnabled(true);
                curCanvasView.setVisibility(View.VISIBLE);

                if (curCanvasView instanceof RelativeLayout || view instanceof LinearLayout) {
                    curCanvasView.setFocusable(true);
                }
                if (view instanceof EditText) {
                    view.setFocusable(true);
                }

                curCanvasView.invalidate();
            } else {
                if (!JsonFormConstants.REPEATING_GROUP.contains(object.optString(JsonFormConstants.TYPE))) {
                    clearHiddenViewsValues(object, addressString);
                }
                curCanvasView.setEnabled(false);
                curCanvasView.setVisibility(View.GONE);
                refreshViews(curCanvasView);
            }

            curCanvasView.setTag(R.id.relevance_decided, visible);

            if (object != null) {
                object.put(JsonFormConstants.IS_VISIBLE, visible);
                //Only keep track of required fields that are invisible
                if (object.has(JsonFormConstants.V_REQUIRED) && object.getJSONObject(JsonFormConstants.V_REQUIRED)
                        .getBoolean(JsonFormConstants.VALUE)) {
                    trackInvisibleFields(object, visible);
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void trackInvisibleFields(final JSONObject object, final boolean visible) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    synchronized (invisibleRequiredFields) {
                        if (visible) {
                            invisibleRequiredFields.remove(object.getString(JsonFormConstants.KEY));
                        } else {
                            invisibleRequiredFields.add(object.getString(JsonFormConstants.KEY));
                        }
                        getmJSONObject().put(JsonFormConstants.INVISIBLE_REQUIRED_FIELDS, invisibleRequiredFields);
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }
                return null;
            }
        }.execute();
    }

    private void refreshViews(View childElement) {
        if (childElement instanceof ViewGroup) {
            childElement.setFocusable(true);
            ViewGroup group = (ViewGroup) childElement;
            refreshNumberSelector(group);
            for (int id = 0; id < group.getChildCount(); id++) {
                View child = group.getChildAt(id);
                if (child instanceof CheckBox) {
                    ((CheckBox) child).setChecked(false);
                } else if (child instanceof RadioButton) {
                    ((RadioButton) child).setChecked(false);
                } else if (child instanceof EditText) {
                    EditText editText = (EditText) child;
                    if (!TextUtils.isEmpty(editText.getText().toString())) {
                        editText.setText("");
                    }
                } else if (child instanceof MaterialSpinner) {
                    MaterialSpinner spinner = (MaterialSpinner) child;
                    spinner.setSelected(false);
                } else if (child instanceof CustomTextView) {
                    resetSelectedNumberBackground(child);

                } else if (child instanceof TextView && child.getId() == R.id.duration) {
                    // clear duration for custom date picker
                    ((TextView) child).setText("");
                }
                refreshViews(group.getChildAt(id));
            }
        }
    }

    private void refreshNumberSelector(View group) {
        //reset value for number selector linear layout
        if (group instanceof LinearLayout) {
            LinearLayout numSelectorLayout = (LinearLayout) group;
            if (numSelectorLayout.getTag(R.id.is_number_selector_linear_layout) != null &&
                    Boolean.TRUE.equals(numSelectorLayout.getTag(R.id.is_number_selector_linear_layout))) {
                numSelectorLayout.setTag(R.id.selected_number_value, null);
            }
        }
    }

    /**
     * Resets the background of the selected text in number selector
     *
     * @param child Selected textview
     */
    private void resetSelectedNumberBackground(View child) {
        Drawable background = child.getBackground();
        if (background instanceof ColorDrawable) {
            int color = ((ColorDrawable) background).getColor();
            if (color == child.getContext().getResources().getColor(R.color.native_number_selector_selected)) {
                child.setBackgroundColor(child.getContext().getResources().getColor(R.color.native_number_selector));
            }
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background)
                    .setColor(child.getContext().getResources().getColor(R.color.native_number_selector));
            child.setBackground(background);
        }
        ((CustomTextView) child).setTextColor(child.getContext().getResources().getColor(R.color.primary_text));
    }

    public JSONArray getExtraFieldsWithValues() {
        return extraFieldsWithValues;
    }

    public void setExtraFieldsWithValues(JSONArray extraFieldsWithValues) {
        this.extraFieldsWithValues = extraFieldsWithValues;
    }

    @Override
    protected void onStart() {
        super.onStart();
        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            lifeCycleListener.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            lifeCycleListener.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            lifeCycleListener.onDestroy();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(JSON_STATE, mJSONObject.toString());
        outState.putSerializable(FORM_STATE, form);
        for (LifeCycleListener lifeCycleListener : lifeCycleListeners) {
            lifeCycleListener.onSaveInstanceState(outState);
        }
    }

    private String getTemplateValue(Object object) {
        StringBuilder result = new StringBuilder();
        if (object instanceof List) {
            List<String> valueList = (List<String>) object;
            for (int i = 0; i < valueList.size(); i++) {
                result.append(valueList.get(i));
                if (i != (valueList.size() - 1)) {
                    result.append(", ");
                }
            }
        } else {
            result.append(
                    object.toString().contains(".0") ? object.toString().substring(0, object.toString().indexOf(".0")) :
                            object.toString()); //Fix automatic conversion float bug
        }

        return result.toString();
    }

    public LocalBroadcastManager getLocalBroadcastManager() {
        return localBroadcastManager;
    }

    private void cacheFormMapValues(String stepName, String parentKey, String childKey, String
            value) {
        formValuesCacheMap.put(stepName + "_" + (parentKey != null ? parentKey + "_" : "") + childKey, value);
    }

    private boolean invokeRefreshLogic(String stepName, String parentKey, String
            childKey, String value) {
        String oldValue = formValuesCacheMap.get(stepName + "_" + (parentKey != null ? parentKey + "_" : "") + childKey);

        return !value.equals(oldValue);

    }

    protected boolean canHaveNumber(JSONObject object) throws JSONException {
        return isNumberWidget(object) || object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.HIDDEN) ||
                object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.SPINNER);
    }

    protected boolean isNumberWidget(JSONObject object) throws JSONException {
        return object.has(JsonFormConstants.EDIT_TYPE) &&
                object.getString(JsonFormConstants.EDIT_TYPE).equals(JsonFormConstants.EDIT_TEXT_TYPE.NUMBER) ||
                object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.NUMBER_SELECTOR);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void refreshExpansionPanel(RefreshExpansionPanelEvent refreshExpansionPanelEvent) {
        if (refreshExpansionPanelEvent != null) {
            try {
                final List<String> values = getExpansionPanelValues(refreshExpansionPanelEvent);
                final LinearLayout linearLayout = refreshExpansionPanelEvent.getLinearLayout();
                getAppExecutors().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        utils.enableExpansionPanelViews(linearLayout);
                    }
                });

                RelativeLayout layoutHeader = (RelativeLayout) linearLayout.getChildAt(0);
                final ImageView status = layoutHeader.findViewById(R.id.statusImageView);

                getAppExecutors().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            formUtils.updateExpansionPanelRecyclerView(values, status, getApplicationContext());
                        } catch (JSONException e) {
                            Timber.e(e);
                        }

                    }
                });

                final LinearLayout contentLayout = (LinearLayout) linearLayout.getChildAt(1);
                final LinearLayout mainContentView = contentLayout.findViewById(R.id.contentView);

                getAppExecutors().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        formUtils.addValuesDisplay(values, mainContentView, getApplicationContext());
                    }
                });

                final LinearLayout buttonLayout = contentLayout.findViewById(R.id.accordion_bottom_navigation);
                final Button undoButton = buttonLayout.findViewById(R.id.undo_button);
                getAppExecutors().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (values.size() > 0) {
                            undoButton.setVisibility(View.VISIBLE);
                            contentLayout.setVisibility(View.VISIBLE);
                            buttonLayout.setVisibility(View.VISIBLE);
                        } else {
                            undoButton.setVisibility(View.GONE);
                            contentLayout.setVisibility(View.GONE);
                            buttonLayout.setVisibility(View.GONE);
                            status.setImageDrawable(JsonFormActivity.this.getResources().getDrawable(R.drawable.icon_task_256));
                        }
                    }
                });


            } catch (JSONException e) {
                Timber.e(e, "JsonFormActivity --> refreshExpansionPanel");
            }
        }

    }

    /**
     * Get the expansion panel values from the Refresh Expansion panel event {@link RefreshExpansionPanelEvent}
     *
     * @param refreshExpansionPanelEvent {@link RefreshExpansionPanelEvent}
     * @return values {@link List<String>}
     * @throws JSONException
     */
    private List<String> getExpansionPanelValues(RefreshExpansionPanelEvent
                                                         refreshExpansionPanelEvent) throws JSONException {
        List<String> values;
        if (refreshExpansionPanelEvent.getValues() != null) {
            values = utils.createExpansionPanelChildren(refreshExpansionPanelEvent.getValues());
        } else {
            values = new ArrayList<>();
        }
        return values;
    }

    @Override
    public AppExecutors getAppExecutors() {
        return appExecutors;
    }

    @Override
    public Map<String, Boolean> stepSkipLogicPresenceMap() {
        return stepSkipLogicPresenceMap;
    }

    @Override
    public boolean isNextStepRelevant() {
        return isNextStepRelevant;
    }

    @Override
    public String nextStep() {
        return nextStep;
    }

    @Override
    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    public void setNextStepRelevant(boolean nextStepRelevant) {
        isNextStepRelevant = nextStepRelevant;
    }

    @Override
    public Map<String, JSONObject> getFormFieldsMap() {
        return formFields;
    }

}