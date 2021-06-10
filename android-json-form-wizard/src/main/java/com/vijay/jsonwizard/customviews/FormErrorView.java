package com.vijay.jsonwizard.customviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.utils.ValidationStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FormErrorView extends LinearLayoutCompat {
    private Map<String, ValidationStatus> invalidFields;
    private HashMap<String, ArrayList<ValidationStatus>> errorsPerStep;

    public FormErrorView(Context context) {
        super(context);
        invalidFields = new ConcurrentHashMap<>();
        errorsPerStep = new HashMap<>();
        this.setOrientation(VERTICAL);
    }


    private Map<String, ValidationStatus> getInvalidFields() {
        return invalidFields;
    }

    private void processInvalidFields() {
        Set<Map.Entry<String, ValidationStatus>> validationStatusEntry = getInvalidFields().entrySet();

        for (Map.Entry<String, ValidationStatus> entry : validationStatusEntry) {
            String key = entry.getKey();
            ValidationStatus value = entry.getValue();
            String[] splitKey = key.split(":");
            String[] splitKeyAndStepName = splitKey[0].split("#");

            ArrayList<ValidationStatus> statusArrayList;
            String errorPerStepKey = splitKeyAndStepName[0] + ": " + splitKeyAndStepName[1];
            if (errorsPerStep.containsKey(errorPerStepKey)) {
                statusArrayList = errorsPerStep.get(errorPerStepKey);
            } else {
                statusArrayList = new ArrayList<>();
            }
            statusArrayList.add(value);

            errorsPerStep.put(errorPerStepKey, statusArrayList);
        }
        addCustomViews();
    }

    private void addCustomViews() {
        List<String> keySet = new ArrayList<>(errorsPerStep.keySet());
        Collections.sort(keySet);
        for (String key : keySet) {
            ArrayList<ValidationStatus> values = errorsPerStep.get(key);
            View view = LayoutInflater.from(this.getContext()).inflate(R.layout.native_form_error_item, null, false);
            FormErrorViewHolder formErrorViewHolder = new FormErrorViewHolder(view);
            String errors;
            StringBuilder stringBuilder = new StringBuilder();
            String spacing = values.size() > 1 ? "\n\n" : "";
            for (int i = 0; i < values.size(); i++) {
                ValidationStatus validationStatus = values.get(i);
                stringBuilder.append(i + 1).append(". ").append(validationStatus.getErrorMessage()).append(spacing);
            }
            errors = stringBuilder.toString();
            String stepName = key.toUpperCase();
            formErrorViewHolder.bindViews(stepName, errors);
            this.addView(formErrorViewHolder.getItemView());
        }
    }

    public void setInvalidFields(Map<String, ValidationStatus> invalidFields) {
        this.invalidFields = invalidFields;
        processInvalidFields();
    }

    class FormErrorViewHolder {
        private TextView stepNameTextView;
        private TextView errorsTextView;
        private View itemView;

        FormErrorViewHolder(View itemView) {
            this.itemView = itemView;
            initViews();
        }

        private void initViews() {
            stepNameTextView = itemView.findViewById(R.id.error_item_step_name);
            errorsTextView = itemView.findViewById(R.id.error_item_values);
        }

        private void bindViews(String stepName, String errors) {
            stepNameTextView.setText(stepName);
            errorsTextView.setText(errors);
        }

        public View getItemView() {
            return itemView;
        }
    }
}
