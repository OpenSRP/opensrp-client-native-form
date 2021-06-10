package com.vijay.jsonwizard.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.VisibleForTesting;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.utils.NumericDatePickerHelper;
import com.vijay.jsonwizard.utils.NumericDatePickerValidator;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by ndegwamartin on 2020-01-20.
 * Custom date picker widget that represents months as numeric digits
 */
public class NumericDatePicker extends DatePicker {

    private NumberPicker dayPicker;
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;
    private long minDate;
    private long maxDate;
    private DatePicker.OnDateChangedListener onDateChangedListener;

    private int minDay;
    private int minMonth;
    private int minYear;
    private int maxDay;
    private int maxMonth;
    private int maxYear;

    private int changedPickerId = 0;

    private int previousDay;

    private final NumberPicker.OnValueChangeListener onValueChangeListener =
            new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {

                    numberPicker.setTag(R.id.previous, oldValue);

                    changedPickerId = numberPicker.getId();

                    resetPickers();

                    changedPickerId = 0;

                    if (onDateChangedListener != null) {
                        onDateChangedListener.onDateChanged(NumericDatePicker.this, getYear(), getMonth(), getDayOfMonth());
                    }
                }
            };

    /**
     * This Reset the number picker value to a correct state with respect to min and max constraints as well as gregorian calendar month differences
     */
    @VisibleForTesting
    protected void resetDatePicker() {

        processDependencySpin(); //Before validation, we can do dependency spin

        NumericDatePickerValidator validator = validateCurrentSelectedDate();

        List<Integer> previousMinResetPickerIds = new ArrayList<>();
        List<Integer> previousMaxResetPickerIds = new ArrayList<>();

        while (!(validator.isValid()) && changedPickerId > 0) {

            NumericDatePickerValidator.Violation violation = validator.getViolation();

            switch (violation) {
                case MALFORMED_DATE:
                    resetPicker(dayPicker, getPreviousDay(previousDay, minDay));

                    break;

                case MAX_DATE:

                    if (changedPickerId == R.id.year || !previousMaxResetPickerIds.contains(R.id.year)) {

                        resetPicker(yearPicker, maxYear);
                        previousMaxResetPickerIds.add(R.id.year);

                    } else if (changedPickerId == R.id.month || !previousMaxResetPickerIds.contains(R.id.month)) {

                        monthPicker.setMaxValue(maxMonth + 1);
                        resetPicker(monthPicker, maxMonth + 1);
                        previousMaxResetPickerIds.add(R.id.month);

                    } else if (changedPickerId == R.id.day || !previousMaxResetPickerIds.contains(R.id.day)) {

                        dayPicker.setMaxValue(maxDay);
                        resetPicker(dayPicker, maxDay);
                        previousMaxResetPickerIds.add(R.id.day);

                    }

                    break;

                case MIN_DATE:

                    if (changedPickerId == R.id.year || !previousMinResetPickerIds.contains(R.id.year)) {

                        resetPicker(yearPicker, minYear);
                        previousMinResetPickerIds.add(R.id.year);

                    } else if (changedPickerId == R.id.month || !previousMinResetPickerIds.contains(R.id.month)) {

                        monthPicker.setMinValue(minMonth + 1);
                        resetPicker(monthPicker, minMonth + 1);
                        previousMinResetPickerIds.add(R.id.month);

                    } else if (changedPickerId == R.id.day || !previousMinResetPickerIds.contains(R.id.day)) {

                        dayPicker.setMinValue(minDay);
                        resetPicker(dayPicker, minDay);
                        previousMinResetPickerIds.add(R.id.day);

                    }

                    break;

                default:
                    break;

            }

            validator = validateCurrentSelectedDate();

        }

        //Before setting constraint, default to today's date if none is set

        if (yearPicker.getValue() == 0) {
            yearPicker.setValue(Calendar.getInstance().get(Calendar.YEAR));
            monthPicker.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1);
            dayPicker.setValue(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        }

        //Year max and min never change

        yearPicker.setMinValue(minYear);
        yearPicker.setMaxValue(maxYear);

        dayPicker.setWrapSelectorWheel(resetWrapSelector(dayPicker));
        monthPicker.setWrapSelectorWheel(resetWrapSelector(monthPicker));
        yearPicker.setWrapSelectorWheel(resetWrapSelector(yearPicker));

        setMinMaxValues();

        previousDay = getDayOfMonth();

    }

    /**
     * Trigger dependent spinners if within max and min constraint bounds
     * The Year has no dependents
     */
    private void processDependencySpin() {

        //We only care about day and month, Year has none

        if (changedPickerId > 0) {

            if (changedPickerId == R.id.month) {

                if (Integer.valueOf(monthPicker.getTag(R.id.previous).toString()).equals(1) && getMonth() == 11 && (getYear() - 1) <= maxYear) {

                    yearPicker.setValue(getYear() - 1);
                    changedPickerId = R.id.year;

                } else if (Integer.valueOf(monthPicker.getTag(R.id.previous).toString()).equals(12) && getMonth() == 0 && (getYear() + 1) >= minYear) {

                    yearPicker.setValue(getYear() + 1);
                    changedPickerId = R.id.year;
                }


            } else if (changedPickerId == R.id.day) {
                if (Integer.valueOf(dayPicker.getTag(R.id.previous).toString()).equals(1) && getDayOfMonth() == getMaxDayForSelectedDate()) {

                    if ((monthPicker.getValue() - 1) <= maxMonth) {

                        monthPicker.setValue(monthPicker.getValue() - 1);

                    } else {

                        monthPicker.setValue(maxMonth + 1);
                    }

                    changedPickerId = R.id.month;

                } else if (Integer.valueOf(dayPicker.getTag(R.id.previous).toString()).equals(getMaxDayForSelectedDate()) && getDayOfMonth() == 1 && (monthPicker.getValue() + 1) >= minMonth) {

                    if ((monthPicker.getValue() - 1) <= maxMonth) {

                        monthPicker.setValue(monthPicker.getValue() + 1);

                    } else {

                        monthPicker.setValue(minMonth + 1);
                    }


                    changedPickerId = R.id.month;
                }

            }

        }

    }

    private void setMinMaxValues() {

        if (isMinMonthEdge()) {
            monthPicker.setMinValue(minMonth + 1);
        } else {
            monthPicker.setMinValue(1);
        }

        if (isMaxMonthEdge()) {
            monthPicker.setMaxValue(maxMonth + 1);
        } else {
            monthPicker.setMaxValue(12);
        }

        if (isMinDayEdge()) {
            dayPicker.setMinValue(minDay);

        } else {
            dayPicker.setMinValue(1);
        }

        if (isMaxDayEdge()) {
            dayPicker.setMaxValue(maxDay);

        } else {
            dayPicker.setMaxValue(getMaxDayForSelectedDate());
        }
    }

    private final NumberPicker.Formatter pickerDigitFormatter = new NumberPicker.Formatter() {
        final StringBuilder stringBuilder = new StringBuilder();
        final Formatter formatter = new Formatter(stringBuilder, Locale.ENGLISH);
        final Object[] arguments = new Object[1];

        public String format(int value) {
            arguments[0] = value;
            stringBuilder.delete(0, stringBuilder.length());
            formatter.format("%02d", arguments);
            return formatter.toString();
        }
    };

    public NumericDatePicker(Context context) {
        super(context);
        setupView();
    }

    public NumericDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    public NumericDatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NumericDatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupView();

    }

    /**
     * Init's the widget components
     */
    private void setupView() {
        int todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int todayMonth = Calendar.getInstance().get(Calendar.MONTH);
        int todayYear = Calendar.getInstance().get(Calendar.YEAR);

        disableSuperCanvas();
        checkConstraintsConstraint();

        inflate(getContext(), R.layout.number_picker_view, this);

        dayPicker = initNumericPicker(R.id.day, 1, 30);

        monthPicker = initNumericPicker(R.id.month, 1, 12);

        yearPicker = initNumericPicker(R.id.year, 0, todayYear + 1000);

        updateDate(todayYear, todayMonth, todayDay);

    }

    @VisibleForTesting
    public NumberPicker initNumericPicker(int viewId, int minConstraint, int maxConstraint) {

        NumberPicker numberPicker = findViewById(viewId);
        numberPicker.setOnValueChangedListener(onValueChangeListener);
        setDateConstraints(numberPicker, minConstraint, maxConstraint);
        numberPicker.setFormatter(pickerDigitFormatter);
        numberPicker.setWrapSelectorWheel(true);

        return numberPicker;
    }

    /**
     * Disables view canvas of super
     */
    private void disableSuperCanvas() {
        ((NumericDatePicker) super.getRootView()).getChildAt(0).setVisibility(GONE);
    }

    /**
     * Helper,sets the pickers constraints individually
     *
     * @param numberPicker the number picker widget
     * @param value        value to set
     * @param isMax        boolean to flag max or min value
     */
    protected void setDateConstraints(NumberPicker numberPicker, int value, boolean isMax) {

        if (isMax) {
            numberPicker.setMaxValue(value);
        } else {
            numberPicker.setMinValue(value);
        }

    }

    /**
     * Helper,sets all the pickers constraints simultaneously
     *
     * @param numberPicker the number picker widget
     * @param minDateValue value to set
     * @param maxDateValue boolean to flag max or min value
     */
    protected void setDateConstraints(NumberPicker numberPicker, int minDateValue, int maxDateValue) {

        setDateConstraints(numberPicker, minDateValue, false);

        setDateConstraints(numberPicker, maxDateValue, true);

    }

    /**
     * Resets widgets to correct state
     */
    private void resetPickers() {

        checkConstraintsConstraint();

        resetDatePicker();


    }

    /**
     * Weired bug where the day value doesn't show unless user touches widget
     * https://issuetracker.google.com/issues/36952035
     */
    private void fixWeirdNumberPickerBug(NumberPicker mPicker) {
        // Fix for bug in Android Picker where the first element is not shown
        EditText firstItem = (EditText) mPicker.getChildAt(0);
        if (firstItem != null) {
            firstItem.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void init(int year, int month, int dayOfMonth, DatePicker.OnDateChangedListener onDateChangedListener) {

        updateDate(year, month, dayOfMonth);

        setOnDateChangedListener(onDateChangedListener);
    }

    @Override
    public void setOnDateChangedListener(DatePicker.OnDateChangedListener onDateChangedListener) {

        this.onDateChangedListener = onDateChangedListener;
    }

    //The month starts from zero
    @Override
    public void updateDate(int year_, int month_, int dayOfMonth_) {

        int year = year_;
        int month = month_;
        int dayOfMonth = dayOfMonth_;

        if (isMaxConstraintViolated(year, month + 1, dayOfMonth)) {

            dayOfMonth = maxDay;
            month = maxMonth;
            year = maxYear;

        }

        if (isMinConstraintViolated(year, month + 1, dayOfMonth)) {

            dayOfMonth = minDay;
            month = minMonth;
            year = minYear;
        }

        dayPicker.setValue(dayOfMonth);
        monthPicker.setValue(month + 1);
        yearPicker.setValue(year);
        resetPickers();

    }

    /**
     * Checks to see if there is a minimum value set and whether its value is greater than the parameter provided
     *
     * @param year       year of date
     * @param month      month of date starts with 1 for January
     * @param dayOfMonth day of month of date
     * @return boolean value indicating whether the constraint was violated
     */
    private boolean isMinConstraintViolated(int year, int month, int dayOfMonth) {
        return minDate > 0 && (new LocalDate(year, month, dayOfMonth).isBefore(new LocalDate(minYear, minMonth + 1, minDay)));
    }

    /**
     * Checks to see if there is a maximum value set and whether its value is less than the parameter provided
     *
     * @param year       year of date
     * @param month      month of date start from 1 for January
     * @param dayOfMonth day of month of date
     */
    private boolean isMaxConstraintViolated(int year, int month, int dayOfMonth) {
        return maxDate > 0 && (new LocalDate(year, month, dayOfMonth).isAfter(new LocalDate(maxYear, maxMonth + 1, maxDay)));
    }

    @Override
    public int getYear() {

        return yearPicker.getValue();
    }

    @Override
    public int getMonth() {
        return monthPicker.getValue() - 1;
    }

    @Override
    public int getDayOfMonth() {

        return dayPicker.getValue();
    }

    @Override
    public long getMinDate() {
        return minDate;
    }

    @Override
    public void setMinDate(long minDate) {

        this.minDate = minDate;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(minDate);

        minDay = cal.get(Calendar.DAY_OF_MONTH);
        minMonth = cal.get(Calendar.MONTH);
        minYear = cal.get(Calendar.YEAR);

        resetPickers();
    }

    @Override
    public long getMaxDate() {
        return this.maxDate;
    }

    @Override
    public void setMaxDate(long maxDate) {

        this.maxDate = maxDate;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(maxDate);

        maxDay = cal.get(Calendar.DAY_OF_MONTH);
        maxMonth = cal.get(Calendar.MONTH);
        maxYear = cal.get(Calendar.YEAR);

        resetPickers();

    }

    public void checkConstraintsConstraint() {
        if (minDate != 0 && maxDate != 0 && new Date(minDate).after(new Date(maxDate))) {
            throw new IllegalStateException("Min constrained date is greater than the Max constraint date");
        }
    }

    /**
     * Sets the current date of the spinner to be displayed
     *
     * @param date
     */
    public void setDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * @return current MAX date of the spinner selected and null if invalid date
     */
    private LocalDate getCurrentSelectedDate() {
        try {
            return new LocalDate(getYear(), getMonth() + 1, getDayOfMonth());
        } catch (IllegalArgumentException e) {
            return null;
        }

    }


    /**
     * Checks if the selected date is valid
     */

    private NumericDatePickerValidator validateCurrentSelectedDate() {

        LocalDate selectedDate = getCurrentSelectedDate();

        NumericDatePickerValidator validator;

        if (selectedDate == null) {

            validator = new NumericDatePickerValidator(false, selectedDate, NumericDatePickerValidator.Violation.MALFORMED_DATE);

        } else if (isMaxConstraintViolated(selectedDate.getYear(), selectedDate.getMonthOfYear(), selectedDate.getDayOfMonth())) {


            validator = new NumericDatePickerValidator(false, selectedDate, NumericDatePickerValidator.Violation.MAX_DATE);


        } else if (isMinConstraintViolated(selectedDate.getYear(), selectedDate.getMonthOfYear(), selectedDate.getDayOfMonth())) {


            validator = new NumericDatePickerValidator(false, selectedDate, NumericDatePickerValidator.Violation.MIN_DATE);


        } else {

            validator = new NumericDatePickerValidator(true, selectedDate, null);
        }

        return validator;
    }

    private void resetPicker(NumberPicker numberPicker, int value) {

        //Cache original values
        int cacheMax = numberPicker.getMaxValue();
        int cacheMin = numberPicker.getMinValue();

        //reset picker
        numberPicker.setDisplayedValues(null);

        //set new value
        if (value != 0)
            numberPicker.setValue(value);

        //reinstate values
        if (cacheMin != 0)
            numberPicker.setMinValue(cacheMin);

        if (cacheMax != 0) {
            numberPicker.setMaxValue(cacheMax);
        }


        if (numberPicker.getId() == R.id.year) {

            changedPickerId = R.id.month;  //Who to process if we still have a violation

        } else if (numberPicker.getId() == R.id.month) {

            changedPickerId = R.id.day; //Who to process if we still have a violation
            dayPicker.setMaxValue(getMaxDayForSelectedDate());//Max day changes with change in month
        }

        fixWeirdNumberPickerBug(numberPicker);
    }

    /**
     * Reset Wrap selector if constraints reached
     */
    private boolean resetWrapSelector(NumberPicker numberPicker) {

        boolean isScroll = true;

        if (numberPicker.getId() == R.id.year) {

            isScroll = !(numberPicker.getValue() == maxYear || numberPicker.getValue() == minYear);

        } else if (numberPicker.getId() == R.id.month) {
            isScroll = !(isMinMonthEdge() || isMaxMonthEdge());

        } else if (numberPicker.getId() == R.id.day) {

            isScroll = !(isMinDayEdge() || isMaxDayEdge());

        }

        return isScroll;
    }

    private boolean isMinMonthEdge() {
        return getYear() <= minYear && monthPicker.getValue() == (minMonth + 1);
    }

    private boolean isMaxMonthEdge() {
        return getYear() >= maxYear && monthPicker.getValue() == (maxMonth + 1);
    }

    private boolean isMinDayEdge() {
        return getYear() <= minYear && getMonth() <= minMonth && dayPicker.getValue() == minDay;
    }

    private boolean isMaxDayEdge() {
        return getYear() >= maxYear && getMonth() >= maxMonth && dayPicker.getValue() == maxDay;
    }

    private int getMaxDayForSelectedDate() {
        return NumericDatePickerHelper.getDaysInMonth(getMonth(), NumericDatePickerHelper.isLeapYear(getYear()));
    }

    private int getPreviousDay(int prevDay, int minDay) {
        if (prevDay > 0) {
            return getMonth() + 1 == 2 ? Math.min(prevDay, (NumericDatePickerHelper.isLeapYear(getYear()) ? 29 : 28)) : prevDay;
        } else {
            return minDay;
        }
    }
}
