package com.vijay.jsonwizard.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.customviews.FormErrorView;
import com.vijay.jsonwizard.interfaces.OnFieldsInvalid;
import com.vijay.jsonwizard.utils.ValidationStatus;

import java.util.Map;

public class JsonFormErrorFragment extends DialogFragment implements View.OnClickListener {

    public static String TAG = JsonFormErrorFragment.class.getSimpleName();
    public OnFieldsInvalid onFieldsInvalid;
    private FormErrorView formError;
    private Toolbar dialogToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.NativeFormsFullScreenDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.native_form_error_fragment, container, false);
        dialogToolbar = view.findViewById(R.id.error_fragment_toolbar);
        setupErrorFragmentToolBar();
        FrameLayout frameLayout = view.findViewById(R.id.error_fragment_frame_layout);
        formError = new FormErrorView(getContext());
        handleFormErrors();
        frameLayout.addView(formError);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setLayout(width, height);
            }
        }
    }

    @Override
    public void onClick(View v) {
        JsonFormErrorFragment.this.dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onFieldsInvalid = (OnFieldsInvalid) getActivity();
        } catch (ClassCastException ex) {
            throw new ClassCastException("Error retrieving passed invalid fields");
        }
    }

    protected void handleFormErrors() {
        Map<String, ValidationStatus> invalidFields = onFieldsInvalid.getPassedInvalidFields();
        formError.setInvalidFields(invalidFields);
    }

    protected void setupErrorFragmentToolBar() {
        dialogToolbar.setNavigationIcon(R.drawable.ic_action_close);
        dialogToolbar.setNavigationOnClickListener(this);
        dialogToolbar.setTitle(R.string.attention);
        dialogToolbar.setTitleTextColor(getContext().getResources().getColor(R.color.white));
        dialogToolbar.setBackgroundColor(getContext().getResources().getColor(R.color.error_color));
    }

}
