package com.vijay.jsonwizard.interfaces;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.fragments.JsonFormFragment;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * Created by vijay on 24-05-2015.
 */
public interface FormWidgetFactory {

    List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception;

    List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception;

    @NonNull
    Set<String> getCustomTranslatableWidgetFields();
}
