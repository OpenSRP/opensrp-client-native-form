package com.vijay.jsonwizard.listeners;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.ExpansionPanelItemModel;
import com.vijay.jsonwizard.event.RefreshExpansionPanelEvent;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class ExpansionPanelUndoButtonClickListener implements View.OnClickListener {
    private FormUtils formUtils = new FormUtils();
    private JsonApi jsonApi;

    public JsonApi getJsonApi() {
        return jsonApi;
    }

    public void setJsonApi(JsonApi jsonApi) {
        this.jsonApi = jsonApi;
    }

    @Override
    public void onClick(View view) {
        String key = (String) view.getTag(R.id.key);
        Context context = (Context) view.getTag(R.id.specify_context);
        String stepName = (String) view.getTag(R.id.specify_step_name);
        setJsonApi((JsonApi) context);
        JSONObject mainJson = getJsonApi().getmJSONObject();
        if (mainJson != null) {
            getExpansionPanel(mainJson, stepName, key, context, view);
        }
    }

    private void getExpansionPanel(JSONObject mainJson, String stepName, String parentKey, Context context, View view) {
        if (mainJson != null) {
            JSONArray fields = formUtils.getFormFields(stepName, context);
            try {
                if (fields.length() > 0) {
                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject item = fields.getJSONObject(i);
                        if (item != null && item.getString(JsonFormConstants.KEY).equals(parentKey) &&
                                item.has(JsonFormConstants.VALUE)) {
                            displayUndoDialog(context, item, mainJson, view);
                        }
                    }
                }
            } catch (JSONException e) {
                Timber.e(e, "ExpansionPanelUndoButtonClickListener -- getExpansionPanel>");
            }
        }
    }

    private void displayUndoDialog(Context context, final JSONObject item, final JSONObject mainJson, final View view)
            throws JSONException {
        Activity activity = (Activity) context;
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayout = getUndoDialogLayout(inflater);

        Button undo = dialogLayout.findViewById(R.id.undo_button);
        final Button cancel = dialogLayout.findViewById(R.id.cancel_button);
        CustomTextView headerTextView = dialogLayout.findViewById(R.id.txt_title_label);
        String testHeader = item.getString(JsonFormConstants.TEXT);
        headerTextView.setText(
                String.format(context.getResources().getString(R.string.undo_test_result), testHeader.toLowerCase()));

        final AlertDialog dialog = getUndoAlertDialog(activity, dialogLayout);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams param = window.getAttributes();
            param.gravity = Gravity.CENTER | Gravity.CENTER_HORIZONTAL;
            window.setAttributes(param);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshExpansionPanelEvent expansionPanelEvent = new RefreshExpansionPanelEvent(null,
                        (LinearLayout) view.getTag(R.id.linearLayout));

                try {
                    resetClearedGlobalValues(mainJson, getUndoneValues(item));
                } catch (JSONException e) {
                    Timber.e(e, "ExpansionPanelUndoButtonClickListener --> displayUndoDialog");
                }

                item.remove(JsonFormConstants.VALUE);
                item.remove(JsonFormConstants.REQUIRED_FIELDS);
                getJsonApi().setmJSONObject(mainJson);
                Utils.postEvent(expansionPanelEvent);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    protected AlertDialog getUndoAlertDialog(@NonNull Activity activity, @NonNull View dialogLayout) {
        return new AlertDialog.Builder(activity)
                .setView(dialogLayout).create();
    }

    protected View getUndoDialogLayout(@NonNull LayoutInflater inflater) {
        return inflater.inflate(R.layout.expasion_panel_undo_dialog, null);
    }

    /**
     * Resets the global values for values that have been undone to empty
     *
     * @param previousSelectedValues values to be reset
     * @throws JSONException JsonException thrown
     */
    private void resetClearedGlobalValues(JSONObject mainJson, List<String> previousSelectedValues) throws JSONException {
        if (mainJson.has(JsonFormConstants.GLOBAL)) {
            JSONObject globals = mainJson.getJSONObject(JsonFormConstants.GLOBAL);
            for (String item : previousSelectedValues) {
                if (globals.has(item)) {
                    globals.put(item, "");
                }
            }
        }
    }

    private List<String> getUndoneValues(JSONObject item) throws JSONException {
        LinkedList<String> previousValues = new LinkedList<>();
        if (item.has(JsonFormConstants.VALUE)) {
            JSONArray valuesArray = item.getJSONArray(JsonFormConstants.VALUE);
            for (int index = 0; index < valuesArray.length(); index++) {
                ExpansionPanelItemModel expansionPanelItem = FormUtils.getExpansionPanelItem(valuesArray.getJSONObject(index).getString(JsonFormConstants.KEY), valuesArray);
                previousValues.add(expansionPanelItem.getKey());
            }
        }
        return previousValues;
    }
}
