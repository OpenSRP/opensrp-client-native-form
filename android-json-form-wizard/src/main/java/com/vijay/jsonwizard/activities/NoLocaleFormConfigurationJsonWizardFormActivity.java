package com.vijay.jsonwizard.activities;

public class NoLocaleFormConfigurationJsonWizardFormActivity extends FormConfigurationJsonWizardFormActivity {
    @Override
    protected boolean supportsLocaleBasedForms() {
        return false;
    }
}
