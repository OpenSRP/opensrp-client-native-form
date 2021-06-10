package com.vijay.jsonwizard.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.TimeUtils;

import com.google.android.material.snackbar.Snackbar;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.CompoundButton;
import com.vijay.jsonwizard.customviews.ExpansionPanelGenericPopupDialog;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.event.BaseEvent;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.KEY;
import static com.vijay.jsonwizard.constants.JsonFormConstants.OPENMRS_ENTITY;
import static com.vijay.jsonwizard.constants.JsonFormConstants.OPENMRS_ENTITY_ID;
import static com.vijay.jsonwizard.constants.JsonFormConstants.OPENMRS_ENTITY_PARENT;
import static com.vijay.jsonwizard.constants.JsonFormConstants.TEXT;
import static com.vijay.jsonwizard.constants.JsonFormConstants.TYPE;
import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;
import static com.vijay.jsonwizard.utils.NativeFormLangUtils.getTranslatedString;
import static com.vijay.jsonwizard.widgets.RepeatingGroupFactory.REFERENCE_EDIT_TEXT_HINT;

public class Utils {
    public final static List<String> PREFICES_OF_INTEREST = Arrays.asList(RuleConstant.PREFIX.GLOBAL, RuleConstant.STEP);
    public final static Set<Character> JAVA_OPERATORS = new HashSet<>(
            Arrays.asList('(', '!', ',', '?', '+', '-', '*', '/', '%', '+', '-', '.', '^', ')', '<', '>', '=', '{', '}', ':',
                    ';', '[', ']'));
    private static ProgressDialog progressDialog;

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public static Date getDateFromString(String dtStart) {
        if (StringUtils.isNotBlank(dtStart) && !"0".equals(dtStart)) {
            try {
                return LocalDate.fromDateFields(DatePickerFactory.DATE_FORMAT.parse(dtStart)).toDate();
            } catch (Exception e) {
                Timber.e(e, " --> getDateFromString");
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getStringFromDate(Date date) {
        try {
            return DatePickerFactory.DATE_FORMAT.format(date);
        } catch (Exception e) {
            Timber.e(e, " --> getStringFromDate");
            return null;
        }
    }

    public static String reverseDateString(String str, String delimiter) {
        String[] strr = str.split(delimiter);
        return strr[2] + "-" + strr[1] + "-" + strr[0];
    }

    public static String getDateFormattedForCalculation(String date, String datePickerDisplayFormat) {
        if (StringUtils.isNotBlank(datePickerDisplayFormat)) {
            return formatDateToPattern(date, datePickerDisplayFormat, DatePickerFactory.DATE_FORMAT.toPattern());
        } else
            return date;
    }

    public static String getDuration(String date) {
        return getDuration(date, null);
    }

    public static String getDuration(String date, String endDate) {
        if (!TextUtils.isEmpty(date)) {
            Calendar calendar = FormUtils.getDate(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Calendar now = Calendar.getInstance();
            if (endDate != null) {
                try {
                    now = FormUtils.getDate(endDate);
                } catch (Exception e) {
                    Timber.e(e, " --> getDuration");
                }
            }
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            long timeDiff = Math.abs(now.getTimeInMillis() - calendar.getTimeInMillis());
            StringBuilder builder = new StringBuilder();
            TimeUtils.formatDuration(timeDiff, builder);
            String duration = "";
            if (timeDiff >= 0 && timeDiff <= TimeUnit.MILLISECONDS.convert(13, TimeUnit.DAYS)) {
                // Represent in days
                long days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
                duration = days + "d";
            } else if (timeDiff > TimeUnit.MILLISECONDS.convert(13, TimeUnit.DAYS) &&
                    timeDiff <= TimeUnit.MILLISECONDS.convert(97, TimeUnit.DAYS)) {
                // Represent in weeks and days
                int weeks = (int) Math.floor((float) timeDiff / TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));
                int days = (int) Math.floor((float) (timeDiff - TimeUnit.MILLISECONDS.convert(weeks * 7, TimeUnit.DAYS)) /
                        TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));

                if (days >= 7) {
                    days = 0;
                    weeks++;
                }

                duration = weeks + "w";
                if (days > 0) {
                    duration += " " + days + "d";
                }
            } else if (timeDiff > TimeUnit.MILLISECONDS.convert(97, TimeUnit.DAYS) &&
                    timeDiff <= TimeUnit.MILLISECONDS.convert(363, TimeUnit.DAYS)) {
                // Represent in months and weeks
                int months = (int) Math.floor((float) timeDiff / TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
                int weeks = (int) Math.floor((float) (timeDiff - TimeUnit.MILLISECONDS.convert(months * 30, TimeUnit.DAYS)) /
                        TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));

                if (weeks >= 4) {
                    weeks = 0;
                    months++;
                }

                if (months < 12) {
                    duration = months + "m";
                    if (weeks > 0 && months < 12) {
                        duration += " " + weeks + "w";
                    }
                } else if (months >= 12) {
                    duration = "1y";
                }
            } else {
                // Represent in years and months
                int years = (int) Math.floor((float) timeDiff / TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS));
                int months = (int) Math
                        .floor((float) (timeDiff - TimeUnit.MILLISECONDS.convert(years * 365, TimeUnit.DAYS)) /
                                TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));

                if (months >= 12) {
                    months = 0;
                    years++;
                }

                duration = years + "y";
                if (months > 0) {
                    duration += " " + months + "m";
                }
            }

            return duration;
        }
        return null;
    }

