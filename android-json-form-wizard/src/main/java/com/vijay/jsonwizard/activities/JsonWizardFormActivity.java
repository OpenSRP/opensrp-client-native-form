package com.vijay.jsonwizard.activities;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.json.JSONException;

/**
 * Created by keyman on 04/12/2018.
 */
public class JsonWizardFormActivity extends JsonFormActivity {

    @Override
    public void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    public void writeValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        callSuperWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
    }

    public void onFormFinish() {
        callSuperFinish();
    }

    protected void callSuperFinish() {
        super.getJsonApiEngine().onFormFinish();
    }

    protected void callSuperWriteValue(String stepName, String key, String value, String openMrsEntityParent, String openMrsEntity, String openMrsEntityId) throws JSONException {
        super.getJsonApiEngine().writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
    }

    protected void initializeFormFragmentCore() {
        JsonWizardFormFragment jsonWizardFormFragment = JsonWizardFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction()
                .add(com.vijay.jsonwizard.R.id.container, jsonWizardFormFragment).commit();
    }
}

