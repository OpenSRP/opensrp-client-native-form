package com.vijay.jsonwizard.mvp;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by vijay on 4/21/15.
 */
public abstract class BaseActivity<V extends ViewState> extends AppCompatActivity {
    // @Icicle
    protected V mViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Icepick.restoreInstanceState(this, savedInstanceState);
        if (savedInstanceState == null) {
            mViewState = createViewState();
            mViewState.setSavedInstance(false);
        } else {
            mViewState.setSavedInstance(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        super.addContentView(view, params);
    }

    protected abstract V createViewState();

    public V getViewState() {
        return mViewState;
    }
}
