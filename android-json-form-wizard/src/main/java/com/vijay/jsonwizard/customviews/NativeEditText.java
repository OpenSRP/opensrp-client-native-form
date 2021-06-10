package com.vijay.jsonwizard.customviews;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.rengwuxian.materialedittext.validation.METLengthChecker;
import com.rengwuxian.materialedittext.validation.METValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NativeEditText extends AppCompatEditText {
    OnFocusChangeListener innerFocusChangeListener;
    OnFocusChangeListener outerFocusChangeListener;
    private List<METValidator> validators;
    private METLengthChecker lengthChecker;
    private boolean firstShown;
    /**
     * Whether to validate as soon as the text has changed. False by default
     */
    private boolean autoValidate;
    /**
     * Whether check the characters count at the beginning it's shown.
     */
    private boolean checkCharactersCountAtBeginning;

    /**
     * Whether the characters count is valid
     */
    private boolean charactersCountValid;

    /**
     * min characters count limit. 0 means no limit. default is 0. NOTE: the character counter will increase the View's height.
     */
    private int minCharacters;

    /**
     * max characters count limit. 0 means no limit. default is 0. NOTE: the character counter will increase the View's height.
     */
    private int maxCharacters;

    public NativeEditText(Context context) {
        super(context);
    }

    public NativeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NativeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initTextWatcher() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkCharactersCount();
                if (autoValidate) {
                    validate();
                } else {
                    setError(null);
                }
                postInvalidate();
            }
        });
    }

    private void checkCharactersCount() {
        if ((!firstShown && !checkCharactersCountAtBeginning) || !hasCharactersCounter()) {
            charactersCountValid = true;
        } else {
            CharSequence text = getText();
            int count = text == null ? 0 : checkLength(text);
            charactersCountValid = (count >= minCharacters && (maxCharacters <= 0 || count <= maxCharacters));
        }
    }

    private boolean hasCharactersCounter() {
        return minCharacters > 0 || maxCharacters > 0;
    }

    /**
     * if the main text matches the regex
     *
     * @deprecated use the new validator interface to add your own custom validator
     */
    @Deprecated
    public boolean isValid(String regex) {
        if (regex == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(getText());
        return matcher.matches();
    }

    /**
     * check if the main text matches the regex, and set the error text if not.
     *
     * @return true if it matches the regex, false if not.
     * @deprecated use the new validator interface to add your own custom validator
     */
    @Deprecated
    public boolean validate(String regex, CharSequence errorText) {
        boolean isValid = isValid(regex);
        if (!isValid) {
            setError(errorText);
        }
        postInvalidate();
        return isValid;
    }

    /**
     * Run validation on a single validator instance
     *
     * @param validator Validator to check
     * @return True if valid, false if not
     */
    public boolean validateWith(@NonNull METValidator validator) {
        CharSequence text = getText();
        boolean isValid = validator.isValid(text, text.length() == 0);
        if (!isValid) {
            setError(validator.getErrorMessage());
        }
        postInvalidate();
        return isValid;
    }

    /**
     * Check all validators, sets the error text if not
     * <p/>
     * NOTE: this stops at the first validator to report invalid.
     *
     * @return True if all validators pass, false if not
     */
    public boolean validate() {
        if (validators == null || validators.isEmpty()) {
            return true;
        }

        CharSequence text = getText();
        boolean isEmpty = text.length() == 0;

        boolean isValid = true;
        for (final METValidator validator : validators) {
            //noinspection ConstantConditions
            isValid = isValid && validator.isValid(text, isEmpty);
            if (!isValid) {
                ((Activity) this.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setError(validator.getErrorMessage());
                    }
                });
                break;
            }
        }
        if (isValid) {
            ((Activity) this.getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setError(null);
                }
            });
        }
        postInvalidate();
        return isValid;
    }

    public boolean hasValidators() {
        return this.validators != null && !this.validators.isEmpty();
    }

    /**
     * Adds a new validator to the View's list of validators
     * <p/>
     * This will be checked with the others in {@link #validate()}
     *
     * @param validator Validator to add
     * @return This instance, for easy chaining
     */
    public NativeEditText addValidator(METValidator validator) {
        if (validators == null) {
            this.validators = new ArrayList<>();
        }
        this.validators.add(validator);
        return this;
    }

    public void clearValidators() {
        if (this.validators != null) {
            this.validators.clear();
        }
    }

    @Nullable
    public List<METValidator> getValidators() {
        return this.validators;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        if (innerFocusChangeListener == null) {
            super.setOnFocusChangeListener(listener);
        } else {
            outerFocusChangeListener = listener;
        }
    }

    public void setLengthChecker(METLengthChecker lengthChecker) {
        this.lengthChecker = lengthChecker;
    }

    private int checkLength(CharSequence text) {
        if (lengthChecker == null) return text.length();
        return lengthChecker.getLength(text);
    }
}
