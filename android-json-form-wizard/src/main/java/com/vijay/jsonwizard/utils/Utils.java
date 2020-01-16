package com.vijay.jsonwizard.utils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.util.TimeUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.CompoundButton;
import com.vijay.jsonwizard.customviews.ExpansionPanelGenericPopupDialog;
import com.vijay.jsonwizard.event.BaseEvent;
import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class Utils {
    private static ProgressDialog progressDialog;
    public final static List<String> PREFICES_OF_INTEREST = Arrays.asList(RuleConstant.PREFIX.GLOBAL, RuleConstant.STEP);
    public final static Set<Character> JAVA_OPERATORS = new HashSet<>(
            Arrays.asList('(', '!', ',', '?', '+', '-', '*', '/', '%', '+', '-', '.', '^', ')', '<', '>', '=', '{', '}', ':',
                    ';', '[', ']'));

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public static Date getDateFromString(String dtStart) {
        if (StringUtils.isNotBlank(dtStart) && !"0".equals(dtStart)) {
            try {
                return DatePickerFactory.DATE_FORMAT.parse(dtStart);
            } catch (ParseException e) {
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
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(context.getString(title));
        progressDialog.setMessage(context.getString(message));
        progressDialog.show();
    }

    public static void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static int pixelToDp(int dpValue, Context context) {
        float dpRatio = context.getResources().getDisplayMetrics().density;
        float pixelForDp = dpValue * dpRatio;

        return (int) pixelForDp;
    }

    /**
     * Loads the strings property files used to keep the strings to be displayed on the forms
     *
     * @param key     {@link String}
     * @param context {@link Context}
     * @return property {@link String}
     */
    public static String getProperty(String key, Context context) {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        String locale = context.getResources().getConfiguration().locale.getLanguage();
        try {
            InputStream inputStream = assetManager.open(String.format("strings/string-%s.properties", locale));
            properties.load(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            return properties.getProperty(key);

        } catch (Exception e) {
            try {
                if (e instanceof FileNotFoundException) {
                    InputStream inputStream = assetManager.open(String.format("strings/string.properties", locale));
                    properties.load(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                    return properties.getProperty(key);
                }
            } catch (Exception e2) {

                return null;
            }
            return null;

        }
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
        CustomTextView extraInfoTextView = (CustomTextView) button
                .getTag(R.id.specify_extra_info_textview);
        JSONObject optionsJson = (JSONObject) button.getTag(R.id.option_json_object);
        String radioButtonText = optionsJson.optString(JsonFormConstants.TEXT);
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
                object.get(RuleConstant.STEP) + "_" + object.get(JsonFormConstants.KEY) : JsonFormConstants.VALUE;
    }

    protected Object getValue(JSONObject object) throws JSONException {
        Object value;

        if (object.has(JsonFormConstants.VALUE)) {

            value = object.opt(JsonFormConstants.VALUE);

            if (isNumberWidget(object)) {
                value = TextUtils.isEmpty(object.optString(JsonFormConstants.VALUE)) ? 0 :
                        processNumberValues(object.optString(JsonFormConstants.VALUE));
            } else if (value != null && !TextUtils.isEmpty(object.getString(JsonFormConstants.VALUE)) &&
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
                object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.NUMBER_SELECTOR);
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

    protected boolean canHaveNumber(JSONObject object) throws JSONException {
        return isNumberWidget(object) || object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.HIDDEN) ||
                object.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.SPINNER);
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

    @NotNull
    public FragmentTransaction getFragmentTransaction(Activity context) {
        Activity activity = context;
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag("GenericPopup");
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        return ft;
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


    private static String cleanConditionString(String conditionStringRaw) {
        String conditionString = conditionStringRaw;

        for (String token : PREFICES_OF_INTEREST) {

            conditionString = conditionString.replaceAll(token, " " + token);
        }

        return conditionString.replaceAll("  ", " ");
    }

    public static Iterable<Object> readYamlFile(String fileName, Context context) {
        Yaml yaml = new Yaml();
        InputStreamReader inputStreamReader;
        Iterable<Object> objectIterable = null;
        try {
            inputStreamReader = new InputStreamReader(context.getAssets().open(fileName));
            objectIterable = yaml.loadAll(inputStreamReader);
        } catch (IOException e) {
            Timber.e(e);
        }

        return objectIterable;
    }
}


