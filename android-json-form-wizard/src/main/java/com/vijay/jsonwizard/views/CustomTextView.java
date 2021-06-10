package com.vijay.jsonwizard.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatTextView;

import com.vijay.jsonwizard.R;

/**
 * Basically this custom TextView also provides the following capabilities:
 * <p>
 * <ul>
 * <li>Grey when disabled - Rather the hint color set in the theme</li>
 * <li>Returns to previous color when re-enabled</li>
 * <li>Hinted text is color {@link com.vijay.jsonwizard.R.color#text_hint_color} - Hinted text
 * is text between <> tags (tags inclusive). Furthermore, the tags are not removed</></li>
 * </ul>
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/04/2018.
 */
public class CustomTextView extends AppCompatTextView {

    private boolean hintOnText = false;


    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isHintOnText() {
        return hintOnText;
    }

    public void setHintOnText(boolean hintOnText) {
        this.hintOnText = hintOnText;
        setText(getText().toString());
    }

    @Override
    public void setTextColor(@ColorInt int color) {
        ColorStateList colorStateList = getTextColors();

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                color,
                colorStateList.getColorForState(new int[]{-android.R.attr.state_enabled}, color),
                colorStateList.getColorForState(new int[]{-android.R.attr.state_checked}, color),
                colorStateList.getColorForState(new int[]{android.R.attr.state_pressed}, color)
        };

        ColorStateList newColorStateList = new ColorStateList(states, colors);
        setTextColor(newColorStateList);
    }

    public void setText(String text) {
        if (hintOnText && isEnabled()) {
            int currentStartScanIndex = 0;

            SpannableString styledString = new SpannableString(text);
            while (currentStartScanIndex > -1) {
                currentStartScanIndex = text.indexOf("<", currentStartScanIndex);
                if (currentStartScanIndex > -1) {
                    int endTagIndex = text.indexOf(">", currentStartScanIndex + 1);

                    if (endTagIndex > currentStartScanIndex) {
                        styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.toaster_note_red_icon)), currentStartScanIndex,
                                endTagIndex + 1, 0);
                    }
                    currentStartScanIndex = endTagIndex;
                }
            }

            setText(styledString);
        } else {
            setText((CharSequence) text);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        String text = getText().toString();
        setText(text);
    }
}
