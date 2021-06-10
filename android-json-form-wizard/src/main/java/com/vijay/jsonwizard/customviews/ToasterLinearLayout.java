package com.vijay.jsonwizard.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vijay.jsonwizard.R;

/**
 * Created by ndegwamartin on 29/11/2018.
 */
public class ToasterLinearLayout extends TextableView {
    public ToasterLinearLayout(Context context) {
        super(context);
    }

    public ToasterLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ToasterLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ToasterLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(CharSequence text) {
        ((TextView) findViewById(R.id.toaster_notes_text)).setText(text);
    }

    @Override
    public CharSequence getText() {
        return ((TextView) findViewById(R.id.toaster_notes_text)).getText();
    }
}
