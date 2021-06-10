package com.vijay.jsonwizard.customviews;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.Nullable;

import com.vijay.jsonwizard.R;

import java.util.Calendar;
import java.util.Date;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

public class TimePickerDialog extends DialogFragment {
    private TimePicker timePicker;
    private android.app.TimePickerDialog.OnTimeSetListener onTimeSetListener;

    private Date date;
    private DialogInterface.OnShowListener onShowListener;

    private Context context;

    public void setContext(Context context) throws IllegalStateException {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (context == null) {
            throw new IllegalStateException(
                    "The Context is not set. Did you forget to set context with TimePickerDialog.setContext method?");
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    public void setOnShowListener(DialogInterface.OnShowListener onShowListener_) {

        onShowListener = onShowListener_;
    }

    public DialogInterface.OnShowListener getOnShowListener(){
        return onShowListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.native_form_dialog_time_picker, container, false);

        Button cancelButton;
        Button okButton;
        if(getOnShowListener()==null){
            setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    InputMethodManager inputManager = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager
                            .hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), HIDE_NOT_ALWAYS);
                }
            });
        }


        timePicker = dialogView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
//        if (minDate != -1) {
//            timePicker.setMinDate(minDate);
//        }
//        if (maxDate != -1) {
//            timePicker.setMaxDate(maxDate);
//        }
//        timePicker.setCalendarViewShown(calendarViewShown);
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            setDate(calendar);
        }
//        DatePickerUtils.themeDatePicker(timePicker, new char[]{'d', 'm', 'y'});

        cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.this.dismiss();
            }
        });

        okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onTimeSetListener != null) {
                    TimePickerDialog.this.dismiss();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        onTimeSetListener.onTimeSet(timePicker, timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute());
//                    }
                }
            }
        });

        return dialogView;
    }

    public void setDate(Date date) {
        this.date = date;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date);
        setDate(calendar);
    }

    private void setDate(Calendar calendar) {
        if (this.timePicker != null) {

            Date today = calendar.getTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(today.getHours());
                timePicker.setMinute(today.getMinutes());
            }
        }
    }

    public void setOnTimeSetListener(android.app.TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener = onTimeSetListener;
    }


}
