package com.vijay.jsonwizard.rules;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.Utils;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ndegwamartin on 17/12/2018.
 */
public class RulesEngineDateUtil {

    public long getDifferenceDays(String dateString) {
        Date date = Utils.getDateFromString(dateString);

        if (date != null) {
            long msDiff = getTimeInMillis() - date.getTime();
            return Math.abs(TimeUnit.MILLISECONDS.toDays(msDiff));
        } else {
            return 0;
        }
    }

    public long getDifferenceDays(String dateString, String dateString2) {
        Date date = Utils.getDateFromString(dateString);
        Date date2 = Utils.getDateFromString(dateString2);

        if (date != null && date2 != null) {
            long msDiff = date.getTime() - date2.getTime();
            return Math.abs(TimeUnit.MILLISECONDS.toDays(msDiff));
        } else {
            return 0;
        }
    }

    public String getDOBFromAge(Integer age) {
        return (new LocalDate()).withMonthOfYear(1).withDayOfMonth(1).minusYears(age)
                .toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN);
    }

    public String getDateToday() {
        return (new LocalDate()).toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN);
    }

    public String getDateTimeToday() {
        String dateTimeFormatPattern = "yyyy-MM-dd HH:mm:ss";
        return (new LocalDateTime()).toString(dateTimeFormatPattern);
    }

    /**
     * Returns a formatted age string from startdate to provided end date
     *
     * @param date
     * @param endDate
     * @return String
     */
    public String getDuration(String date, String endDate) {
        return Utils.getDuration(Utils.getDateFormattedForCalculation(date,  Form.getDatePickerDisplayFormat()),
                endDate);
    }

    public String getWeeksAndDaysFromDays(Integer days) {
        double weeks = Math.round(Math.floor(days / 7));
        Integer dayz = days % 7;

        return String.format("%.0f weeks %d days", weeks, dayz);
    }

    public String formatDate(String dateString, String duration) {
        LocalDate date = new LocalDate(Utils.reverseDateString(Utils.getDateFormattedForCalculation(dateString,  Form.getDatePickerDisplayFormat()), "-"));
        int result = 0;
        String cleanDuration = duration.trim().toLowerCase();

        if (cleanDuration.length() == 1) {
            switch (cleanDuration) {
                case "d":
                    result = Days.daysBetween(date, LocalDate.now()).getDays();
                    break;
                case "w":
                    result = Weeks.weeksBetween(date, LocalDate.now()).getWeeks();
                    break;
                case "m":
                    result = Months.monthsBetween(date, LocalDate.now()).getMonths();
                    break;
                case "y":
                    result = Years.yearsBetween(date, LocalDate.now()).getYears();
                    break;
                default:
                    break;
            }
        }

        return "wd".equals(cleanDuration) ? getDuration(dateString).replace("w", " weeks").replace("d", " days") : String.valueOf(Math.abs(result));
    }

    /**
     * Returns a formatted age string from given date till today
     *
     * @param date
     * @return String date
     */
    public String getDuration(String date) {
        return Utils.getDuration(date);
    }

    /**
     * @param durationString
     * @return String with date
     */
    public String addDuration(String durationString) {
        return addDuration((new LocalDate()).toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN), durationString);
    }

    /**
     * @param dateString
     * @param durationString
     * @return String with date
     */
    public String addDuration(String dateString, String durationString) {
        LocalDate date = new LocalDate(Utils.reverseDateString(Utils.getDateFormattedForCalculation(dateString,  Form.getDatePickerDisplayFormat()), "-"));
        String[] durationArr = getDurationArray(durationString);

        for (String duration : durationArr) {
            char suffix = duration.charAt(duration.length() - 1);
            switch (suffix) {
                case 'd':
                    date = date.plusDays(getDurationValue(duration));
                    break;
                case 'w':
                    date = date.plusWeeks(getDurationValue(duration));
                    break;
                case 'm':
                    date = date.plusMonths(getDurationValue(duration));
                    break;
                case 'y':
                    date = date.plusYears(getDurationValue(duration));
                    break;
                default:
                    break;

            }
        }

        return date.toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN);
    }

    @NonNull
    private String[] getDurationArray(String durationString) {
        String cleanDurationString = durationString.trim().toLowerCase();

        return cleanDurationString.contains("-") ? cleanDurationString.trim().split("-") : new String[]{cleanDurationString};
    }

    private Integer getDurationValue(String s) {
        return Integer.valueOf(s.substring(0, s.length() - 1));
    }

    /**
     * @param durationString
     * @return String with date
     */
    public String subtractDuration(String durationString) {
        return subtractDuration((new LocalDate()).toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN), durationString);
    }

    /**
     * @param dateString
     * @param durationString
     * @return String with date
     */
    public String subtractDuration(String dateString, String durationString) {

        LocalDate date = new LocalDate(Utils.reverseDateString(Utils.getDateFormattedForCalculation(dateString, Form.getDatePickerDisplayFormat()), "-"));

        String[] durationArr = getDurationArray(durationString);

        for (String duration : durationArr) {

            char suffix = duration.charAt(duration.length() - 1);
            switch (suffix) {
                case 'd':
                    date = date.minusDays(getDurationValue(duration));
                    break;
                case 'w':
                    date = date.minusWeeks(getDurationValue(duration));
                    break;
                case 'm':
                    date = date.minusMonths(getDurationValue(duration));
                    break;
                case 'y':
                    date = date.minusYears(getDurationValue(duration));
                    break;
                default:
                    break;
            }
        }

        return date.toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN);
    }

    public String minDate(String minimumDate) {

        if (!TextUtils.isEmpty(minimumDate)) {
            Calendar minDate = FormUtils.getDate(minimumDate);
            minDate.set(Calendar.HOUR_OF_DAY, 0);
            minDate.set(Calendar.MINUTE, 0);
            minDate.set(Calendar.SECOND, 0);
            minDate.set(Calendar.MILLISECOND, 0);
            return Utils.getStringFromDate(minDate.getTime());
        } else {
            return "";
        }
    }

    public String maxDate(String maximumDate) {

        if (!TextUtils.isEmpty(maximumDate)) {
            Calendar maxDate = FormUtils.getDate(maximumDate);
            maxDate.set(Calendar.HOUR_OF_DAY, 23);
            maxDate.set(Calendar.MINUTE, 59);
            maxDate.set(Calendar.SECOND, 59);
            maxDate.set(Calendar.MILLISECOND, 999);
            return Utils.getStringFromDate(maxDate.getTime());
        } else {
            return "";
        }
    }

    public String getSecondaryValue(String value) {

        if (value.contains(":")) {
            String[] valArray = value.split(":");
            return valArray[1];
        } else {
            return value;
        }

    }

    public long getTimeInMillis() {
        return System.currentTimeMillis();
    }

}
