package com.vijay.jsonwizard.widgets;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.VisibleForTesting;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class NumberSelectorFactory implements FormWidgetFactory {
    public static final String TAG = NumberSelectorFactory.class.getCanonicalName();
    public static NumberSelectorFactoryReceiver receiver;
    private static CustomTextView selectedTextView;
    private Context context;
    private CommonListener listener;
    private Map<String, JSONObject> jsonObjectMap = new HashMap<>();
    private Map<String, LinearLayout> rootLayoutMap = new HashMap<>();

    private static NumberSelectorFactory numberSelectorFactory;

    public static NumberSelectorFactory getNumberSelectorFactory() {
        if (numberSelectorFactory == null) {
            numberSelectorFactory = new NumberSelectorFactory();
        }
        return numberSelectorFactory;
    }


    public static void setNumberSelectorFactory(NumberSelectorFactory numberSelectorFactory) {
        NumberSelectorFactory.numberSelectorFactory = numberSelectorFactory;
    }

    @SuppressLint("NewApi")
    private static void setSelectedColor(Context context, CustomTextView customTextView, int item, int numberOfSelectors,
                                         String textColor) {
        if (customTextView != null && item > -1) {
            customTextView.setBackgroundColor(context.getResources().getColor(R.color.native_number_selector_selected));
            customTextView.setTextColor(Color.parseColor(textColor));
            if (item == 0) {
                if (numberOfSelectors == 1) {
                    customTextView.setBackgroundResource(R.drawable.number_selector_rounded_background_selected);
                } else {
                    customTextView.setBackgroundResource(R.drawable.number_selector_left_rounded_background_selected);
                }
            } else if (item == (numberOfSelectors - 1)) {
                customTextView.setBackgroundResource(R.drawable.number_selector_right_rounded_background_selected);
            }
        }
        //Change background color (for first and last drawables) after it was reset by skip logic
        if (customTextView != null && customTextView.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) customTextView.getBackground()).setColor(context.getResources()
                    .getColor(R.color.native_number_selector_selected));
        }
    }

    @SuppressLint("NewApi")
    private static void setDefaultColor(Context context, CustomTextView customTextView, int item, int numberOfSelectors,
                                        String textColor) {
        if (customTextView != null && item > -1) {
            customTextView.setBackgroundColor(context.getResources().getColor(R.color.native_number_selector));
            customTextView.setTextColor(Color.parseColor(textColor));
            if (item == 0) {
                if (numberOfSelectors == 1) {
                    customTextView.setBackgroundResource(R.drawable.number_selector_rounded_background);
                } else {
                    customTextView.setBackgroundResource(R.drawable.number_selector_left_rounded_background);
                }
            } else if (item == numberOfSelectors - 1) {
                customTextView.setBackgroundResource(R.drawable.number_selector_right_rounded_background);
            }
        }
    }

    /**
     * Sets backgrounds the different backgrounds for the number selectors.
     *
     * @param textView {@link CustomTextView}
     * @author dubdabasoduba
     */
    public static void setBackgrounds(CustomTextView textView) {
        String defaultColor = (String) textView.getTag(R.id.number_selector_default_text_color);
        String selectedColor = (String) textView.getTag(R.id.number_selector_selected_text_color);
        int item = (int) textView.getTag(R.id.number_selector_item);
        int numberOfSelectors = (int) textView.getTag(R.id.number_selector_number_of_selectors);

        ViewParent textViewParent = textView.getParent();
        int childCount = ((ViewGroup) textViewParent).getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < numberOfSelectors; i++) {
                CustomTextView customTextView = (CustomTextView) ((ViewGroup) textViewParent).getChildAt(i);
                if (customTextView != null) {
                    if ((customTextView.getId() == textView.getId())) {
                        setSelectedColor(textView.getContext(), textView, item, numberOfSelectors, selectedColor);
                    } else {
                        int otherItem = (int) customTextView.getTag(R.id.number_selector_item);
                        setDefaultColor(customTextView.getContext(), customTextView, otherItem, numberOfSelectors,
                                defaultColor);
                    }
                }
            }
        }
    }


    public static void setSelectedTextViews(CustomTextView customTextView) {
        selectedTextView = customTextView;
        ((View) customTextView.getParent()).setTag(R.id.selected_number_value, customTextView.getText().toString());
        customTextView.setError(null);
    }

    public static CustomTextView getSelectedTextView() {
        return selectedTextView;
    }

    /**
     * Get numbers to display in the number selector dialog
     *
     * @param jsonObject
     */
    private static List<String> getNumbersForNumberSelectorDialog(JSONObject jsonObject, int startNumber) {
        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);

        List<String> numbers = new ArrayList<>();
        for (int i = startNumber; i <= maxValue; i++) {
            numbers.add(String.valueOf(i));
        }
        return numbers;
    }

    public static void setSelectedTextViewText(String viewText) {
        selectedTextView.setText(viewText);
    }

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView, ViewGroup childAt) {

        boolean isRequired = Boolean.valueOf((String) childAt.getTag(R.id.v_required));
        String errorMessage = (String) childAt.getTag(R.id.error);
        String selectedNumber = (String) childAt.getTag(R.id.selected_number_value);
        if (isRequired && TextUtils.isEmpty(selectedNumber) && (childAt.getChildCount() != 0 && childAt.getVisibility() == View.VISIBLE)) {
            return new ValidationStatus(false, errorMessage, formFragmentView, childAt);
        }
        return new ValidationStatus(true, null, formFragmentView, childAt);

    }

    public NumberSelectorFactoryReceiver getNumberSelectorsReceiver() {
        if (receiver == null) {
            receiver = new NumberSelectorFactoryReceiver();
        }
        return receiver;
    }

    /**
     * Display list of numbers in a popup menu
     *
     * @param textView the text view that the popup will be displayed against.
     */
    public static void createNumberSelector(final CustomTextView textView) {
        setBackgrounds(textView);
        setSelectedTextViews(textView);
        Context context = textView.getContext();
        List<String> numbers = (List<String>) textView.getTag(R.id.number_selector_dialog_numbers);
        PopupMenu menu = new PopupMenu(context, textView);
        menu.inflate(R.menu.menu_popup);
        for (final String number : numbers) {
            String parentKey = (String) ((View) textView.getParent()).getTag(R.id.key);
            String openMrsEntityParent = (String) textView.getTag(R.id.openmrs_entity_parent);
            String openMrsEntity = (String) textView.getTag(R.id.openmrs_entity);
            String openMrsEntityId = (String) textView.getTag(R.id.openmrs_entity_id);
            Boolean popup = (Boolean) textView.getTag(R.id.extraPopup);
            Intent intent = new Intent().putExtra(JsonFormConstants.IS_NUMBER_SELECTOR_MENU, true)
                    .putExtra(JsonFormConstants.PARENT_KEY, parentKey)
                    .putExtra(JsonFormConstants.OPENMRS_ENTITY_PARENT, openMrsEntityParent)
                    .putExtra(JsonFormConstants.OPENMRS_ENTITY, openMrsEntity)
                    .putExtra(JsonFormConstants.OPENMRS_ENTITY_ID, openMrsEntityId)
                    .putExtra(JsonFormConstants.IS_POPUP, popup);

            menu.getMenu().add(number).setIntent(intent);
        }
        CommonListener listener = (CommonListener) textView.getTag(R.id.number_selector_listener);
        menu.setOnMenuItemClickListener(listener);
        menu.show();
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject,
                                       CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject,
                                       CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, false);
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }

    private List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener listener,
                                  boolean popup) throws JSONException {
        this.context = context;
        this.listener = listener;
        jsonObjectMap.put(jsonObject.getString(JsonFormConstants.KEY), jsonObject);
        setNumberSelectorFactory(this);
        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculations = jsonObject.optString(JsonFormConstants.CALCULATION);

        LinearLayout rootLayout = getRootLayout(context);
        LinearLayout.LayoutParams layoutParams = FormUtils
                .getLinearLayoutParams(FormUtils.MATCH_PARENT, FormUtils.WRAP_CONTENT, 1, 2, 1,
                        2);

        rootLayout.setLayoutParams(layoutParams);
        rootLayout.setOrientation(LinearLayout.HORIZONTAL);
        rootLayout.setId(ViewUtil.generateViewId());
        rootLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        rootLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        rootLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        rootLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        rootLayout.setTag(R.id.extraPopup, popup);
        rootLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        canvasIds.put(rootLayout.getId());
        rootLayout.setTag(R.id.canvas_ids, canvasIds.toString());
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(rootLayout);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(rootLayout);
        }

        if (!TextUtils.isEmpty(calculations) && context instanceof JsonApi) {
            rootLayout.setTag(R.id.calculation, calculations);
            ((JsonApi) context).addCalculationLogicView(rootLayout);
        }
        views.add(rootLayout);
        createTextViews(context, jsonObject, rootLayout, listener, popup);
        rootLayout.setTag(R.id.is_number_selector_linear_layout, true);
        addRequiredTag(rootLayout, jsonObject);
        ((JsonApi) context).addFormDataView(rootLayout);
        rootLayoutMap.put(jsonObject.getString(JsonFormConstants.KEY), rootLayout);

        return views;
    }

    @NotNull
    @VisibleForTesting
    protected LinearLayout getRootLayout(Context context) {
        return new LinearLayout(context);
    }

    @SuppressLint("NewApi")
    private void createTextViews(Context context, JSONObject jsonObject, LinearLayout linearLayout, CommonListener
            listener, boolean popup) throws JSONException {
        int startSelectionNumber = jsonObject.optInt(JsonFormConstants.START_SELECTION_NUMBER, 1);
        int width = 0; //since we are using layout weight this will be calculated so its set to 0
        int numberOfSelectors = jsonObject.optInt(JsonFormConstants.NUMBER_OF_SELECTORS, 5);

        if (linearLayout.getTag(R.id.is_automatic) == null) {
            jsonObject.put(JsonFormConstants.NUMBER_OF_SELECTORS_ORIGINAL, numberOfSelectors);
        }

        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        for (int i = 0; i < numberOfSelectors; i++) {
            CustomTextView customTextView = createCustomView(context, jsonObject, width, numberOfSelectors, listener,
                    linearLayout, i, popup);
            customTextView.setBackgroundColor(context.getResources().getColor(R.color.native_number_selector));

            if (i == 0) {
                customTextView.setBackgroundResource(R.drawable.number_selector_left_rounded_background);
            } else if (i == (numberOfSelectors - 1)) {
                customTextView.setBackgroundResource(R.drawable.number_selector_right_rounded_background);
            }

            //Set a tag to the last text view displayed in the number selector widget
            if (i == (numberOfSelectors - 1) && (numberOfSelectors < maxValue)) {
                customTextView.setTag(R.id.toolbar_parent_layout, linearLayout);
                customTextView.setTag(R.id.is_number_selector_dialog_textview, true);
                List<String> numbers = getNumbersForNumberSelectorDialog(jsonObject,
                        (startSelectionNumber + (numberOfSelectors - 1)));
                customTextView.setTag(R.id.number_selector_dialog_numbers, numbers);

            }
            customTextView.setOnClickListener(listener);
            linearLayout.addView(customTextView);
            showSelectedTextView(jsonObject, customTextView);
        }
    }

    public String getText(int item, int startSelectionNumber, int numberOfSelectors, int maxValue) {
        String text = startSelectionNumber == 0 ? String.valueOf(item) : startSelectionNumber == 1 ? String
                .valueOf(item + 1) : String.valueOf(startSelectionNumber + item);
        if ((item == (numberOfSelectors - 1)) && (maxValue - 1) > Integer.parseInt(text)) {
            text = text + "+";
        }
        return text;
    }

    @SuppressLint("NewApi")
    private CustomTextView createCustomView(Context context, JSONObject jsonObject, int width, int numberOfSelectors,
                                            CommonListener listener, LinearLayout linearLayout, int item, boolean popup)
            throws JSONException {
        int startSelectionNumber = jsonObject.optInt(JsonFormConstants.START_SELECTION_NUMBER, 1);
        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        String textColor = jsonObject.optString(JsonFormConstants.TEXT_COLOR, JsonFormConstants.DEFAULT_TEXT_COLOR);
        String selectedTextColor = jsonObject
                .optString(JsonFormConstants.NUMBER_SELECTOR_SELCTED_TEXT_COLOR, JsonFormConstants
                        .DEFAULT_NUMBER_SELECTOR_TEXT_COLOR);

        CustomTextView customTextView = getCustomTextView(context, jsonObject, numberOfSelectors, item);

        customTextView.setId(ViewUtil.generateViewId());
        customTextView.setPadding(0, 15, 0, 15);
        setDefaultColor(context, customTextView, item, numberOfSelectors, textColor);
        customTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        customTextView.setClickable(true);
        customTextView.setTag(R.id.number_selector_item, item);
        customTextView.setTag(R.id.number_selector_number_of_selectors, numberOfSelectors);
        customTextView.setTag(R.id.number_selector_max_number, maxValue);
        customTextView.setTag(R.id.number_selector_default_text_color, textColor);
        customTextView.setTag(R.id.number_selector_selected_text_color, selectedTextColor);
        customTextView.setTag(R.id.number_selector_start_selection_number, startSelectionNumber);
        customTextView.setTag(R.id.number_selector_listener, listener);
        customTextView.setTag(R.id.extraPopup, popup);
        if (item == numberOfSelectors - 1) {
            customTextView.setTag(R.id.number_selector_layout, linearLayout);
        }
        customTextView.setTag(R.id.json_object, jsonObject);

        return customTextView;
    }

    @NotNull
    @VisibleForTesting
    protected CustomTextView getCustomTextView(Context context, JSONObject jsonObject, int numberOfSelectors, int item) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        int startSelectionNumber = jsonObject.optInt(JsonFormConstants.START_SELECTION_NUMBER, 1);
        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        String textColor = jsonObject.optString(JsonFormConstants.TEXT_COLOR, JsonFormConstants.DEFAULT_TEXT_COLOR);
        String textSize = jsonObject.getString(JsonFormConstants.TEXT_SIZE);
        textSize = textSize == null ? String
                .valueOf(context.getResources().getDimension(R.dimen.default_label_text_size)) : String
                .valueOf(FormUtils.getValueFromSpOrDpOrPx(textSize, context));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        layoutParams.setMargins(1, 0, 1, 0);

        return FormUtils.getTextViewWith(context, Integer.parseInt(textSize), getText(item,
                startSelectionNumber, numberOfSelectors, maxValue),
                jsonObject.getString(JsonFormConstants.KEY) + JsonFormConstants.SUFFIX.TEXT_VIEW,
                jsonObject.getString(JsonFormConstants.TYPE), openMrsEntityParent, openMrsEntity, openMrsEntityId, "",
                layoutParams, FormUtils.FONT_BOLD_PATH, 0, textColor);
    }

    private void showSelectedTextView(JSONObject jsonObject, CustomTextView customTextView) {
        String text = customTextView.getText().toString();
        String numberValue = jsonObject.optString(JsonFormConstants.VALUE);
        if (text.contains("+")) {
            text = text.replace("+", "");
        }

        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(numberValue)) {
            if (customTextView.getText().toString().contains("+") && (Integer.valueOf(numberValue) > Integer
                    .valueOf(text))) {
                displaySelections(jsonObject, customTextView);
            }

            if (!TextUtils.isEmpty(jsonObject.optString(JsonFormConstants.VALUE)) && text
                    .equals(jsonObject.optString(JsonFormConstants.VALUE))) {
                displaySelections(jsonObject, customTextView);
            }

        }
    }

    /**
     * Sets the value text on the text views & updates the color drawables
     *
     * @param jsonObject     {@link JSONObject}
     * @param customTextView {@link CustomTextView}
     * @author dubdabasoduba
     */
    private void displaySelections(JSONObject jsonObject, CustomTextView customTextView) {
        customTextView.setText(jsonObject.optString(JsonFormConstants.VALUE));
        setBackgrounds(customTextView);
        setSelectedTextViews(customTextView);
    }

    private void addRequiredTag(View rootLayout, JSONObject jsonObject) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(JsonFormConstants.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                rootLayout.setTag(R.id.v_required, "true");
                rootLayout.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR, null));
            }
        }

    }


    public class NumberSelectorFactoryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context contextInner, Intent intent) {
            try {
                NumberSelectorFactory numberSelectorFactory = getNumberSelectorFactory();

                int maxValue = intent.getIntExtra(JsonFormConstants.MAX_SELECTION_VALUE, 0);

                JSONObject jsonObject = numberSelectorFactory.jsonObjectMap.get(intent.getStringExtra(JsonFormConstants.JSON_OBJECT_KEY));
                boolean isPopUp = intent.getBooleanExtra(JsonFormConstants.IS_POPUP, false);

                if (jsonObject != null) {
                    jsonObject.put(JsonFormConstants.MAX_SELECTION_VALUE, maxValue);

                    if (jsonObject.has(JsonFormConstants.NUMBER_OF_SELECTORS_ORIGINAL)) {
                        jsonObject.put(JsonFormConstants.NUMBER_OF_SELECTORS, maxValue < jsonObject.getInt(JsonFormConstants
                                .NUMBER_OF_SELECTORS_ORIGINAL) ? maxValue : jsonObject
                                .getInt(JsonFormConstants.NUMBER_OF_SELECTORS_ORIGINAL));
                    } else {
                        jsonObject.put(JsonFormConstants.NUMBER_OF_SELECTORS, maxValue < jsonObject.getInt(JsonFormConstants
                                .NUMBER_OF_SELECTORS) ? maxValue : jsonObject.getInt(JsonFormConstants.NUMBER_OF_SELECTORS));

                    }
                    LinearLayout rootLayout = numberSelectorFactory.rootLayoutMap.get(intent.getStringExtra(JsonFormConstants.JSON_OBJECT_KEY));
                    if (rootLayout != null) {
                        rootLayout.removeAllViews();
                        rootLayout.setTag(R.id.is_automatic, true);
                        createTextViews(numberSelectorFactory.context, jsonObject, rootLayout, numberSelectorFactory.listener, isPopUp);
                        rootLayout.setTag(R.id.is_automatic, null);
                    }
                }

            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }
}
