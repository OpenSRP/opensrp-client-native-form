package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vijay on 24-05-2015.
 */
public class LabelFactory implements FormWidgetFactory {
    private final String TAG = this.getClass().getSimpleName();
    private CustomTextView numberText;
    private FormUtils formUtils = new FormUtils();

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener
                                               listener, boolean popup) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, false);
    }

    private List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener
            listener, boolean popup) throws JSONException {
        List<View> views = new ArrayList<>(1);
        if (jsonObject.has(JsonFormConstants.TEXT)) {
            JSONArray canvasIds = new JSONArray();
            ConstraintLayout constraintLayout = formUtils.createLabelLinearLayout(stepName, canvasIds, jsonObject, context, listener);

            constraintLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));

            boolean hasBg = jsonObject.optBoolean("has_bg", false);
            String topMargin = jsonObject.optString("top_margin", "0dp");
            String bottomMargin = null;
            if (hasBg) {
                bottomMargin = jsonObject.optString("bottom_margin", "0dp");
            }

            int topMarginInt = getValueFromSpOrDpOrPx(context, topMargin);
            int bottomMarginInt = (int) context.getResources().getDimension(R.dimen.default_bottom_margin);
            if (hasBg) {
                bottomMarginInt = getValueFromSpOrDpOrPx(context, bottomMargin);
            }

            LinearLayout.LayoutParams layoutParams = FormUtils
                    .getLinearLayoutParams(FormUtils.MATCH_PARENT, FormUtils.WRAP_CONTENT, 0,
                            topMarginInt, 0, bottomMarginInt);
            constraintLayout.setLayoutParams(layoutParams);

            createLabelTextView(jsonObject, context, constraintLayout);

            // Set the id for the view
            constraintLayout.setId(ViewUtil.generateViewId());
            canvasIds.put(constraintLayout.getId());
            constraintLayout.setTag(R.id.canvas_ids, canvasIds.toString());
            constraintLayout.setTag(R.id.extraPopup, popup);
            views.add(constraintLayout);
        } else {
            Log.e(TAG, "A label requires a text. You cannot have a label with blank text");
        }
        return views;
    }

    public int getValueFromSpOrDpOrPx(Context context, String bottomMargin) {
        return FormUtils.getValueFromSpOrDpOrPx(bottomMargin, context);
    }

    /**
     * Instantiates the label Custom TextView and adds in its attributes
     *
     * @param jsonObject
     * @param context
     * @param constraintLayout
     * @throws JSONException
     */
    private void createLabelTextView(JSONObject jsonObject, Context context, ConstraintLayout constraintLayout)
            throws JSONException {
        boolean hintOnText = jsonObject.optBoolean(JsonFormConstants.HINT_ON_TEXT, false);
        boolean hasBg = jsonObject.optBoolean(JsonFormConstants.HAS_BG, false);
        String labelNumber = jsonObject.optString(JsonFormConstants.LABEL_NUMBER, null);

        String bgColor = null;
        String topPadding = null;
        String bottomPadding = null;
        String leftPadding = null;
        String rightPadding = null;
        if (hasBg) {
            bgColor = jsonObject.optString(JsonFormConstants.BG_COLOR, "#F3F3F3");
            topPadding = jsonObject.optString(JsonFormConstants.TOP_PADDING, "5dp");
            bottomPadding = jsonObject.optString(JsonFormConstants.BOTTOM_PADDING, "5dp");
            leftPadding = jsonObject.optString(JsonFormConstants.LEFT_PADDING, "5dp");
            rightPadding = jsonObject.optString(JsonFormConstants.RIGHT_PADDING, "5dp");
        }

        int bgColorInt = 0;
        if (hasBg) {
            bgColorInt = Color.parseColor(bgColor);
        }

        CustomTextView labelText = constraintLayout.findViewById(R.id.label_text);
        if (bgColorInt != 0) {
            labelText.setBackgroundColor(bgColorInt);
            if (labelNumber != null && numberText != null) {
                numberText.setBackgroundColor(bgColorInt);
            }
        }

        if (hasBg) {
            labelText.setPadding(
                    getValueFromSpOrDpOrPx(context, leftPadding),
                    getValueFromSpOrDpOrPx(context, topPadding),
                    getValueFromSpOrDpOrPx(context, rightPadding),
                    getValueFromSpOrDpOrPx(context, bottomPadding)
            );
        }
        int labelTextSize = getValueFromSpOrDpOrPx(context, jsonObject.optString(JsonFormConstants.TEXT_SIZE, String.valueOf(context.getResources().getDimension(R
                .dimen.default_label_text_size))));
        String textStyle = jsonObject.optString(JsonFormConstants.TEXT_STYLE, JsonFormConstants.NORMAL);
        FormUtils.setTextStyle(textStyle, labelText);
        labelText.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        labelText.setTextSize(labelTextSize);
        labelText.setEnabled(!jsonObject
                .optBoolean(JsonFormConstants.READ_ONLY, false));//Gotcha: Should be set before createLabelText is used
        labelText.setHintOnText(hintOnText);//Gotcha: Should be set before createLabelText is used
        labelText.setText(createLabelText(jsonObject));

        createNumberLabel(constraintLayout, labelNumber, jsonObject, labelTextSize, textStyle, context);
    }

    private void createNumberLabel(ConstraintLayout constraintLayout, String labelNumber, JSONObject jsonObject,
                                   int labelTextSize,
                                   String textStyle, Context context) {
        if (!TextUtils.isEmpty(labelNumber)) {
            boolean hasBg = jsonObject.optBoolean(JsonFormConstants.HAS_BG, false);

            String topPadding = null;
            String bottomPadding = null;
            String leftPadding = null;
            String rightPadding = null;
            if (hasBg) {
                topPadding = jsonObject.optString(JsonFormConstants.TOP_PADDING, "5dp");
                bottomPadding = jsonObject.optString(JsonFormConstants.BOTTOM_PADDING, "5dp");
                leftPadding = jsonObject.optString(JsonFormConstants.LEFT_PADDING, "5dp");
                rightPadding = jsonObject.optString(JsonFormConstants.RIGHT_PADDING, "5dp");
            }


            numberText = constraintLayout.findViewById(R.id.label_text_number);
            numberText.setVisibility(View.VISIBLE);
            Boolean readOnly = jsonObject.optBoolean(JsonFormConstants.READ_ONLY);
            String labelTextColor = readOnly ? "#737373" : jsonObject.optString(JsonFormConstants.TEXT_COLOR, null);
            FormUtils.setTextStyle(textStyle, numberText);
            numberText.setTextSize(labelTextSize);
            numberText.setEnabled(!jsonObject
                    .optBoolean(JsonFormConstants.READ_ONLY, false));//Gotcha: Should be set before createLabelText is used
            numberText.setText(labelNumber + ". ");
            if (labelTextColor != null) {
                numberText.setTextColor(Color.parseColor(labelTextColor));
            }
            numberText.setPadding(
                    getValueFromSpOrDpOrPx(context, leftPadding),
                    getValueFromSpOrDpOrPx(context, topPadding),
                    getValueFromSpOrDpOrPx(context, rightPadding),
                    getValueFromSpOrDpOrPx(context, bottomPadding));
        }
    }

    /**
     * Generates the spanned text to be passed to the label Custom TextView
     *
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private Spanned createLabelText(JSONObject jsonObject) throws JSONException {
        String text = jsonObject.getString(JsonFormConstants.TEXT);
        Boolean readOnly = jsonObject.optBoolean(JsonFormConstants.READ_ONLY);
        String asterisks = getAsterisk(jsonObject);
        String labelTextColor = readOnly ? "#737373" : jsonObject.optString(JsonFormConstants.TEXT_COLOR, null);
        String combinedLabelText = getCombinedLabel(text, asterisks, labelTextColor);

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html
                .fromHtml(combinedLabelText, Html.FROM_HTML_MODE_LEGACY) : Html
                .fromHtml(combinedLabelText);
    }

    private String getAsterisk(JSONObject jsonObject) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        String asterisks = "";
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(JsonFormConstants.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                asterisks = "<font color=" + "#CF0800" + "> *</font>";
            }
        }
        return asterisks;
    }

    private String getCombinedLabel(String text, String asterisks, String labelTextColor) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? "<font color=" + labelTextColor + ">" + Html
                .escapeHtml(text) + "</font>" + asterisks : "<font color=" + labelTextColor + ">" + TextUtils
                .htmlEncode(text) + "</font>" +
                asterisks;
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}
