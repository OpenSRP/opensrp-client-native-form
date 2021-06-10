package com.vijay.jsonwizard.interfaces;

import androidx.annotation.Nullable;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 21-05-2020.
 */
public interface OnFormFetchedCallback<T> {

    void onFormFetched(@Nullable T form);
}
