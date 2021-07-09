package com.vijay.jsonwizard.utils;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.BuildConfig;
import com.vijay.jsonwizard.NativeFormLibrary;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.ExpansionPanelGenericPopupDialog;
import com.vijay.jsonwizard.domain.ExpansionPanelItemModel;
import com.vijay.jsonwizard.domain.ExpansionPanelValuesModel;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.GenericDialogInterface;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.OnFormFetchedCallback;
import com.vijay.jsonwizard.interfaces.RollbackDialogCallback;
import com.vijay.jsonwizard.model.DynamicLabelInfo;
import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.views.CustomTextView;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Facts;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.client.utils.contract.ClientFormContract;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static com.vijay.jsonwizard.utils.Utils.convertStreamToString;
import static com.vijay.jsonwizard.utils.Utils.isEmptyJsonArray;

/**
 * Created by vijay on 24-05-2015.
 */
public class FormUtils {
    public static final String FONT_BOLD_PATH = "fonts/Roboto-Bold.ttf";
    //public static final String FONT_REGULAR_PATH = "fonts/Roboto-Regular.ttf";
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;
    public static final String METADATA_PROPERTY = "metadata";
    public static final String LOOK_UP_JAVAROSA_PROPERTY = "look_up";
    public static final String NATIIVE_FORM_DATE_FORMAT_PATTERN = "dd-MM-yyyy";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            NATIIVE_FORM_DATE_FORMAT_PATTERN, Locale.ENGLISH);
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private static final String START_JAVAROSA_PROPERTY = "start";
    private static final String END_JAVAROSA_PROPERTY = "end";
    private static final String TODAY_JAVAROSA_PROPERTY = "today";
    private static final String DEFAULT_FORM_IMAGES_FOLDER = "image/";
    private static final String TAG = FormUtils.class.getSimpleName();
    private Utils utils = new Utils();
    private GenericDialogInterface genericDialogInterface;

    public static Point getViewLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    public static LinearLayout.LayoutParams getLinearLayoutParams(int width, int height, int left,
                                                                  int top, int right,
                                                                  int bottom) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.setMargins(left, top, right, bottom);
        return layoutParams;
    }

    public static RelativeLayout.LayoutParams getRelativeLayoutParams(int width, int height, int left,
                                                                      int top, int right,
                                                                      int bottom) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.setMargins(left, top, right, bottom);
        return layoutParams;
    }

    public static CustomTextView getTextViewWith(Context context, int textSizeInSp, String text,
                                                 String key, String type, String openMrsEntityParent,
                                                 String openMrsEntity, String openMrsEntityId,
                                                 String relevance,
                                                 LinearLayout.LayoutParams layoutParams, String fontPath) {
        return getTextViewWith(context, textSizeInSp, text, key, type, openMrsEntityParent,
                openMrsEntity, openMrsEntityId,
                relevance,
                layoutParams, fontPath, 0, null);
    }

    public static CustomTextView getTextViewWith(Context context, int textSizeInSp, String text,
                                                 String key, String type, String openMrsEntityParent,
                                                 String openMrsEntity, String openMrsEntityId,
                                                 String relevance,
                                                 LinearLayout.LayoutParams layoutParams, String fontPath, int bgColor,
                                                 String textColor) {
        CustomTextView textView = new CustomTextView(context);
        textView.setText(text);
        textView.setTag(R.id.key, key);
        textView.setTag(R.id.type, type);
        textView.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        textView.setTag(R.id.openmrs_entity, openMrsEntity);
        textView.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        textView.setId(ViewUtil.generateViewId());
        textView.setTextSize(textSizeInSp);
        textView.setLayoutParams(layoutParams);

        if (bgColor != 0) {
            textView.setBackgroundColor(bgColor);
        }

        if (textColor != null) {
            textView.setTextColor(Color.parseColor(textColor));
        }

        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            textView.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(textView);
        }
        return textView;
    }

    public static void updateStartProperties(PropertyManager propertyManager, JSONObject form)
            throws Exception {
        if (form.has(METADATA_PROPERTY)) {
            if (form.getJSONObject(METADATA_PROPERTY).has(START_JAVAROSA_PROPERTY)) {
                Calendar calendar = Calendar.getInstance();
                JSONObject start = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(START_JAVAROSA_PROPERTY);
                String value = DATE_TIME_FORMAT.format(calendar.getTime());
                if (value == null) {
                    value = "";
                }
                start.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.DEVICE_ID_PROPERTY)) {
                JSONObject deviceId = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.DEVICE_ID_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.DEVICE_ID_PROPERTY);
                if (value == null) {
                    value = "";
                }
                deviceId.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.SUBSCRIBER_ID_PROPERTY)) {
                JSONObject subscriberId = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.SUBSCRIBER_ID_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.SUBSCRIBER_ID_PROPERTY);
                if (value == null) {
                    value = "";
                }
                subscriberId.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.SIM_SERIAL_PROPERTY)) {
                JSONObject simSerial = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.SIM_SERIAL_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.SIM_SERIAL_PROPERTY);
                if (value == null) {
                    value = "";
                }
                simSerial.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.PHONE_NUMBER_PROPERTY)) {
                JSONObject simSerial = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.PHONE_NUMBER_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.PHONE_NUMBER_PROPERTY);
                if (value == null) {
                    value = "";
                }
                simSerial.put(JsonFormConstants.VALUE, value);
            }
        }
    }

    public static void updateEndProperties(PropertyManager propertyManager, JSONObject form)
            throws Exception {
        if (form.has(METADATA_PROPERTY)) {
            if (form.getJSONObject(METADATA_PROPERTY).has(END_JAVAROSA_PROPERTY)) {
                Calendar calendar = Calendar.getInstance();
                JSONObject end = form.getJSONObject(METADATA_PROPERTY).getJSONObject(END_JAVAROSA_PROPERTY);
                String value = DATE_TIME_FORMAT.format(calendar.getTime());
                if (value == null) {
                    value = "";
                }
                end.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(TODAY_JAVAROSA_PROPERTY)) {
                Calendar calendar = Calendar.getInstance();
                JSONObject today = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(TODAY_JAVAROSA_PROPERTY);
                String value = DATE_FORMAT.format(calendar.getTime());
                if (value == null) {
                    value = "";
                }
                today.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.DEVICE_ID_PROPERTY)) {
                JSONObject deviceId = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.DEVICE_ID_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.DEVICE_ID_PROPERTY);
                if (value == null) {
                    value = "";
                }
                deviceId.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.SUBSCRIBER_ID_PROPERTY)) {
                JSONObject subscriberId = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.SUBSCRIBER_ID_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.SUBSCRIBER_ID_PROPERTY);
                if (value == null) {
                    value = "";
                }
                subscriberId.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.SIM_SERIAL_PROPERTY)) {
                JSONObject simSerial = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.SIM_SERIAL_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.SIM_SERIAL_PROPERTY);
                if (value == null) {
                    value = "";
                }
                simSerial.put(JsonFormConstants.VALUE, value);
            }

            if (form.getJSONObject(METADATA_PROPERTY).has(PropertyManager.PHONE_NUMBER_PROPERTY)) {
                JSONObject simSerial = form.getJSONObject(METADATA_PROPERTY)
                        .getJSONObject(PropertyManager.PHONE_NUMBER_PROPERTY);
                String value = propertyManager.getSingularProperty(
                        PropertyManager.PHONE_NUMBER_PROPERTY);
                if (value == null) {
                    value = "";
                }
                simSerial.put(JsonFormConstants.VALUE, value);
            }
        }
    }

    public static JSONObject createOptiBPDataObject(String clientId, String clientOpenSRPId) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.OptibpConstants.OPTIBP_KEY_CLIENT_ID, clientId);
        jsonObject.put(JsonFormConstants.OptibpConstants.OPTIBP_KEY_CLIENT_OPENSRP_ID, clientOpenSRPId);
        return jsonObject;
    }

    public Map<String, View> createRadioButtonAndCheckBoxLabel(String stepName, LinearLayout linearLayout,
                                                               JSONObject jsonObject, Context context,
                                                               JSONArray canvasIds, final Boolean readOnly,
                                                               CommonListener listener, boolean popup) throws JSONException {
        Map<String, View> createdViewsMap = new HashMap<>();
        String label = jsonObject.optString(JsonFormConstants.LABEL, "");
        if (StringUtils.isNotBlank(label)) {
            String asterisks = "";
            final int labelTextSize = FormUtils.getValueFromSpOrDpOrPx(jsonObject.optString(JsonFormConstants.LABEL_TEXT_SIZE, String.valueOf(context
                    .getResources().getDimension(R.dimen.default_label_text_size))), context);
            String labelTextColor = jsonObject.optString(JsonFormConstants.LABEL_TEXT_COLOR, JsonFormConstants.DEFAULT_TEXT_COLOR);
            JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
            final ConstraintLayout labelConstraintLayout = createLabelLinearLayout(stepName, canvasIds, jsonObject, context, listener);
            labelConstraintLayout.setTag(R.id.extraPopup, popup);
            final CustomTextView labelText = labelConstraintLayout.findViewById(R.id.label_text);
            ImageView editButton = labelConstraintLayout.findViewById(R.id.label_edit_button);
            if (requiredObject != null) {
                String requiredValue = requiredObject.getString(JsonFormConstants.VALUE);
                if (StringUtils.isNotBlank(requiredValue) && (Boolean.TRUE.toString().equalsIgnoreCase(requiredValue) || Boolean.parseBoolean(requiredValue))) {
                    asterisks = "<font color=#CF0800> *</font>";
                }
            }

            final String combinedLabelText = "<font color=" + labelTextColor + ">" + label + "</font>" + asterisks;

            //Applying textStyle to the text;
            final String textStyle = jsonObject.optString(JsonFormConstants.TEXT_STYLE, JsonFormConstants.NORMAL);
            if (labelText != null && editButton != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTextStyle(textStyle, labelText);
                        labelText.setText(Html.fromHtml(combinedLabelText));
                        labelText.setTextSize(labelTextSize);
                        labelConstraintLayout.setEnabled(!readOnly);
                    }
                });
                labelText.setTag(R.id.extraPopup, popup);
                labelText.setTag(R.id.original_text, Html.fromHtml(combinedLabelText));
                canvasIds.put(labelConstraintLayout.getId());
                linearLayout.addView(labelConstraintLayout);
                createdViewsMap.put(JsonFormConstants.EDIT_BUTTON, editButton);
                createdViewsMap.put(JsonFormConstants.CUSTOM_TEXT, labelText);
            }
        }
        return createdViewsMap;
    }

    public static int getValueFromSpOrDpOrPx(String spOrDpOrPx, Context context) {
        int px = 0;
        if (!TextUtils.isEmpty(spOrDpOrPx)) {
            if (spOrDpOrPx.contains("sp")) {
                int unitValues = Integer.parseInt(spOrDpOrPx.replace("sp", ""));
                px = spToPx(context, unitValues);
            } else if (spOrDpOrPx.contains("dp")) {
                int unitValues = Integer.parseInt(spOrDpOrPx.replace("dp", ""));
                px = FormUtils.dpToPixels(context, unitValues);
            } else if (spOrDpOrPx.contains("px")) {
                px = Integer.parseInt(spOrDpOrPx.replace("px", ""));
            } else {
                px = (int) context.getResources().getDimension(R.dimen.default_label_text_size);
            }
        }

        return px;
    }

    public ConstraintLayout createLabelLinearLayout(String stepName, JSONArray canvasIds,
                                                    JSONObject jsonObject,
                                                    Context context,
                                                    CommonListener listener) throws JSONException {
        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT, null);
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY, null);
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID, null);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);

        ConstraintLayout constraintLayout = getConstraintLayout(stepName, canvasIds, jsonObject, context, openMrsEntityParent, openMrsEntity, openMrsEntityId);
        attachRefreshLogic(context, relevance, calculation, constraints, constraintLayout);

        ImageView imageView = constraintLayout.findViewById(R.id.label_info);
        showInfoIcon(stepName, jsonObject, listener, FormUtils.getInfoDialogAttributes(jsonObject), imageView, canvasIds);

        return constraintLayout;
    }

    private void attachRefreshLogic(Context context, String relevance, String calculation, String constraints, ConstraintLayout constraintLayout) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            constraintLayout.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(constraintLayout);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            constraintLayout.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(constraintLayout);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            constraintLayout.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addCalculationLogicView(constraintLayout);
        }
    }

    @NotNull
    public ConstraintLayout getConstraintLayout(String stepName, JSONArray canvasIds, JSONObject jsonObject, Context context, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        ConstraintLayout constraintLayout = getRootConstraintLayout(context);
        constraintLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(constraintLayout.getId());
        constraintLayout.setTag(R.id.canvas_ids, canvasIds.toString());
        constraintLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        constraintLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        constraintLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        constraintLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        constraintLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        constraintLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        constraintLayout.setId(ViewUtil.generateViewId());
        return constraintLayout;
    }

    public ConstraintLayout getRootConstraintLayout(Context context) {
        return (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.native_form_labels, null);
    }

    /**
     *
     */
    public static void setTextStyle(String textStyle, AppCompatTextView view) {
        if (view != null && StringUtils.isNotBlank(textStyle)) {
            switch (textStyle) {
                case JsonFormConstants.BOLD:
                    view.setTypeface(null, Typeface.BOLD);
                    break;
                case JsonFormConstants.ITALIC:
                    view.setTypeface(null, Typeface.ITALIC);
                    break;
                case JsonFormConstants.BOLD_ITALIC:
                    view.setTypeface(null, Typeface.BOLD_ITALIC);
                    break;
                case JsonFormConstants.NORMAL:
                default:
                    view.setTypeface(null, Typeface.NORMAL);
                    break;
            }
        }
    }

    public static int spToPx(Context context, float sp) {
        return (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int dpToPixels(Context context, float dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, context.getResources().getDisplayMetrics());
    }

    public void showInfoIcon(String stepName, JSONObject jsonObject, CommonListener listener,
                             @NonNull HashMap<String, String> imageAttributes, ImageView imageView, JSONArray canvasIds)
            throws JSONException {
        if (imageView != null) {
            //Display custom dialog if has image is true otherwise normal alert dialog is enough
            if (imageAttributes.get(JsonFormConstants.LABEL_INFO_HAS_IMAGE) != null &&
                    Boolean.parseBoolean(imageAttributes.get(JsonFormConstants.LABEL_INFO_HAS_IMAGE))) {

                imageView.setTag(R.id.label_dialog_image_src,
                        imageAttributes.get(JsonFormConstants.LABEL_INFO_IMAGE_SRC));
                imageView.setVisibility(View.VISIBLE);

            }

            if (imageAttributes.get(JsonFormConstants.LABEL_INFO_TEXT) != null) {

                imageView
                        .setTag(R.id.label_dialog_info, imageAttributes.get(JsonFormConstants.LABEL_INFO_TEXT));
                imageView
                        .setTag(R.id.label_dialog_title, imageAttributes.get(JsonFormConstants.LABEL_INFO_TITLE));
                imageView.setVisibility(View.VISIBLE);

            }

            if (imageAttributes.get(JsonFormConstants.LABEL_IS_DYNAMIC) != null) {
                imageView.setTag(R.id.dynamic_label_info, jsonObject.getJSONArray(JsonFormConstants.DYNAMIC_LABEL_INFO));
                imageView.setTag(R.id.label_dialog_title, imageAttributes.get(JsonFormConstants.LABEL_INFO_TITLE));
                imageView.setVisibility(View.VISIBLE);
            }

            imageView.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
            imageView.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
            imageView.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
            imageView.setTag(R.id.canvas_ids, canvasIds.toString());
            imageView.setOnClickListener(listener);
        }
    }

    public static HashMap<String, String> getInfoDialogAttributes(JSONObject jsonObject) {
        HashMap<String, String> imageAttributes = new HashMap<>();
        imageAttributes.put(JsonFormConstants.LABEL_INFO_TITLE,
                jsonObject.optString(JsonFormConstants.LABEL_INFO_TITLE, ""));
        imageAttributes.put(JsonFormConstants.LABEL_INFO_TEXT,
                jsonObject.optString(JsonFormConstants.LABEL_INFO_TEXT, null));
        imageAttributes.put(JsonFormConstants.LABEL_INFO_HAS_IMAGE,
                jsonObject.optString(JsonFormConstants.LABEL_INFO_HAS_IMAGE, null));
        imageAttributes.put(JsonFormConstants.LABEL_INFO_IMAGE_SRC,
                jsonObject.optString(JsonFormConstants.LABEL_INFO_IMAGE_SRC, null));
        imageAttributes.put(JsonFormConstants.LABEL_IS_DYNAMIC,
                jsonObject.optString(JsonFormConstants.LABEL_IS_DYNAMIC, null));
        return imageAttributes;
    }

    public static Drawable readImageFromAsset(Context context, String fileName) throws IOException {
        return Drawable.createFromStream(context.getAssets().open(fileName), null);
    }

    public static ArrayList<DynamicLabelInfo> getDynamicLabelInfoList(JSONArray jsonArray) {
        ArrayList<DynamicLabelInfo> dynamicLabelInfos = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject dynamicLabelJsonObject = jsonArray.getJSONObject(i);
                dynamicLabelInfos.add(new DynamicLabelInfo(dynamicLabelJsonObject.getString(JsonFormConstants.DYNAMIC_LABEL_TITLE),
                        dynamicLabelJsonObject.getString(JsonFormConstants.DYNAMIC_LABEL_TEXT), dynamicLabelJsonObject.getString(JsonFormConstants.DYNAMIC_LABEL_IMAGE_SRC)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dynamicLabelInfos;
    }

    public static void setEditButtonAttributes(JSONObject jsonObject, View editableView,
                                               ImageView editButton,
                                               CommonListener listener) throws JSONException {
        editButton.setTag(R.id.editable_view, editableView);
        editButton.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        editButton.setTag(R.id.type, jsonObject.getString("type"));
        editButton.setOnClickListener(listener);
    }

    /**
     * Checks and uncheck the radio buttons in a linear layout view follows this fix
     * https://stackoverflow.com/a/26961458/5784584
     *
     * @param parent {@link ViewGroup}
     */
    public static void setRadioExclusiveClick(ViewGroup parent) {
        final List<RadioButton> radioButtonList = Utils.getRadioButtons(parent);
        for (RadioButton radioButton : radioButtonList) {
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RadioButton radioButtonView = (RadioButton) view;
                    for (RadioButton button : radioButtonList) {
                        if (button.getId() != radioButtonView.getId()) {
                            button.setChecked(false);
                            try {
                                Utils.resetRadioButtonsSpecifyText(button);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

    }

    /**
     * This method returns a {@link Calendar} object at mid-day corresponding to a date matching the
     * format specified in {@code DATE_FORMAT} or a day in reference to today e.g today, today-1,
     * today+10
     *
     * @param dayString_ The string to be converted to a date
     * @return The calendar object corresponding to the day, or object corresponding to today's date
     * if an error occurred
     */
    public static Calendar getDate(String dayString_) {
        Calendar calendarDate = Calendar.getInstance();

        if (dayString_ != null && dayString_.trim().length() > 0) {
            String dayString = dayString_.trim().toLowerCase();
            if (!"today".equals(dayString)) {
                Pattern pattern = Pattern.compile("today\\s*([-\\+])\\s*(\\d+)([dmywDMY]{1})");
                Matcher matcher = pattern.matcher(dayString);
                if (matcher.find()) {
                    int timeValue = Integer.parseInt(matcher.group(2));
                    if ("-".equals(matcher.group(1))) {
                        timeValue = timeValue * -1;
                    }

                    int field = Calendar.DATE;
                    if (matcher.group(3).equalsIgnoreCase("y")) {
                        field = Calendar.YEAR;
                    } else if (matcher.group(3).equalsIgnoreCase("m")) {
                        field = Calendar.MONTH;
                    } else if (matcher.group(3).equalsIgnoreCase("w")) {
                        field = Calendar.WEEK_OF_MONTH;
                    }

                    calendarDate.add(field, timeValue);
                } else {
                    try {
                        calendarDate.setTime(DATE_FORMAT.parse(dayString));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //set time to mid-day
        calendarDate.set(Calendar.HOUR_OF_DAY, 12);
        calendarDate.set(Calendar.MINUTE, 0);
        calendarDate.set(Calendar.SECOND, 0);
        calendarDate.set(Calendar.MILLISECOND, 0);

        return calendarDate;
    }

    public static void setRequiredOnHint(AppCompatEditText editText) {
        if (!TextUtils.isEmpty(editText.getHint())) {
            SpannableString hint = new SpannableString(editText.getHint() + " *");
            hint.setSpan(new ForegroundColorSpan(Color.RED), hint.length() - 1, hint.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editText.setHint(hint);
        }
    }

    public static void setEditMode(JSONObject jsonObject, View editableView, ImageView editButton)
            throws JSONException {
        if (jsonObject.has(JsonFormConstants.EDITABLE)) {
            boolean editable = jsonObject.getBoolean(JsonFormConstants.EDITABLE);
            if (editable) {
                editButton.setVisibility(View.VISIBLE);
                editableView.setEnabled(false);
            } else {
                editButton.setVisibility(View.GONE);
            }
        } else if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            boolean readyOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            editableView.setEnabled(!readyOnly);
            editButton.setVisibility(View.GONE);
        } else if (jsonObject.has(JsonFormConstants.EDITABLE) && jsonObject
                .has(JsonFormConstants.READ_ONLY)) {
            editButton.setVisibility(View.VISIBLE);
            editableView.setEnabled(false);
        }
    }

    public static JSONObject getSubFormJson(String formIdentity, String subFormsLocation,
                                            Context context) throws Exception {

        return new JSONObject(loadSubForm(formIdentity, getSubFormLocation(subFormsLocation), context));
    }

    public static JSONObject getSubFormJson(String formIdentity, String subFormsLocation,
                                            Context context, boolean translateSubForm) throws Exception {

        return new JSONObject(loadSubForm(formIdentity, getSubFormLocation(subFormsLocation), context, translateSubForm));
    }

    public static String getSubFormLocation(String subFormsLocation) {
        return TextUtils.isEmpty(subFormsLocation) ? JsonFormConstants.DEFAULT_SUB_FORM_LOCATION : subFormsLocation;
    }

    public static String loadSubForm(String formIdentity, String defaultSubFormLocation,
                                     Context context, boolean translateSubForm) throws IOException {

        String subForm = loadSubForm(formIdentity, defaultSubFormLocation, context);
        return translateSubForm ? NativeFormLangUtils.getTranslatedString(subForm, context) : subForm;
    }

    public static String loadSubForm(String formIdentity, String defaultSubFormLocation,
                                     Context context) throws IOException {

        return convertStreamToString(context.getAssets().open(defaultSubFormLocation + "/" + formIdentity + ".json"));
    }

    public static JSONObject getFieldFromForm(JSONObject jsonForm, String key) throws JSONException {
        JSONObject field = new JSONObject();
        if (jsonForm != null) {
            JSONArray formFields = getMultiStepFormFields(jsonForm);
            if (formFields != null) {
                for (int i = 0; i < formFields.length(); i++) {
                    JSONObject widget = formFields.getJSONObject(i);
                    if (widget.has(JsonFormConstants.KEY) && key
                            .equals(widget.getString(JsonFormConstants.KEY))) {
                        field = widget;
                        break;
                    }
                }
            }
        }
        return field;
    }

    public static JSONArray getMultiStepFormFields(JSONObject jsonForm) {
        JSONArray fields = new JSONArray();
        try {
            if (jsonForm.has(JsonFormConstants.COUNT)) {
                int stepCount = Integer.parseInt(jsonForm.getString(JsonFormConstants.COUNT));
                for (int i = 0; i < stepCount; i++) {
                    String stepName = JsonFormConstants.STEP + (i + 1);
                    JSONObject step = jsonForm.has(stepName) ? jsonForm.getJSONObject(stepName) : null;
                    if (step != null && step.has(JsonFormConstants.FIELDS)) {
                        JSONArray stepFields = step.getJSONArray(JsonFormConstants.FIELDS);
                        for (int k = 0; k < stepFields.length(); k++) {
                            JSONObject field = stepFields.getJSONObject(k);
                            fields.put(field);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e, " --> getMultiStepFormFields()");
        }
        return fields;
    }

    public static JSONArray fields(JSONObject jsonForm, String step) {
        try {
            JSONObject stepJSONObject = jsonForm.has(step) ? jsonForm.getJSONObject(step) : null;
            if (stepJSONObject == null) {
                return null;
            }

            return stepJSONObject.has(JsonFormConstants.FIELDS) ? stepJSONObject
                    .getJSONArray(JsonFormConstants.FIELDS) : null;
        } catch (JSONException e) {
            Timber.e(e, " -->  fields()");
        }
        return null;
    }

    public static JSONObject getFieldJSONObject(JSONArray jsonArray, String key) {
        if (isEmptyJsonArray(jsonArray) || key == null) {
            return null;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = getJSONObject(jsonArray, i);
            String keyVal = getString(jsonObject, JsonFormConstants.KEY);
            if (keyVal != null && keyVal.equals(key)) {
                return jsonObject;
            }
        }
        return null;
    }

    public static JSONObject getJSONObject(JSONArray jsonArray, int index) {
        if (isEmptyJsonArray(jsonArray)) {
            return null;
        }

        try {
            return jsonArray.getJSONObject(index);
        } catch (JSONException e) {
            return null;

        }
    }

    public static String getString(JSONObject jsonObject, String field) {
        if (jsonObject == null) {
            return null;
        }

        try {
            return jsonObject.has(field) ? jsonObject.getString(field) : null;
        } catch (JSONException e) {
            return null;
        }
    }

    /***
     *  This method adds an attribute to toggle visibility of the edit text
     *  You will find it useful when working with calculations
     *  that require you to check whether the value is empty since hidden widget defaults to '0'
     * @param jsonObject JsonObject that defines the edit Text
     * @param editText The edit text that you want to toggle its visibility
     * @throws JSONException throws JsonException
     * @author ellykits
     */
    public static void toggleEditTextVisibility(JSONObject jsonObject, AppCompatEditText editText)
            throws JSONException {
        if (jsonObject.has(JsonFormConstants.HIDDEN)) {
            boolean hidden = jsonObject.getBoolean(JsonFormConstants.HIDDEN);
            editText.setVisibility(hidden ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Get Bitmap given a location in the assets folder and the name of the file
     *
     * @param context
     * @param folder    The folder within the Assets folder where the image resides
     * @param imageFile Path and the name of the file
     * @return Bitmap
     */
    public static Bitmap getBitmap(Context context, String folder, String imageFile) {
        Bitmap bitmap = null;
        String filePath;
        if (TextUtils.isEmpty(imageFile)) {
            return null;
        }
        try {
            if (TextUtils.isEmpty(folder)) {
                filePath = DEFAULT_FORM_IMAGES_FOLDER + imageFile;
            } else {
                filePath = folder + imageFile;
            }
            InputStream is = context.getAssets().open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
        return bitmap;
    }

    public static JSONArray getCheckboxValueJsonArray(HashSet<String> optionValues) {
        return new JSONArray(optionValues);
    }

    public static HashSet<String> getCurrentCheckboxValues(JSONArray optionsArray)
            throws JSONException {
        HashSet<String> result = new HashSet<>();
        for (int i = 0; i < optionsArray.length(); i++) {
            result.add(optionsArray.getString(i));
        }
        return result;
    }

    /**
     * Set view OpenMRS Entity attributes
     *
     * @param jsonObject JSON Object containing OpenMRS entity attributes
     * @param view       View to be set with OpenMRS attributes
     */
    public static void setViewOpenMRSEntityAttributes(JSONObject jsonObject, View view) {
        String key = jsonObject.optString(JsonFormConstants.KEY, "");
        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT, "");
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY, "");
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID, "");

        view.setTag(R.id.key, key);
        view.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        view.setTag(R.id.openmrs_entity, openMrsEntity);
        view.setTag(R.id.openmrs_entity_id, openMrsEntityId);
    }

    public static ExpansionPanelItemModel getExpansionPanelItem(String key, JSONArray value) throws JSONException {
        ExpansionPanelItemModel result = null;
        for (int j = 0; j < value.length(); j++) {
            JSONObject valueItem = value.getJSONObject(j);
            if (valueItem.getString(JsonFormConstants.KEY).equals(key)) {
                JSONArray valueItemJSONArray = valueItem.getJSONArray(JsonFormConstants.VALUES);
                result = extractExpansionPanelItems(valueItem, valueItemJSONArray);
                break;
            }
        }
        return result;
    }

    private static ExpansionPanelItemModel extractExpansionPanelItems(
            JSONObject valueItem, JSONArray valueItemJSONArray) throws JSONException {
        ExpansionPanelItemModel result;
        String selectedKeys;
        String selectedValues;
        switch (valueItem.getString(JsonFormConstants.TYPE)) {
            case JsonFormConstants.EXTENDED_RADIO_BUTTON:
            case JsonFormConstants.NATIVE_RADIO_BUTTON:
                selectedKeys = valueItemJSONArray.getString(0).split(":")[0];
                selectedValues = valueItemJSONArray.getString(0).split(":")[1];
                result = new ExpansionPanelItemModel(valueItem.getString(JsonFormConstants.KEY), selectedKeys, selectedValues);
                break;
            case JsonFormConstants.CHECK_BOX:
                StringBuilder keysStringBuilder = new StringBuilder("[");
                StringBuilder valuesStringBuilder = new StringBuilder();
                selectedKeys = formatCheckboxValues(keysStringBuilder, valueItemJSONArray, 0) + "]";
                selectedValues = formatCheckboxValues(valuesStringBuilder, valueItemJSONArray, 1);
                result = new ExpansionPanelItemModel(valueItem.getString(JsonFormConstants.KEY), selectedKeys, selectedValues);
                break;
            default:
                result = new ExpansionPanelItemModel(valueItem.getString(JsonFormConstants.KEY),
                        valueItemJSONArray.getString(0), valueItemJSONArray.getString(0));
                break;
        }
        return result;
    }

    /**
     * Returns formatted checkbox values in this format:  [item1, item2, item2]
     * Can be used to return list of selected checkbox keys or the list of values  for checkboxes
     *
     * @param sb                 String builder to be used for the formatting
     * @param valueItemJSONArray JsonArray with the selected values from the checkbox
     * @param i                  index flag used to determine whether to get list for keys/values; 0 returns key list, 1 returns values list
     * @return List of selected keys or values
     * @throws JSONException exception thrown
     */
    @NonNull
    public static String formatCheckboxValues(StringBuilder sb, JSONArray valueItemJSONArray, int i) throws JSONException {
        String result;
        for (int index = 0; index < valueItemJSONArray.length(); index++) {
            sb.append(valueItemJSONArray.getString(index).split(":")[i]);
            sb.append(", ");
        }
        result = sb.toString().replaceAll(", $", "");
        return result;
    }

    public static boolean isFieldRequired(JSONObject fieldObject) throws JSONException {
        boolean isValueRequired = false;
        if (fieldObject.has(JsonFormConstants.V_REQUIRED)) {
            JSONObject valueRequired = fieldObject.getJSONObject(JsonFormConstants.V_REQUIRED);
            String value = valueRequired.getString(JsonFormConstants.VALUE);
            isValueRequired = Boolean.parseBoolean(value);
        }
        //Don't check required for hidden, toaster notes, spacer and label widgets
        return (!fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.LABEL) &&
                !fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.SPACER) &&
                !fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.TOASTER_NOTES) &&
                !fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.HIDDEN)) &&
                isValueRequired;
    }

    public void showGenericDialog(View view) {
        Context context = (Context) view.getTag(R.id.specify_context);
        String specifyContent = (String) view.getTag(R.id.specify_content);
        String specifyContentForm = (String) view.getTag(R.id.specify_content_form);
        String stepName = (String) view.getTag(R.id.specify_step_name);
        CommonListener listener = (CommonListener) view.getTag(R.id.specify_listener);
        JsonFormFragment formFragment = (JsonFormFragment) view.getTag(R.id.specify_fragment);
        JSONArray jsonArray = (JSONArray) view.getTag(R.id.secondaryValues);
        String parentKey = (String) view.getTag(R.id.key);
        String type = (String) view.getTag(R.id.type);
        CustomTextView customTextView = (CustomTextView) view.getTag(R.id.specify_textview);
        CustomTextView reasonsTextView = (CustomTextView) view.getTag(R.id.specify_reasons_textview);
        String toolbarHeader = "";
        String container = "";
        LinearLayout rootLayout = (LinearLayout) view.getTag(R.id.main_layout);
        if (type != null && type.equals(JsonFormConstants.EXPANSION_PANEL)) {
            toolbarHeader = (String) view.getTag(R.id.header);
            container = (String) view.getTag(R.id.contact_container);
        }

        if (specifyContent != null) {
            ExpansionPanelGenericPopupDialog genericPopupDialog = new ExpansionPanelGenericPopupDialog();
            genericPopupDialog.setCommonListener(listener);
            genericPopupDialog.setFormFragment(formFragment);
            genericPopupDialog.setFormIdentity(specifyContent);
            genericPopupDialog.setFormLocation(specifyContentForm);
            genericPopupDialog.setStepName(stepName);
            genericPopupDialog.setSecondaryValues(jsonArray);
            genericPopupDialog.setParentKey(parentKey);
            genericPopupDialog.setLinearLayout(rootLayout);
            genericPopupDialog.setContext(context);
            utils.setExpansionPanelDetails(type, toolbarHeader, container, genericPopupDialog);
            genericPopupDialog.setWidgetType(type);
            if (customTextView != null && reasonsTextView != null) {
                genericPopupDialog.setCustomTextView(customTextView);
                genericPopupDialog.setPopupReasonsTextView(reasonsTextView);
            }

            utils.setChildKey(view, type, genericPopupDialog);

            FragmentTransaction fragmentTransaction = utils.getFragmentTransaction((Activity) context);
            genericPopupDialog.show(fragmentTransaction, "GenericPopup");
            resetFocus(context);
        } else {
            Toast.makeText(context, "Please specify the sub form to display ", Toast.LENGTH_LONG).show();
            Timber.e("No sub form specified. Please specify one in order to use the expansion panel.");
        }
    }

    /**
     * This clears the focus of the whole step when the popups are opened to avoid the scroll to the first edittext after
     * the popup close
     *
     * @param context {@link Context}
     */
    public void resetFocus(Context context) {
        if (context != null) {
            Activity activity = (Activity) context;
            LinearLayout mainLayout = activity.findViewById(R.id.main_layout);
            mainLayout.clearFocus();
        } else {
            Timber.d("The context is empty");
        }
    }

    public Map<String, String> addAssignedValue(String itemKey, String optionKey, String keyValue, String itemType, String itemText) {
        Map<String, String> value;
        if (genericDialogInterface != null && !TextUtils.isEmpty(genericDialogInterface.getWidgetType()) &&
                genericDialogInterface.getWidgetType().equals(JsonFormConstants.EXPANSION_PANEL)) {
            String[] labels = itemType.split(";");
            String type = "";
            if (labels.length >= 1) {
                type = labels[0];
            }
            value = returnValue(itemKey, optionKey, keyValue, itemType, itemText, type);
        } else {
            value = returnValue(itemKey, optionKey, keyValue, itemType, itemText, itemType);
        }

        return value;
    }

    private Map<String, String> returnValue(String itemKey, String optionKey, String keyValue, String itemType, String itemText, String type) {
        Map<String, String> value = new HashMap<>();
        if (!TextUtils.isEmpty(type)) {
            switch (type) {
                case JsonFormConstants.CHECK_BOX:
                    value.put(itemKey, optionKey + ":" + itemText + ":" + keyValue + ";" + itemType);
                    break;
                case JsonFormConstants.NATIVE_RADIO_BUTTON:
                case JsonFormConstants.EXTENDED_RADIO_BUTTON:
                    value.put(itemKey, keyValue + ":" + itemText + ";" + itemType);
                    break;
                default:
                    value.put(itemKey, keyValue + ";" + itemType);
                    break;
            }
        }
        return value;
    }

    public String getRadioButtonText(JSONObject item, String value) {
        String text = "";
        if (item != null && item.has(JsonFormConstants.OPTIONS_FIELD_NAME)) {
            try {
                JSONArray options = item.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
                for (int i = 0; i < options.length(); i++) {
                    JSONObject option = options.getJSONObject(i);
                    if (option != null && option.has(JsonFormConstants.KEY)) {
                        String key = option.getString(JsonFormConstants.KEY);
                        if (key.equals(value)) {
                            text = option.getString(JsonFormConstants.TEXT);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }

        }
        return text;
    }

    public String getSpecifyText(JSONArray jsonArray) {
        StringBuilder specifyText = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    String type = jsonObject.optString(JsonFormConstants.TYPE, null);
                    JSONArray itemArray = jsonObject.getJSONArray(JsonFormConstants.VALUES);
                    for (int j = 0; j < itemArray.length(); j++) {
                        String s = getValueFromSecondaryValues(type, itemArray.getString(j));
                        if (!TextUtils.isEmpty(s)) {
                            specifyText.append(s).append(",").append(" ");
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return specifyText.toString().replaceAll(", $", "");
    }

    public String getValueFromSecondaryValues(String type, String itemString) {
        String newString;
        String[] strings = itemString.split(":");
        if (type.equals(JsonFormConstants.CHECK_BOX) || type
                .equals(JsonFormConstants.NATIVE_RADIO_BUTTON)) {
            newString = strings[1];
        } else {
            if (strings.length > 1) {
                newString = strings[1];
            } else {
                newString = strings[0];
            }
        }

        return newString;
    }

    public JSONArray getSecondaryValues(JSONObject jsonObject, String type) {
        JSONArray value = new JSONArray();
        String secondaryValues = type
                .equals(JsonFormConstants.EXPANSION_PANEL) ? JsonFormConstants.VALUE
                : JsonFormConstants.SECONDARY_VALUE;

        if (jsonObject != null && jsonObject.has(secondaryValues)) {
            try {
                value = jsonObject.getJSONArray(secondaryValues);
            } catch (JSONException e) {
                Log.i(TAG, Log.getStackTraceString(e));
            }
        }

        return value;
    }

    /**
     * Gets the fields from a specific step in the json form
     *
     * @return formFields {JSONArray}
     * @author dubdabasoduba
     */
    public JSONArray getFormFields(String stepName, Context context) {
        Activity activity = (Activity) context;
        JsonApi jsonApi = (JsonApi) activity;
        JSONArray fields = new JSONArray();
        JSONObject mJSONObject = jsonApi.getmJSONObject();
        if (mJSONObject != null) {
            JSONObject parentJson = jsonApi.getStep(stepName);
            try {
                if (parentJson.has(JsonFormConstants.SECTIONS) && parentJson
                        .get(JsonFormConstants.SECTIONS) instanceof JSONArray) {
                    JSONArray sections = parentJson.getJSONArray(JsonFormConstants.SECTIONS);
                    for (int i = 0; i < sections.length(); i++) {
                        JSONObject sectionJson = sections.getJSONObject(i);
                        if (sectionJson.has(JsonFormConstants.FIELDS)) {
                            fields = concatArray(fields, sectionJson.getJSONArray(JsonFormConstants.FIELDS));
                        }
                    }
                } else if (parentJson.has(JsonFormConstants.FIELDS) && parentJson
                        .get(JsonFormConstants.FIELDS) instanceof JSONArray) {
                    fields = parentJson.getJSONArray(JsonFormConstants.FIELDS);

                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return fields;
    }

    public JSONArray concatArray(JSONArray... arrs)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); i++) {
                result.put(arr.get(i));
            }
        }
        return result;
    }

    public JSONObject getOpenMRSAttributes(JSONObject jsonObject) throws JSONException {
        String openmrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openmrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openmrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        JSONObject openmrsAttributes = new JSONObject();
        openmrsAttributes.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, openmrsEntityParent);
        openmrsAttributes.put(JsonFormConstants.OPENMRS_ENTITY, openmrsEntity);
        openmrsAttributes.put(JsonFormConstants.OPENMRS_ENTITY_ID, openmrsEntityId);

        return openmrsAttributes;
    }

    public void addValuesDisplay(List<String> expansionWidgetValues, LinearLayout contentView, Context context) {
        if (expansionWidgetValues.size() > 0) {
            if (contentView.getChildCount() > 0) {
                contentView.removeAllViews();
            }
            for (int i = 0; i < expansionWidgetValues.size(); i++) {
                String[] valueObject = expansionWidgetValues.get(i).split(":");
                if (valueObject.length >= 2 && !JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_EARLIER.equals(valueObject[1]) &&
                        !JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_TODAY.equals(valueObject[1])) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout valuesLayout = (LinearLayout) inflater.inflate(R.layout.native_expansion_panel_list_item, null);
                    CustomTextView listHeader = valuesLayout.findViewById(R.id.item_header);
                    CustomTextView listValue = valuesLayout.findViewById(R.id.item_value);
                    listValue.setTextColor(context.getResources().getColor(R.color.text_color_primary));

                    listHeader.setText(valueObject[0]);
                    listValue.setText(valueObject[1]);

                    contentView.addView(valuesLayout);
                }
            }
        }
    }

    /**
     * This updates the expansion panel child values affect the done is selected from the pop up. It also updates the
     * expansion panel status image. It changes it to green when done, yellow when ordered, grey when not done
     *
     * @param values          {@link List<String>}
     * @param statusImageView {@link ImageView}
     * @throws JSONException
     * @author dubdabasoduba
     */
    public void updateExpansionPanelRecyclerView(List<String> values, ImageView statusImageView, Context context)
            throws JSONException {
        JSONArray list = new JSONArray(values);
        for (int k = 0; k < list.length(); k++) {
            String[] stringValues = list.getString(k).split(":");
            if (stringValues.length >= 2) {
                String valueDisplay = list.getString(k).split(":")[1];
                if (valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE_TODAY) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_TODAY) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE_EARLIER) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_EARLIER) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.ORDERED) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.ORDERED) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTypesUtils.NOT_DONE) ||
                        valueDisplay.equals(JsonFormConstants.AncRadioButtonOptionTextUtils.NOT_DONE)) {

                    changeIcon(statusImageView, valueDisplay, context);
                    break;
                }
            }
        }
    }

    /**
     * Changes the Expansion panel status icon after selection
     *
     * @param imageView {@link ImageView}
     * @param type      {@link String}
     * @param context   {@link Context}
     * @author dubdabasoduba
     */
    public void changeIcon(ImageView imageView, String type, Context context) {
        if (!TextUtils.isEmpty(type)) {
            switch (type) {
                case JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE_TODAY:
                case JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_TODAY:
                case JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE:
                case JsonFormConstants.AncRadioButtonOptionTextUtils.DONE:
                case JsonFormConstants.AncRadioButtonOptionTypesUtils.DONE_EARLIER:
                case JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_EARLIER:
                    imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_done_256));
                    break;
                case JsonFormConstants.AncRadioButtonOptionTypesUtils.ORDERED:
                case JsonFormConstants.AncRadioButtonOptionTextUtils.ORDERED:
                    imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_ordered_256));
                    break;
                case JsonFormConstants.AncRadioButtonOptionTypesUtils.NOT_DONE:
                case JsonFormConstants.AncRadioButtonOptionTextUtils.NOT_DONE:
                    imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_not_done_256));
                    break;
                default:
                    imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_task_256));
                    break;
            }
        }
    }

    public Facts getCheckBoxResults(JSONObject jsonObject) throws JSONException {
        Facts result = new Facts();
        JSONArray options = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        for (int j = 0; j < options.length(); j++) {
            if (options.getJSONObject(j).has(JsonFormConstants.VALUE)) {
                if (jsonObject.has(RuleConstant.IS_RULE_CHECK) && jsonObject.getBoolean(RuleConstant.IS_RULE_CHECK)) {
                    if (Boolean.valueOf(options.getJSONObject(j).getString(JsonFormConstants.VALUE))) {//Rules engine use only true values
                        result.put(options.getJSONObject(j).getString(JsonFormConstants.KEY),
                                options.getJSONObject(j).getString(JsonFormConstants.VALUE));
                    }
                } else {
                    result.put(options.getJSONObject(j).getString(JsonFormConstants.KEY),
                            options.getJSONObject(j).getString(JsonFormConstants.VALUE));
                }
            } else {
                if (jsonObject.has(RuleConstant.IS_RULE_CHECK) && jsonObject.getBoolean(RuleConstant.IS_RULE_CHECK)) {
                    JSONArray values = jsonObject.optJSONArray(JsonFormConstants.VALUE);
                    if (values != null) {
                        JSONObject optionsObject = options.optJSONObject(j);
                        if (optionsObject != null) {
                            String key = optionsObject.optString(JsonFormConstants.KEY);
                            for (int i = 0; i < values.length(); i++) {
                                String value = values.optString(i);
                                if (value.equals(key)) {
                                    result.put(key, value);
                                }
                            }
                        }
                    }
                }
            }

            //Backward compatibility Fix
            if (jsonObject.has(RuleConstant.IS_RULE_CHECK) && !jsonObject.getBoolean(RuleConstant.IS_RULE_CHECK)) {
                if (options.getJSONObject(j).has(JsonFormConstants.VALUE)) {
                    result.put(JsonFormConstants.VALUE, options.getJSONObject(j).getString(JsonFormConstants.VALUE));
                } else {
                    result.put(JsonFormConstants.VALUE, JsonFormConstants.FALSE);
                }
            }
        }
        return result;
    }

    /**
     * @param multiRelevance {@link Boolean}
     * @param object         {@link JSONObject}
     * @return result {@link Facts}
     * @throws JSONException
     */
    public Facts getRadioButtonResults(Boolean multiRelevance, JSONObject object) throws JSONException {
        Facts result = new Facts();
        if (multiRelevance) {
            JSONArray jsonArray = object.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
            for (int j = 0; j < jsonArray.length(); j++) {
                if (object.has(JsonFormConstants.VALUE)) {
                    if (object.getString(JsonFormConstants.VALUE).equals(jsonArray.getJSONObject(j).getString(JsonFormConstants.KEY))) {
                        result.put(jsonArray.getJSONObject(j).getString(JsonFormConstants.KEY), String.valueOf(true));
                    } else {
                        if (!object.has(RuleConstant.IS_RULE_CHECK) || !object.getBoolean(RuleConstant.IS_RULE_CHECK)) {
                            result.put(jsonArray.getJSONObject(j).getString(JsonFormConstants.KEY), String.valueOf(false));
                        }
                    }
                } else {
                    Timber.e("option for Key " + jsonArray.getJSONObject(j).getString(JsonFormConstants.KEY) + " has NO value");
                }
            }
        } else {
            result.put(utils.getKey(object), utils.getValue(object));
        }

        return result;
    }

    public void updateValueToJSONArray(JSONObject jsonObject, String valueString) {
        try {
            JSONArray values = null;
            if (StringUtils.isNotEmpty(valueString)) {
                values = new JSONArray(valueString);
            }
            if (values != null) {
                jsonObject.put(JsonFormConstants.VALUE, values);
            }
        } catch (JSONException e) {
            Timber.e(e, "%s --> updateValueToJSONArray", this.getClass().getCanonicalName());
        }
    }

    public String addFormDetails(String formString) {
        String form = "";
        try {
            if (StringUtils.isNoneBlank(formString)) {
                JSONObject jsonForm = new JSONObject(formString);
                String formVersion = jsonForm.optString(JsonFormConstants.FORM_VERSION, "");
                JSONObject detailsJsonObject = jsonForm.optJSONObject(JsonFormConstants.Properties.DETAILS);

                if (detailsJsonObject == null)
                    detailsJsonObject = new JSONObject();

                detailsJsonObject.put(JsonFormConstants.Properties.APP_VERSION_NAME, BuildConfig.VERSION_NAME);
                detailsJsonObject.put(JsonFormConstants.Properties.APP_FORM_VERSION, formVersion);
                jsonForm.put(JsonFormConstants.Properties.DETAILS, detailsJsonObject);
                form = String.valueOf(jsonForm);
            }
        } catch (JSONException e) {
            Timber.e(e, "%s --> addFormDetails", this.getClass().getCanonicalName());
        }
        return form;
    }

    public boolean checkValuesContent(JSONArray value) throws JSONException {
        boolean showHiddenViews = true;
        if (value.length() == 1) {
            JSONObject jsonObject = value.getJSONObject(0);
            if (jsonObject.has(JsonFormConstants.TYPE) &&
                    JsonFormConstants.EXTENDED_RADIO_BUTTON.equals(jsonObject.getString(JsonFormConstants.TYPE))) {
                JSONArray values = jsonObject.getJSONArray(JsonFormConstants.VALUES);
                if (values.length() == 1) {
                    String object = values.getString(0);
                    if (object.contains(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_EARLIER) ||
                            object.contains(JsonFormConstants.AncRadioButtonOptionTextUtils.DONE_TODAY)) {
                        showHiddenViews = false;
                    }
                }
            }
        }
        return showHiddenViews;
    }

    public JSONArray createExpansionPanelValues(JSONArray formFields) {
        JSONArray selectedValues = new JSONArray();
        try {
            String dateField = "";
            for (int i = 0; i < formFields.length(); i++) {
                JSONObject field = formFields.getJSONObject(i);
                if (field != null && field.has(JsonFormConstants.TYPE) &&
                        !JsonFormConstants.LABEL.equals(field.getString(JsonFormConstants.TYPE)) &&
                        !JsonFormConstants.SECTIONS.equals(field.getString(JsonFormConstants.TYPE)) &&
                        !JsonFormConstants.SPACER.equals(field.getString(JsonFormConstants.TYPE)) &&
                        !JsonFormConstants.TOASTER_NOTES.equals(field.getString(JsonFormConstants.TYPE))) {
                    JSONArray valueOpenMRSAttributes = new JSONArray();
                    JSONObject openMRSAttributes = getFieldOpenMRSAttributes(field);
                    String key = field.getString(JsonFormConstants.KEY);
                    String type = field.getString(JsonFormConstants.TYPE);
                    String label;
                    if (JsonFormConstants.HIDDEN.equals(type)) {
                        label = JsonFormConstants.HIDDEN;
                    } else {
                        label = getWidgetLabel(field);
                    }
                    JSONArray values = new JSONArray();
                    if (JsonFormConstants.CHECK_BOX.equals(field.getString(JsonFormConstants.TYPE)) && field.has(JsonFormConstants.OPTIONS_FIELD_NAME)) {
                        values = getOptionsValueCheckBox(field.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME));
                        getOptionsOpenMRSAttributes(field, valueOpenMRSAttributes);
                    } else if ((JsonFormConstants.EXTENDED_RADIO_BUTTON.equals(field.getString(JsonFormConstants.TYPE)) || JsonFormConstants.NATIVE_RADIO_BUTTON.equals(field.getString(JsonFormConstants.TYPE))) && field.has(JsonFormConstants.OPTIONS_FIELD_NAME) && field.has(JsonFormConstants.VALUE)) {
                        values.put(getOptionsValueRadioButton(field.optString(JsonFormConstants.VALUE), field.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME)));
                        getOptionsOpenMRSAttributes(field, valueOpenMRSAttributes);
                    } else if (JsonFormConstants.SPINNER.equals(field.getString(JsonFormConstants.TYPE)) && field.has(JsonFormConstants.VALUE)) {
                        values.put(field.optString(JsonFormConstants.VALUE));
                        getSpinnerValueOpenMRSAttributes(field, valueOpenMRSAttributes);
                    } else {
                        if (field.has(JsonFormConstants.VALUE)) {
                            values.put(field.optString(JsonFormConstants.VALUE));
                            if (JsonFormConstants.HIDDEN.equals(type) && key.contains(JsonFormConstants.DATE_TODAY_HIDDEN)) {
                                dateField = key + ":" + field.optString(JsonFormConstants.VALUE);
                            }
                        } else {
                            if (JsonFormConstants.DATE_PICKER.equals(type) && dateField.contains(key)) {
                                String[] datePickerValues = dateField.split(":");
                                if (datePickerValues.length > 1 && !datePickerValues[1].equals("0")) {
                                    values.put(datePickerValues[1]);
                                }
                            }
                        }
                    }

                    if (values.length() > 0) {
                        if (!TextUtils.isEmpty(label) && field.has(JsonFormConstants.INDEX)) {
                            int index = field.optInt(JsonFormConstants.INDEX);
                            if (JsonFormConstants.HIDDEN.equals(type)) {
                                label = "";
                            }
                            selectedValues.put(createValueObject(key, type, label, index, values, openMRSAttributes,
                                    valueOpenMRSAttributes));
                        } else {
                            selectedValues.put(createSecondaryValueObject(key, type, values, openMRSAttributes,
                                    valueOpenMRSAttributes));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e, " --> createExpansionPanelValues");
        }

        return selectedValues;
    }

    public JSONObject getFieldOpenMRSAttributes(JSONObject item) throws JSONException {
        JSONObject openMRSAttribute = new JSONObject();
        openMRSAttribute
                .put(JsonFormConstants.OPENMRS_ENTITY_PARENT, item.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT));
        openMRSAttribute.put(JsonFormConstants.OPENMRS_ENTITY, item.getString(JsonFormConstants.OPENMRS_ENTITY));
        openMRSAttribute.put(JsonFormConstants.OPENMRS_ENTITY_ID, item.getString(JsonFormConstants.OPENMRS_ENTITY_ID));
        return openMRSAttribute;
    }

    /**
     * This Native radio block caters for situations where the radio button used has no label provide but is required to be displayed on the answers on the expansion panel content.
     * Display label will be used and the label when the answers are display.
     */
    private String getWidgetLabel(JSONObject jsonObject) throws JSONException {
        String label = "";
        String widgetType = jsonObject.getString(JsonFormConstants.TYPE);
        if (!TextUtils.isEmpty(widgetType)) {
            switch (widgetType) {
                case JsonFormConstants.EDIT_TEXT:
                case JsonFormConstants.DATE_PICKER:
                    label = jsonObject.optString(JsonFormConstants.HINT, "");
                    break;
                case JsonFormConstants.NATIVE_RADIO_BUTTON:
                    if (StringUtils.isNotBlank(jsonObject.optString(JsonFormConstants.DISPLAY_LABEL))) {
                        label = jsonObject.optString(JsonFormConstants.DISPLAY_LABEL, "");
                    } else {
                        label = jsonObject.optString(JsonFormConstants.LABEL, "");
                    }
                    break;
                default:
                    label = jsonObject.optString(JsonFormConstants.LABEL, "");
                    break;
            }
        }
        return label;
    }

    public JSONArray getOptionsValueCheckBox(JSONArray options) throws JSONException {
        JSONArray secondaryValues = new JSONArray();
        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.getJSONObject(i);
            if (option.has(JsonFormConstants.KEY) && option.has(JsonFormConstants.VALUE) &&
                    JsonFormConstants.TRUE.equals(option.getString(JsonFormConstants.VALUE))) {
                String key = option.getString(JsonFormConstants.KEY);
                String text = option.getString(JsonFormConstants.TEXT);
                String secondaryValue = key + ":" + text + ":" + JsonFormConstants.TRUE;
                secondaryValues.put(secondaryValue);
            }
        }
        return secondaryValues;
    }

    public void getOptionsOpenMRSAttributes(JSONObject item, JSONArray valueOpenMRSAttributes) throws JSONException {
        JSONArray options = item.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        if (options.length() > 0) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject itemOption = options.getJSONObject(i);
                if ((JsonFormConstants.NATIVE_RADIO_BUTTON.equals(item.getString(JsonFormConstants.TYPE)) ||
                        JsonFormConstants.EXTENDED_RADIO_BUTTON.equals(item.getString(JsonFormConstants.TYPE))) &&
                        item.has(JsonFormConstants.VALUE)) {
                    String value = item.optString(JsonFormConstants.VALUE);
                    if (itemOption.has(JsonFormConstants.KEY) && value.equals(itemOption.getString(JsonFormConstants.KEY))) {
                        extractOptionOpenMRSAttributes(valueOpenMRSAttributes, itemOption,
                                item.getString(JsonFormConstants.KEY));
                    }
                } else if (JsonFormConstants.CHECK_BOX.equals(item.getString(JsonFormConstants.TYPE)) &&
                        itemOption.has(JsonFormConstants.VALUE) &&
                        JsonFormConstants.TRUE.equals(itemOption.getString(JsonFormConstants.VALUE))) {
                    extractOptionOpenMRSAttributes(valueOpenMRSAttributes, itemOption,
                            item.getString(JsonFormConstants.KEY));
                }
            }
        }
    }

    public String getOptionsValueRadioButton(String value, JSONArray options) throws JSONException {
        String secondaryValue = "";
        if (!TextUtils.isEmpty(value)) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                if (option.has(JsonFormConstants.KEY) && value.equals(option.getString(JsonFormConstants.KEY))) {
                    String key = option.getString(JsonFormConstants.KEY);
                    String text = option.getString(JsonFormConstants.TEXT);
                    secondaryValue = key + ":" + text;
                    break;
                }
            }
        }
        return secondaryValue;
    }

    public void getSpinnerValueOpenMRSAttributes(JSONObject item, JSONArray valueOpenMRSAttributes) throws JSONException {

        if (item == null || !item.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.SPINNER)) {
            return;
        }

        String spinnerValue = item.getString(JsonFormConstants.VALUE);
        String spinnerKey = item.getString(JsonFormConstants.KEY);
        if (item.has(JsonFormConstants.OPENMRS_CHOICE_IDS)) {
            JSONObject openMRSChoiceIds = item.getJSONObject(JsonFormConstants.OPENMRS_CHOICE_IDS);
            Iterator<String> keys = openMRSChoiceIds.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (spinnerValue.equals(key)) {
                    addOpenMRSAttributes(valueOpenMRSAttributes, item, spinnerKey,
                            openMRSChoiceIds.getString(key));
                    break;
                }
            }
        } else if (item.has(JsonFormConstants.OPTIONS_FIELD_NAME)) {
            // if an options block is defined
            JSONArray options = item.optJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                if (option.get(JsonFormConstants.KEY).equals(spinnerValue)) {
                    addOpenMRSAttributes(valueOpenMRSAttributes, option, spinnerKey,
                            option.getString(JsonFormConstants.OPENMRS_ENTITY_ID));
                    break;
                }
            }
        }
    }


    private void addOpenMRSAttributes(JSONArray valueOpenMRSAttributes, JSONObject item, String key, String openMRSEntityId) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.KEY, key);
        jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, item.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT));
        jsonObject.put(JsonFormConstants.OPENMRS_ENTITY, item.getString(JsonFormConstants.OPENMRS_ENTITY));
        jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_ID, openMRSEntityId);

        valueOpenMRSAttributes.put(jsonObject);
    }

    private JSONObject createValueObject(String key, String type, String label, int index, JSONArray values, JSONObject openMRSAttributes, JSONArray valueOpenMRSAttributes) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (values.length() > 0) {
                jsonObject.put(JsonFormConstants.KEY, key);
                jsonObject.put(JsonFormConstants.TYPE, type);
                jsonObject.put(JsonFormConstants.LABEL, label);
                jsonObject.put(JsonFormConstants.INDEX, index);
                jsonObject.put(JsonFormConstants.VALUES, values);
                jsonObject.put(JsonFormConstants.OPENMRS_ATTRIBUTES, openMRSAttributes);
                if (valueOpenMRSAttributes.length() > 0) {
                    jsonObject.put(JsonFormConstants.VALUE_OPENMRS_ATTRIBUTES, valueOpenMRSAttributes);
                }
            }
        } catch (JSONException e) {
            Timber.e(e, " --> createValueObject");

        }
        return jsonObject;
    }

    /**
     * @param key
     * @param type
     * @param values
     * @param openMRSAttributes
     * @param valueOpenMRSAttributes
     * @return
     */
    public JSONObject createSecondaryValueObject(String key, String type, JSONArray values, JSONObject openMRSAttributes,
                                                 JSONArray valueOpenMRSAttributes) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (values.length() > 0) {
                jsonObject.put(JsonFormConstants.KEY, key);
                jsonObject.put(JsonFormConstants.TYPE, type);
                jsonObject.put(JsonFormConstants.VALUES, values);
                jsonObject.put(JsonFormConstants.OPENMRS_ATTRIBUTES, openMRSAttributes);
                if (valueOpenMRSAttributes.length() > 0) {
                    jsonObject.put(JsonFormConstants.VALUE_OPENMRS_ATTRIBUTES, valueOpenMRSAttributes);
                }
            }
        } catch (JSONException e) {
            Timber.e(e, " --> createSecondaryValueObject");

        }
        return jsonObject;
    }

    /**
     * Extracts the openmrs attributes of the Radio button & check box components on popups.
     *
     * @param valueOpenMRSAttributes {@link JSONArray}
     * @param itemOption             {@link JSONObject}
     * @param itemKey                {@link String}
     * @throws JSONException
     */
    protected void extractOptionOpenMRSAttributes(JSONArray valueOpenMRSAttributes, JSONObject itemOption, String itemKey)
            throws JSONException {
        if (itemOption.has(JsonFormConstants.OPENMRS_ENTITY_PARENT) && itemOption.has(JsonFormConstants.OPENMRS_ENTITY) &&
                itemOption.has(JsonFormConstants.OPENMRS_ENTITY_ID)) {
            String openmrsEntityParent = itemOption.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
            String openmrsEntity = itemOption.getString(JsonFormConstants.OPENMRS_ENTITY);
            String openmrsEntityId = itemOption.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

            JSONObject valueOpenMRSObject = new JSONObject();
            valueOpenMRSObject.put(JsonFormConstants.KEY, itemKey);
            valueOpenMRSObject.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, openmrsEntityParent);
            valueOpenMRSObject.put(JsonFormConstants.OPENMRS_ENTITY, openmrsEntity);
            valueOpenMRSObject.put(JsonFormConstants.OPENMRS_ENTITY_ID, openmrsEntityId);

            valueOpenMRSAttributes.put(valueOpenMRSObject);
        }
    }

    /**
     * Loads the values from the expansion panel
     *
     * @param formFields {@link JSONArray} -- The form values mostly the accordion widgets
     * @param parentKey  {@link String} -- the accordion widget key
     * @return values {@link JSONArray} -- the extracted accordion values
     */
    public JSONArray loadExpansionPanelValues(JSONArray formFields, String parentKey) {
        JSONArray values = new JSONArray();
        try {
            if (formFields != null && formFields.length() > 0) {
                for (int i = 0; i < formFields.length(); i++) {
                    JSONObject item = formFields.getJSONObject(i);
                    if (item.has(JsonFormConstants.KEY) && item.getString(JsonFormConstants.KEY).equals(parentKey) && item.has(JsonFormConstants.VALUE)) {
                        values = item.getJSONArray(JsonFormConstants.VALUE);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e, " --> loadExpansionPanelValues");
        }
        return values;
    }

    /**
     * Creates the expasnion panel secondary values maps
     *
     * @param secondaryValues {@link JSONArray}
     * @return expansionPanelValuesMap {@link Map<>}
     */
    public Map<String, ExpansionPanelValuesModel> createSecondaryValuesMap(JSONArray secondaryValues) {
        Map<String, ExpansionPanelValuesModel> expansionPanelValuesModelMap = new HashMap<>();
        if (secondaryValues != null && secondaryValues.length() > 0) {
            for (int i = 0; i < secondaryValues.length(); i++) {
                if (!secondaryValues.isNull(i)) {
                    try {
                        JSONObject jsonObject = secondaryValues.getJSONObject(i);
                        String key = jsonObject.getString(JsonFormConstants.KEY);
                        String type = jsonObject.getString(JsonFormConstants.TYPE);
                        String label = jsonObject.getString(JsonFormConstants.LABEL);
                        JSONArray values = jsonObject.getJSONArray(JsonFormConstants.VALUES);
                        int index = jsonObject.optInt(JsonFormConstants.INDEX);

                        JSONObject openmrsAttributes = getSecondaryOpenMRSAttributes(jsonObject);
                        JSONArray valueOpenMRSAttributes = getValueOpenMRSAttributes(jsonObject);

                        expansionPanelValuesModelMap.put(key,
                                new ExpansionPanelValuesModel(key, type, label, index, values, openmrsAttributes,
                                        valueOpenMRSAttributes));
                    } catch (JSONException e) {
                        Timber.e(e, " --> createSecondaryValuesMap");
                    }
                }
            }
        }

        return expansionPanelValuesModelMap;
    }

    /**
     * Gets the expansion secondary values openmrs attributtes
     *
     * @param jsonObject {@link JSONObject} -- expansion panel value item.
     * @return openmrsAttributtes {@link JSONObject}
     * @throws JSONException
     */
    public JSONObject getSecondaryOpenMRSAttributes(JSONObject jsonObject) throws JSONException {
        JSONObject openmrsAttributes = new JSONObject();
        if (jsonObject.has(JsonFormConstants.OPENMRS_ATTRIBUTES)) {
            openmrsAttributes = jsonObject.getJSONObject(JsonFormConstants.OPENMRS_ATTRIBUTES);
        }
        return openmrsAttributes;
    }

    /**
     * Gets the expansion secondary values, value openmrs attributes
     *
     * @param jsonObject {@link JSONObject} -- expansion panel value item.
     * @return valueOpenmrsAttributes {@link JSONObject}
     * @throws JSONException
     */
    public JSONArray getValueOpenMRSAttributes(JSONObject jsonObject) throws JSONException {
        JSONArray valueOpenMRSAttributes = new JSONArray();
        if (jsonObject.has(JsonFormConstants.VALUE_OPENMRS_ATTRIBUTES)) {
            valueOpenMRSAttributes = jsonObject.getJSONArray(JsonFormConstants.VALUE_OPENMRS_ATTRIBUTES);
        }
        return valueOpenMRSAttributes;
    }

    /**
     * Assigns each subform widget its value from the expansion panels values attributte
     *
     * @param fields                       {@link JSONArray} -- subforms fields.
     * @param expansionPanelValuesModelMap {@link Map} -- secondary values map
     * @return fields {@link JSONArray} -- all the sub form fields
     */
    public JSONArray addExpansionPanelFormValues(JSONArray fields, Map<String, ExpansionPanelValuesModel> expansionPanelValuesModelMap) {
        if (fields != null && expansionPanelValuesModelMap != null) {
            for (int i = 0; i < fields.length(); i++) {
                JSONObject formValue;
                try {
                    formValue = fields.getJSONObject(i);
                    String key = formValue.getString(JsonFormConstants.KEY);
                    formValue.put(JsonFormConstants.INDEX, String.valueOf(i));
                    if (expansionPanelValuesModelMap.containsKey(key)) {
                        SecondaryValueModel secondaryValueModel = expansionPanelValuesModelMap.get(key);
                        String type = secondaryValueModel.getType();
                        if (type != null && (type.equals(JsonFormConstants.CHECK_BOX))) {
                            if (formValue.has(JsonFormConstants.OPTIONS_FIELD_NAME)) {
                                JSONArray options = formValue.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
                                JSONArray values = secondaryValueModel.getValues();
                                setCompoundButtonValues(options, values);
                            }
                        } else {
                            JSONArray values = secondaryValueModel.getValues();
                            if (type != null && (type.equals(JsonFormConstants.NATIVE_RADIO_BUTTON) ||
                                    type.equals(JsonFormConstants.EXTENDED_RADIO_BUTTON))) {
                                for (int k = 0; k < values.length(); k++) {
                                    formValue.put(JsonFormConstants.VALUE, getValueKey(values.getString(k)));
                                }
                            } else {
                                formValue.put(JsonFormConstants.VALUE, setValues(values, type));
                            }
                        }
                    }
                } catch (JSONException e) {
                    Timber.e(e, " --> loadSubForms");
                }
            }
        }
        return fields;
    }

    public void setCompoundButtonValues(JSONArray options, JSONArray secondValues) {
        for (int i = 0; i < options.length(); i++) {
            JSONObject jsonObject;
            try {
                jsonObject = options.getJSONObject(i);
                String mainKey = jsonObject.getString(JsonFormConstants.KEY);
                for (int j = 0; j < secondValues.length(); j++) {
                    String key = getValueKey(secondValues.getString(j));
                    if (mainKey.equals(key)) {
                        jsonObject.put(JsonFormConstants.VALUE, true);
                        break;
                    }
                }
            } catch (JSONException e) {
                Timber.e(e, " --> setCompoundButtonValues");
            }
        }
    }

    public String getValueKey(String value) {
        String key = "";
        String[] strings = value.split(":");
        if (strings.length > 0) {
            key = strings[0];
        }
        return key;
    }

    public String setValues(JSONArray jsonArray, String type) {
        FormUtils formUtils = new FormUtils();
        String value = "";
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                value = formUtils.getValueFromSecondaryValues(type, jsonArray.getString(i));
            } catch (JSONException e) {
                Timber.e(e, " --> setValues");
            }
        }

        return value.replaceAll(", $", "");
    }

    @Nullable
    public JSONObject getFormJsonFromRepositoryOrAssets(@NonNull Context context, @NonNull String formIdentity) throws JSONException {
        ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
        return getFormJsonFromRepositoryOrAssetsWithOptionalCallback(context, clientFormRepository, formIdentity, null);
    }

    @Nullable
    public JSONObject getFormJsonFromRepositoryOrAssets(@NonNull Context context, @NonNull ClientFormContract.Dao clientFormRepository, @NonNull String formIdentity) throws JSONException {
        return getFormJsonFromRepositoryOrAssetsWithOptionalCallback(context, clientFormRepository, formIdentity, null);
    }


    /**
     * Fetches the JSON form from the repository or assets folder and handles the JSONException thrown
     * by providing the user with rollback capability. The rollback form chosen by the user will be
     * returned in the callback
     *
     * @param context
     * @param formIdentity
     * @param onFormFetchedCallback
     */
    public void getFormJsonFromRepositoryOrAssets(@NonNull Context context, @NonNull String formIdentity, @NonNull OnFormFetchedCallback<JSONObject> onFormFetchedCallback) {
        ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
        try {
            getFormJsonFromRepositoryOrAssetsWithOptionalCallback(context, clientFormRepository, formIdentity, onFormFetchedCallback);
        } catch (JSONException ex) {
            Timber.wtf(ex, "This should never happen --> Error was handled but onFormFetchedCallback was NULL");
        }
    }


    public void getFormJsonFromRepositoryOrAssets(@NonNull Context context, @NonNull ClientFormContract.Dao clientFormRepository, @NonNull String formIdentity, @Nullable OnFormFetchedCallback<JSONObject> onFormFetchedCallback) {
        try {
            getFormJsonFromRepositoryOrAssetsWithOptionalCallback(context, clientFormRepository, formIdentity, onFormFetchedCallback);
        } catch (JSONException ex) {
            Timber.wtf(ex, "This should never happen --> Error was handled but onFormFetchedCallback was NULL");
        }
    }

    /**
     * Fetches the JSON form from the repository or assets folder and handles the JSONException thrown
     * by providing the user with rollback capability. The rollback form chosen by the user will be
     * * returned in the callback
     *
     * @param context
     * @param clientFormRepository
     * @param formIdentity
     * @param onFormFetchedCallback
     * @return
     * @throws JSONException
     */
    private JSONObject getFormJsonFromRepositoryOrAssetsWithOptionalCallback(@NonNull Context context, @Nullable ClientFormContract.Dao clientFormRepository, String formIdentity, @Nullable final OnFormFetchedCallback<JSONObject> onFormFetchedCallback) throws JSONException {
        if (clientFormRepository != null) {
            ClientFormContract.Model clientForm = getClientFormFromRepository(context, clientFormRepository, formIdentity);

            try {
                if (clientForm != null) {
                    Timber.d("============%s form loaded from db============", formIdentity);
                    String formVersion = clientForm.getVersion();
                    JSONObject formJson = new JSONObject(clientForm.getJson());
                    formJson.put(JsonFormConstants.FORM_VERSION, formVersion);
                    injectFormStatus(formJson, clientForm);

                    if (onFormFetchedCallback != null) {
                        onFormFetchedCallback.onFormFetched(formJson);
                        return null;
                    } else {
                        return formJson;
                    }
                }
            } catch (JSONException e) {
                Timber.e(e);

                if (onFormFetchedCallback != null) {
                    handleJsonFormOrRulesError(context, clientFormRepository, false, formIdentity, new OnFormFetchedCallback<String>() {
                        @Override
                        public void onFormFetched(@Nullable String form) {
                            try {
                                JSONObject jsonObject = form == null ? null : new JSONObject(form);
                                onFormFetchedCallback.onFormFetched(jsonObject);
                            } catch (JSONException ex) {
                                Timber.e(ex);
                            }
                        }
                    });
                } else {
                    throw e;
                }
            }
        }

        Timber.d("============%s form loaded from Assets=============", formIdentity);
        JSONObject jsonObject = getFormJson(context, formIdentity);

        if (onFormFetchedCallback != null) {
            onFormFetchedCallback.onFormFetched(jsonObject);
            return null;
        } else {
            return jsonObject;
        }
    }

    public JSONObject getFormJson(@NonNull Context context, @NonNull String formIdentity) {
        try {
            String locale = context.getResources().getConfiguration().locale.getLanguage();
            locale = locale.equalsIgnoreCase(Locale.ENGLISH.getLanguage()) ? "" : "-" + locale;

            InputStream inputStream;
            try {
                inputStream = context.getApplicationContext().getAssets()
                        .open("json.form" + locale + "/" + formIdentity + JsonFormConstants.JSON_FILE_EXTENSION);
            } catch (FileNotFoundException e) {
                // file for the language not found, defaulting to english language
                inputStream = context.getApplicationContext().getAssets()
                        .open("json.form/" + formIdentity + JsonFormConstants.JSON_FILE_EXTENSION);
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, CharEncoding.UTF_8));
            String jsonString;
            StringBuilder stringBuilder = new StringBuilder();

            while ((jsonString = reader.readLine()) != null) {
                stringBuilder.append(jsonString);
            }
            inputStream.close();

            return new JSONObject(stringBuilder.toString());
        } catch (IOException | JSONException e) {
            Timber.e(e);
            return null;
        }
    }

    protected String getLocaleFormIdentity(final Context context, final String formIdentity){
        String locale = context.getResources().getConfiguration().locale.getLanguage();
        if (!Locale.ENGLISH.getLanguage().equals(locale)) {
            return formIdentity + "-" + locale;
        }
        return formIdentity;
    }

    private ClientFormContract.Model getClientFormFromRepository(@NonNull Context context, @NonNull ClientFormContract.Dao clientFormRepository, String formIdentity) {
        //Check the current locale of the app to load the correct version of the form in the desired language
        String localeFormIdentity = getLocaleFormIdentity(context, formIdentity);

        ClientFormContract.Model clientForm = clientFormRepository.getActiveClientFormByIdentifier(localeFormIdentity);

        if (clientForm == null) {
            String revisedFormName = extractFormNameWithoutExtension(localeFormIdentity);
            clientForm = clientFormRepository.getActiveClientFormByIdentifier(revisedFormName);
        }
        return clientForm;
    }

    public void handleJsonFormOrRulesError(@NonNull Context context, @NonNull String formIdentity, @NonNull OnFormFetchedCallback<String> onFormFetchedCallback) {
        ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
        if (clientFormRepository != null) {
            handleJsonFormOrRulesError(context, clientFormRepository, false, formIdentity, onFormFetchedCallback);
        } else {
            Timber.e(new Exception(), "Cannot handle JSON Form/Rules File error because client form respository is null");
        }
    }

    public void handleJsonFormOrRulesError(@NonNull Context context, @NonNull ClientFormContract.Dao clientFormRepository, @NonNull String formIdentity, @NonNull OnFormFetchedCallback<String> onFormFetchedCallback) {
        handleJsonFormOrRulesError(context, clientFormRepository, false, formIdentity, onFormFetchedCallback);
    }

    public void handleJsonFormOrRulesError(@NonNull final Context context, @NonNull final ClientFormContract.Dao clientFormRepository, final boolean isRulesFile, @NonNull final String formIdentity, @NonNull final OnFormFetchedCallback<String> onFormFetchedCallback) {
        final ClientFormContract.Model clientForm = getClientFormFromRepository(context, clientFormRepository, formIdentity);
        List<ClientFormContract.Model> clientForms = clientFormRepository.getClientFormByIdentifier(clientForm.getIdentifier());

        if (clientForms.size() > 0) {
            // Show dialog asking user if they want to rollback to the previous available version X
            // if YES, then provide that form instead
            // if NO, then continue down

            boolean dialogIsShowing = (context instanceof ClientFormContract.View) && ((ClientFormContract.View) context).isVisibleFormErrorAndRollbackDialog();

            if (!dialogIsShowing) {
                FormRollbackDialogUtil.showAvailableRollbackFormsDialog(context, clientFormRepository, clientForms, clientForm, new RollbackDialogCallback() {
                    @Override
                    public void onFormSelected(@NonNull ClientFormContract.Model selectedForm) {
                        if (selectedForm.getJson() == null && selectedForm.getVersion().equals(JsonFormConstants.CLIENT_FORM_ASSET_VERSION)) {

                            if (isRulesFile) {
                                try {
                                    clientForm.setJson(convertStreamToString(context.getAssets().open(formIdentity)));
                                } catch (IOException e) {
                                    Timber.e(e);
                                }
                            } else {
                                try {
                                    JSONObject jsonObject = getFormJson(context, formIdentity);
                                    String formVersion = clientForm.getVersion();
                                    jsonObject.put(JsonFormConstants.FORM_VERSION, formVersion);

                                    if (jsonObject != null) {
                                        clientForm.setJson(jsonObject.toString());
                                    }
                                } catch (JSONException e) {
                                    Timber.e(e);
                                }
                            }
                        }

                        onFormFetchedCallback.onFormFetched(clientForm.getJson());
                    }

                    @Override
                    public void onCancelClicked() {
                        onFormFetchedCallback.onFormFetched(null);
                    }
                });
            }
        }
    }

    @Nullable
    public JSONObject getSubFormJsonFromRepository(@NonNull Context context, @NonNull ClientFormContract.Dao clientFormDao, String formIdentity, String subFormsLocation, boolean translateSubForm) throws JSONException {
        //Check the current locale of the app to load the correct version of the form in the desired language
        String localeFormIdentity = getLocaleFormIdentity(context, formIdentity);

        String dbFormName = StringUtils.isBlank(subFormsLocation) ? localeFormIdentity : subFormsLocation + "/" + localeFormIdentity;
        ClientFormContract.Model clientForm = clientFormDao.getActiveClientFormByIdentifier(dbFormName);

        if (clientForm == null) {
            String revisedFormName = extractFormNameWithoutExtension(dbFormName);
            clientForm = clientFormDao.getActiveClientFormByIdentifier(revisedFormName);

            if (clientForm == null) {
                String finalSubFormsLocation = getSubFormLocation(subFormsLocation);
                dbFormName = StringUtils.isBlank(finalSubFormsLocation) ? localeFormIdentity : finalSubFormsLocation + "/" + localeFormIdentity;
                clientForm = clientFormDao.getActiveClientFormByIdentifier(dbFormName);
            }
        }

        if (clientForm != null) {
            Timber.d("============%s form loaded from db============", dbFormName);
            String originalJson = clientForm.getJson();

            if (translateSubForm) {
                originalJson = NativeFormLangUtils.getTranslatedStringWithDBResourceBundle(context, originalJson, null);
            }
            return new JSONObject(originalJson);
        }

        return null;
    }

    @Nullable
    public BufferedReader getRulesFromRepository(@NonNull Context context, @NonNull ClientFormContract.Dao clientFormDao, @NonNull String fileName) {
        //Check the current locale of the app to load the correct version of the form in the desired language
        String localeFormIdentity = getLocaleFormIdentity(context, fileName);

        ClientFormContract.Model clientForm = clientFormDao.getActiveClientFormByIdentifier(localeFormIdentity);
        if (clientForm == null && StringUtils.isNotBlank(fileName) && fileName.contains("/") && !fileName.endsWith("/")) {
            // Strip anything before the '/'
            localeFormIdentity =  localeFormIdentity.split("/")[1];
            //retry with just the filename without the file path prefix
            clientForm = clientFormDao.getActiveClientFormByIdentifier(localeFormIdentity);

        }
        if (clientForm != null) {
            Timber.d("============%s form loaded from db============", localeFormIdentity);
            String originalJson = clientForm.getJson();

            return new BufferedReader(new StringReader(originalJson));
        }

        return null;
    }

    public String getPropertiesFileContentsFromDB(String identifier) {
        ClientFormContract.Dao clientFormRepository = NativeFormLibrary.getInstance().getClientFormDao();
        if (clientFormRepository != null) {
            ClientFormContract.Model clientForm = clientFormRepository.getActiveClientFormByIdentifier(identifier);
            if (clientForm != null) {
                return clientForm.getJson();
            }
        }
        return null;
    }

    @NonNull
    protected String extractFormNameWithoutExtension(String localeFormIdentity) {
        return localeFormIdentity.endsWith(JsonFormConstants.JSON_FILE_EXTENSION)
                ? localeFormIdentity.substring(0, localeFormIdentity.length() - JsonFormConstants.JSON_FILE_EXTENSION.length()) :
                localeFormIdentity + JsonFormConstants.JSON_FILE_EXTENSION;
    }

    public void injectFormStatus(@NonNull JSONObject jsonObject, @NonNull ClientFormContract.Model clientForm) {
        if (clientForm.isNew()) {
            try {
                jsonObject.put(JsonFormConstants.Properties.IS_NEW, clientForm.isNew());
                jsonObject.put(JsonFormConstants.Properties.CLIENT_FORM_ID, clientForm.getId());
                jsonObject.put(JsonFormConstants.Properties.FORM_VERSION, clientForm.getVersion());
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }

    public static int getClientFormId(@NonNull JSONObject jsonObject) {
        try {
            return jsonObject.getInt(JsonFormConstants.Properties.CLIENT_FORM_ID);
        } catch (JSONException e) {
            Timber.e(e);
            return 0;
        }
    }

    public static boolean isFormNew(@NonNull JSONObject jsonObject) {
        return jsonObject.optBoolean(JsonFormConstants.Properties.IS_NEW, false);
    }

}
