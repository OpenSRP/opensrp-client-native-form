package com.vijay.jsonwizard.validators.edittext;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.JsonApi;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by samuelgithengi on 3/4/19.
 */
public class CumulativeTotalValidator extends METValidator {


    private final String TAG = CumulativeTotalValidator.class.getName();

    private JSONArray relatedFields;
    private JsonApi jsonApi;
    private JsonFormFragment formFragment;
    private String step;
    private String totalValueFieldKey;
    private MaterialEditText referenceEditText;
    private List<ReferenceValidator> referenceValidators = new ArrayList<>();


    public CumulativeTotalValidator(@NonNull String errorMessage, @NonNull JsonFormFragment formFragment,
                                    String step, String totalValueFieldKey, JSONArray relatedFields, JsonApi jsonApi) {
        super(errorMessage);
        this.formFragment = formFragment;
        this.step = step;
        this.totalValueFieldKey = totalValueFieldKey;
        this.relatedFields = relatedFields;
        this.jsonApi = jsonApi;
    }

    @Override
    public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
        if (!isEmpty) {

            try {
                float cumulativeTotal = Float.parseFloat(text.toString());
                float totalMaxFieldValue = 0;
                View totalMaxField = jsonApi.getFormDataView(step + ":" + totalValueFieldKey);

                if (totalMaxField instanceof TextView)
                    totalMaxFieldValue = Float.parseFloat(((TextView) totalMaxField).getText().toString());

                for (int i = 0; i < relatedFields.length(); i++) {
                    View relatedView = jsonApi.getFormDataView(step + ":" + relatedFields.getString(i));
                    if (relatedView instanceof MaterialEditText) {
                        MaterialEditText editText = (MaterialEditText) relatedView;
                        CharSequence value = editText.getText();
                        if (!TextUtils.isEmpty(value))
                            cumulativeTotal += Float.parseFloat(value.toString());
                    }

                }

                if (cumulativeTotal != totalMaxFieldValue) {
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }

            for (ReferenceValidator validator : referenceValidators) {
                disableValidator(validator.getEditText());
            }
            disableValidator(referenceEditText);
        }
        return true;
    }

    private void disableValidator(@Nullable MaterialEditText editText) {
        if (editText != null) {
            try {
                jsonApi.writeValue(step, getTag(editText.getTag(R.id.key)), editText.getText().toString(),
                        getTag(editText.getTag(R.id.openmrs_entity_parent)),
                        getTag(editText.getTag(R.id.openmrs_entity)),
                        getTag(editText.getTag(R.id.openmrs_entity_id)), false);
            } catch (JSONException e) {
                Timber.e(e, "Error writing field value");
            }
            editText.setError(null);
            editText.postInvalidate();
        }
    }

    private String getTag(Object object) {
        return object == null ? null : object.toString();

    }


    public boolean isValid(@NonNull MaterialEditText referenceEditText, boolean isEmpty) {
        this.referenceEditText = referenceEditText;
        return isValid(TextUtils.isEmpty(referenceEditText.getText()) ? "0" : referenceEditText.getText(), isEmpty);
    }

    public List<ReferenceValidator> getReferenceValidators() {
        return referenceValidators;
    }
}
