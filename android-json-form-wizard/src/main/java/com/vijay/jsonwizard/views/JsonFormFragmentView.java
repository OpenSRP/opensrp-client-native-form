package com.vijay.jsonwizard.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.mvp.MvpView;
import com.vijay.jsonwizard.mvp.ViewState;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by vijay on 5/14/15.
 */
public interface JsonFormFragmentView<V extends ViewState> extends MvpView {
    Bundle getArguments();

    void setActionBarTitle(String title);

    Context getContext();

    void showToast(String message);

    void showSnackBar(String message);

    CommonListener getCommonListener();

    void addFormElements(List<View> views);

    ActionBar getSupportActionBar();

    Toolbar getToolbar();

    void setToolbarTitleColor(int white);

    void updateVisibilityOfNextAndSave(boolean next, boolean save);

    void hideKeyBoard();

    void transactThis(JsonFormFragment next);

    void startActivityForResult(Intent intent, int requestCode);

    void updateRelevantImageView(Bitmap bitmap, String imagePath, String currentKey);

    void writeValue(String stepName, String key, String value, String openMrsEntityParent,
                    String openMrsEntity, String openMrsEntityId, boolean popup);

    void writeValue(String stepName, String prentKey, String childObjectKey, String childKey,
                    String value, String openMrsEntityParent, String openMrsEntity,
                    String openMrsEntityId, boolean popup);

    void writeMetaDataValue(String metaDataKey, Map<String, String> values);

    JSONObject getStep(String stepName);

    String getCurrentJsonState();

    void finishWithResult(Intent returnIntent);

    void setUpBackButton();

    void backClick();

    void unCheckAllExcept(String parentKey, String childKey, CompoundButton compoundButton);

    void unCheck(String parentKey, String exclusiveKey, CompoundButton compoundButton);

    String getCount();

    boolean displayScrollBars();

    boolean skipBlankSteps();

    void onFormStart();

    void onFormFinish();

    void scrollToView(View view);

    void startSimprintsRegistration(String projectId, String userId, String moduleId);

    void startSimprintsVerification(String projectId, String userId, String moduleId, String guId);
}
