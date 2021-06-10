package com.vijay.jsonwizard.validators.edittext;

import androidx.annotation.NonNull;

import com.rengwuxian.materialedittext.validation.METValidator;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static com.vijay.jsonwizard.utils.FormUtils.fields;
import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

/**
 * @author Vincent Karuri
 */
public class RelativeNumericValidator extends METValidator {

    private JsonFormFragment formFragment;
    private String bindMaxValTo;
    private String step;
    private int exception;
    protected boolean isMaxValidator;

    public RelativeNumericValidator(@NonNull String errorMessage, @NonNull JsonFormFragment formFragment, @NonNull String bindMaxValTo, int exception, String step, boolean isMaxValidator) {
        super(errorMessage);
        this.formFragment = formFragment;
        this.bindMaxValTo = bindMaxValTo;
        this.step = step == null ? STEP1 : step;
        this.exception = exception;
        this.isMaxValidator = isMaxValidator;
    }

    public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
        return isValid(text, isEmpty, isMaxValidator);
    }

    public boolean isValid(@NonNull CharSequence text, boolean isEmpty, boolean isMaxValidator) {
        if (!isEmpty) {
            try {
                JSONObject formJSONObject = new JSONObject(formFragment.getCurrentJsonState());
                JSONArray formFields = fields(formJSONObject, step);
                int relativeMaxFieldValue = getFieldJSONObject(formFields, bindMaxValTo).optInt(JsonFormConstants.VALUE);
                int currentTextValue = Integer.parseInt(text.toString());
                boolean isInvalid = isMaxValidator ? currentTextValue > relativeMaxFieldValue : currentTextValue < relativeMaxFieldValue;
                if (isInvalid && (currentTextValue != exception || exception == Integer.MIN_VALUE)) {
                    return false;
                }
            } catch (Exception e) {
                Timber.e(e);
                return false;
            }
        }
        return true;
    }


}

