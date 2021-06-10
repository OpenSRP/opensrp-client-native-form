package com.vijay.jsonwizard.widgets;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.GenericTextWatcher;
import com.vijay.jsonwizard.customviews.TimePickerDialog;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

import static com.vijay.jsonwizard.widgets.TimePickerFactory.KEY.DURATION;

public class TimePickerFactory implements FormWidgetFactory {
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final String TIME_FORMAT_REGEX = "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$";
    private static final String TAG = "TimePickerFactory";

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, popup);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, false);
    }

    @Override
    @NonNull
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> customTranslatableWidgetFields = new HashSet<>();
        customTranslatableWidgetFields.add(DURATION + "." + JsonFormConstants.LABEL);
        return customTranslatableWidgetFields;
    }

    private List<View> attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject,
                                  boolean popup) {
        List<View> views = new ArrayList<>(1);
        RelativeLayout dateViewRelativeLayout = getRelativeLayout(context);
        MaterialEditText editText = dateViewRelativeLayout.findViewById(R.id.edit_text);
        TextView duration = dateViewRelativeLayout.findViewById(R.id.duration);
        attachLayout(stepName, context, formFragment, jsonObject, editText, duration);
        JSONArray canvasIds = new JSONArray();
        dateViewRelativeLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(dateViewRelativeLayout.getId());
        editText.setTag(R.id.canvas_ids, canvasIds.toString());
        editText.setTag(R.id.extraPopup, popup);

        ((JsonApi) context).addFormDataView(editText);
        views.add(dateViewRelativeLayout);

        return views;
    }

    @VisibleForTesting
    protected RelativeLayout getRelativeLayout(Context context) {
        return (RelativeLayout) LayoutInflater.from(context).inflate(getLayout(), null);
    }

    protected void attachLayout(String stepName, final Context context, JsonFormFragment formFragment, JSONObject jsonObject,
                                final MaterialEditText editText, final TextView duration) {

        try {
            String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
            String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
            String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
            String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
            String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
            String calculations = jsonObject.optString(JsonFormConstants.CALCULATION);

            duration.setTag(R.id.key, jsonObject.getString(TimePickerFactory.KEY.KEY));
            duration.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
            duration.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
            duration.setTag(R.id.openmrs_entity, openMrsEntity);
            duration.setTag(R.id.openmrs_entity_id, openMrsEntityId);

            editText.setTag(com.vijay.jsonwizard.R.id.locale_independent_value, jsonObject.optString(TimePickerFactory.KEY.VALUE));
            if (jsonObject.has(TimePickerFactory.KEY.DURATION)) {
                duration.setTag(R.id.label, jsonObject.getJSONObject(TimePickerFactory.KEY.DURATION).getString(JsonFormConstants.LABEL));
            }

            updateEditText(editText, jsonObject, stepName, context);
            editText.setTag(R.id.json_object, jsonObject);

            final TimePickerDialog timePickerDialog = createTimeDialog(context, editText);


            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePickerDialog((Activity) context, timePickerDialog);
                }
            });

            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    updateTimeText(editText, "");
                    return true;
                }
            });

            GenericTextWatcher genericTextWatcher = getGenericTextWatcher(stepName, (Activity) context, formFragment,
                    editText, timePickerDialog);
            editText.addTextChangedListener(genericTextWatcher);
            addRefreshLogicView(context, editText, relevance, constraints, calculations);
            editText.setFocusable(false);
        } catch (JSONException e) {
            Timber.e(e);
        } catch (ParseException e) {
            Timber.e(e);
        }
    }

    protected int getLayout() {
        return R.layout.native_form_item_time_picker;
    }

    private void updateEditText(MaterialEditText editText, JSONObject jsonObject, String stepName, Context context
    ) throws JSONException, ParseException {

        Locale locale = getCurrentLocale(context);
        SimpleDateFormat TIME_FORMAT_LOCALE = new SimpleDateFormat("hh:mm", locale);

        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);

        editText.setHint(jsonObject.getString(TimePickerFactory.KEY.HINT));
        editText.setFloatingLabelText(jsonObject.getString(TimePickerFactory.KEY.HINT));
        editText.setId(ViewUtil.generateViewId());
        editText.setTag(R.id.key, jsonObject.getString(TimePickerFactory.KEY.KEY));
        editText.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        editText.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        editText.setTag(R.id.openmrs_entity, openMrsEntity);
        editText.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        editText.setTag(R.id.address, stepName + ":" + jsonObject.getString(TimePickerFactory.KEY.KEY));
        if (jsonObject.has(JsonFormConstants.V_REQUIRED)) {
            JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
            boolean requiredValue = requiredObject.getBoolean(TimePickerFactory.KEY.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                editText.addValidator(new RequiredValidator(requiredObject.getString(JsonFormConstants.ERR)));
                FormUtils.setRequiredOnHint(editText);
            }
        }

        if (StringUtils.isNotBlank(jsonObject.optString(TimePickerFactory.KEY.VALUE))) {
            updateTimeText(editText, TIME_FORMAT_LOCALE.format(TIME_FORMAT.parse(jsonObject.optString(TimePickerFactory.KEY.VALUE))));
        } else if (jsonObject.has(TimePickerFactory.KEY.DEFAULT)) {
            updateTimeText(editText, TIME_FORMAT_LOCALE.format(TIME_FORMAT.parse(jsonObject.optString(TimePickerFactory.KEY.VALUE))));
        }

        if (jsonObject.has(JsonFormConstants.READ_ONLY)) {
            boolean readOnly = jsonObject.getBoolean(JsonFormConstants.READ_ONLY);
            editText.setEnabled(!readOnly);
            editText.setFocusable(!readOnly);
        }
    }

    private TimePickerDialog createTimeDialog(Context context, final MaterialEditText editText) {
        final TimePickerDialog mTimePicker = new TimePickerDialog();
        mTimePicker.setContext(context);
        mTimePicker.setOnTimeSetListener(new android.app.TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                editText.setTag(R.id.locale_independent_value, String.format(Locale.ENGLISH, "%02d", hourOfDay) + ":" + String.format(Locale.ENGLISH, "%02d", minute));
                updateTimeText(editText, hourOfDay, minute);
            }
        });
        return mTimePicker;
    }

    private static void showTimePickerDialog(Activity context,
                                             TimePickerDialog timePickerDialog
    ) {
        FragmentTransaction ft = context.getFragmentManager().beginTransaction();
        Fragment prev = context.getFragmentManager().findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);
        timePickerDialog.show(ft, TAG);

    }

    private void updateTimeText(MaterialEditText editText, String durationText) {
        editText.setText(durationText);
    }

    @NonNull
    private GenericTextWatcher getGenericTextWatcher(String stepName, final Activity context, JsonFormFragment formFragment,
                                                     final MaterialEditText editText,
                                                     final TimePickerDialog timePickerDialog) {
        GenericTextWatcher genericTextWatcher = new GenericTextWatcher(stepName, formFragment, editText);
        genericTextWatcher.addOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimePickerDialog(context, timePickerDialog);
                }
            }
        });
        return genericTextWatcher;
    }

    private void addRefreshLogicView(Context context, MaterialEditText editText, String relevance, String constraints,
                                     String calculations) {
        if (StringUtils.isNotBlank(relevance) && context instanceof JsonApi) {
            editText.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(editText);
        }

        if (StringUtils.isNotBlank(constraints) && context instanceof JsonApi) {
            editText.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(editText);
        }

        if (StringUtils.isNotBlank(calculations) && context instanceof JsonApi) {
            editText.setTag(R.id.calculation, calculations);
            ((JsonApi) context).addCalculationLogicView(editText);
        }
    }

    @VisibleForTesting
    protected Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage().equals("ar") ? Locale.ENGLISH : context.getResources().getConfiguration().locale;
    }

    private void updateTimeText(MaterialEditText editText, int selectedHour, int selectedMinute) {
        String durationText = String.format("%02d:%02d", selectedHour, selectedMinute);
        editText.setText(durationText);
    }

    public static class KEY {
        public static final String DURATION = "duration";
        public static final String HINT = "hint";
        public static final String KEY = "key";
        public static final String VALUE = (JsonFormConstants.VALUE);
        public static final String DEFAULT = "default";
    }
}
