package com.vijay.jsonwizard.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * Created by ndegwamartin on 29/11/2018.
 */
public abstract class TextableView extends LinearLayout {
    public TextableView(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public TextableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public TextableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public TextableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // a fix to reset paddingLeft attribute
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.padding,
                    android.R.attr.paddingLeft}, defStyleAttr, defStyleRes);

            if (!a.hasValue(0) && !a.hasValue(1)) {
                setPadding(0, getPaddingTop(), getPaddingRight(), getPaddingBottom());
            }

            a.recycle();
        }

        setClickable(true);
    }

    public abstract void setText(CharSequence text);

    public abstract CharSequence getText();

}
