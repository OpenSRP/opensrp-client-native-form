package com.vijay.jsonwizard.widgets;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.DatePickerDialog;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static com.vijay.jsonwizard.widgets.DatePickerFactory.DATE_FORMAT;


/**
 * Created by samuelgithengi on 8/16/18.
 */
public class NativeRadioButtonFactory implements FormWidgetFactory {

    private static final String TAG = NativeRadioButtonFactory.class.getCanonicalName();
    private FormUtils formUtils = new FormUtils();
    private RadioButton radioButton;
    private CustomTextView extraInfoTextView;
    private CustomTextView specifyTextView;
    private CustomTextView reasonsTextView;
    private Context context;
    private JsonFormFragment formFragment;
    private String stepName;
    private JSONArray canvasIds;
    private CustomTextViewClickListener customTextViewClickListener = new CustomTextViewClickListener();

    public static void showDateDialog(View view) {
        Context context = (Context) view.getTag(R.id.specify_context);
        CustomTextView customTextView = (CustomTextView) view.getTag(R.id.specify_textview);
        RadioButton radioButton = (RadioButton) view.getTag(R.id.native_radio_button);
        DatePickerDialog datePickerDialog = new DatePickerDialog();
        JSONObject jsonObject = (JSONObject) ((radioButton).getTag(R.id.option_json_object));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            datePickerDialog.setCalendarViewShown(false);

            try {
                if (jsonObject != null) {

                    if (jsonObject.has(JsonFormConstants.MIN_DATE)) {
                        Calendar minDate = FormUtils.getDate(jsonObject.getString(JsonFormConstants.MIN_DATE));
                        minDate.set(Calendar.HOUR_OF_DAY, 0);
                        minDate.set(Calendar.MINUTE, 0);
                        minDate.set(Calendar.SECOND, 0);
                        minDate.set(Calendar.MILLISECOND, 0);
                        datePickerDialog.setMinDate(minDate.getTimeInMillis());
                    }

                    if (jsonObject.has(JsonFormConstants.MAX_DATE)) {
                        Calendar maxDate = FormUtils.getDate(jsonObject.getString(JsonFormConstants.MAX_DATE));
                        maxDate.set(Calendar.HOUR_OF_DAY, 23);
                        maxDate.set(Calendar.MINUTE, 59);
                        maxDate.set(Calendar.SECOND, 59);
                        maxDate.set(Calendar.MILLISECOND, 999);
                        datePickerDialog.setMaxDate(maxDate.getTimeInMillis());

                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }

        }

        datePickerDialog.setContext(context);
        setDate(datePickerDialog, radioButton, customTextView, context);
        showDatePickerDialog((Activity) context, datePickerDialog, radioButton);
    }

    private static void setDate(DatePickerDialog datePickerDialog, final RadioButton radioButton,
                                final CustomTextView customTextView, final Context context) {
        final String[] arrayString = radioButton.getText().toString().split(":");
        datePickerDialog.setOnDateSetListener(new android.app.DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendarDate = Calendar.getInstance();
                calendarDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendarDate.set(Calendar.MONTH, monthOfYear);
                calendarDate.set(Calendar.YEAR, year);
                if (calendarDate.getTimeInMillis() >= view.getMinDate() &&
                        calendarDate.getTimeInMillis() <= view.getMaxDate()) {
                    radioButton.setText(arrayString[0] + ": " + DATE_FORMAT.format(calendarDate.getTime()));
                    customTextView.setText(
                            createSpecifyText(context.getResources().getString(R.string.radio_button_tap_to_change)));

                    if (context instanceof JsonFormActivity) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(customTextView.getTag(R.id.key).toString(),
                                customTextView.getTag(R.id.childKey) + ":" + DATE_FORMAT.format(calendarDate.getTime()));

                        Intent intent = new Intent(JsonFormConstants.INTENT_ACTION.JSON_FORM_ACTIVITY);
                        intent.putExtra(JsonFormConstants.INTENT_KEY.MESSAGE, map);
                        intent.putExtra(JsonFormConstants.STEPNAME, ((String) customTextView.getTag(R.id.specify_step_name)));

                        intent.putExtra(JsonFormConstants.INTENT_KEY.MESSAGE_TYPE,
                                JsonFormConstants.MESSAGE_TYPE.GLOBAL_VALUES);

                        ((JsonFormActivity) context).getLocalBroadcastManager().sendBroadcast(intent);
                    }
                    String key = (String) customTextView.getTag(R.id.key);
                    String childKey = (String) customTextView.getTag(R.id.childKey);
                    String stepName = (String) customTextView.getTag(R.id.specify_step_name);
                    Context context = (Context) customTextView.getTag(R.id.specify_context);

                    JSONArray fields = new FormUtils().getFormFields(stepName, context);
                    if (fields.length() > 0) {
                        for (int i = 0; i < fields.length(); i++) {
                            try {
                                JSONObject widget = fields.getJSONObject(i);
                                if (widget != null && widget.getString(JsonFormConstants.KEY).equals(key)) {
                                    radioButtonOptionAssignSecondaryValue(widget, childKey, calendarDate);
                                }
                                if (widget != null && widget.getString(JsonFormConstants.KEY)
                                        .equals(key + JsonFormConstants.SPECIFY_DATE_HIDDEN_FIELD_SUFFIX)) {
                                    assignHiddenDateValue(widget, calendarDate);
                                }
                            } catch (JSONException e) {
                                Log.i(TAG, Log.getStackTraceString(e));
                            }
                        }
                    }

                } else {
                    radioButton.setText(arrayString[0]);
                }
            }
        });
    }

    private static void showDatePickerDialog(Activity context, DatePickerDialog datePickerDialog, RadioButton radioButton) {
        FragmentTransaction ft = context.getFragmentManager().beginTransaction();
        Fragment prev = context.getFragmentManager().findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        datePickerDialog.show(ft, TAG);
        Calendar calendar = getDate(radioButton);
        datePickerDialog.setDate(calendar.getTime());
    }

    private static String createSpecifyText(String text) {
        return text == null || text.isEmpty() ? "" : "(" + text + ")";
    }

    /**
     * This assign the secondary value to the radio button options
     *
     * @param jsonObject
     * @param childKey
     * @param calendarDate
     * @throws JSONException
     */
    private static void radioButtonOptionAssignSecondaryValue(JSONObject jsonObject, String childKey, Calendar calendarDate)
            throws JSONException {
        if (jsonObject.has(JsonFormConstants.OPTIONS_FIELD_NAME)) {
            JSONArray jsonArray = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject option = jsonArray.getJSONObject(i);
                if (option.has(JsonFormConstants.KEY) && option.getString(JsonFormConstants.KEY).equals(childKey)) {
                    addSecondaryValue(option, calendarDate);
                }
            }
        }
    }

    private static void assignHiddenDateValue(JSONObject widget, Calendar calendarDate) {
        try {
            widget.put(JsonFormConstants.VALUE, DATE_FORMAT.format(calendarDate.getTime()));
        } catch (Exception e) {
            Log.i(TAG, Log.getStackTraceString(e));
        }
    }

    private static Calendar getDate(RadioButton radioButton) {
        String[] arrayString = radioButton.getText().toString().split(":");
        String dateString = "";
        if (arrayString.length > 1) {
            dateString = arrayString[1];
        }
        return FormUtils.getDate(dateString);
    }

    /**
     * Add the secondary value object
     *
     * @param item
     * @param calendarDate
     * @throws JSONException
     */
    private static void addSecondaryValue(JSONObject item, Calendar calendarDate) throws JSONException {
        JSONObject valueObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        valueObject.put(JsonFormConstants.KEY, item.getString(JsonFormConstants.KEY));
        valueObject.put(JsonFormConstants.TYPE, JsonFormConstants.DATE_PICKER);
        valueObject.put(JsonFormConstants.VALUES, jsonArray.put(DATE_FORMAT.format(calendarDate.getTime())));

        try {
            item.put(JsonFormConstants.SECONDARY_VALUE, new JSONArray().put(valueObject));
        } catch (Exception e) {
            Timber.e(e, "--> addSecondaryValue");
        }
    }

    public static ValidationStatus validate(final JsonFormFragmentView formFragmentView, RadioGroup radioGroup) {
        final String error = (String) radioGroup.getTag(R.id.error);
        if (radioGroup.isEnabled() && error != null) {
            final LinearLayout linearLayout = (LinearLayout) radioGroup.getParent();
            if (linearLayout != null && linearLayout.isShown()) {
                boolean isValid = performValidation(radioGroup);
                final TextView errorTextView = linearLayout.findViewById(R.id.error_textView);
                if (!isValid) {
                    updateRadioButtonGroupWithError(formFragmentView, error, linearLayout);
                    return new ValidationStatus(false, error, formFragmentView, radioGroup);
                } else if (errorTextView != null) {
                    ((JsonFormActivity) formFragmentView.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorTextView.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }
        return new ValidationStatus(true, null, formFragmentView, radioGroup);
    }

    private static void updateRadioButtonGroupWithError(final JsonFormFragmentView formFragmentView, final String error, final LinearLayout linearLayout) {
        ((JsonFormActivity) formFragmentView.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (linearLayout.getChildAt(0) instanceof ConstraintLayout) {
                    ConstraintLayout constraintLayout = (ConstraintLayout) linearLayout.getChildAt(0);
                    TextView errorTextView = linearLayout.findViewById(R.id.error_textView);
                    if (errorTextView == null) {
                        errorTextView = new TextView(formFragmentView.getContext());
                        errorTextView.setId(R.id.error_textView);
                        errorTextView.setTextColor(formFragmentView.getContext().getResources().getColor(R.color.toaster_note_red_icon));
                        errorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(constraintLayout.getLayoutParams());
                        layoutParams.topToBottom = R.id.label_text;
                        layoutParams.leftMargin = FormUtils.dpToPixels(formFragmentView.getContext(), 8);
                        constraintLayout.addView(errorTextView, new ConstraintLayout.LayoutParams(layoutParams));
                    }
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText(error);

                }
            }
        });

    }

    private static boolean performValidation(RadioGroup radioGroup) {
        boolean isChecked = false;
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            isChecked = getCheckedRadio(radioGroup.getChildAt(i));
            if (isChecked) {
                break;
            }
        }
        return isChecked;
    }

    /**
     * @param item radiogroup nested layout
     * @return true if any of the radio buttons is selected.
     */
    private static boolean getCheckedRadio(View item) {
        if (item instanceof ViewGroup) {
            ViewGroup outerRelativeLayout = (ViewGroup) item;
            //Get radio button on the fourth hierarchy of the nested radio group layout
            ViewGroup mainRadioLayout = (ViewGroup) outerRelativeLayout.getChildAt(0);
            ViewGroup radioContentLinearLayout = (ViewGroup) mainRadioLayout.getChildAt(0);
            RadioButton radioButton = (RadioButton) radioContentLinearLayout.getChildAt(0);

            return radioButton.isChecked();
        }
        return false;
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, listener, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment,
                                       JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    protected List<View> attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject,
                                    CommonListener listener, boolean popup) throws JSONException {

        JSONArray canvasIds = new JSONArray();
        this.stepName = stepName;
        this.formFragment = formFragment;
        this.context = context;
        this.canvasIds = canvasIds;

        boolean readOnly = false;
        boolean editable = false;
        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
        }
        if (jsonObject.has(JsonFormConstants.EDITABLE)) {
            editable = jsonObject.getBoolean(JsonFormConstants.EDITABLE);
        }
        List<View> views = new ArrayList<>(1);
        ImageView editButton;

        LinearLayout rootLayout = getLinearRootLayout(context);

        Map<String, View> labelViews = new HashMap<>();
        String label = jsonObject.optString(JsonFormConstants.LABEL, "");
        if (StringUtils.isNotBlank(label)) {
            labelViews = formUtils.createRadioButtonAndCheckBoxLabel(stepName, rootLayout, jsonObject, context, canvasIds, readOnly, listener, popup);
        }

        View radioGroup = addRadioButtonOptionsElements(jsonObject, context, stepName, rootLayout, listener, popup);
        radioGroup.setTag(R.id.json_object, jsonObject);

        if (labelViews != null && labelViews.size() > 0) {
            editButton = (ImageView) labelViews.get(JsonFormConstants.EDIT_BUTTON);
            if (editButton != null) {
                FormUtils.setEditButtonAttributes(jsonObject, radioGroup, editButton, listener);
                if (editable) {
                    editButton.setVisibility(View.VISIBLE);
                }
            }
        }

        populateTags(rootLayout, stepName, popup, "", "", "", jsonObject);

        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        attachRelevance(rootLayout, context, relevance);

        rootLayout.setTag(R.id.extraPopup, popup);
        views.add(rootLayout);
        return views;
    }

    private void attachRelevance(@NonNull View view, @NonNull Context context, @Nullable String relevance) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            view.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(view);
        }
    }

    private void populateTags(@NonNull View view, @NonNull String stepName,
                              boolean popUp,
                              String openmrsEntity, String openmrsEntityParent,
                              String openmrsEntityId, JSONObject jsonObject) {
        JSONArray canvasIds = new JSONArray();
        view.setId(ViewUtil.generateViewId());
        canvasIds.put(view.getId());
        view.setTag(R.id.canvas_ids, canvasIds.toString());
        view.setTag(R.id.key, jsonObject.optString(JsonFormConstants.KEY));
        view.setTag(R.id.openmrs_entity_parent, openmrsEntityParent);
        view.setTag(R.id.openmrs_entity, openmrsEntity);
        view.setTag(R.id.openmrs_entity_id, openmrsEntityId);
        view.setTag(R.id.type, jsonObject.optString(JsonFormConstants.TYPE));
        view.setTag(R.id.extraPopup, popUp);
        view.setTag(R.id.address, stepName + ":" + jsonObject.optString(JsonFormConstants.KEY));
    }

    public LinearLayout getLinearRootLayout(Context context) {
        return (LinearLayout) LayoutInflater.from(context).inflate(R.layout.native_form_compound_button_parent, null);
    }

    /**
     * Creates the Radio Button options from the JSON definitions
     *
     * @param jsonObject   {@link JSONObject}
     * @param context      {@link Context}
     * @param stepName     {@link String}
     * @param linearLayout {@link LinearLayout}
     * @param listener     {@link CommonListener}
     * @throws JSONException {@link JSONException}
     * @author dubdabasoduba
     */
    protected View addRadioButtonOptionsElements(JSONObject jsonObject, Context context, String stepName,
                                                 LinearLayout linearLayout,
                                                 CommonListener listener, boolean popup)
            throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        JSONArray options = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
        Boolean extraRelCheck = jsonObject.optBoolean(JsonFormConstants.EXTRA_REL, false);
        String extraRelArray = null;
        if (extraRelCheck) {
            extraRelArray = jsonObject.optString(JsonFormConstants.HAS_EXTRA_REL, null);
        }

        RadioGroup radioGroup = getRadioGroup(jsonObject, context, stepName, popup, openMrsEntityParent, openMrsEntity, openMrsEntityId, extraRelCheck, extraRelArray);
        canvasIds.put(radioGroup.getId());
        addRequiredValidator(radioGroup, jsonObject);
        for (int i = 0; i < options.length(); i++) {
            JSONObject item = options.getJSONObject(i);

            RelativeLayout radioGroupLayout = getRadioGroupLayout(jsonObject, context, stepName, popup, openMrsEntityParent, openMrsEntity, openMrsEntityId, item);

            //Showing optional info alert dialog on individual radio buttons
            ImageView imageView = radioGroupLayout.findViewById(R.id.info_icon);
            formUtils.showInfoIcon(stepName, jsonObject, listener, FormUtils.getInfoDialogAttributes(item), imageView, canvasIds);

            createRadioButton(radioGroupLayout, jsonObject, item, listener, popup);

            radioGroup.addView(radioGroupLayout);
            ((JsonApi) context).addFormDataView(radioGroup);
        }

        prepareViewChecks(context, relevance, constraints, calculation, radioGroup);

        FormUtils.setRadioExclusiveClick(radioGroup);
        radioGroup.setLayoutParams(FormUtils.getLinearLayoutParams(FormUtils.MATCH_PARENT, FormUtils.WRAP_CONTENT, 0, 0, 0,
                (int) context.getResources().getDimension(R.dimen.extra_bottom_margin)));
        radioGroup.setTag(R.id.canvas_ids, canvasIds.toString());

        linearLayout.addView(radioGroup);
        return radioGroup;
    }

    @NotNull
    public RadioGroup getRadioGroup(JSONObject jsonObject, Context context, String stepName, boolean popup, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId, Boolean extraRelCheck, String extraRelArray) throws JSONException {
        RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        radioGroup.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        radioGroup.setTag(R.id.openmrs_entity, openMrsEntity);
        radioGroup.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        radioGroup.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        radioGroup.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        radioGroup.setTag(R.id.extraRelCheck, extraRelCheck);
        radioGroup.setTag(R.id.extraRelArray, extraRelArray);
        radioGroup.setTag(R.id.extraPopup, popup);
        radioGroup.setId(ViewUtil.generateViewId());
        return radioGroup;
    }

    @NotNull
    public RelativeLayout getRadioGroupLayout(JSONObject jsonObject, Context context, String stepName, boolean popup, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId, JSONObject item) throws JSONException {
        RelativeLayout radioGroupLayout = getRadioGroupLayout(context);
        radioGroupLayout.setId(ViewUtil.generateViewId());
        radioGroupLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        radioGroupLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        radioGroupLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        radioGroupLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        radioGroupLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        radioGroupLayout.setTag(R.id.childKey, item.getString(JsonFormConstants.KEY));
        radioGroupLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        radioGroupLayout.setTag(R.id.extraPopup, popup);
        radioGroupLayout.setTag(R.id.native_radio_button_layout_view_id, radioGroupLayout.getId());
        return radioGroupLayout;
    }

    public RelativeLayout getRadioGroupLayout(Context context) {
        return (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.native_item_radio_button, null);
    }

    private void prepareViewChecks(Context context, String relevance, String constraints, String calculation, RadioGroup radioGroup) {

        attachRelevance(radioGroup, context, relevance);

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            radioGroup.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(radioGroup);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            radioGroup.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(radioGroup);
        }
    }

    private void addRequiredValidator(RadioGroup radioGroup, JSONObject jsonObject) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(JsonFormConstants.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                radioGroup.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR, null));
            }
        }
    }

    private void createRadioButton(RelativeLayout rootLayout, JSONObject jsonObject, JSONObject item,
                                   final CommonListener listener, boolean popup) throws JSONException {
        boolean readOnly = false;
        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
        }

        String specifyInfo = item.optString(JsonFormConstants.CONTENT_INFO, null);
        String extraInfo = item.optString(JsonFormConstants.NATIVE_RADIO_EXTRA_INFO, null);
        String text_color = item.optString(JsonFormConstants.CONTENT_INFO_COLOR, JsonFormConstants.DEFAULT_HINT_TEXT_COLOR);

        if (extraInfo != null) {
            createExtraInfoDisplayTextView(rootLayout, jsonObject, readOnly, item, this.stepName, this.context, text_color);
        }


        final RadioButton radioButton = rootLayout.findViewById(R.id.mainRadioButton);
        if (radioButton != null) {
            setRadioButtonTags(rootLayout, jsonObject, item, extraInfo, radioButton);

            if (!TextUtils.isEmpty(jsonObject.optString(JsonFormConstants.VALUE)) &&
                    jsonObject.optString(JsonFormConstants.VALUE).equals(item.getString(JsonFormConstants.KEY))) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        radioButton.setChecked(true);
                    }
                });
            }
            String optionTextColor = JsonFormConstants.DEFAULT_TEXT_COLOR;
            if (item.has(JsonFormConstants.TEXT_COLOR)) {
                optionTextColor = item.getString(JsonFormConstants.TEXT_COLOR);
            }
            String optionTextSize = String.valueOf(this.context.getResources().getDimension(R.dimen.options_default_text_size));
            if (item.has(JsonFormConstants.TEXT_SIZE)) {
                optionTextSize = item.getString(JsonFormConstants.TEXT_SIZE);
            }

            String optionText = item.getString(JsonFormConstants.TEXT);

            if (item.has(JsonFormConstants.SECONDARY_VALUE)) {
                optionText = getOptionTextWithSecondaryValue(item, jsonObject);
            }

            setRadioButton(radioButton);
            if (specifyInfo != null) {
                createSpecifyReasonsDisplayTextView(rootLayout, jsonObject, item, this.stepName, text_color);
                createSpecifyTextView(rootLayout, listener, jsonObject, this.context, readOnly, item, popup, this.formFragment,
                        this.stepName
                );
            }

            addPopupRelevantTags(radioButton, jsonObject, popup, item, this.context, this.formFragment, this.stepName, listener);
            radioButton.setTextColor(Color.parseColor(optionTextColor));
            radioButton.setTextSize(FormUtils.getValueFromSpOrDpOrPx(optionTextSize, this.context));
            radioButton.setText(optionText);
            radioButton.setEnabled(!readOnly);
            radioButton.setOnCheckedChangeListener(listener);
        }
    }

    private void setRadioButtonTags(RelativeLayout rootLayout, JSONObject jsonObject, JSONObject item, String extraInfo,
                                    RadioButton radioButton) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);

        int generatedViewId = ViewUtil.generateViewId();
        if (radioButton != null) {
            radioButton.setId(generatedViewId);
            radioButton.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
            radioButton.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
            radioButton.setTag(R.id.openmrs_entity, openMrsEntity);
            radioButton.setTag(R.id.openmrs_entity_id, openMrsEntityId);
            radioButton.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
            radioButton.setTag(R.id.childKey, item.getString(JsonFormConstants.KEY));
            radioButton.setTag(R.id.address, this.stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
            radioButton.setTag(R.id.relevance, relevance);
            rootLayout.setTag(R.id.native_radio_button_view_id, generatedViewId);
            if (extraInfo != null) {
                radioButton.setTag(R.id.native_radio_button_extra_info, true);
            } else {
                radioButton.setTag(R.id.native_radio_button_extra_info, false);

            }
            radioButton.setTag(jsonObject.getString(JsonFormConstants.TYPE));
        }
    }

    /**
     * Creates the specify the text view and adds in the radio on checked change listener
     *
     * @param rootLayout   {@link RelativeLayout}
     * @param listener     {@link CommonListener}
     * @param jsonObject   {@link JSONObject}
     * @param context      {@link Context}
     * @param readOnly     {@link Boolean}
     * @param item         {@link JSONObject}
     * @param popup        {@link Boolean}
     * @param formFragment {@link JsonFormFragment}
     * @param stepName     {@link String}
     * @throws JSONException {@link JSONException}
     * @author dubdabasoduba
     */
    private void createSpecifyTextView(RelativeLayout rootLayout, final CommonListener listener, JSONObject jsonObject,
                                       Context context, Boolean readOnly, JSONObject item, Boolean popup,
                                       JsonFormFragment formFragment, String stepName)
            throws JSONException {
        String optionKey = item.optString(JsonFormConstants.KEY, "");
        String widgetValue = jsonObject.optString(JsonFormConstants.VALUE, "");

        String specifyText;
        if (item.has(JsonFormConstants.SECONDARY_VALUE) && widgetValue.equals(optionKey))
            specifyText = context.getResources().getString(R.string.radio_button_tap_to_change);
        else specifyText = item.getString(JsonFormConstants.CONTENT_INFO);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);

        CustomTextView specifyTextView = rootLayout.findViewById(R.id.specifyTextView);
        addTextViewAttributes(jsonObject, item, specifyTextView, stepName, "");
        specifyTextView.setText(createSpecifyText(specifyText));
        specifyTextView.setTextSize(context.getResources().getDimension(R.dimen.specify_date_default_text_size));
        specifyTextView.setId(ViewUtil.generateViewId());
        specifyTextView.setEnabled(!readOnly);
        specifyTextView.setTag(R.id.relevance, relevance);
        specifyTextView.setVisibility(View.VISIBLE);
        addPopupRelevantTags(specifyTextView, jsonObject, popup, item, context, formFragment, stepName, listener);
        specifyTextView.setOnClickListener(customTextViewClickListener);
        setSpecifyTextView(specifyTextView);
    }

    /**
     * Returns the radio button option text after concatenating with the secondary value in the case of a date picker popup.
     *
     * @param item {@link JSONObject}
     * @return optionText {@link String}
     * @throws JSONException {@link JSONException}
     * @author dubdabasoduba
     */
    private String getOptionTextWithSecondaryValue(JSONObject item, JSONObject jsonObject) throws JSONException {
        String optionText = item.getString(JsonFormConstants.TEXT);
        String optionKey = item.getString(JsonFormConstants.KEY);
        String widgetValue = jsonObject.optString(JsonFormConstants.VALUE, "");

        if (optionKey.equals(widgetValue) && item.has(JsonFormConstants.SECONDARY_VALUE)) {
            JSONArray secondaryValueArray = item.getJSONArray(JsonFormConstants.SECONDARY_VALUE);
            if (secondaryValueArray != null && secondaryValueArray.length() > 0) {
                JSONObject secondaryValue = secondaryValueArray.getJSONObject(0);
                String secondValueKey = secondaryValue.getString(JsonFormConstants.KEY);
                String secondValueType = secondaryValue.getString(JsonFormConstants.TYPE);
                if (item.getString(JsonFormConstants.KEY).equals(secondValueKey) &&
                        secondValueType.equals(JsonFormConstants.DATE_PICKER)) {
                    String secondaryValueDate = getSecondaryDateValue(secondaryValue.getJSONArray(JsonFormConstants.VALUES));
                    optionText = item.getString(JsonFormConstants.TEXT) + ":" + secondaryValueDate;
                }
            }
        }

        return optionText;
    }

    private void addPopupRelevantTags(View view, JSONObject jsonObject, boolean popup, JSONObject item, Context context,
                                      JsonFormFragment formFragment, String stepName, CommonListener listener)
            throws JSONException {
        String specifyWidget = item.optString(JsonFormConstants.CONTENT_WIDGET, "");
        String specifyContent = item.optString(JsonFormConstants.CONTENT_FORM, null);
        String specifyContentForm = item.optString(JsonFormConstants.CONTENT_FORM_LOCATION, null);

        if (view != null) {
            view.setTag(R.id.extraPopup, popup);
            view.setTag(R.id.json_object, jsonObject);
            view.setTag(R.id.option_json_object, item);
            view.setTag(R.id.specify_textview, getSpecifyTextView());
            view.setTag(R.id.native_radio_button, getRadioButton());
            view.setTag(R.id.specify_reasons_textview, getReasonsTextView());
            view.setTag(R.id.specify_extra_info_textview, getExtraInfoTextView());
            view.setTag(R.id.specify_type, JsonFormConstants.CONTENT_INFO);
            view.setTag(R.id.specify_context, context);
            view.setTag(R.id.specify_widget, specifyWidget);
            view.setTag(R.id.specify_content, specifyContent);
            view.setTag(R.id.specify_content_form, specifyContentForm);
            view.setTag(R.id.specify_listener, listener);
            view.setTag(R.id.specify_step_name, stepName);
            view.setTag(R.id.specify_fragment, formFragment);
            view.setTag(R.id.secondaryValues, formUtils.getSecondaryValues(item, jsonObject.getString(JsonFormConstants.TYPE)));
        }
    }

    private void createExtraInfoDisplayTextView(RelativeLayout rootLayout, JSONObject jsonObject, Boolean readOnly,
                                                JSONObject item, String stepName, Context context, String text_color)
            throws JSONException {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String text = item.getString(JsonFormConstants.NATIVE_RADIO_EXTRA_INFO);

        CustomTextView extraInfoTextView = rootLayout.findViewById(R.id.extraInfoTextView);
        extraInfoTextView.setTextSize(context.getResources().getDimension(R.dimen.extra_info_default_text_size));
        extraInfoTextView.setVisibility(View.VISIBLE);
        addTextViewAttributes(jsonObject, item, extraInfoTextView, stepName, text_color);
        extraInfoTextView.setText(text);
        extraInfoTextView.setEnabled(!readOnly);
        setExtraInfoTextView(extraInfoTextView);
        extraInfoTextView.setTag(R.id.relevance, relevance);
    }

    /**
     * Displays the selected items from the popup
     *
     * @param rootLayout
     * @param jsonObject
     * @param item
     * @param stepName
     * @param text_color
     * @throws JSONException
     * @author kitoto
     */
    private void createSpecifyReasonsDisplayTextView(RelativeLayout rootLayout, JSONObject jsonObject, JSONObject item,
                                                     String stepName, String text_color) throws JSONException {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String popupReasonsText = "";
        CustomTextView reasonsTextView = rootLayout.findViewById(R.id.reasonsTextView);
        String optionKey = item.optString(JsonFormConstants.KEY, "");
        String widgetValue = jsonObject.optString(JsonFormConstants.VALUE, "");
        if (widgetValue.equals(optionKey)) {
            if (item.has(JsonFormConstants.SECONDARY_VALUE)) {
                popupReasonsText = formUtils.getSpecifyText(item.getJSONArray(JsonFormConstants.SECONDARY_VALUE));
                if (item.has(JsonFormConstants.CONTENT_WIDGET) &&
                        !item.getString(JsonFormConstants.CONTENT_WIDGET).equals(JsonFormConstants.DATE_PICKER)) {
                    reasonsTextView.setVisibility(View.VISIBLE);
                }
            }

            if (item.has(JsonFormConstants.SECONDARY_SUFFIX)) {
                String suffix = item.getString(JsonFormConstants.SECONDARY_SUFFIX);
                if (!TextUtils.isEmpty(popupReasonsText)) {
                    popupReasonsText = popupReasonsText + " " + suffix;
                }
            }

            addTextViewAttributes(jsonObject, item, reasonsTextView, stepName, text_color);
            reasonsTextView.setTag(R.id.relevance, relevance);
            reasonsTextView.setText(JsonFormConstants.SECONDARY_PREFIX + popupReasonsText);
        }
        setReasonsTextView(reasonsTextView);
    }

    /**
     * Returns the date from the secondary values in-case of a date picker pop up
     *
     * @param values {@link JSONArray}
     * @return date {@link String}
     * @throws JSONException {@link JSONException}
     * @author dubdabasoduba
     */
    private String getSecondaryDateValue(JSONArray values) throws JSONException {
        String date = "";
        if (values != null && values.length() > 0) {
            date = values.getString(0);
        }
        return date;
    }

    private void addTextViewAttributes(JSONObject jsonObject, JSONObject item, CustomTextView customTextView,
                                       String stepName, String text_color) throws JSONException {
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        if (!TextUtils.isEmpty(text_color)) {
            customTextView.setTextColor(Color.parseColor(text_color));
        }
        customTextView.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        customTextView.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        customTextView.setTag(R.id.openmrs_entity, openMrsEntity);
        customTextView.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        customTextView.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        customTextView.setTag(R.id.childKey, item.getString(JsonFormConstants.KEY));
        customTextView.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
    }

    public CustomTextView getExtraInfoTextView() {
        return extraInfoTextView;
    }

    private void setExtraInfoTextView(CustomTextView customTextView) {
        extraInfoTextView = customTextView;
    }

    public CustomTextView getSpecifyTextView() {
        return specifyTextView;
    }

    public void setSpecifyTextView(CustomTextView specifyTextView) {
        this.specifyTextView = specifyTextView;
    }

    public CustomTextView getReasonsTextView() {
        return reasonsTextView;
    }

    public void setReasonsTextView(CustomTextView reasonsTextView) {
        this.reasonsTextView = reasonsTextView;
    }

    private RadioButton getRadioButton() {
        return radioButton;
    }

    private void setRadioButton(RadioButton radioButton) {
        this.radioButton = radioButton;
    }

    private class CustomTextViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            RadioButton radioButton = (RadioButton) view.getTag(R.id.native_radio_button);
            radioButton.setChecked(false);
            radioButton.performClick();
        }
    }

    @Override
    @NonNull
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> customTranslatableWidgetFields = new HashSet<>();
        customTranslatableWidgetFields.add(JsonFormConstants.OPTIONS_FIELD_NAME + "." + JsonFormConstants.TEXT);
        customTranslatableWidgetFields.add(JsonFormConstants.OPTIONS_FIELD_NAME + "." + JsonFormConstants.CONTENT_INFO);
        customTranslatableWidgetFields.add(JsonFormConstants.LABEL_INFO_TEXT);
        customTranslatableWidgetFields.add(JsonFormConstants.LABEL_INFO_TITLE);
        return customTranslatableWidgetFields;
    }
}
