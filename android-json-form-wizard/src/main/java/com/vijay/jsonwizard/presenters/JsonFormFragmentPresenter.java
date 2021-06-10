package com.vijay.jsonwizard.presenters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.adapter.DynamicLabelAdapter;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.customviews.NativeEditText;
import com.vijay.jsonwizard.customviews.RadioButton;
import com.vijay.jsonwizard.fragments.JsonFormErrorFragment;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.model.DynamicLabelInfo;
import com.vijay.jsonwizard.mvp.MvpBasePresenter;
import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.task.ExpansionPanelGenericPopupDialogTask;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ImageUtils;
import com.vijay.jsonwizard.utils.PermissionUtils;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;
import com.vijay.jsonwizard.widgets.CheckBoxFactory;
import com.vijay.jsonwizard.widgets.CountDownTimerFactory;
import com.vijay.jsonwizard.widgets.EditTextFactory;
import com.vijay.jsonwizard.widgets.GpsFactory;
import com.vijay.jsonwizard.widgets.ImagePickerFactory;
import com.vijay.jsonwizard.widgets.MultiSelectListFactory;
import com.vijay.jsonwizard.widgets.NativeEditTextFactory;
import com.vijay.jsonwizard.widgets.NativeRadioButtonFactory;
import com.vijay.jsonwizard.widgets.NumberSelectorFactory;
import com.vijay.jsonwizard.widgets.SpinnerFactory;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import timber.log.Timber;

import static com.vijay.jsonwizard.utils.FormUtils.dpToPixels;
import static com.vijay.jsonwizard.utils.FormUtils.getDynamicLabelInfoList;

/**
 * Created by vijay on 5/14/15.
 */
