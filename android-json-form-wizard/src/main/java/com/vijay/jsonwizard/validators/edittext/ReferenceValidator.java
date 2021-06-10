package com.vijay.jsonwizard.validators.edittext;

import androidx.annotation.NonNull;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

/**
 * Created by samuelgithengi on 9/6/19.
 */
public class ReferenceValidator extends METValidator {

    private CumulativeTotalValidator referenceValidator;

    private MaterialEditText editText;
    private MaterialEditText referenceEditText;


    public ReferenceValidator(@NonNull String errorMessage, CumulativeTotalValidator referenceValidator, MaterialEditText editText, MaterialEditText referenceEditText) {
        super(errorMessage);
        this.referenceValidator = referenceValidator;
        this.editText = editText;
        this.referenceEditText = referenceEditText;
    }

    @Override
    public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {

        return referenceValidator.isValid(referenceEditText, isEmpty);

    }

    public MaterialEditText getEditText() {
        return editText;
    }
}
