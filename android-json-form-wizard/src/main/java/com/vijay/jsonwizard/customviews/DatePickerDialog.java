package com.vijay.jsonwizard.customviews;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.Nullable;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.utils.DatePickerUtils;

import java.util.Calendar;
import java.util.Date;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

/**
 * Created by Jason Rogena - jrogena@ona.io on 08/05/2017.
 */

public class DatePickerDialog extends DialogFragment {
    private DatePicker datePicker;
    private android.app.DatePickerDialog.OnDateSetListener onDateSetListener;
    private DialogInterface.OnShowListener onShowListener;
    private Date date;
    private long minDate;
    private long maxDate;
    private boolean calendarViewShown;
    private Context context;
    private char[] ymdOrder = new char[]{'d', 'm', 'y'};
    private boolean isNumericDatePicker = false;

    public DatePickerDialog() {
        this.minDate = -1;
        this.maxDate = -1;
        this.calendarViewShown = true;
    }

    public void setContext(Context context) throws IllegalStateException {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (context == null) {
            throw new IllegalStateException(
                    "The Context is not set. Did you forget to set context with DatePickerDialog.setContext method?");
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    public void setOnShowListener(DialogInterface.OnShowListener onShowListener_) {
        onShowListener = onShowListener_;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.native_form_dialog_date_picker, container, false);

        Button cancelButton;
        Button okButton;

        setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager inputManager = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager
                        .hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), HIDE_NOT_ALWAYS);
            }
        });

        datePicker = dialogView.findViewById(isNumericDatePicker ? R.id.date_picker_numeric : R.id.date_picker);
        datePicker.setVisibility(View.VISIBLE);

        if (minDate != -1) {
            datePicker.setMinDate(minDate);
        }
        if (maxDate != -1) {
            datePicker.setMaxDate(maxDate);
        }
        datePicker.setCalendarViewShown(calendarViewShown);
        if (date != null) {

            Calendar calendar = Calendar.getInstance();

            Date minDate = new Date(datePicker.getMinDate());
            Date maxDate = new Date(datePicker.getMaxDate());

            if (datePicker.getMinDate() != 0) {
                calendar.setTime(date.before(minDate) ? minDate : date);
            }

            if (datePicker.getMaxDate() != 0) {
                calendar.setTime(date.after(maxDate) ? maxDate : date);

            }

            if (datePicker.getMinDate() == 0 && datePicker.getMaxDate() == 0) {

                calendar.setTime(date);
            }

            setDate(calendar);
        }
        DatePickerUtils.themeDatePicker(datePicker, ymdOrder);

        cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.this.dismiss();
            }
        });

        okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDateSetListener != null) {
                    DatePickerDialog.this.dismiss();
                    onDateSetListener.onDateSet(datePicker, datePicker.getYear(),
                            datePicker.getMonth(), datePicker.getDayOfMonth());
                }
            }
        });

        return dialogView;
    }

    public void setMinDate(long minDate) {
        this.minDate = minDate;
    }

    public void setMaxDate(long maxDate) {
        this.maxDate = maxDate;
    }

    public void setCalendarViewShown(boolean calendarViewShown) {
        this.calendarViewShown = calendarViewShown;
    }

    public void setYmdOrder(char[] ymdOrder) {
        this.ymdOrder = ymdOrder;
    }

    public void setDate(Date date) {
        this.date = date;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date);
        setDate(calendar);
    }

    private void setDate(Calendar calendar) {

        if (this.datePicker != null) {
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }
    }

    public void setOnDateSetListener(android.app.DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    public DatePicker getDatePicker() {
        return this.datePicker;
    }

    public void setNumericDatePicker(boolean numericDatePicker) {
        isNumericDatePicker = numericDatePicker;
    }
}