    public static void showProgressDialog(@StringRes int title, @StringRes int message, Context context) {
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(context.getString(title));
        progressDialog.setMessage(context.getString(message));
        progressDialog.show();
    }

    public static void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public static int pixelToDp(int dpValue, Context context) {
        float dpRatio = context.getResources().getDisplayMetrics().density;
        float pixelForDp = dpValue * dpRatio;

        return (int) pixelForDp;
    }

    public static void postEvent(BaseEvent event) {
        EventBus.getDefault().post(event);
    }

    public static JSONObject getJsonObjectFromJsonArray(String key, JSONArray jsonArray) {
        JSONObject jsonObject = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tempJsonObject = jsonArray.optJSONObject(i);
            if (tempJsonObject != null && tempJsonObject.has(key)) {
                jsonObject = tempJsonObject;
                break;
            }
        }
        return jsonObject;
    }

    /**
     * Get the actual radio buttons on the parent view given
     *
     * @param parent {@link ViewGroup}
     * @return radioButtonList
     */
    public static List<RadioButton> getRadioButtons(ViewGroup parent) {
        List<RadioButton> radioButtonList = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof RadioButton) {
                radioButtonList.add((RadioButton) view);
            } else if (view instanceof ViewGroup) {
                List<RadioButton> nestedRadios = getRadioButtons((ViewGroup) view);
                radioButtonList.addAll(nestedRadios);
            }
        }
        return radioButtonList;
    }

    /**
     * Resets the radio buttons specify text in another option is selected
     *
     * @param button {@link CompoundButton}
     * @author kitoto
     */
    public static void resetRadioButtonsSpecifyText(RadioButton button) throws JSONException {
        CustomTextView specifyText = (CustomTextView) button.getTag(R.id.specify_textview);
        CustomTextView reasonsText = (CustomTextView) button.getTag(R.id.specify_reasons_textview);
        CustomTextView extraInfoTextView = (CustomTextView) button.getTag(R.id.specify_extra_info_textview);
        JSONObject optionsJson = (JSONObject) button.getTag(R.id.option_json_object);
        String radioButtonText = optionsJson.optString(TEXT);
        button.setText(radioButtonText);

        if (specifyText != null && optionsJson.has(JsonFormConstants.CONTENT_INFO)) {
            String specifyInfo = optionsJson.optString(JsonFormConstants.CONTENT_INFO);
            String newText = "(" + specifyInfo + ")";
            specifyText.setText(newText);
            optionsJson.put(JsonFormConstants.SECONDARY_VALUE, "");
        }

        if (reasonsText != null) {
            LinearLayout reasonTextViewParent = (LinearLayout) reasonsText.getParent();
            LinearLayout radioButtonParent = (LinearLayout) button.getParent().getParent();
            if (reasonTextViewParent.equals(radioButtonParent)) {
                reasonsText.setVisibility(View.GONE);
            }
        }
        if (extraInfoTextView != null) {
            extraInfoTextView.setVisibility(View.VISIBLE);
        }

    }

    private static String cleanConditionString(String conditionStringRaw) {
        String conditionString = conditionStringRaw;

        for (String token : PREFICES_OF_INTEREST) {

            conditionString = conditionString.replaceAll(token, " " + token);
        }

        return conditionString.replaceAll("  ", " ");
    }

    public static void buildRulesWithUniqueId(JSONObject element, String uniqueId, String ruleType,
                                              Context context, Map<String, List<Map<String, Object>>> rulesFileMap, String stepName) throws JSONException {
        JSONObject rules = element.optJSONObject(ruleType);
        if (rules != null) {
            if (rules.has(RuleConstant.RULES_ENGINE) && context != null) {
                JSONObject jsonRulesEngineObject = rules.optJSONObject(RuleConstant.RULES_ENGINE);
                JSONObject jsonExRules = jsonRulesEngineObject.optJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES);
                String fileName = JsonFormConstants.RULE + jsonExRules.optString(RuleConstant.RULES_DYNAMIC);

                if (!rulesFileMap.containsKey(fileName)) {
                    Iterable<Object> objectIterable = readYamlFile(fileName, context);
                    List<Map<String, Object>> arrayList = new ArrayList<>();
                    if (objectIterable != null) {
                        while (objectIterable.iterator().hasNext()) {
                            Map<String, Object> yamlRulesMap = (Map<String, Object>) objectIterable.iterator().next();
                            if (yamlRulesMap != null) {
                                arrayList.add(yamlRulesMap);
                            }
                        }
                    }
                    rulesFileMap.put(fileName, arrayList);
                }

                List<Map<String, Object>> mapArrayList = rulesFileMap.get(fileName);

                JSONArray jsonArrayRules = new JSONArray();
                JSONObject keyJsonObject = new JSONObject();
                keyJsonObject.put(KEY, ruleType + "/" + uniqueId);
                jsonArrayRules.put(keyJsonObject);
                for (Map<String, Object> map : mapArrayList) {
                    JSONObject jsonRulesDynamicObject = new JSONObject();
                    String strCondition = (String) map.get(RuleConstant.CONDITION);
                    List<String> conditionKeys = getConditionKeys(strCondition);

                    for (String conditionKey : conditionKeys) {
                        if (conditionKey.startsWith(stepName)) {
                            strCondition = strCondition.replace(conditionKey, conditionKey + "_" + uniqueId);
                        }
                    }

                    String action = ((ArrayList<String>) map.get(RuleConstant.ACTIONS)).get(0);
                    List<String> actionKeys = getConditionKeys(action);
                    String updatedAction = action;
                    for (String actionKey : actionKeys) {
                        if (actionKey.startsWith(stepName)) {
                            updatedAction = action.replace(actionKey, actionKey + "_" + uniqueId);
                        }
                    }

                    jsonRulesDynamicObject.put(RuleConstant.NAME, String.valueOf(map.get(RuleConstant.NAME)).concat("_").concat(uniqueId));
                    jsonRulesDynamicObject.put(RuleConstant.DESCRIPTION, String.valueOf(map.get(RuleConstant.DESCRIPTION)).concat("_").concat(uniqueId));
                    jsonRulesDynamicObject.put(RuleConstant.PRIORITY, map.get(RuleConstant.PRIORITY));
                    jsonRulesDynamicObject.put(RuleConstant.ACTIONS, updatedAction);
                    jsonRulesDynamicObject.put(RuleConstant.CONDITION, String.valueOf(strCondition));
                    jsonArrayRules.put(jsonRulesDynamicObject);
                }

                jsonExRules.put(RuleConstant.RULES_DYNAMIC, jsonArrayRules);

            } else {
                String currKey = rules.keys().next();
                JSONObject rulesObj = rules.getJSONObject(currKey);
                String newKey = currKey + "_" + uniqueId;
                rules.remove(currKey);
                rules.put(newKey, rulesObj);
            }
        }

    }

    public static NativeFormsProperties getProperties(Context context) {
        NativeFormsProperties properties = new NativeFormsProperties();

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(JsonFormConstants.APP_PROPERTIES_FILE);
            properties.load(inputStream);
        } catch (Exception exception) {
            Timber.e(exception);
        }

        return properties;
    }

    public static void removeDeletedViewsFromJsonForm(Collection<View> viewCollection, ArrayList<String> removeThisFields) {
        Iterator<View> viewIterator = viewCollection.iterator();
        while (viewIterator.hasNext()) {
            View view = viewIterator.next();
            String key = (String) view.getTag(R.id.key);
            if (removeThisFields.contains(key)) {
                viewIterator.remove();
            }
        }
    }

    public static void updateSubFormFields(JSONObject subForm, Form form) {
        for (int i = 0; i < subForm.optJSONArray(JsonFormConstants.CONTENT_FORM).length(); i++) {
            handleFieldBehaviour(subForm.optJSONArray(JsonFormConstants.CONTENT_FORM).optJSONObject(i), form);
        }
    }

    public static void handleFieldBehaviour(JSONObject fieldObject, Form form) {
        String key = fieldObject.optString(KEY);

        if (form != null && form.getHiddenFields() != null && form.getHiddenFields().contains(key)) {
            makeFieldHidden(fieldObject);
        }

        if (form != null && form.getDisabledFields() != null && form.getDisabledFields().contains(key)) {
            makeFieldDisabled(fieldObject);
        }

    }

    /**
     * Used to change type of field to hidden and put attribute disabled as true
     *
     * @param fieldObject
     */
    public static void makeFieldDisabled(JSONObject fieldObject) {
        try {
            makeFieldHidden(fieldObject);
            fieldObject.put(JsonFormConstants.DISABLED, true);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    /**
     * Used to change type of field to hidden
     *
     * @param fieldObject
     */
    public static void makeFieldHidden(JSONObject fieldObject) {
        try {
            fieldObject.put(TYPE, JsonFormConstants.HIDDEN);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public static void removeDeletedInvalidFields(String fieldKeyPrefix, Map<String, ValidationStatus> invalidFields, ArrayList<String> fieldsToRemove) {
        for (String fieldToRemove : fieldsToRemove) {
            invalidFields.remove(fieldKeyPrefix + fieldToRemove);
        }
    }

    public static String getFieldKeyPrefix(String stepName, String stepTitle) {
        return stepName + "#" + stepTitle + ":";
    }

    public List<String> createExpansionPanelChildren(JSONArray jsonArray) throws JSONException {
        List<String> stringList = new ArrayList<>();
        String label;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!jsonArray.isNull(i)) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has(JsonFormConstants.VALUES) && jsonObject.has(JsonFormConstants.LABEL) &&
                        !"".equals(jsonObject.getString(JsonFormConstants.LABEL))) {
                    //Get label and replace any colon in some labels. Not needed at this point
                    label = jsonObject.getString(JsonFormConstants.LABEL).replace(":", "");
                    stringList.add(label + ":" + getStringValue(jsonObject));
                }
            }
        }

        return stringList;
    }

    private String getStringValue(JSONObject jsonObject) throws JSONException {
        StringBuilder value = new StringBuilder();
        if (jsonObject != null) {
            JSONArray jsonArray = jsonObject.getJSONArray(JsonFormConstants.VALUES);
            for (int i = 0; i < jsonArray.length(); i++) {
                String stringValue = jsonArray.getString(i);
                value.append(getValueFromSecondaryValues(stringValue));
                value.append(", ");
            }
        }

        return value.toString().replaceAll(", $", "");
    }

    private String getValueFromSecondaryValues(String itemString) {
        String[] strings = itemString.split(":");
        return strings.length > 1 ? strings[1] : strings[0];
    }

    protected String getKey(JSONObject object) throws JSONException {
        return object.has(RuleConstant.IS_RULE_CHECK) && object.getBoolean(RuleConstant.IS_RULE_CHECK) ?
                object.get(RuleConstant.STEP) + "_" + object.get(KEY) : VALUE;
    }

    protected Object getValue(JSONObject object) throws JSONException {
        Object value;

        if (object.has(VALUE)) {

            value = object.opt(VALUE);

            if (isNumberWidget(object)) {
                value = TextUtils.isEmpty(object.optString(VALUE)) ? 0 :
                        processNumberValues(object.optString(VALUE));
            } else if (value != null && !TextUtils.isEmpty(object.getString(VALUE)) &&
                    canHaveNumber(object)) {
                value = processNumberValues(value);
            }

        } else {
            value = isNumberWidget(object) ? 0 : "";
        }

        return value;
    }

    protected boolean isNumberWidget(JSONObject object) throws JSONException {
        return object.has(JsonFormConstants.EDIT_TYPE) &&
                object.getString(JsonFormConstants.EDIT_TYPE).equals(JsonFormConstants.EDIT_TEXT_TYPE.NUMBER) ||
                object.getString(TYPE).equals(JsonFormConstants.NUMBER_SELECTOR);
    }

    protected Object processNumberValues(Object object) {
        Object value = object;
        try {
            if (value.toString().contains(".")) {
                value = String.valueOf((float) Math.round(Float.valueOf(value.toString()) * 100) / 100);
            } else {
                value = Integer.valueOf(value.toString());
            }
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return value;
    }

    protected boolean canHaveNumber(JSONObject object) throws JSONException {
        return isNumberWidget(object) || object.getString(TYPE).equals(JsonFormConstants.HIDDEN) ||
                object.getString(TYPE).equals(JsonFormConstants.SPINNER);
    }

    public void setChildKey(View view, String type, ExpansionPanelGenericPopupDialog genericPopupDialog) {
        String childKey;
        if (type != null && (type.equals(JsonFormConstants.CHECK_BOX) || type.equals(JsonFormConstants.NATIVE_RADIO_BUTTON) || type.equals(JsonFormConstants.EXTENDED_RADIO_BUTTON))) {
            childKey = (String) view.getTag(com.vijay.jsonwizard.R.id.childKey);
            genericPopupDialog.setChildKey(childKey);
        }
    }

    public void setExpansionPanelDetails(String type, String toolbarHeader, String container, ExpansionPanelGenericPopupDialog genericPopupDialog) {
        if (type != null && type.equals(JsonFormConstants.EXPANSION_PANEL)) {
            genericPopupDialog.setHeader(toolbarHeader);
            genericPopupDialog.setContainer(container);
        }
    }

    /**
     * Gets the {@link androidx.fragment.app.FragmentTransaction} from the {@link Context} and removes any {@link androidx.fragment.app.Fragment} with the tag `GenericPopup` from the transaction.
     * Then nullifies the stack by calling {@link androidx.fragment.app.FragmentTransaction#addToBackStack(String)} with a null value.
     *
     * @param context {@link Activity} The activity context where this transaction called from
     * @return fragmentTransaction {@link androidx.fragment.app.FragmentTransaction}
     */
    @NotNull
    public FragmentTransaction getFragmentTransaction(Activity context) {
        FragmentTransaction fragmentTransaction = context.getFragmentManager().beginTransaction();
        Fragment fragment = context.getFragmentManager().findFragmentByTag("GenericPopup");
        if (fragment != null) {
            fragmentTransaction.remove(fragment);
        }

        fragmentTransaction.addToBackStack(null);
        return fragmentTransaction;
    }

    /**
     * Enabling the expansion panel views after they were disabled on sub form opening.
     *
     * @param linearLayout {@link LinearLayout}
     */
    public void enableExpansionPanelViews(LinearLayout linearLayout) {
        RelativeLayout layoutHeader = (RelativeLayout) linearLayout.getChildAt(0);
        RelativeLayout expansionHeaderLayout = layoutHeader.findViewById(R.id.expansion_header_layout);
        expansionHeaderLayout.setEnabled(true);
        expansionHeaderLayout.setClickable(true);

        ImageView statusImageView = expansionHeaderLayout.findViewById(R.id.statusImageView);
        statusImageView.setEnabled(true);
        statusImageView.setClickable(true);

        CustomTextView topBarTextView = expansionHeaderLayout.findViewById(R.id.topBarTextView);
        topBarTextView.setClickable(true);
        topBarTextView.setEnabled(true);

        LinearLayout contentLayout = (LinearLayout) linearLayout.getChildAt(1);
        LinearLayout buttonLayout = contentLayout.findViewById(R.id.accordion_bottom_navigation);
        Button okButton = buttonLayout.findViewById(R.id.ok_button);
        okButton.setEnabled(true);
        okButton.setClickable(true);
    }


    @NonNull
    private static String cleanToken(String conditionTokenRaw) {
        String conditionToken = conditionTokenRaw.trim();

        for (int i = 0; i < conditionToken.length(); i++) {
            if (JAVA_OPERATORS.contains(conditionToken.charAt(i))) {
                if (i == 0) {
                    conditionToken = cleanToken(conditionToken.substring(1));
                } else {
                    conditionToken = conditionToken.substring(0, conditionToken.indexOf(conditionToken.charAt(i)));
                    break;
                }
            }
        }

        return conditionToken;
    }

    public static List<String> getConditionKeys(String condition) {
        String cleanString = cleanConditionString(condition);
        String[] conditionTokens = cleanString.split(" ");
        Map<String, Boolean> conditionKeys = new HashMap<>();

        for (String token : conditionTokens) {
            if (token.contains(RuleConstant.STEP) || token.contains(RuleConstant.PREFIX.GLOBAL)) {
                String conditionToken = cleanToken(token);
                conditionKeys.put(conditionToken, true);
            }
        }

        return new ArrayList<>(conditionKeys.keySet());
    }

    public static Iterable<Object> readYamlFile(String fileName, Context context) {
        return new Yaml().loadAll(getTranslatedYamlFile(fileName, context));
    }

    /**
     * Translates a yaml file specified by {@param fileName} and returns its String representation
     *
     * @param fileName
     * @param context
     * @return Translated Yaml file in its String representation
     */
    public static String getTranslatedYamlFile(String fileName, Context context) {
        return getTranslatedString(getAssetFileAsString(fileName, context), context);
    }

    /**
     * Translates a yaml file specified by {@param fileName} using properties stored in the database
     * and returns its String representation
     *
     * @param fileName
     * @param context
     * @return Translated Yaml file in its String representation
     */
    public static String getTranslatedYamlFileWithDBProperties(String fileName, Context context) {
        return NativeFormLangUtils.getTranslatedStringWithDBResourceBundle(context, getAssetFileAsString(fileName, context), null);
    }

    /**
     * Gets the contents of a file specified by {@param fileName} from the assets folder as a {@link String}
     *
     * @param fileName
     * @param context
     * @return A file from the assets folder as a String
     */
    public static String getAssetFileAsString(String fileName, Context context) {
        InputStream inputStream = null;
        String fileContents = "";
        try {
            inputStream = context.getAssets().open(fileName);
            fileContents = convertStreamToString(inputStream);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            closeCloseable(inputStream);
        }
        return fileContents;
    }

    public static void closeCloseable(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private static void closeScanner(Scanner scanner) {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Returns file contents for the file at {@param filePath} as a String
     * <p>
     * Defaults to an empty {@link String} if the file does not exist or is empty
     *
     * @param filePath
     * @return
     */
    public static String getFileContentsAsString(String filePath) {
        return getFileContentsAsString(new File(filePath));
    }

    public static String getFileContentsAsString(File file) {
        Scanner scanner = null;
        String fileContents = "";
        try {
            scanner = new Scanner(file);
            fileContents = scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            closeScanner(scanner);
        }
        return fileContents;
    }


    /**
     * Converts an {@link InputStream} into a {@link String}
     *
     * @param inputStream
     * @return String representation of an {@link InputStream}
     */
    public static String convertStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String data = scanner.hasNext() ? scanner.next() : "";
        closeScanner(scanner);
        return data;
    }


    /**
     * Gets form config entries as specified in json.form.config.json
     *
     * @param formName
     * @param configLocation
     * @param context
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static JSONObject getFormConfig(@NonNull String formName, @NonNull String configLocation, @NonNull Context context) throws JSONException, IOException {
        String fileContent = getAssetFileAsString(configLocation, context);
        JSONObject formConfig = null;
        if (StringUtils.isNotBlank(fileContent)) {
            JSONArray jsonArray = new JSONArray(fileContent);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (formName.equals(jsonObject.optString(JsonFormConstants.FORM_NAME))) {
                    formConfig = jsonObject;
                    break;
                }
            }
        }
        return formConfig;
    }

    /**
     * Converts jsonArray to set
     *
     * @param jsonArray
     * @return
     */
    public static Set<String> convertJsonArrayToSet(@Nullable JSONArray jsonArray) {
        if (jsonArray != null) {
            Set<String> strings = new HashSet<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                strings.add(jsonArray.optString(i));
            }
            return strings;
        }
        return null;
    }

    /***
     * Checks if step has no skip logic fields and that fields do not have type hidden
     * @param formFragment {@link JsonFormFragment}
     */
    public static void checkIfStepHasNoSkipLogic(JsonFormFragment formFragment) {
        String step = formFragment.getJsonApi().nextStep();
        if (formFragment.getJsonApi().stepSkipLogicPresenceMap().get(step) == null) {
            boolean hasNoSkipLogic = false;
            JSONObject jsonObject = formFragment.getJsonApi().getmJSONObject();
            if (StringUtils.isNotBlank(step)) {
                JSONObject jsonStepObject = jsonObject.optJSONObject(step);
                JSONArray fields = jsonStepObject.optJSONArray(JsonFormConstants.FIELDS);
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject object = fields.optJSONObject(i);
                    if (object.has(TYPE)
                            && !object.optString(TYPE).equals(JsonFormConstants.HIDDEN)
                            && !object.has(JsonFormConstants.RELEVANCE)) {
                        hasNoSkipLogic = true;
                        break;
                    }
                }
                formFragment.getJsonApi().stepSkipLogicPresenceMap().put(step, hasNoSkipLogic);
            }
        }
    }

    /***
     * removes the generated dynamic rules by repeating group
     */
    public static void removeGeneratedDynamicRules(JsonFormFragment formFragment) {
        JSONObject form = formFragment.getJsonApi().getmJSONObject();
        JSONArray jsonArray = FormUtils.getMultiStepFormFields(form);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject.has(JsonFormConstants.RELEVANCE) &&
                    jsonObject.optJSONObject(JsonFormConstants.RELEVANCE).has(RuleConstant.RULES_ENGINE) &&
                    jsonObject.optJSONObject(JsonFormConstants.RELEVANCE).optJSONObject(RuleConstant.RULES_ENGINE)
                            .has(JsonFormConstants.JSON_FORM_KEY.EX_RULES) &&
                    jsonObject.optJSONObject(JsonFormConstants.RELEVANCE).optJSONObject(RuleConstant.RULES_ENGINE)
                            .optJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES).has(RuleConstant.RULES_DYNAMIC)) {
                jsonArray.optJSONObject(i).remove(JsonFormConstants.RELEVANCE);
            }
            if (jsonObject.has(JsonFormConstants.CALCULATION) &&
                    jsonObject.optJSONObject(JsonFormConstants.CALCULATION).has(RuleConstant.RULES_ENGINE) &&
                    jsonObject.optJSONObject(JsonFormConstants.CALCULATION).optJSONObject(RuleConstant.RULES_ENGINE)
                            .has(JsonFormConstants.JSON_FORM_KEY.EX_RULES) &&
                    jsonObject.optJSONObject(JsonFormConstants.CALCULATION).optJSONObject(RuleConstant.RULES_ENGINE)
                            .optJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES).has(RuleConstant.RULES_DYNAMIC)) {
                jsonArray.optJSONObject(i).remove(JsonFormConstants.CALCULATION);
            }
        }
    }

    public static final boolean isRunningOnUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static String formatDateToPattern(String date, String inputFormat, String outputFormat) {
        if (StringUtils.isEmpty(date)) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
        sdf.setLenient(false);
        Date newDate = null;
        try {
            newDate = sdf.parse(date);
        } catch (ParseException e) {
            Timber.e(e);
        }
        if (newDate == null) {
            return date;
        }
        sdf = new SimpleDateFormat(outputFormat);
        return sdf.format(newDate);
    }


    public static int getResourceId(Context context, String name, ResourceType resourceType) {
        try {
            return context.getResources().getIdentifier(name, resourceType.getType(), context.getPackageName());
        } catch (Exception e) {
            Timber.e(e);
            return -1;
        }
    }

    public static boolean isEmptyJsonArray(JSONArray jsonArray) {
        return jsonArray == null || jsonArray.length() == 0;
    }

    public static void showAlertDialog(Context context, String title, String message,
                                       String negativeBtnTxt, String positiveBtnTxt,
                                       DialogInterface.OnClickListener negativeBtnListener,
                                       DialogInterface.OnClickListener positiveBtnListener) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.AppThemeAlertDialog).setTitle(title)
                .setMessage(message);

        if (negativeBtnListener != null) {
            alertDialogBuilder.setNegativeButton(negativeBtnTxt, negativeBtnListener);
        }

        if (positiveBtnListener != null) {
            alertDialogBuilder.setPositiveButton(positiveBtnTxt, positiveBtnListener);
        }

        alertDialogBuilder.create().show();
    }

    public static boolean isEmptyJsonObject(JSONObject jsonObject) {
        return jsonObject == null || jsonObject.length() == 0;
    }


    /**
     * Returns the object that holds the repeating group count
     *
     * @return
     * @throws JSONException
     */
    @Nullable
    public static JSONObject getRepeatingGroupCountObj(@NotNull WidgetArgs widgetArgs) throws JSONException {
        String repeatingGroupCountObjKey = widgetArgs.getJsonObject().get(KEY) + "_count";
        JSONObject stepJsonObject = widgetArgs.getFormFragment().getStep(widgetArgs.getStepName());
        if (stepJsonObject == null) {
            return null;
        }
        JSONArray stepFields = stepJsonObject.optJSONArray(JsonFormConstants.FIELDS);
        JSONObject repeatingGroupCountObj = FormUtils.getFieldJSONObject(stepFields, repeatingGroupCountObjKey);
        // prevents re-adding the count object during form traversals
        if (repeatingGroupCountObj != null) {
            return repeatingGroupCountObj;
        }

        repeatingGroupCountObj = new JSONObject();
        repeatingGroupCountObj.put(KEY, repeatingGroupCountObjKey);
        repeatingGroupCountObj.put(OPENMRS_ENTITY_PARENT, "");
        repeatingGroupCountObj.put(OPENMRS_ENTITY, "");
        repeatingGroupCountObj.put(OPENMRS_ENTITY_ID, "");
        repeatingGroupCountObj.put(TYPE, "");
        repeatingGroupCountObj.put(VALUE, "0");
        repeatingGroupCountObj.put(TEXT, widgetArgs.getJsonObject().get(REFERENCE_EDIT_TEXT_HINT));
        stepFields.put(repeatingGroupCountObj);
        return repeatingGroupCountObj;
    }
}


