package com.vijay.jsonwizard.customviews;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.rey.material.widget.TextView;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.widgets.GpsFactory;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/24/17.
 */
public class GpsDialog extends Dialog implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final double MIN_ACCURACY = 4d;
    private final View dataView;
    private final TextView latitudeTV, longitudeTV, altitudeTV, accuracyTV;
    private final Context context;
    private TextView dialogAccuracyTV;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    public GpsDialog(Context context, View dataView, TextView latitudeTV, TextView longitudeTV, TextView altitudeTV, TextView accuracyTV) {
        super(context);
        this.context = context;
        this.dataView = dataView;
        this.latitudeTV = latitudeTV;
        this.longitudeTV = longitudeTV;
        this.altitudeTV = altitudeTV;
        this.accuracyTV = accuracyTV;
        init();
    }

    protected void init() {
        this.setContentView(R.layout.dialog_gps);
        setTitle(R.string.loading_location);
        this.setCancelable(false);
        this.lastLocation = null;
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                disconnectGoogleApiClient();
            }
        });
        Button okButton = (Button) this.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndDismiss();
            }
        });
        Button cancelButton = (Button) this.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GpsDialog.this.dismiss();
            }
        });

        this.dialogAccuracyTV = (TextView) this.findViewById(R.id.accuracy);

        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                initGoogleApiClient();
            }
        });
    }

    protected void saveAndDismiss() {
        updateLocationViews(lastLocation);
        GpsDialog.this.dismiss();
    }

    protected void initGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        googleApiClient.connect();
    }

    private void disconnectGoogleApiClient() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    private void updateLocationViews(Location location) {
        if (location != null) {
            location.getProvider();

            latitudeTV.setText(String.format(context.getString(R.string.latitude), String.valueOf(location.getLatitude())));
            longitudeTV.setText(String.format(context.getString(R.string.longitude), String.valueOf(location.getLongitude())));
            altitudeTV.setText(String.format(context.getString(R.string.altitude), String.valueOf(location.getAltitude()) + " m"));
            accuracyTV.setText(String.format(context.getString(R.string.accuracy), String.valueOf(location.getAccuracy()) + " m"));
            dataView.setTag(R.id.raw_value, GpsFactory.constructString(location));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Do nothing when the connection is suspended - This is bad and probably needs a review
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        this.dismiss();
        Toast.makeText(context, R.string.could_not_get_your_location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            dialogAccuracyTV.setText(String.format(context.getString(R.string.accuracy), String.valueOf(location.getAccuracy()) + " m"));
        }

        lastLocation = location;
        if (lastLocation != null && lastLocation.getAccuracy() <= MIN_ACCURACY) {
            saveAndDismiss();
        }
    }

    public View getDataView() {
        return dataView;
    }

    public TextView getLatitudeTV() {
        return latitudeTV;
    }

    public TextView getLongitudeTV() {
        return longitudeTV;
    }

    public TextView getAltitudeTV() {
        return altitudeTV;
    }

    public TextView getAccuracyTV() {
        return accuracyTV;
    }

    public TextView getDialogAccuracyTV() {
        return dialogAccuracyTV;
    }

    public void setDialogAccuracyTV(TextView dialogAccuracyTV) {
        this.dialogAccuracyTV = dialogAccuracyTV;
    }
}
