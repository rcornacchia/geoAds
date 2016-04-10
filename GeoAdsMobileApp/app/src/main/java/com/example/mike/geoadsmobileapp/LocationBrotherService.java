package com.example.mike.geoadsmobileapp;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationBrotherService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String mAndroidId;
    private RequestQueue mVolleyQueue;

    private final int APP_ACCESS_FINE_LOCATION_CODE = 100;
    private final String SERVER_URL = "http://209.2.224.152:8000/location";

    @Override
    public void onCreate() {
        super.onCreate();
        // Instantiate the RequestQueue.
        mVolleyQueue = Volley.newRequestQueue(this);

        // Get unique 64-bit hex string of android device
        mAndroidId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        System.out.println("Android id is: " + mAndroidId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("Location services connected");
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == -1) {
            System.out.println("permissionCheck failed.");
            return;
        }

        // TODO: Unfortunately, get the initial location does not work
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            System.out.println("It was null...");
//          location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("onLocationChanged called");
        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connecting failed");
    }

    @Override
    public void onConnectionSuspended(int arg) {
        System.out.println("Location services suspended");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            System.out.println("Building mGoogleApiClient");
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds
                .setFastestInterval(1000); // 1 second

        mGoogleApiClient.connect();

        return START_STICKY;
    }


    private void handleNewLocation(Location location) {
        String latitude;
        String longitude;
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        System.out.println("Last location: lat=" + latitude + " lon=" + longitude);
        JSONObject locationJSON = new JSONObject();
        try {
            locationJSON.put("id", mAndroidId);
            locationJSON.put("lat", latitude);
            locationJSON.put("lon", longitude);
        }
        catch (JSONException e) {
            System.out.println("Unable to create locationJSON");
            return;
        }
        JsonObjectRequest locationJSONreq =
                new JsonObjectRequest(Request.Method.POST,
                        SERVER_URL,
                        locationJSON,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                System.out.println("Successful JSON POST: " + response.toString());
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println("JSON POST Volley error: " + error.getMessage());
                            }
                        });
        mVolleyQueue.add(locationJSONreq);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We do not provide binding so we return null
        return null;
    }

    @Override
    public void onDestroy() {
        // Clean up after ourselves
        mGoogleApiClient.disconnect();
    }
}