public class JsonFormFragmentPresenter extends
        MvpBasePresenter<JsonFormFragmentView<JsonFormFragmentViewState>> {

    protected static final int RESULT_LOAD_IMG = 1;
    private static final String TAG = "FormFragmentPresenter";
    private final JsonFormFragment formFragment;
    protected JSONObject mStepDetails;
    protected String key;
    protected String type;
    private String mStepName;
    private String mCurrentKey;
    private String mCurrentPhotoPath;
    private JsonFormInteractor mJsonFormInteractor;
    private Map<String, ValidationStatus> invalidFields;
    private Stack<String> incorrectlyFormattedFields;
    private JsonFormErrorFragment errorFragment;
    private FormUtils formUtils = new FormUtils();
    private boolean cleanupAndExit;

    public JsonFormFragmentPresenter(JsonFormFragment formFragment,
                                     JsonFormInteractor jsonFormInteractor) {
        this(formFragment);
        mJsonFormInteractor = jsonFormInteractor;
    }

    public JsonFormFragmentPresenter(JsonFormFragment formFragment) {
        this.formFragment = formFragment;
        mJsonFormInteractor = JsonFormInteractor.getInstance();
        invalidFields = this.formFragment.getJsonApi().getInvalidFields();
        incorrectlyFormattedFields = new Stack<>();
    }

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView, View childAt,
                                            boolean requestFocus) {
        if (childAt instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) childAt;
            ValidationStatus validationStatus = NativeRadioButtonFactory
                    .validate(formFragmentView, radioGroup);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof NativeEditText) {
            NativeEditText editText = (NativeEditText) childAt;
            ValidationStatus validationStatus = NativeEditTextFactory
                    .validate(formFragmentView, editText);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof MaterialEditText) {
            MaterialEditText editText = (MaterialEditText) childAt;
            ValidationStatus validationStatus = EditTextFactory.validate(formFragmentView, editText);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof ImageView) {
            ValidationStatus validationStatus = ImagePickerFactory
                    .validate(formFragmentView, (ImageView) childAt);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof Button) {
            String type = (String) childAt.getTag(R.id.type);
            if (!TextUtils.isEmpty(type) && type.equals(JsonFormConstants.GPS)) {
                ValidationStatus validationStatus = GpsFactory.validate(formFragmentView, (Button) childAt);
                if (!validationStatus.isValid()) {
                    if (requestFocus) {
                        validationStatus.requestAttention();
                    }
                    return validationStatus;
                }
            }
        } else if (childAt instanceof MaterialSpinner) {
            final MaterialSpinner spinner = (MaterialSpinner) childAt;
            final ValidationStatus validationStatus = SpinnerFactory.validate(formFragmentView, spinner);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                ((JsonFormActivity) formFragmentView.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSpinnerError(spinner, validationStatus.getErrorMessage());
                    }
                });
                return validationStatus;
            }
        } else if (childAt instanceof ViewGroup
                && childAt.getTag(R.id.is_checkbox_linear_layout) != null &&
                Boolean.TRUE.equals(childAt.getTag(R.id.is_checkbox_linear_layout))) {
            LinearLayout checkboxLinearLayout = (LinearLayout) childAt;
            ValidationStatus validationStatus = CheckBoxFactory
                    .validate(formFragmentView, checkboxLinearLayout);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }

        } else if (childAt instanceof ViewGroup
                && childAt.getTag(R.id.is_number_selector_linear_layout) != null &&
                Boolean.TRUE.equals(childAt.getTag(R.id.is_number_selector_linear_layout))) {
            ValidationStatus validationStatus = NumberSelectorFactory
                    .validate(formFragmentView, (ViewGroup) childAt);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        } else if (childAt instanceof RelativeLayout
                && childAt.getTag(R.id.is_multiselect_relative_layout) != null &&
                Boolean.TRUE.equals(childAt.getTag(R.id.is_multiselect_relative_layout))) {
            ValidationStatus validationStatus = MultiSelectListFactory
                    .validate(formFragmentView, (RelativeLayout) childAt);
            if (!validationStatus.isValid()) {
                if (requestFocus) {
                    validationStatus.requestAttention();
                }
                return validationStatus;
            }
        }

        return new ValidationStatus(true, null, null, null);
    }

    private static void setSpinnerError(MaterialSpinner spinner, String spinnerError) {
        try {
            spinner.setError(spinnerError);
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }
    }

    public JsonFormFragment getFormFragment() {
        return formFragment;
    }

    public JsonFormInteractor getmJsonFormInteractor() {
        return mJsonFormInteractor;
    }

    public void addFormElements() {
        final ProgressDialog dialog = new ProgressDialog(formFragment.getContext());
        dialog.setCancelable(false);
        dialog.setTitle(formFragment.getContext().getString(com.vijay.jsonwizard.R.string.loading));
        dialog.setMessage(formFragment.getContext().getString(com.vijay.jsonwizard.R.string.loading_form_message));
        dialog.show();
        mStepName = getView().getArguments().getString("stepName");
        JSONObject step = getView().getStep(mStepName);
        if (step == null) {
            return;
        }
        try {
            mStepDetails = new JSONObject(step.toString());
            formFragment.getJsonApi().setNextStep(mStepName);
        } catch (JSONException e) {
            Timber.e(e);
        }
        formFragment.getJsonApi().getAppExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //fragment has been detached when skipping steps
                if (getView() == null || cleanupAndExit) {
                    dismissDialog(dialog);
                    return;
                }
                final List<View> views = mJsonFormInteractor
                        .fetchFormElements(mStepName, formFragment, mStepDetails, getView().getCommonListener(),
                                false);
                if (cleanupAndExit) {
                    dismissDialog(dialog);
                    return;
                }
                formFragment.getJsonApi().initializeDependencyMaps();

                formFragment.getJsonApi().getAppExecutors().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog(dialog);
                        if (getView() != null && !cleanupAndExit) {
                            getView().addFormElements(views);
                            formFragment.getJsonApi().invokeRefreshLogic(null, false, null, null, mStepName, false);
                            if (formFragment.getJsonApi().skipBlankSteps()) {
                                Utils.checkIfStepHasNoSkipLogic(formFragment);
                                if (mStepName.equals(JsonFormConstants.STEP1) && !formFragment.getJsonApi().isPreviousPressed()) {
                                    formFragment.getJsonApi().getAppExecutors().diskIO().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            formFragment.skipLoadedStepsOnNextPressed();
                                        }
                                    });
                                }
                            }
                            String next = mStepDetails.optString(JsonFormConstants.NEXT);
                            formFragment.getJsonApi().setNextStep(next);
                        }
                    }
                });
            }
        });
    }

    private void dismissDialog(ProgressDialog dialog) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

    }

    @SuppressLint("ResourceAsColor")
    public void setUpToolBar() {
        getView().setActionBarTitle(mStepDetails.optString(JsonFormConstants.STEP_TITLE));
        getView().setToolbarTitleColor(R.color.white);
        if (mStepDetails.has("bottom_navigation")) {
            getView().updateVisibilityOfNextAndSave(false, false);
            return;
        }
        if (!mStepName.equals(JsonFormConstants.FIRST_STEP_NAME)) {
            getView().setUpBackButton();
        }

        if (mStepDetails.has("next")) {
            getView().updateVisibilityOfNextAndSave(true, false);
        } else {
            getView().updateVisibilityOfNextAndSave(false, true);
        }
    }

    public void onBackClick() {
        getView().hideKeyBoard();
        getView().backClick();
    }

    public Stack<String> getIncorrectlyFormattedFields() {
        return incorrectlyFormattedFields;
    }

    public JsonFormErrorFragment getErrorFragment() {
        return errorFragment;
    }

    public void setErrorFragment(JsonFormErrorFragment errorFragment) {
        this.errorFragment = errorFragment;
    }

    public boolean onNextClick(LinearLayout mainView) {
        validateAndWriteValues();
        checkAndStopCountdownAlarm();
        boolean validateOnSubmit = validateOnSubmit();
        if (validateOnSubmit && getIncorrectlyFormattedFields().isEmpty()) {
            boolean isSkipped = executeRefreshLogicForNextStep();
            return !isSkipped && moveToNextStep();
        } else if (isFormValid()) {
            boolean isSkipped = executeRefreshLogicForNextStep();
            return !isSkipped && moveToNextStep();
        } else {
            getView().showSnackBar(
                    getView().getContext().getResources().getString(R.string.json_form_on_next_error_msg));
        }
        return false;
    }

    public void validateAndWriteValues() {
        for (View childView : formFragment.getJsonApi().getFormDataViews()) {
            ValidationStatus validationStatus = validateView(childView);
            String key = (String) childView.getTag(R.id.key);
            String address = (String) childView.getTag(R.id.address);
            String openMrsEntityParent = (String) childView.getTag(R.id.openmrs_entity_parent);
            String openMrsEntity = (String) childView.getTag(R.id.openmrs_entity);
            String openMrsEntityId = (String) childView.getTag(R.id.openmrs_entity_id);
            Boolean popup = (Boolean) childView.getTag(R.id.extraPopup);
            String fieldKey = Utils.getFieldKeyPrefix(mStepName, getStepTitle()) + key;

            if (StringUtils.isNotBlank(address) && mStepName.equals(address.split(":")[0])) {

                if (childView instanceof MaterialEditText) {
                    MaterialEditText editText = (MaterialEditText) childView;
                    if (editText.getParent() != null && ((ViewGroup) editText.getParent()).isShown()) {

                        String rawValue = (String) editText.getTag(R.id.raw_value);
                        if (rawValue == null) {
                            rawValue = editText.getText().toString();
                        }

                        handleWrongFormatInputs(validationStatus, fieldKey, rawValue);

                        String type = (String) childView.getTag(R.id.type);
                        rawValue = JsonFormConstants.DATE_PICKER.equals(type) || JsonFormConstants.TIME_PICKER.equals(type) ? childView.getTag(R.id.locale_independent_value).toString() : rawValue;
                        getView().writeValue(mStepName, key, rawValue, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);

                        //for repeating grp referenceEditText validation
                        if (editText.getId() == R.id.reference_edit_text && editText.getTag(R.id.has_required_validator) != null && validationStatus.isValid()) {
                            View doneButton = ((ViewGroup) editText.getParent()).findViewById(R.id.btn_repeating_group_done);
                            Object o = doneButton.getTag(R.id.is_repeating_group_generated);
                            if (o == null) {
                                validationStatus.setIsValid(false);
                                editText.setError(getFormFragment().getString(R.string.repeating_group_not_generated_error_message));
                                validationStatus.setErrorMessage(getFormFragment().getString(R.string.repeating_group_not_generated_error_message));
                            }
                        }
                    } else {
                        validationStatus.setIsValid(true);
                    }
                } else if (childView instanceof NativeEditText) {
                    NativeEditText editText = (NativeEditText) childView;

                    String rawValue = (String) editText.getTag(R.id.raw_value);
                    if (rawValue == null) {
                        rawValue = editText.getText().toString();
                    }

                    handleWrongFormatInputs(validationStatus, fieldKey, rawValue);

                    getView().writeValue(mStepName, key, rawValue, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                } else if (childView instanceof ImageView) {
                    Object path = childView.getTag(R.id.imagePath);
                    if (path instanceof String) {
                        getView().writeValue(mStepName, key, (String) path, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                    }
                } else if (childView instanceof CheckBox) {
                    String parentKey = (String) childView.getTag(R.id.key);
                    String childKey = (String) childView.getTag(R.id.childKey);
                    getView().writeValue(mStepName, parentKey, JsonFormConstants.OPTIONS_FIELD_NAME, childKey, String.valueOf(((CheckBox) childView).isChecked()), openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                } else if (childView instanceof RadioButton) {
                    String parentKey = (String) childView.getTag(R.id.key);
                    String childKey = (String) childView.getTag(R.id.childKey);
                    if (((RadioButton) childView).isChecked()) {
                        getView().writeValue(mStepName, parentKey, childKey, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                    }
                } else if (childView instanceof Button) {
                    Button button = (Button) childView;
                    String rawValue = (String) button.getTag(R.id.raw_value);
                    getView().writeValue(mStepName, key, rawValue, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
                }

                if (!validationStatus.isValid()) {
                    invalidFields.put(fieldKey, validationStatus);
                } else {
                    if (invalidFields.size() > 0) {
                        invalidFields.remove(fieldKey);
                    }
                }
            }
        }

        //remove invalid fields not belonging to current step since formdata view are cleared when view is created
        if (invalidFields != null && !invalidFields.isEmpty()) {
            for (Map.Entry<String, ValidationStatus> entry : invalidFields.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.isNotBlank(key) && !key.startsWith(mStepName)) {
                    invalidFields.remove(key);
                }
            }
        }
        formFragment.getOnFieldsInvalidCallback().passInvalidFields(invalidFields);
    }

    /**
     * Check if alarm is ringing and stop it if so
     */
    public void checkAndStopCountdownAlarm() {
        try {
            JSONObject formJSONObject = new JSONObject(formFragment.getCurrentJsonState());
            JSONArray fields = FormUtils.fields(formJSONObject, mStepName);
            for (int i = 0; i < fields.length(); i++) {
                JSONObject fieldObject = (JSONObject) fields.get(i);
                if (fieldObject.has(JsonFormConstants.COUNTDOWN_TIME_VALUE)) {
                    CountDownTimerFactory.stopAlarm();
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Countdown alarm could not be stopped!");
        }
    }

    public boolean validateOnSubmit() {
        JSONObject entireJsonForm = formFragment.getJsonApi().getmJSONObject();
        return entireJsonForm.optBoolean(JsonFormConstants.VALIDATE_ON_SUBMIT, false);
    }

    public boolean executeRefreshLogicForNextStep() {
        boolean isSkipped = false;
        final String nextStep = getFormFragment().getJsonApi().nextStep();
        if (StringUtils.isNotBlank(nextStep)) {
            getmJsonFormInteractor().fetchFormElements(nextStep, getFormFragment(), getFormFragment().getJsonApi().getmJSONObject().optJSONObject(nextStep), getView().getCommonListener(), false);
            getFormFragment().getJsonApi().initializeDependencyMaps();
            cleanDataForNextStep();
            getFormFragment().getJsonApi().invokeRefreshLogic(null, false, null, null, nextStep, true);
            if (!getFormFragment().getJsonApi().isNextStepRelevant()) {
                Utils.checkIfStepHasNoSkipLogic(getFormFragment());
            }
            isSkipped = getFormFragment().skipStepsOnNextPressed(nextStep);
        }
        return isSkipped;
    }

    private void cleanDataForNextStep() {
        getFormFragment().getJsonApi().setNextStepRelevant(false);
    }

    protected boolean moveToNextStep() {
        final String nextStep = getFormFragment().getJsonApi().nextStep();
        if (StringUtils.isNotBlank(nextStep)) {
            JsonFormFragment next = getNextJsonFormFragment(nextStep);
            getView().hideKeyBoard();
            getView().transactThis(next);
            return true;
        }
        return false;
    }

    protected JsonFormFragment getNextJsonFormFragment(String nextStep) {
        return JsonFormFragment.getFormFragment(nextStep);
    }

    /**
     * Check if form is valid
     *
     * @return true if invalidFields is empty otherwise false
     */
    public boolean isFormValid() {
        return getInvalidFields().size() == 0;
    }

    /**
     * Validates the passed view
     *
     * @param childAt view to be validated
     * @return ValidationStatus for the view
     */
    private ValidationStatus validateView(View childAt) {
        return validate(getView(), childAt, true);
    }

    public String getStepTitle() {
        return mStepDetails.optString(JsonFormConstants.STEP_TITLE);
    }

    private void handleWrongFormatInputs(ValidationStatus validationStatus, String fieldKey,
                                         String rawValue) {
        if (!TextUtils.isEmpty(rawValue) && !validationStatus.isValid()) {
            if (!incorrectlyFormattedFields.contains(fieldKey)) {
                incorrectlyFormattedFields.push(fieldKey);
            }
        } else if (!TextUtils.isEmpty(rawValue) && validationStatus.isValid()) {
            incorrectlyFormattedFields.remove(fieldKey);
        } else if ((TextUtils.isEmpty(rawValue) && !validationStatus.isValid())) {
            incorrectlyFormattedFields.remove(fieldKey);
        }
    }

    public Map<String, ValidationStatus> getInvalidFields() {
        return invalidFields;
    }

    public void onSaveClick(LinearLayout mainView) {
        validateAndWriteValues();
        checkAndStopCountdownAlarm();
        boolean isFormValid = isFormValid();
        if (isFormValid || Boolean.valueOf(mainView.getTag(R.id.skip_validation).toString())) {
            Utils.removeGeneratedDynamicRules(formFragment);
            Intent returnIntent = new Intent();
            getView().onFormFinish();
            returnIntent.putExtra("json", formUtils.addFormDetails(getView().getCurrentJsonState()));
            returnIntent.putExtra(JsonFormConstants.SKIP_VALIDATION,
                    Boolean.valueOf(mainView.getTag(R.id.skip_validation).toString()));
            getView().finishWithResult(returnIntent);
        } else {
            if (showErrorsOnSubmit()) {
                launchErrorDialog();
                getView().showToast(getView().getContext().getResources()
                        .getString(R.string.json_form_error_msg, getInvalidFields().size()));
            } else {
                getView().showSnackBar(getView().getContext().getResources()
                        .getString(R.string.json_form_error_msg, getInvalidFields().size()));

            }
        }
    }

    public boolean showErrorsOnSubmit() {
        JSONObject entireJsonForm = formFragment.getJsonApi().getmJSONObject();
        return entireJsonForm.optBoolean(JsonFormConstants.SHOW_ERRORS_ON_SUBMIT, false);
    }

    protected void launchErrorDialog() {
        if (errorFragment == null) {
            errorFragment = new JsonFormErrorFragment();
        }
        FragmentManager fm = ((JsonFormFragment) getView()).getChildFragmentManager();
        @SuppressLint("CommitTransaction") FragmentTransaction ft = fm.beginTransaction();
        errorFragment.show(ft, JsonFormErrorFragment.TAG);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            String imagePath = mCurrentPhotoPath;
            getView().updateRelevantImageView(ImageUtils
                    .loadBitmapFromFile(getView().getContext(), imagePath,
                            ImageUtils.getDeviceWidth(getView().getContext()),
                            dpToPixels(getView().getContext(), 200)), imagePath, mCurrentKey);
            //cursor.close();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");

        if (grantResults.length == 0) {
            return;
        }

        switch (requestCode) {
            case PermissionUtils.CAMERA_PERMISSION_REQUEST_CODE:
                if (PermissionUtils
                        .verifyPermissionGranted(permissions, grantResults, Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    dispatchTakePictureIntent(key, type);
                }
                break;

            case PermissionUtils.PHONE_STATE_PERMISSION_REQUEST_CODE:
                //TODO Find out functionality which uses Read Phone State permission
                break;
            default:
                break;

        }
    }

    private void dispatchTakePictureIntent(String key, String type) {
        if (PermissionUtils.isPermissionGranted(formFragment,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermissionUtils.CAMERA_PERMISSION_REQUEST_CODE)) {

            if (JsonFormConstants.CHOOSE_IMAGE.equals(type)) {
                getView().hideKeyBoard();
                mCurrentKey = key;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getView().getContext().getPackageManager()) != null) {
                    File imageFile = null;
                    try {
                        imageFile = createImageFile();
                    } catch (IOException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }

                    if (imageFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getView().getContext(),
                                getView().getContext().getPackageName() + "" + ".fileprovider", imageFile);

                        // Grant permission to the default camera app
                        PackageManager packageManager = getView().getContext().getPackageManager();
                        Context applicationContext = getView().getContext().getApplicationContext();

                        applicationContext
                                .grantUriPermission(
                                        takePictureIntent.resolveActivity(packageManager).getPackageName(),
                                        photoURI,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        applicationContext.grantUriPermission("com.vijay.jsonwizard", photoURI,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        getView().startActivityForResult(takePictureIntent, RESULT_LOAD_IMG);
                    }
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getView().getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onClick(View v) {
        key = (String) v.getTag(R.id.key);
        type = (String) v.getTag(R.id.type);
        switch (type) {
            case JsonFormConstants.FINGER_PRINT:
                openSimprints(v);
                break;
            case JsonFormConstants.CHOOSE_IMAGE:
                dispatchTakePictureIntent(key, type);
                break;
            case JsonFormConstants.NATIVE_RADIO_BUTTON:
                nativeRadioButtonClickActions(v);
                break;
            case JsonFormConstants.CHECK_BOX:
                if (v.getId() == R.id.label_edit_button) {
                    setCheckboxesEditable(v);
                } else {
                    showInformationDialog(v);
                }
                break;
            case JsonFormConstants.LABEL:
                showInformationDialog(v);
                break;
            case JsonFormConstants.TOASTER_NOTES:
                String info = (String) v.getTag(R.id.label_dialog_info);
                if (!TextUtils.isEmpty(info)) {
                    showInformationDialog(v);
                }
                break;
            case JsonFormConstants.NUMBER_SELECTOR:
                createNumberSelector(v);
                break;
            case JsonFormConstants.SPINNER:
                if (v.getId() == R.id.spinner_edit_button) {
                    setViewEditable(v);
                } else {
                    showInformationDialog(v);
                }
                break;
            case JsonFormConstants.EDIT_TEXT:
                String infoIcon = (String) v.getTag(R.id.label_dialog_info);
                if (!TextUtils.isEmpty(infoIcon)) {
                    showInformationDialog(v);
                } else {
                    setViewEditable(v);
                }
                break;
            case JsonFormConstants.NATIVE_EDIT_TEXT:
                setViewEditable(v);
                break;
            case JsonFormConstants.DATE_PICKER:
                if (v.getId() == R.id.date_picker_info_icon) {
                    showInformationDialog(v);
                }
                break;
            default:

                break;
        }
    }

    private void openSimprints(View v) {
        String projectId = (String) v.getTag(R.id.project_id);
        String userId = (String) v.getTag(R.id.user_id);
        String moduleId = (String) v.getTag(R.id.module_id);
        String fingerPrintOption = (String) v.getTag(R.id.finger_print_option);
        if (!TextUtils.isEmpty(fingerPrintOption) && fingerPrintOption.equalsIgnoreCase(JsonFormConstants.SIMPRINTS_OPTION_REGISTER)) {
            getView().startSimprintsRegistration(projectId, userId, moduleId);

        } else if (!TextUtils.isEmpty(fingerPrintOption) && fingerPrintOption.equalsIgnoreCase(JsonFormConstants.SIMPRINTS_OPTION_VERIFY)) {
            String guId = (String) v.getTag(R.id.guid);
            getView().startSimprintsVerification(projectId, userId, moduleId, guId);
        }

    }

    protected void nativeRadioButtonClickActions(View view) {
        String type = (String) view.getTag(R.id.specify_type);
        String specifyWidget = (String) view.getTag(R.id.specify_widget);
        Timber.i("The dialog content widget is this: " + specifyWidget);
        if (JsonFormConstants.CONTENT_INFO.equals(type) && specifyWidget
                .equals(JsonFormConstants.DATE_PICKER)) {
            NativeRadioButtonFactory.showDateDialog(view);
        } else if (JsonFormConstants.CONTENT_INFO.equals(type) && !specifyWidget.equals(JsonFormConstants.DATE_PICKER)) {
            new ExpansionPanelGenericPopupDialogTask(view).execute();
        } else if (view.getId() == R.id.label_edit_button) {
            setRadioViewsEditable(view);
        } else {
            showInformationDialog(view);
        }
    }

    private void setViewEditable(View editButton) {
        resetWidgetReadOnly(editButton);
        View editableView = (View) editButton.getTag(R.id.editable_view);
        editableView.setEnabled(true);
        editableView.setFocusable(true);
        editableView.requestFocus();
        editableView.requestFocusFromTouch();
    }

    private void resetWidgetReadOnly(View view) {
        JSONObject jsonForm = this.formFragment.getJsonApi().getmJSONObject();
        JSONObject field;
        try {
            field = FormUtils.getFieldFromForm(jsonForm, (String) view.getTag(R.id.key));
            if (field.has(JsonFormConstants.READ_ONLY) && field.getBoolean(JsonFormConstants.READ_ONLY)) {
                field.put(JsonFormConstants.READ_ONLY, false);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void setCheckboxesEditable(View editButton) {
        List<View> checkboxLayouts = (ArrayList<View>) editButton.getTag(R.id.editable_view);
        resetWidgetReadOnly(editButton);
        for (View checkboxLayout : checkboxLayouts) {
            setViewGroupEditable(checkboxLayout);
        }
    }

    protected void setRadioViewsEditable(View editButton) {
        RadioGroup radioGroup = (RadioGroup) editButton.getTag(R.id.editable_view);
        resetWidgetReadOnly(editButton);
        radioGroup.setEnabled(true);
        radioGroup.setFocusable(true);
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View childElement = radioGroup.getChildAt(i);
            setViewGroupEditable(childElement);
        }
    }

    private void setViewGroupEditable(View childElement) {
        if (childElement instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) childElement;
            for (int id = 0; id < group.getChildCount(); id++) {
                group.getChildAt(id).setFocusable(true);
                group.getChildAt(id).setEnabled(true);
                setViewGroupEditable(group.getChildAt(id));
            }
        }
    }

    protected void showInformationDialog(View view) {
        if (view.getTag(R.id.label_dialog_image_src) != null) {
            showCustomDialog(view);
        } else if (view.getTag(R.id.dynamic_label_info) != null) {
            showDynamicDialog(view);
        } else {
            showAlertDialog(view);
        }
    }

    @VisibleForTesting
    protected AlertDialog.Builder getAlertDialogBuilder() {
        return new AlertDialog.Builder(getView().getContext(),
                R.style.AppThemeAlertDialog);
    }

    private void showAlertDialog(View view) {
        AlertDialog.Builder builderSingle = getAlertDialogBuilder();
        builderSingle.setTitle((String) view.getTag(R.id.label_dialog_title));
        builderSingle.setMessage((String) view.getTag(R.id.label_dialog_info));
        builderSingle.setIcon(R.drawable.dialog_info_filled);

        builderSingle.setNegativeButton(getView().getContext().getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.show();
    }

    @VisibleForTesting
    protected Dialog getCustomDialog(View view) {
        return new Dialog(view.getContext());
    }

    private void showCustomDialog(@NonNull View view) {
        final Dialog dialog = getCustomDialog(view);
        dialog.setContentView(R.layout.native_form_custom_dialog);
        if (dialog.getWindow() != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (view.getContext().getResources().getDisplayMetrics().widthPixels
                    * 0.90);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }


        String titleString = (String) view.getTag(R.id.label_dialog_title);
        if (StringUtils.isNotBlank(titleString)) {
            TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
            dialogTitle.setText(titleString);
            dialogTitle.setVisibility(View.VISIBLE);
        }

        String dialogString = (String) view.getTag(R.id.label_dialog_info);
        if (StringUtils.isNotBlank(dialogString)) {
            TextView dialogText = dialog.findViewById(R.id.dialogText);
            dialogText.setText(dialogString);
            dialogText.setVisibility(View.VISIBLE);
        }

        ImageView dialogImage = dialog.findViewById(R.id.dialogImage);
        dialogImage.setFitsSystemWindows(true);
        dialogImage.setScaleType(ScaleType.FIT_XY);
        AppCompatButton dialogButton = dialog.findViewById(R.id.dialogButton);
        String imagePath = (String) view.getTag(R.id.label_dialog_image_src);


        dialogImage.setVisibility(View.VISIBLE);
        try {
            dialogImage.setImageDrawable(FormUtils.readImageFromAsset(view.getContext(), imagePath));
        } catch (IOException e) {
            Timber.e(e);
        }
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showDynamicDialog(@NonNull View view) {
        final Dialog dialog = getCustomDialog(view);
        dialog.setContentView(R.layout.native_form_dynamic_dialog);
        if (dialog.getWindow() != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (view.getContext().getResources().getDisplayMetrics().widthPixels
                    * 0.90);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        String titleString = (String) view.getTag(R.id.label_dialog_title);
        if (StringUtils.isNotBlank(titleString)) {
            TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
            dialogTitle.setText(titleString);
            dialogTitle.setVisibility(View.VISIBLE);
        }

        RecyclerView dialogRecyclerView = dialog.findViewById(R.id.dialogRecyclerView);
        JSONArray dynamicLabelInfoArray = (JSONArray) view.getTag(R.id.dynamic_label_info);
        ArrayList<DynamicLabelInfo> dynamicLabelInfoList = getDynamicLabelInfoList(dynamicLabelInfoArray);

        if (dynamicLabelInfoList.size() > 0) {
            DynamicLabelAdapter dynamicLabelAdapter = new DynamicLabelAdapter(view.getContext(), dynamicLabelInfoList);
            dialogRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            dialogRecyclerView.setAdapter(dynamicLabelAdapter);
            dialogRecyclerView.setVisibility(View.VISIBLE);
        }

        AppCompatButton dialogButton = dialog.findViewById(R.id.dialogButton);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialogButton.setVisibility(View.VISIBLE);

        dialog.show();
    }

    //household_photo,JsonFormConstants.CHOOSE_IMAGE
    public void onClickCameraIcon(String key, String type) {

        dispatchTakePictureIntent(key, type);
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton instanceof CheckBox) {
            String parentKey = (String) compoundButton.getTag(R.id.key);
            String openMrsEntityParent = (String) compoundButton.getTag(R.id.openmrs_entity_parent);
            String openMrsEntity = (String) compoundButton.getTag(R.id.openmrs_entity);
            String openMrsEntityId = (String) compoundButton.getTag(R.id.openmrs_entity_id);
            String childKey = (String) compoundButton.getTag(R.id.childKey);
            Boolean popup = (Boolean) compoundButton.getTag(R.id.extraPopup);
            if (popup == null) {
                popup = false;
            }
            JSONObject formObjectForStep = getFormObjectForStep(mStepName, parentKey);

            if (formObjectForStep != null && formObjectForStep
                    .has(JsonFormConstants.JSON_FORM_KEY.EXCLUSIVE)) {
                try {
                    JSONArray exclusiveArray = formObjectForStep
                            .getJSONArray(JsonFormConstants.JSON_FORM_KEY.EXCLUSIVE);
                    Set<String> exclusiveSet = new HashSet<>();
                    for (int i = 0; i < exclusiveArray.length(); i++) {
                        exclusiveSet.add(exclusiveArray.getString(i));
                    }

                    if (isChecked) {
                        if (exclusiveSet.contains(childKey)) {
                            getView().unCheckAllExcept(parentKey, childKey, compoundButton);
                        } else {
                            for (String excludeKey : exclusiveSet) {
                                getView().unCheck(parentKey, excludeKey, compoundButton);
                            }
                        }
                    }

                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            if (getView() != null)
                getView().writeValue(mStepName, parentKey, JsonFormConstants.OPTIONS_FIELD_NAME, childKey,
                        String.valueOf(compoundButton.isChecked()), openMrsEntityParent, openMrsEntity,
                        openMrsEntityId, popup);
        } else if (
                (compoundButton instanceof AppCompatRadioButton || compoundButton instanceof RadioButton)
                        && isChecked) {
            String parentKey = (String) compoundButton.getTag(R.id.key);
            String openMrsEntityParent = (String) compoundButton.getTag(R.id.openmrs_entity_parent);
            String openMrsEntity = (String) compoundButton.getTag(R.id.openmrs_entity);
            String openMrsEntityId = (String) compoundButton.getTag(R.id.openmrs_entity_id);
            String childKey = (String) compoundButton.getTag(R.id.childKey);
            Boolean popup = (Boolean) compoundButton.getTag(R.id.extraPopup);
            if (popup == null) {
                popup = false;
            }

            String specifyWidget = (String) compoundButton.getTag(R.id.specify_widget);
            if (!TextUtils.isEmpty(specifyWidget)) {
                nativeRadioButtonClickActions(compoundButton);
            }

            getView().unCheckAllExcept(parentKey, childKey, compoundButton);
            getView().writeValue(mStepName, parentKey, childKey, openMrsEntityParent, openMrsEntity,
                    openMrsEntityId, popup);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String parentKey = (String) parent.getTag(R.id.key);
        String openMrsEntityParent = (String) parent.getTag(R.id.openmrs_entity_parent);
        String openMrsEntity = (String) parent.getTag(R.id.openmrs_entity);
        String openMrsEntityId = (String) parent.getTag(R.id.openmrs_entity_id);
        JSONArray jsonArray = (JSONArray) parent.getTag(R.id.keys);
        Boolean popup = (Boolean) parent.getTag(R.id.extraPopup);
        if (popup == null) {
            popup = false;
        }

        String value = parent.getItemAtPosition(position).toString();
        if (jsonArray != null && position > -1 && jsonArray.length() > 0) {
            try {
                value = jsonArray.getString(position);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        if (getView() != null) {
            getView().writeValue(mStepName, parentKey, value, openMrsEntityParent, openMrsEntity,
                    openMrsEntityId, popup);
        }
    }

    private void createNumberSelector(View view) {
        Object isNumberSelectorTextView = view.getTag(R.id.is_number_selector_dialog_textview);
        CustomTextView customTextView = (CustomTextView) view;

        if (isNumberSelectorTextView != null && (boolean) isNumberSelectorTextView) {
            NumberSelectorFactory.createNumberSelector(customTextView);
            //Save the last value if none is selected
            if (customTextView.getText().toString().contains("+")) {
                String currentText = customTextView.getText().toString().replace("+", "");
                customTextView.setText(currentText);
                saveValueFromCustomView(customTextView);
            }
        } else {
            int item = (int) customTextView.getTag(R.id.number_selector_item);
            int numberOfSelectors = (int) customTextView.getTag(R.id.number_selector_number_of_selectors);
            if (item <= (numberOfSelectors - 1)) {
                NumberSelectorFactory.setBackgrounds(customTextView);
            }
            NumberSelectorFactory.setSelectedTextViews(customTextView);
            saveValueFromCustomView(customTextView);
        }

    }

    private void saveValueFromCustomView(CustomTextView customTextView) {
        //String parentKey = (String) customTextView.getTag(R.id.key);
        String parentKey = (String) ((View) customTextView.getParent()).getTag(R.id.key);
        String openMrsEntityParent = (String) customTextView.getTag(R.id.openmrs_entity_parent);
        String openMrsEntity = (String) customTextView.getTag(R.id.openmrs_entity);
        String openMrsEntityId = (String) customTextView.getTag(R.id.openmrs_entity_id);
        Boolean popup = (Boolean) customTextView.getTag(R.id.extraPopup);

        if (popup == null) {
            popup = false;
        }
        String value = String.valueOf(customTextView.getText());
        getView().writeValue(mStepName, parentKey, value, openMrsEntityParent, openMrsEntity,
                openMrsEntityId, popup);
    }

    private JSONObject getFormObjectForStep(String mStepName, String fieldKey) {
        try {
            if (getView() != null) {
                JSONArray array = getView().getStep(mStepName).getJSONArray(JsonFormConstants.FIELDS);

                for (int i = 0; i < array.length(); i++) {
                    if (array.getJSONObject(i).get(JsonFormConstants.KEY).equals(fieldKey)) {
                        return array.getJSONObject(i);
                    }
                }
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    public JsonFormInteractor getInteractor() {
        return mJsonFormInteractor;
    }

    public boolean onMenuItemClick(MenuItem item) {
        Intent intent = item.getIntent();
        if (intent != null && intent.getBooleanExtra(JsonFormConstants.IS_NUMBER_SELECTOR_MENU, true)) {
            String parentKey = intent.getStringExtra(JsonFormConstants.PARENT_KEY);
            String value = item.getTitle().toString();
            String openMrsEntityParent = intent.getStringExtra(JsonFormConstants.OPENMRS_ENTITY_PARENT);
            String openMrsEntity = intent.getStringExtra(JsonFormConstants.OPENMRS_ENTITY);
            String openMrsEntityId = intent.getStringExtra(JsonFormConstants.OPENMRS_ENTITY_ID);
            boolean popup = intent.getBooleanExtra(JsonFormConstants.IS_POPUP, false);
            NumberSelectorFactory.setSelectedTextViewText(value);
            getView().writeValue(mStepName, parentKey, value, openMrsEntityParent, openMrsEntity,
                    openMrsEntityId, popup);

            return true;
        }

        return false;
    }

    public String getmCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void preLoadRules(final JSONObject formJSONObject, final String stepName) {
        formFragment.getJsonApi().getAppExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                preLoadRules(stepName, formJSONObject);
            }
        });

    }


    private void preLoadRules(String stepName, JSONObject formJSONObject) {
        Set<String> ruleFiles = new HashSet<>();
        JSONObject step = formJSONObject.optJSONObject(stepName);
        if (step == null)
            return;
        JSONArray fields = step.optJSONArray(JsonFormConstants.FIELDS);
        if (fields == null)
            return;
        for (int i = 0; i < fields.length(); i++) {
            if (cleanupAndExit)
                return;
            JSONObject calculation = fields.optJSONObject(i).optJSONObject(JsonFormConstants.CALCULATION);
            JSONObject relevance = fields.optJSONObject(i).optJSONObject(JsonFormConstants.RELEVANCE);

            addRules(calculation, ruleFiles);
            addRules(relevance, ruleFiles);
        }

        for (final String fileName : ruleFiles) {
            formFragment.getJsonApi().getAppExecutors().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if (cleanupAndExit)
                        return;
                    formFragment.getJsonApi().getRulesEngineFactory().getRulesFromAsset(fileName);
                }
            });
        }
    }

    private void addRules(JSONObject jsonObject, Set<String> ruleFiles) {
        if (jsonObject != null) {
            JSONObject ruleEngine = jsonObject.optJSONObject(RuleConstant.RULES_ENGINE);
            if (ruleEngine != null) {
                JSONObject exRules = ruleEngine.optJSONObject(JsonFormConstants.JSON_FORM_KEY.EX_RULES);
                String file = exRules.optString(RuleConstant.RULES_FILE, null);
                if (file != null) {
                    ruleFiles.add(exRules.optString(RuleConstant.RULES_FILE));
                }
            }
        }

    }

    public void cleanUp() {
        cleanupAndExit = true;
    }
}
