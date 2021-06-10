package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.task.AttachRepeatingGroupTask;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.validators.edittext.MaxNumericValidator;
import com.vijay.jsonwizard.validators.edittext.MinNumericValidator;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.KEY;
import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;

/**
 * @author Vincent Karuri
 */
public class RepeatingGroupFactory implements FormWidgetFactory {

    public static final String REFERENCE_EDIT_TEXT_HINT = "reference_edit_text_hint";
    private static Map<Integer, String> repeatingGroupLayouts = new HashMap<>();
    private final String REPEATING_GROUP_LABEL = "repeating_group_label";
    private final String REFERENCE_EDIT_TEXT = "reference_edit_text";
    private final String REPEATING_GROUP_MAX = "repeating_group_max";
    private final String REPEATING_GROUP_MIN = "repeating_group_min";
    protected int MAX_NUM_REPEATING_GROUPS = 35;
    protected int MIN_NUM_REPEATING_GROUPS = 0;


    @Override
    public List<View> getViewsFromJson(final String stepName, final Context context, final JsonFormFragment formFragment, final JSONObject jsonObject, final CommonListener listener, final boolean popup) throws Exception {
        List<View> views = new ArrayList<>(1);
        LinearLayout rootLayout = getRootLayout(context);

        final int rootLayoutId = View.generateViewId();

        rootLayout.setId(rootLayoutId);
        views.add(rootLayout);

        JSONArray repeatingGroupLayout = jsonObject.getJSONArray(VALUE);
        repeatingGroupLayouts.put(rootLayoutId, repeatingGroupLayout.toString());

        final WidgetArgs widgetArgs = new WidgetArgs()
                .withStepName(stepName)
                .withContext(context)
                .withFormFragment(formFragment)
                .withJsonObject(jsonObject)
                .withListener(listener)
                .withPopup(popup);

        final MaterialEditText referenceEditText = rootLayout.findViewById(R.id.reference_edit_text);
        final ImageButton doneButton = rootLayout.findViewById(R.id.btn_repeating_group_done);

        final String referenceEditTextHint = jsonObject.optString(REFERENCE_EDIT_TEXT_HINT, context.getString(R.string.enter_number_of_repeating_group_items));
        final String repeatingGroupLabel = jsonObject.optString(REPEATING_GROUP_LABEL, context.getString(R.string.repeating_group_item));
        String remoteReferenceEditText = jsonObject.optString(REFERENCE_EDIT_TEXT);
        setRepeatingGroupNumLimits(widgetArgs);

        JSONObject countFieldObject = Utils.getRepeatingGroupCountObj(widgetArgs);

        //get the generated number of groups
        final String generatedGroupsCount = countFieldObject != null ? countFieldObject.optString(VALUE) : "";


        // Enables us to fetch this value from a previous edit_text & disable this one
        retrieveRepeatingGroupCountFromRemoteReferenceEditText(rootLayout, getJsonApi(widgetArgs),
                referenceEditText, remoteReferenceEditText, doneButton, widgetArgs);

        setUpReferenceEditText(doneButton, referenceEditText, referenceEditTextHint,
                repeatingGroupLabel, widgetArgs);


        // Disable the done button if the reference edit text being used is remote & has a valid value
        if (isRemoteReferenceValueUsed(referenceEditText)) {
            doneButton.setVisibility(View.GONE);
        } else {
            //updates referenceEditText with previous count of the repeating grp if it exists
            if (StringUtils.isNotBlank(generatedGroupsCount) && !"0".equals(generatedGroupsCount)) {
                formFragment.getJsonApi().getAppExecutors().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        referenceEditText.setText(generatedGroupsCount);
                    }
                });
                //needed to ensure that repeating grp generated status is maintained after form traversals
                doneButton.setTag(R.id.is_repeating_group_generated, true);
            }

            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addOnDoneAction(referenceEditText, doneButton, widgetArgs);
                }
            });
        }

        ((JsonApi) context).addFormDataView(referenceEditText);
        setViewTags(rootLayout, widgetArgs);
        prepareViewChecks(rootLayout, context, widgetArgs);

        return views;
    }

    @Override
    public List<View> getViewsFromJson(final String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return getViewsFromJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>(Collections.singletonList("reference_edit_text_hint"));
    }

    @VisibleForTesting
    protected LinearLayout getRootLayout(Context context) {
        return (LinearLayout) LayoutInflater.from(context).inflate(getLayout(), null);
    }

    /**
     * Sets min and max number of repeating groups
     */
    private void setRepeatingGroupNumLimits(WidgetArgs widgetArgs) {
        try {
            MIN_NUM_REPEATING_GROUPS = widgetArgs.getJsonObject().optInt(REPEATING_GROUP_MIN, MIN_NUM_REPEATING_GROUPS);
            MAX_NUM_REPEATING_GROUPS = widgetArgs.getJsonObject().optInt(REPEATING_GROUP_MAX, MAX_NUM_REPEATING_GROUPS);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
    }

    private void setViewTags(@NonNull LinearLayout rootLayout, WidgetArgs widgetArgs) {
        JSONArray canvasIds = new JSONArray();
        canvasIds.put(rootLayout.getId());
        rootLayout.setTag(R.id.canvas_ids, canvasIds.toString());
        rootLayout.setTag(R.id.key, widgetArgs.getJsonObject().optString(KEY));
        rootLayout.setTag(R.id.type, widgetArgs.getJsonObject().optString(JsonFormConstants.TYPE));
        rootLayout.setTag(R.id.extraPopup, widgetArgs.isPopup());
        rootLayout.setTag(R.id.address, widgetArgs.getStepName() + ":" + widgetArgs.getJsonObject().optString(KEY));
    }

    private void prepareViewChecks(@NonNull LinearLayout view, @NonNull Context context, WidgetArgs widgetArgs) {
        String relevance = widgetArgs.getJsonObject().optString(JsonFormConstants.RELEVANCE);
        String constraints = widgetArgs.getJsonObject().optString(JsonFormConstants.CONSTRAINTS);
        String calculation = widgetArgs.getJsonObject().optString(JsonFormConstants.CALCULATION);

        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            view.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(view);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            view.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(view);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            view.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(view);
        }
    }

    private void retrieveRepeatingGroupCountFromRemoteReferenceEditText(final @NonNull View rootLayout,
                                                                        @NonNull JsonApi context,
                                                                        @NonNull final MaterialEditText referenceEditText,
                                                                        @Nullable String remoteReferenceEditTextAddress,
                                                                        @NonNull final ImageButton doneButton,
                                                                        @NonNull final WidgetArgs widgetArgs) throws JSONException {

        if (!TextUtils.isEmpty(remoteReferenceEditTextAddress) && remoteReferenceEditTextAddress.contains(":")) {
            String finalRemoteReferenceEditTextAddress = remoteReferenceEditTextAddress.trim();
            String[] addressSections = finalRemoteReferenceEditTextAddress.split(":");

            if (addressSections.length > 1) {
                String remoteReferenceEditTextStep = addressSections[0];
                String remoteReferenceEditTextKey = addressSections[1];
                JSONObject stepJsonObject = context.getmJSONObject().optJSONObject(remoteReferenceEditTextStep);

                if (stepJsonObject != null) {
                    JSONArray fields = getStepFields(stepJsonObject);

                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject stepField = fields.optJSONObject(i);
                        if (stepField != null && stepField.has(KEY) && stepField.getString(KEY).equals(remoteReferenceEditTextKey)) {
                            String fieldValue = stepField.optString(VALUE);

                            if (!StringUtils.isEmpty(fieldValue)) {
                                try {
                                    final int remoteReferenceValue = Integer.parseInt(fieldValue);
                                    referenceEditText.setTag(R.id.repeating_group_remote_reference_value, remoteReferenceValue);

                                    // Start the repeating groups
                                    Object visibilityTag = rootLayout.getTag(R.id.relevance_decided);
                                    if (visibilityTag != null && (boolean) visibilityTag) {
                                        attachRepeatingGroup(referenceEditText.getParent().getParent(),
                                                remoteReferenceValue, doneButton, widgetArgs);
                                    } else {
                                        setGlobalLayoutListener(rootLayout, referenceEditText,
                                                remoteReferenceValue, doneButton, widgetArgs);
                                    }
                                } catch (NumberFormatException ex) {
                                    Timber.e(ex);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    protected void setGlobalLayoutListener(@NonNull final View rootLayout,
                                           @NonNull final MaterialEditText referenceEditText,
                                           @NonNull final int remoteReferenceValue,
                                           @NonNull final ImageButton doneButton,
                                           @NonNull final WidgetArgs widgetArgs) {

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                Object visibilityTag = rootLayout.getTag(R.id.relevance_decided);
                if (visibilityTag != null && (boolean) visibilityTag) {
                    attachRepeatingGroup(referenceEditText.getParent().getParent(),
                            remoteReferenceValue, doneButton, widgetArgs);

                    rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private void setUpReferenceEditText(final ImageButton doneButton,
                                        final MaterialEditText referenceEditText,
                                        final String referenceEditTextHint,
                                        final String repeatingGroupLabel,
                                        final WidgetArgs widgetArgs) throws JSONException {
        // We should disable this edit_text if another referenced edit text is being used
        Context context = widgetArgs.getContext();
        if (isRemoteReferenceValueUsed(referenceEditText)) {
            referenceEditText.setVisibility(View.GONE);
        } else {
            // generate repeating groups on when keyboard done button is pressed
            referenceEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView focusTextView, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        addOnDoneAction(focusTextView, doneButton, widgetArgs);
                        return true;
                    }
                    return false;
                }
            });
        }

        referenceEditText.setTag(R.id.address, widgetArgs.getStepName() + ":" + widgetArgs.getJsonObject().getString(KEY));
        attachTextChangedListener(referenceEditText, doneButton, widgetArgs);
        referenceEditText.setHint(referenceEditTextHint);
        referenceEditText.setTag(R.id.repeating_group_label, repeatingGroupLabel);
        referenceEditText.setTag(R.id.extraPopup, false);
        referenceEditText.setTag(R.id.repeating_group_item_count, 1);
        referenceEditText.setTag(R.id.key, JsonFormConstants.REFERENCE_EDIT_TEXT);
        referenceEditText.setTag(R.id.openmrs_entity_parent, "");
        referenceEditText.setTag(R.id.openmrs_entity, "");
        referenceEditText.setTag(R.id.openmrs_entity_id, "");

        if (!isRemoteReferenceValueUsed(referenceEditText)) {
            referenceEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            referenceEditText.addValidator(new RegexpValidator(context.getString(R.string.repeating_group_number_format_err_msg), "\\d*"));
            referenceEditText.addValidator(new MaxNumericValidator(context.getString(R.string.repeating_group_max_value_err_msg, MAX_NUM_REPEATING_GROUPS), MAX_NUM_REPEATING_GROUPS));
            referenceEditText.addValidator(new MinNumericValidator(context.getString(R.string.repeating_group_min_value_err_msg, MIN_NUM_REPEATING_GROUPS), MIN_NUM_REPEATING_GROUPS));
            addRequiredValidator(widgetArgs.getJsonObject(), referenceEditText);
        }
    }

    private void attachTextChangedListener(final MaterialEditText referenceEditText,
                                           final ImageButton doneButton,
                                           final WidgetArgs widgetArgs) {

        referenceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                doneButton.setImageResource(R.drawable.ic_done_grey);
                ValidationStatus validationStatus = JsonFormFragmentPresenter
                        .validate(widgetArgs.getFormFragment(), referenceEditText, false);
                if (validationStatus.isValid() && widgetArgs.getJsonObject().optBoolean(JsonFormConstants.EXPAND_ON_TEXT_CHANGE)) {
                    addOnDoneAction(referenceEditText, doneButton, widgetArgs);
                }
            }
        });
    }

    private void addRequiredValidator(JSONObject jsonObject, MaterialEditText editText) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                editText.addValidator(new RequiredValidator(requiredObject.getString(JsonFormConstants.ERR)));
                editText.setTag(R.id.has_required_validator, true);
                FormUtils.setRequiredOnHint(editText);
            }
        }
    }

    protected void addOnDoneAction(final TextView textView, final ImageButton doneButton, final WidgetArgs widgetArgs) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) widgetArgs.getFormFragment().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
            textView.clearFocus();
            attachRepeatingGroup(textView.getParent().getParent(),
                    parseIntWithDefault(textView.getText().toString()), doneButton, widgetArgs);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static int parseIntWithDefault(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void attachRepeatingGroup(final ViewParent parent, final int numRepeatingGroups,
                                      final ImageButton doneButton, final WidgetArgs widgetArgs) {
        if (numRepeatingGroups > MAX_NUM_REPEATING_GROUPS) {
            return;
        }

        new AttachRepeatingGroupTask(parent, numRepeatingGroups, repeatingGroupLayouts,
                widgetArgs, doneButton).execute();
    }

    protected int getLayout() {
        return R.layout.native_form_repeating_group;
    }

    private boolean isRemoteReferenceValueUsed(@NonNull View referenceEditText) {
        Object tagValue = referenceEditText.getTag(R.id.repeating_group_remote_reference_value);
        return tagValue instanceof Integer && (int) tagValue > -1;
    }

    public int remoteReferenceValue(@NonNull View referenceEditText) {
        Object tagValue = referenceEditText.getTag(R.id.repeating_group_remote_reference_value);
        return tagValue instanceof Integer ? (int) tagValue : 0;
    }

    private JsonApi getJsonApi(WidgetArgs widgetArgs) {
        Context context = widgetArgs.getContext();
        return context instanceof JsonApi ? (JsonApi) context : null;
    }

    private JSONArray getStepFields(JSONObject step) {
        return step.optJSONArray(FIELDS);
    }
}