package com.example.mike.geoadsmobileapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.provider.Settings.Secure; // for ANDROID_ID

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener  {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final int APP_ACCESS_FINE_LOCATION_CODE = 100;
    private final String SERVER_URL = "http://209.2.224.152:8081/location";
    String android_id;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final TextView mTextView = (TextView) findViewById(R.id.text);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);
        String url = "http://www.google.com";

        // Get unique 64-bit hex string of android device
        android_id = Secure.getString(getApplicationContext().getContentResolver(),
                Secure.ANDROID_ID);
        System.out.println("Android id is: " + android_id);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == -1) {
            System.out.println("NO permission for ACCESS_FINE_LOCATION, requesting...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    APP_ACCESS_FINE_LOCATION_CODE);
        }


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds
                .setFastestInterval(1 * 1000); // 1 second
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.mike.geoadsmobileapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.mike.geoadsmobileapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
    }

    private void handleNewLocation(Location location) {
        String latitude;
        String longitude;
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        System.out.println("Last location: lat=" + latitude + " lon=" + longitude);
        JSONObject locationJSON = new JSONObject();
        try {
            locationJSON.put("id", android_id);
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
        queue.add(locationJSONreq);
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("Location services connected");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            System.out.println("Location is null");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("onLocationChanged called");
        handleNewLocation(location);
        return;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        return;
    }

    @Override
    public void onConnectionSuspended(int arg) {
        System.out.println("Location services suspended");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient == null) {
            System.out.println("mGoogleApiClient is null!");
        }
        mGoogleApiClient.connect();
        System.out.println("mGoogleApiClient connected");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch(requestCode) {
            case APP_ACCESS_FINE_LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("ACCESS_FINE_LOCATION permission granted");
                }
                else {
                    System.out.println("ACCESS_FINE_LOCATION permission denied");
                }
            }
        }
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
        }

        return super.onOptionsItemSelected(item);
    }
}
