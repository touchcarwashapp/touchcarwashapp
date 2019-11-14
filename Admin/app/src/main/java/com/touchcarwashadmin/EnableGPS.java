package com.touchcarwashadmin;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest.Builder;
import com.google.android.gms.location.LocationSettingsResult;

public class EnableGPS implements ConnectionCallbacks, OnConnectionFailedListener {
    public static final int REQUEST_LOCATION = 1;
    public Activity activity;
    Context context;
    GoogleApiClient googleApiClient;
    LocationManager locationManager;
    LocationRequest locationRequest;
    Builder locationSettingsRequest;
    PendingResult<LocationSettingsResult> pendingResult;

    EnableGPS(Activity activity2, Context context2) {
        activity = activity2;
        context = context2;
    }

    public void mEnableGps() {
        googleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        mLocationSetting();
    }

    public void mLocationSetting() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(100);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationSettingsRequest = new Builder().addLocationRequest(locationRequest);
        mResult();
    }
    

    public void mResult() {
        pendingResult = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest.build());
        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                int statusCode = status.getStatusCode();
                if (statusCode != 0 && statusCode == 6) {
                    try {
                        status.startResolutionForResult(activity, 1);
                    } catch (SendIntentException e) {
                    }
                }
            }
        });
    }

    public void onConnected(@Nullable Bundle bundle) {
    }

    public void onConnectionSuspended(int i) {
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
