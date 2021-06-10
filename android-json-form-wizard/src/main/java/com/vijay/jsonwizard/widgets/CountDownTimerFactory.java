package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;

import com.emredavarci.circleprogressbar.CircleProgressBar;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CountDownTimerFactory implements FormWidgetFactory {

    private static CountDownTimer timer;
    private static Ringtone alarmTone;
    private View rootLayout;
    private TextView labelView;
    private CircleProgressBar progressBar;
    private int elapsedCount = 0;
    private int progressBarMaxValue = 100;
    private long millis;
    private long intervalMillis;
    private WidgetArgs widgetArgs;

    public static void stopAlarm() {
        if (timer != null) {
            timer.cancel();
        }
        if (alarmTone != null && alarmTone.isPlaying()) {
            alarmTone.stop();
        }
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        widgetArgs = new WidgetArgs()
                .withStepName(stepName)
                .withJsonObject(jsonObject)
                .withContext(context)
                .withPopup(popup);

        rootLayout = getRootView(context);
        initializeViewConfigs(jsonObject);
        setWidgetTags(jsonObject, stepName);
        formatWidget(jsonObject, context);
        writeStartTimestamp(); // Write timestamp when the countdown started
        startCountDown(millis, intervalMillis, context);
        initSpecialViewsRefs(context, jsonObject, progressBar);

        ((JsonApi) context).addFormDataView(progressBar);

        List<View> views = new ArrayList<>(1);
        views.add(rootLayout);
        return views;
    }

    @VisibleForTesting
    protected View getRootView(Context context) {
        return LayoutInflater.from(context).inflate(getLayout(), null);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return getViewsFromJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    private void setWidgetTags(JSONObject jsonObject, String stepName) {
        JSONArray canvasIds = new JSONArray();
        rootLayout.setId(ViewUtil.generateViewId());

        FormUtils.setViewOpenMRSEntityAttributes(jsonObject, rootLayout);
        FormUtils.setViewOpenMRSEntityAttributes(jsonObject, labelView);
        FormUtils.setViewOpenMRSEntityAttributes(jsonObject, progressBar);
        canvasIds.put(rootLayout.getId());

        progressBar.setTag(R.id.canvas_ids, canvasIds.toString());
        progressBar.setTag(R.id.type, jsonObject.optString(JsonFormConstants.TYPE));
        progressBar.setTag(R.id.address, stepName + ":" + jsonObject.optString(JsonFormConstants.KEY));
        progressBar.setTag(R.id.extraPopup, false);
    }

    private void initializeViewConfigs(JSONObject jsonObject) {
        labelView = rootLayout.findViewById(R.id.timerLabel);
        progressBar = rootLayout.findViewById(R.id.progressCircularBar);

        String label = jsonObject.optString(JsonFormConstants.LABEL, "");

        labelView.setText(label);

        progressBar.setStrokeWidthDimension(20);
        progressBar.setMaxValue(progressBarMaxValue);

        String countdownTimeUnit = jsonObject.optString(JsonFormConstants.COUNTDOWN_TIME_UNIT, JsonFormConstants.DEFAULT_COUNTDOWN_TIME_UNIT);
        String countdownTimeValue = jsonObject.optString(JsonFormConstants.COUNTDOWN_TIME_VALUE, "0");
        String countdownInterval = jsonObject.optString(JsonFormConstants.COUNTDOWN_INTERVAL_SECONDS, "1");
        long time = Long.parseLong(countdownTimeValue);
        long intervalSeconds = Long.parseLong(countdownInterval);
        intervalMillis = intervalSeconds * 1000; // Interval unit of measurement is SECONDS by default. No other accepted
        if (countdownTimeUnit.equals(JsonFormConstants.DEFAULT_COUNTDOWN_TIME_UNIT)) {
            millis = time * 1000;
        } else if (countdownTimeUnit.equals(JsonFormConstants.MINUTES_COUNTDOWN_TIME_UNIT)) {
            millis = time * 60 * 1000;
        }
        if (intervalMillis > millis) {
            intervalMillis = 1000; // Default interval of 1 second if interval specified is greater than countdown time
        }
    }

    private void formatWidget(JSONObject jsonObject, Context context) {

        String labelTextSize = jsonObject.optString(JsonFormConstants.LABEL_TEXT_SIZE, "8sp");
        String labelTextColor = jsonObject.optString(JsonFormConstants.LABEL_TEXT_COLOR, "#535F67");
        String progressBarBackgroundColor = jsonObject.optString(JsonFormConstants.PROGRESSBAR_BACKGROUND_COLOR, "#B6BBBE");
        String progressBarColor = jsonObject.optString(JsonFormConstants.PROGRESSBAR_COLOR, "#535F67");
        String progressBarTextColor = jsonObject.optString(JsonFormConstants.PROGRESSBAR_TEXT_COLOR, "#535F67");

        labelView.setTextSize(FormUtils.getValueFromSpOrDpOrPx(labelTextSize, context));
        labelView.setTextColor(Color.parseColor(labelTextColor));

        progressBar.setBackgroundColor(progressBarBackgroundColor);
        progressBar.setProgressColor(progressBarColor);
        progressBar.setTextColor(progressBarTextColor);
    }

    private void initSpecialViewsRefs(Context context, JSONObject jsonObject, CircleProgressBar circleProgressBar) {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);

        if (StringUtils.isNotBlank(relevance) && context instanceof JsonApi) {
            circleProgressBar.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(circleProgressBar);
        }

        if (StringUtils.isNotBlank(constraints) && context instanceof JsonApi) {
            circleProgressBar.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(circleProgressBar);
        }

        if (StringUtils.isNotBlank(calculation) && context instanceof JsonApi) {
            circleProgressBar.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(circleProgressBar);
        }
    }

    private int getLayout() {
        return R.layout.native_form_countdown_timer;
    }

    private void writeStartTimestamp() {
        JsonApi jsonApi = (JsonApi) widgetArgs.getContext();
        String stepName = widgetArgs.getStepName();
        String key = (String) rootLayout.getTag(R.id.key);
        String openMrsEntityParent = (String) rootLayout.getTag(R.id.openmrs_entity_parent);
        String openMrsEntity = (String) rootLayout.getTag(R.id.openmrs_entity);
        String openMrsEntityId = (String) rootLayout.getTag(R.id.openmrs_entity_id);
        boolean extraPopup = widgetArgs.isPopup();
        String timeStampString = String.valueOf(System.currentTimeMillis());
        try {
            jsonApi.writeValue(stepName, key, timeStampString, openMrsEntityParent, openMrsEntity, openMrsEntityId, extraPopup);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    /**
     * Count down to the end of the time specified
     *
     * @param millis            The time in milliseconds to count down to
     * @param countdownInterval The intervals for running the countdown
     */
    private void startCountDown(final long millis, long countdownInterval, final Context context) {
        timer = new CountDownTimer(millis, countdownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                elapsedCount++;
                progressBar.setText(getFormattedTimeText(millisUntilFinished));
                int progress = (int) (elapsedCount * 100 / (millis / 1000));
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                elapsedCount = 0;
                progressBar.setText(getFormattedTimeText(0));
                progressBar.setProgress(progressBarMaxValue);
                onCountdownFinish(context);
            }
        };
        timer.start();
    }

    public void ringAlarm(Context context) {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null)
            alarmTone = RingtoneManager.getRingtone(context, Settings.System.DEFAULT_RINGTONE_URI);
        else alarmTone = RingtoneManager.getRingtone(context, alert);
        if (!alarmTone.isPlaying()) {
            alarmTone.play();
        }
    }

    private String getFormattedTimeText(long timeValue) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeValue), TimeUnit.MILLISECONDS.toSeconds(timeValue) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeValue)));
    }

    /**
     * Override this to provide more on countdown complete post processing
     */
    @VisibleForTesting
    protected void onCountdownFinish(Context context) {
        // Countdown complete post processing
        ringAlarm(context);
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}
