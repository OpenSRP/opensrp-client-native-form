package com.vijay.jsonwizard.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageViewFactory implements FormWidgetFactory {

    private View rootLayout;
    private TextView descriptionTextView;

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        rootLayout = getRootLayout(context);
        descriptionTextView = rootLayout.findViewById(R.id.imageViewLabel);
        WidgetArgs widgetArgs = new WidgetArgs()
                .withJsonObject(jsonObject)
                .withContext(context)
                .withFormFragment(formFragment)
                .withListener(listener)
                .withPopup(popup)
                .withStepName(stepName);

        setWidgetTags(widgetArgs);
        setViewConfigs(widgetArgs);

        List<View> views = new ArrayList<>(1);
        views.add(rootLayout);
        return views;
    }

    @VisibleForTesting
    protected View getRootLayout(Context context) {
        return LayoutInflater.from(context).inflate(getLayout(), null);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return getViewsFromJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    private int getLayout() {
        return R.layout.native_form_image_view;
    }

    private void setWidgetTags(WidgetArgs widgetArgs) {
        JSONObject jsonObject = widgetArgs.getJsonObject();
        String stepName = widgetArgs.getStepName();

        FormUtils.setViewOpenMRSEntityAttributes(jsonObject, rootLayout);
        FormUtils.setViewOpenMRSEntityAttributes(jsonObject, descriptionTextView);

        JSONArray canvasIds = new JSONArray();
        rootLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(rootLayout.getId());
        rootLayout.setTag(R.id.canvas_ids, canvasIds);
        rootLayout.setTag(R.id.type, jsonObject.optString(JsonFormConstants.TYPE));
        rootLayout.setTag(R.id.address, stepName + ":" + jsonObject.optString(JsonFormConstants.KEY));
        rootLayout.setTag(R.id.extraPopup, false);
    }


    private void setViewConfigs(WidgetArgs widgetArgs) {
        JSONObject jsonObject = widgetArgs.getJsonObject();
        Context context = widgetArgs.getContext();

        String descriptionText = jsonObject.optString(JsonFormConstants.TEXT, "");

        if (!TextUtils.isEmpty(descriptionText)) {
            descriptionTextView.setText(Html.fromHtml(descriptionText));
            String textColor = jsonObject.optString(JsonFormConstants.TEXT_COLOR, "#000000");
            descriptionTextView.setTextColor(Color.parseColor(textColor));
            String textSize = jsonObject.optString(JsonFormConstants.TEXT_SIZE, String.valueOf(context.getResources().getDimension(R.dimen.label_text_size)));
            descriptionTextView.setTextSize(FormUtils.getValueFromSpOrDpOrPx(textSize, context));
        }

        Bitmap bitmap = getBitmap(widgetArgs);
        if (bitmap != null) {
            ImageView imageView = rootLayout.findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
        }
    }

    protected Bitmap getBitmap(WidgetArgs widgetArgs) {
        JSONObject jsonObject = widgetArgs.getJsonObject();
        return getBitmap(widgetArgs.getContext(), jsonObject.optString(JsonFormConstants.IMAGE_FILE, ""),
                jsonObject.optString(JsonFormConstants.IMAGE_FOLDER, ""));
    }

    @VisibleForTesting
    protected Bitmap getBitmap(Context context, String imageFile, String folderName) {
        return TextUtils.isEmpty(imageFile) ? null : FormUtils.getBitmap(context, folderName, imageFile);
    }

    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return new HashSet<>();
    }
}
