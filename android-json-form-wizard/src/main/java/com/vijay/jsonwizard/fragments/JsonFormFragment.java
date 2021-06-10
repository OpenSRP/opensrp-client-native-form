package com.vijay.jsonwizard.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.RadioButton;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.OnFieldsInvalid;
import com.vijay.jsonwizard.mvp.MvpFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.utils.NativeFormsProperties;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.simprint.SimPrintsLibrary;
import org.smartregister.simprint.SimPrintsRegisterActivity;
import org.smartregister.simprint.SimPrintsVerifyActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by vijay on 5/7/15.
 */
public class JsonFormFragment extends MvpFragment<JsonFormFragmentPresenter, JsonFormFragmentViewState>
        implements CommonListener, JsonFormFragmentView<JsonFormFragmentViewState> {
    private static final String TAG = "JsonFormFragment";
    public OnFieldsInvalid onFieldsInvalid;
    protected LinearLayout mMainView;
    protected ScrollView mScrollView;
    private Menu mMenu;
    private JsonApi mJsonApi;
    private Map<String, List<View>> lookUpMap = new HashMap<>();
    private Button previousButton;
    private Button nextButton;
    private String stepName;
    private LinearLayout bottomNavigation;
    private BottomNavigationListener navigationListener;
    private boolean shouldSkipStep = true;

    private static NativeFormsProperties nativeFormProperties;

    public static JsonFormFragment getFormFragment(String stepName) {
        JsonFormFragment jsonFormFragment = new JsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);

        return jsonFormFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        nativeFormProperties = Utils.getProperties(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.native_json_form_fragment, null);

        mMainView = rootView.findViewById(R.id.main_layout);
        mScrollView = rootView.findViewById(R.id.scroll_view);
        previousButton = rootView.findViewById(R.id.previous_button);
        nextButton = rootView.findViewById(R.id.next_button);
        bottomNavigation = rootView.findViewById(R.id.bottom_navigation_layout);
        navigationListener = new BottomNavigationListener();
        if (getArguments() != null) {
            stepName = getArguments().getString(JsonFormConstants.STEPNAME);
        }

        setupToolbarBackButton();
        showScrollBars();

        JSONObject step = getStep(stepName);
        if (step.optBoolean(JsonFormConstants.BOTTOM_NAVIGATION)) {
            initializeBottomNavigation(step, rootView);
        }

        presenter.preLoadRules(getJsonApi().getmJSONObject(), stepName);
        return rootView;
    }

    private void setupToolbarBackButton() {
        if (getArguments() != null) {
            String stepName = getArguments().getString(JsonFormConstants.STEPNAME);
            if (getStep(stepName).optBoolean(JsonFormConstants.DISPLAY_BACK_BUTTON)) {
                getSupportActionBar().setHomeAsUpIndicator(getHomeUpIndicator());
                setUpBackButton();
            }
        }

    }

    protected void showScrollBars() {
        boolean displayScrollBars = displayScrollBars();
        if (displayScrollBars) {
            mScrollView.setScrollbarFadingEnabled(false);
            mScrollView.setScrollBarFadeDuration(0);
        }
    }

    protected void initializeBottomNavigation(JSONObject step, View rootView) {
        if (step.has(JsonFormConstants.PREVIOUS)) {
            previousButton.setVisibility(View.VISIBLE);
            if (step.has(JsonFormConstants.PREVIOUS_LABEL)) {
                previousButton.setText(step.optString(JsonFormConstants.PREVIOUS_LABEL));
            }
        }

        if (step.has(JsonFormConstants.NEXT)) {
            nextButton.setVisibility(View.VISIBLE);
            if (step.has(JsonFormConstants.NEXT_LABEL)) {
                nextButton.setText(step.optString(JsonFormConstants.NEXT_LABEL));
            }
        } else if (step.optString(JsonFormConstants.NEXT_TYPE).equalsIgnoreCase(JsonFormConstants.SUBMIT)) {
            nextButton.setTag(R.id.submit, true);
            nextButton.setVisibility(View.VISIBLE);
            if (step.has(JsonFormConstants.SUBMIT_LABEL)) {
                nextButton.setText(step.optString(JsonFormConstants.SUBMIT_LABEL));
            } else {
                nextButton.setText(R.string.submit);
            }
        } else if (!step.has(JsonFormConstants.NEXT)) {
            nextButton.setTag(R.id.submit, true);
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setText(R.string.save);
        }

        if (step.has(JsonFormConstants.BOTTOM_NAVIGATION_ORIENTATION)) {
            // layout orientation
            int orientation = "vertical".equals(step.optString(JsonFormConstants.BOTTOM_NAVIGATION_ORIENTATION)) ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL;
            bottomNavigation.setOrientation(orientation);
            bottomNavigation.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            bottomNavigation.removeView(previousButton);
            bottomNavigation.addView(previousButton);
            // nav btn params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int layoutMargin = Utils.pixelToDp((int) getContext().getResources().getDimension(R.dimen.bottom_navigation_margin), getContext());
            params.setMargins(layoutMargin, layoutMargin, layoutMargin, layoutMargin);
            previousButton.setLayoutParams(params);
            nextButton.setLayoutParams(params);
        }

        rootView.findViewById(R.id.previous_button).setOnClickListener(navigationListener);
        rootView.findViewById(R.id.next_button).setOnClickListener(navigationListener);
    }

    @DrawableRes
    protected int getHomeUpIndicator() {
        return R.drawable.ic_action_close;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof JsonApi) {
            setmJsonApi((JsonApi) activity);
        }

        super.onAttach(activity);

        if (getActivity() instanceof OnFieldsInvalid) {
            onFieldsInvalid = (OnFieldsInvalid) getActivity();
        } else {
            Timber.e("Error retrieving passed invalid fields");
        }
    }

    public void setmJsonApi(JsonApi mJsonApi) {
        this.mJsonApi = mJsonApi;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getJsonApi().isPreviousPressed()) {
            skipStepOnPreviousPressed();
        }
    }

    @Override
    public void onDetach() {
        setmJsonApi(null);
        presenter.cleanUp();
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        menu.clear();
        inflater.inflate(R.menu.menu_toolbar, menu);
        presenter.setUpToolBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            presenter.onBackClick();
            presenter.checkAndStopCountdownAlarm();
            return true;
        } else if (item.getItemId() == R.id.action_next) {
            return next();
        } else if (item.getItemId() == R.id.action_save) {
            try {
                boolean skipValidation = ((JsonFormActivity) mMainView.getContext()).getIntent()
                        .getBooleanExtra(JsonFormConstants.SKIP_VALIDATION, false);
                return save(skipValidation);
            } catch (Exception e) {
                Timber.e(e, " --> onOptionsItemSelected");
                return save(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Skips blank by relevance steps when next is clicked on the json wizard forms.
     */
    public void skipLoadedStepsOnNextPressed() {
        if (skipBlankSteps()) {
            JSONObject formStep = getStep(getArguments().getString(JsonFormConstants.STEPNAME));
            String next = formStep.optString(JsonFormConstants.NEXT, "");
            if (StringUtils.isNotEmpty(next)) {
                checkIfStepIsBlank(formStep);
                if (shouldSkipStep() && !stepHasNoSkipLogic(JsonFormConstants.STEP1)) {
                    getJsonApi().setNextStep(next);
                    markStepAsSkipped(formStep);
                    next();
                }
            }
        }
    }

    /**
     * Returns the current form step number when given than steps next step number.
     * This number is used to figure out which steps to pop when previous is clicked.
     *
     * @return formNumber {@link Integer}
     */
    private int getFormStepNumber() {
        return Integer.parseInt(getArguments().getString(JsonFormConstants.STEPNAME).substring(4));
    }

    /***
     * Adds a property 'skipped=true' to a step object if the step is skipped
     * @param formStep {@link JSONObject}
     */
    private void markStepAsSkipped(JSONObject formStep) {
        try {
            formStep.put("skipped", true);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    /***
     * Skips blank steps when next is clicked on the json wizard forms.
     * @param step {@link JSONObject}
     */

    public boolean skipStepsOnNextPressed(String step) {
        boolean isSkipped = false;
        if (skipBlankSteps()) {
            JSONObject formStep = getJsonApi().getmJSONObject().optJSONObject(step);
            String next = formStep.optString(JsonFormConstants.NEXT, "");
            if (StringUtils.isNotEmpty(next) && (!getJsonApi().isNextStepRelevant() && !nextStepHasNoSkipLogic())) {
                markStepAsSkipped(formStep);
                getJsonApi().setNextStep(next);
                isSkipped = true;
                next();
            }
        }
        return isSkipped;
    }

    /**
     * Skips blank by relevance steps when previous is clicked on the json wizard forms.
     */
    public void skipStepOnPreviousPressed() {
        if (skipBlankSteps()) {
            int currentFormStepNumber = getFormStepNumber();
            for (int i = currentFormStepNumber; i >= 1; i--) {
                JSONObject formStep = getJsonApi().getmJSONObject().optJSONObject(JsonFormConstants.STEP + i);
                if (formStep != null) {
                    checkIfStepIsBlank(formStep);
                    if (shouldSkipStep()) {
                        getFragmentManager().popBackStack();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * should not be used alone, use with {@link #nextStepHasNoSkipLogic()}
     * Checks if a given step is blank due to relevance hiding all the widgets
     *
     * @param formStep {@link JSONObject}
     */
    private void checkIfStepIsBlank(JSONObject formStep) {
        try {
            if (formStep.has(JsonFormConstants.FIELDS)) {
                JSONArray fields = formStep.getJSONArray(JsonFormConstants.FIELDS);
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    if (field.has(JsonFormConstants.TYPE) && !JsonFormConstants.HIDDEN.equals(field.getString(JsonFormConstants.TYPE))) {
                        boolean isVisible = field.optBoolean(JsonFormConstants.IS_VISIBLE, true);
                        if (isVisible) {
                            setShouldSkipStep(false);
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Timber.e(e, "%s --> checkIfStepIsBlank", this.getClass().getCanonicalName());
        }
    }

    /***
     * check @link{{@link #stepHasNoSkipLogic(String)}}
     * @return boolean
     */
    private boolean nextStepHasNoSkipLogic() {
        return stepHasNoSkipLogic(getJsonApi().nextStep());
    }

    /***
     * It returns true if the step has no relevance fields
     * @param step
     * @return boolean
     */
    public boolean stepHasNoSkipLogic(@Nullable String step) {
        if (StringUtils.isNotBlank(step)) {
            Boolean nextStepHasNoRelevance = getJsonApi().stepSkipLogicPresenceMap().get(step);
            if (nextStepHasNoRelevance != null) {
                return nextStepHasNoRelevance;
            }
            return false;
        } else {
            return nextStepHasNoSkipLogic();
        }
    }

    public boolean save(boolean skipValidation) {
        try {
            mMainView.setTag(R.id.skip_validation, skipValidation);
            presenter.onSaveClick(mMainView);
            return true;
        } catch (Exception e) {
            Timber.e(e, " --> save");
        }

        return false;
    }

    public boolean shouldSkipStep() {
        return shouldSkipStep;
    }

    public boolean next() {
        try {
            return presenter.onNextClick(mMainView);
        } catch (Exception e) {
            Timber.e(e, " --> next");
        }

        return false;
    }

    public void setShouldSkipStep(boolean shouldSkipStep) {
        this.shouldSkipStep = shouldSkipStep;
    }

    public JsonApi getJsonApi() {
        return mJsonApi;
    }

    @Override
    protected JsonFormFragmentViewState createViewState() {
        return new JsonFormFragmentViewState();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mJsonApi.clearFormDataViews();
        presenter.addFormElements();
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        return new JsonFormFragmentPresenter(this);
    }

    @Override
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSnackBar(String message) {
        Utils.showSnackBar(getMainView(), message);
    }

    @Override
    public CommonListener getCommonListener() {
        return this;
    }

    @Override
    public void addFormElements(List<View> views) {
        for (View view : views) {
            mMainView.addView(view);
        }
        mJsonApi.refreshHiddenViews(false);
        mJsonApi.resetFocus();
    }

    @Override
    public ActionBar getSupportActionBar() {
        return ((JsonFormActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public Toolbar getToolbar() {
        return ((JsonFormActivity) getActivity()).getToolbar();
    }

    @Override
    public void setToolbarTitleColor(int colorId) {
        getToolbar().setTitleTextColor(getContext().getResources().getColor(colorId));
    }

    @Override
    public void updateVisibilityOfNextAndSave(boolean next, boolean save) {
        mMenu.findItem(R.id.action_next).setVisible(next);
        mMenu.findItem(R.id.action_save).setVisible(save);
    }

    @Override
    public void hideKeyBoard() {
        super.hideSoftKeyboard();
    }

    @Override
    public void transactThis(JsonFormFragment next) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, next).addToBackStack(next.getArguments().getString(JsonFormConstants.STEPNAME))
                .commitAllowingStateLoss(); // use https://stackoverflow.com/a/10261449/9782187
    }

    @Override
    public void updateRelevantImageView(Bitmap bitmap, String imagePath, String currentKey) {
        int childCount = mMainView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mMainView.getChildAt(i);
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                String key = (String) imageView.getTag(R.id.key);
                if (key.equals(currentKey)) {
                    if (bitmap != null)
                        imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setTag(R.id.imagePath, imagePath);
                }
            }
        }
    }

    @Override
    public void writeValue(String stepName, String key, String selectedValue, String openMrsEntityParent,
                           String openMrsEntity, String openMrsEntityId, boolean popup) {
        try {
            mJsonApi.writeValue(stepName, key, selectedValue, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);
        } catch (JSONException e) {
            Timber.e(e, " --> writeValue");
        }
    }

    @Override
    public void writeValue(String stepName, String prentKey, String childObjectKey, String childKey, String value,
                           String openMrsEntityParent, String openMrsEntity, String openMrsEntityId, boolean popup) {
        try {
            mJsonApi.writeValue(stepName, prentKey, childObjectKey, childKey, value, openMrsEntityParent, openMrsEntity,
                    openMrsEntityId, popup);
        } catch (JSONException e) {
            Timber.e(e, " --> writeValue");
        }
    }

    @Override
    public void writeMetaDataValue(String metaDataKey, Map<String, String> values) {
        try {
            mJsonApi.writeMetaDataValue(metaDataKey, values);
        } catch (JSONException e) {
            Timber.e(e, " --> writeMetaDataValue");
        }
    }

    @Override
    public JSONObject getStep(String stepName) {
        return mJsonApi.getStep(stepName);
    }

    @Override
    public String getCurrentJsonState() {
        return mJsonApi.currentJsonState();
    }

    @Override
    public void finishWithResult(Intent returnIntent) {
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }

    @Override
    public void setUpBackButton() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void backClick() {
        getActivity().onBackPressed();
    }

    @Override
    public void unCheckAllExcept(String parentKey, String childKey, CompoundButton compoundButton) {
        ViewGroup mMainView = null;
        if (compoundButton instanceof CheckBox) {
            mMainView = (ViewGroup) compoundButton.getParent().getParent();
        }
        if (mMainView != null) {
            int childCount = mMainView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = mMainView.getChildAt(i);
                if (isCheckbox(view)) {
                    uncheckCheckbox(view, parentKey, childKey);
                }
            }
        }
    }

    @Override
    public void unCheck(String parentKey, String exclusiveKey, CompoundButton compoundButton) {
        ViewGroup mMainView = compoundButton instanceof CheckBox ? (ViewGroup) compoundButton.getParent().getParent() :
                (ViewGroup) compoundButton.getParent().getParent().getParent();
        if (mMainView != null) {
            int childCount = mMainView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = mMainView.getChildAt(i);

                if (view instanceof RadioButton) {
                    AppCompatRadioButton radio =
                            (((ViewGroup) view).getChildAt(0)).findViewWithTag(JsonFormConstants.NATIVE_RADIO_BUTTON);
                    String parentKeyAtIndex = (String) radio.getTag(R.id.key);
                    String childKeyAtIndex = (String) radio.getTag(R.id.childKey);
                    if (radio.isChecked() && parentKeyAtIndex.equals(parentKey) && childKeyAtIndex.equals(exclusiveKey)) {
                        radio.setChecked(false);
                        break;
                    }
                } else if (isCheckbox(view)) {
                    CheckBox checkBox = ((LinearLayout) view).findViewWithTag(JsonFormConstants.CHECK_BOX);
                    String parentKeyAtIndex = (String) checkBox.getTag(R.id.key);
                    String childKeyAtIndex = (String) checkBox.getTag(R.id.childKey);
                    if (checkBox.isChecked() && parentKeyAtIndex.equals(parentKey) && childKeyAtIndex.equals(exclusiveKey)) {
                        checkBox.setChecked(false);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getCount() {
        return getJsonApi().getCount();
    }

    @Override
    public boolean displayScrollBars() {
        return getJsonApi().displayScrollBars();
    }

    @Override
    public boolean skipBlankSteps() {
        return getJsonApi() != null && getJsonApi().skipBlankSteps();
    }

    @Override
    public void onFormStart() {
        mJsonApi.onFormStart();
    }

    @Override
    public void onFormFinish() {
        mJsonApi.onFormFinish();
    }

    @Override
    public void scrollToView(final View view) {
        if (getActivity() != null) {
            getJsonApi().getAppExecutors().mainThread().execute(new Runnable() {
                @Override
                public void run() {
                    view.requestFocus();
                    if (!(view instanceof MaterialEditText)) {
                        mScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                int viewLength = view.getBottom() - view.getHeight();
                                if (viewLength < 0) {
                                    viewLength = 0;
                                }
                                mScrollView.scrollTo(0, viewLength);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void startSimprintsRegistration(String projectId, String userId, String moduleId) {
        if (!TextUtils.isEmpty(projectId) && !TextUtils.isEmpty(userId) && !TextUtils.isEmpty(moduleId)) {
            SimPrintsLibrary.init(getActivity(), projectId, userId);
            SimPrintsRegisterActivity.startSimprintsRegisterActivity(getActivity(), moduleId, JsonFormConstants.ACTIVITY_REQUEST_CODE.REQUEST_CODE_REGISTER);

        } else {
            // SimprintsLibrary.init(getActivity(),"tZqJnw0ajK04LMYdZzyw","test_user");
            Toast.makeText(getActivity(), getString(R.string.simprints_init_fail), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void startSimprintsVerification(String projectId, String userId, String moduleId, String guId) {
        if (!TextUtils.isEmpty(projectId) && !TextUtils.isEmpty(userId) && !TextUtils.isEmpty(moduleId) && !TextUtils.isEmpty(guId)) {
            SimPrintsLibrary.init(getActivity(), projectId, userId);
            SimPrintsVerifyActivity.startSimprintsVerifyActivity(getActivity(), moduleId, guId, JsonFormConstants.ACTIVITY_REQUEST_CODE.REQUEST_CODE_VERIFY);

        } else {
            // SimprintsLibrary.init(getActivity(),"tZqJnw0ajK04LMYdZzyw","test_user");

            Toast.makeText(getActivity(), getString(R.string.simprints_init_fail), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isCheckbox(View view) {
        return view instanceof LinearLayout && view.getTag(R.id.type).equals(JsonFormConstants.CHECK_BOX + "_parent");
    }

    private void uncheckCheckbox(View view, String parentKey, String childKey) {
        CheckBox checkBox = view.findViewWithTag(JsonFormConstants.CHECK_BOX);
        String parentKeyAtIndex = (String) checkBox.getTag(R.id.key);
        String childKeyAtIndex = (String) checkBox.getTag(R.id.childKey);
        if (checkBox.isChecked() && parentKeyAtIndex.equals(parentKey) && !childKeyAtIndex.equals(childKey)) {
            checkBox.setChecked(false);
        }
    }

    public LinearLayout getMainView() {
        return mMainView;
    }

    @Override
    public void onClick(View v) {
        presenter.onClick(v);
    }

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        presenter.onCheckedChanged(buttonView, isChecked);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        presenter.onItemSelected(parent, view, position, id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Timber.d("onNothingSelected called");
    }

    public Map<String, List<View>> getLookUpMap() {
        return lookUpMap;
    }

    public JsonFormFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return presenter.onMenuItemClick(item);
    }

    protected class BottomNavigationListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view != null) {
                if (view.getId() == R.id.next_button) {
                    Object isSubmit = view.getTag(R.id.submit);
                    if (isSubmit != null && Boolean.valueOf(isSubmit.toString())) {
                        save(false);
                    } else {
                        next();
                    }
                } else if (view.getId() == R.id.previous_button) {
                    getFragmentManager().popBackStack();
                }
            }
        }
    }

    public OnFieldsInvalid getOnFieldsInvalidCallback() {
        return onFieldsInvalid;
    }

    public void setOnFieldsInvalid(OnFieldsInvalid onFieldsInvalid) {
        this.onFieldsInvalid = onFieldsInvalid;
    }

    /**
     * Getter for native form properties
     */
    public static NativeFormsProperties getNativeFormProperties() {
        return nativeFormProperties;
    }
}
