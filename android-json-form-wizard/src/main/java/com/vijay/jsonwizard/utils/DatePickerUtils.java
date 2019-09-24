package com.vijay.jsonwizard.utils;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by keyman on 25/04/2017.
 */
public class DatePickerUtils {

    private static final String TAG = DatePickerUtils.class.getCanonicalName();
    private static final int SPINNER_COUNT = 3;

    public static void themeDatePicker(DatePickerDialog dialog, char[] ymdOrder) {
        if (!dialog.isShowing()) {
            throw new IllegalStateException("Dialog must be showing");
        }

        themeDatePicker(dialog.getDatePicker(), ymdOrder);
    }

    public static void themeDatePicker(DatePicker datePicker, char[] ymdOrder) {
        try {
            preventShowingKeyboard(datePicker);
            final int idYear = Resources.getSystem().getIdentifier("year", "id", "android");
            final int idMonth = Resources.getSystem().getIdentifier("month", "id", "android");
            final int idDay = Resources.getSystem().getIdentifier("day", "id", "android");
            final int idLayout = Resources.getSystem().getIdentifier("pickers", "id", "android");

            final NumberPicker spinnerYear = datePicker.findViewById(idYear);
            final NumberPicker spinnerMonth = datePicker.findViewById(idMonth);
            final NumberPicker spinnerDay = datePicker.findViewById(idDay);
            final LinearLayout layout = datePicker.findViewById(idLayout);

            layout.removeAllViews();
            for (int i = 0; i < SPINNER_COUNT; i++) {
                switch (ymdOrder[i]) {
                    case 'y':
                        layout.addView(spinnerYear);
                        setImeOptions(spinnerYear, i);
                        break;
                    case 'm':
                        layout.addView(spinnerMonth);
                        setImeOptions(spinnerMonth, i);
                        break;
                    case 'd':
                        layout.addView(spinnerDay);
                        setImeOptions(spinnerDay, i);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid char[] ymdOrder");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public static void preventShowingKeyboard(DatePicker datePicker) {
        datePicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    private static void setImeOptions(NumberPicker spinner, int spinnerIndex) {
        final int imeOptions;
        if (spinnerIndex < SPINNER_COUNT - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        int idPickerInput = Resources.getSystem().getIdentifier("numberpicker_input", "id", "android");
        TextView input = spinner.findViewById(idPickerInput);
        input.setImeOptions(imeOptions);
    }
}
