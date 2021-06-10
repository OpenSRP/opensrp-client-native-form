package com.vijay.jsonwizard.validators.edittext;

import android.util.Log;

import androidx.annotation.NonNull;

import com.rengwuxian.materialedittext.validation.METValidator;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static com.vijay.jsonwizard.utils.FormUtils.fields;
import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

/**
 * @author Vincent Karuri
 */
public class RelativeMaxNumericValidator extends METValidator {

        private JsonFormFragment formFragment;
        private String bindMaxValTo;
        private String step;
        private int exception;

        private final String TAG = RelativeMaxNumericValidator.class.getName();

        public RelativeMaxNumericValidator(@NonNull String errorMessage, @NonNull  JsonFormFragment formFragment, @NonNull String bindMaxValTo, int exception, String step) {
            super(errorMessage);
            this.formFragment = formFragment;
            this.bindMaxValTo = bindMaxValTo;
            this.step = step == null ? STEP1 : step;
            this.exception = exception;
        }

        public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
            if (!isEmpty) {
                try {
                    JSONObject formJSONObject = new JSONObject(formFragment.getCurrentJsonState());
                    JSONArray formFields = fields(formJSONObject, step);
                    int relativeMaxFieldValue = getFieldJSONObject(formFields, bindMaxValTo).optInt(JsonFormConstants.VALUE);
                    int currentTextValue = Integer.parseInt(text.toString());
                    if (currentTextValue > relativeMaxFieldValue && (currentTextValue != exception || exception == Integer.MIN_VALUE)) {
                        return false;
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    return false;
                }
            }
            return true;
        }
}

