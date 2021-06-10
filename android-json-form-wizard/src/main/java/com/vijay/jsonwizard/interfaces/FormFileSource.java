package com.vijay.jsonwizard.interfaces;

import android.content.Context;

import androidx.annotation.Nullable;

import org.jeasy.rules.api.Rules;
import org.json.JSONObject;

import java.io.InputStream;

public interface FormFileSource {

    Rules getRulesFromFile(Context context, String fileName) throws Exception;

    /**
     * Reads a file source and returns the JSON object of the content.
     * Any exception apart from IOException & JSONException are propagated
     *
     * @param context
     * @param fileName
     * @return
     */
    @Nullable
    JSONObject getFormFromFile(Context context, String fileName) throws Exception;

    /**
     * gets context aware file InputStream
     * @param context
     * @param fileName
     * @return
     * @throws Exception
     */
    InputStream getFileInputStream(Context context, String fileName) throws Exception;

}
