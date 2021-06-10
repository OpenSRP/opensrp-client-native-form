package org.smartregister.nativeform_tester;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.activities.JsonFormBaseActivity;
import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.factory.FileSourceFactoryHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.nativeform.R;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_GET_JSON = 1234;
    private static final String TAG = MainActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.child_enrollment).setOnClickListener(this);
        findViewById(R.id.wizard_form).setOnClickListener(this);
        findViewById(R.id.native_form_basic).setOnClickListener(this);
        findViewById(R.id.rules_engine_skip_logic).setOnClickListener(this);
        findViewById(R.id.numbers_selector_widget).setOnClickListener(this);
        findViewById(R.id.generic_dialog_button).setOnClickListener(this);
        findViewById(R.id.validation_form_button).setOnClickListener(this);
        findViewById(R.id.expansion_panel_button).setOnClickListener(this);
        findViewById(R.id.repeating_group_button).setOnClickListener(this);
        findViewById(R.id.multiselect_list).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_single) {
            try {
                startForm(REQUEST_CODE_GET_JSON, "single_form", null, false);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else if (id == R.id.action_wizard) {
            try {
                startForm(REQUEST_CODE_GET_JSON, "wizard_form", null, false);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else if (id == R.id.action_validation) {
            try {
                startForm(REQUEST_CODE_GET_JSON, "validation_form", null, false);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String jsonString = data.getStringExtra("json");
            Log.i(getClass().getName(), "Result json String !!!! " + jsonString);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void startForm(int jsonFormActivityRequestCode, String formName, String entityId, boolean translate) throws Exception {

        final String STEP1 = "step1";
        final String FIELDS = "fields";
        final String KEY = "key";
        final String ZEIR_ID = "ZEIR_ID";
        final String VALUE = "value";

        String currentLocationId = "Kenya";


        JSONObject jsonForm = getFormJson(formName);
        if (jsonForm != null) {
            jsonForm.getJSONObject("metadata").put("encounter_location", currentLocationId);

            switch (formName) {
                case "rules_engine_demo": {
                    Intent intent = new Intent(this, JsonWizardFormActivity.class);
                    intent.putExtra("json", jsonForm.toString());
                    Log.d(getClass().getName(), "form is " + jsonForm.toString());

                    Form form = new Form();
                    form.setName("Rules engine demo");
                    form.setWizard(true);
                    form.setNextLabel(getString(R.string.next));
                    form.setPreviousLabel(getString(R.string.previous));
                    form.setSaveLabel(getString(R.string.save));
                    form.setActionBarBackground(R.color.customAppThemeBlue);
                    form.setNavigationBackground(R.color.button_navy_blue);
                    form.setHideSaveLabel(true);
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    startActivityForResult(intent, jsonFormActivityRequestCode);
                    break;
                }
                case "wizard_form": {
                    Intent intent = new Intent(this, JsonWizardFormActivity.class);
                    intent.putExtra("json", jsonForm.toString());
                    Log.d(getClass().getName(), "form is " + jsonForm.toString());

                    Form form = new Form();
                    form.setName(getString(R.string.profile));
                    form.setWizard(true);
                    form.setActionBarBackground(R.color.profile_actionbar);
                    form.setNavigationBackground(R.color.profile_navigation);
                    form.setHideSaveLabel(true);
                    form.setNextLabel(getString(R.string.next));
                    form.setPreviousLabel(getString(R.string.previous));
                    form.setSaveLabel(getString(R.string.save));
                    form.setBackIcon(R.drawable.ic_icon_positive);
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                    startActivityForResult(intent, jsonFormActivityRequestCode);
                    break;
                }
                case "basic_form": {
                    Intent intent = new Intent(this, JsonWizardFormActivity.class);
                    intent.putExtra("json", jsonForm.toString());
                    Log.d(getClass().getName(), "form is " + jsonForm.toString());

                    Form form = new Form();
                    form.setName(getString(R.string.basic_form));
                    form.setWizard(true);
                    form.setActionBarBackground(R.color.profile_actionbar);
                    form.setNavigationBackground(R.color.profile_navigation);
                    form.setHideSaveLabel(true);
                    form.setNextLabel(getString(R.string.next));
                    form.setPreviousLabel(getString(R.string.previous));
                    form.setSaveLabel(getString(R.string.save));
                    form.setBackIcon(R.drawable.ic_icon_positive);
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                    startActivityForResult(intent, jsonFormActivityRequestCode);
                    break;
                }
                case "validation_form": {
                    Intent intent = new Intent(this, JsonWizardFormActivity.class);
                    intent.putExtra("json", jsonForm.toString());
                    Log.d(getClass().getName(), "form is " + jsonForm.toString());

                    Form form = new Form();
                    form.setName(getString(R.string.validation_test));
                    form.setWizard(true);
                    form.setNextLabel("Next");
                    form.setPreviousLabel("Previous");
                    form.setSaveLabel("Submit");
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                    startActivityForResult(intent, jsonFormActivityRequestCode);
                    break;
                }
                default: {

                    String currentEntityID = (entityId == null) ? "ABC" + Math.random() : entityId;


                    // Inject zeir id into the form
                    JSONObject stepOne = jsonForm.getJSONObject(STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(FIELDS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getString(KEY)
                                .equalsIgnoreCase(ZEIR_ID)) {
                            jsonObject.remove(VALUE);
                            jsonObject.put(VALUE, currentEntityID);
                            continue;
                        }
                    }

                    Intent intent = new Intent(this, JsonFormActivity.class);
                    intent.putExtra("json", jsonForm.toString());
                    intent.putExtra(JsonFormConstants.PERFORM_FORM_TRANSLATION, translate);
                    Timber.d("form is " + jsonForm.toString());
                    startActivityForResult(intent, jsonFormActivityRequestCode);
                    break;
                }
            }

        }

    }


    public JSONObject getFormJson(String formIdentity) {
        try {
            return FileSourceFactoryHelper.getFileSource(JsonFormBaseActivity.DATA_SOURCE)
                    .getFormFromFile(getApplicationContext(), formIdentity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        try {
            switch (id) {
                case R.id.child_enrollment:
                    startForm(REQUEST_CODE_GET_JSON, "single_form", null, true);
                    break;
                case R.id.wizard_form:
                    startForm(REQUEST_CODE_GET_JSON, "wizard_form", null, false);
                    break;
                case R.id.native_form_basic:
                    startForm(REQUEST_CODE_GET_JSON, "basic_form", null, false);
                    break;
                case R.id.rules_engine_skip_logic:
                    startForm(REQUEST_CODE_GET_JSON, "rules_engine_demo", null, false);
                    break;
                case R.id.numbers_selector_widget:
                    startForm(REQUEST_CODE_GET_JSON, "constraints_demo", null, false);
                    break;
                case R.id.generic_dialog_button:
                    startForm(REQUEST_CODE_GET_JSON, "generic_popup_form", null, false);
                    break;
                case R.id.validation_form_button:
                    startForm(REQUEST_CODE_GET_JSON, "validation_form", null, false);
                    break;
                case R.id.expansion_panel_button:
                    startForm(REQUEST_CODE_GET_JSON, "expansion_panel_form", null, false);
                    break;
                case R.id.repeating_group_button:
                    startForm(REQUEST_CODE_GET_JSON, "repeating_group", null, false);
                    break;
                case R.id.multiselect_list:
                    startForm(REQUEST_CODE_GET_JSON, "multi_select_list_form", null, false);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }
}
