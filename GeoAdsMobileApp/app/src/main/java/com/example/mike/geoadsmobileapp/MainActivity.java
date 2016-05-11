package com.example.mike.geoadsmobileapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class MainActivity extends AppCompatActivity {
    private final int APP_ACCESS_FINE_LOCATION_CODE = 100;
//    private static final String TAG = "MainActivity";
//    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private final int OVERLAY_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView mTextView = (TextView) findViewById(R.id.text);

        // Make sure we have APP_ACCESS_FINE_LOCATION permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == -1) {
            System.out.println("NO permission for ACCESS_FINE_LOCATION, requesting...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    APP_ACCESS_FINE_LOCATION_CODE);
        }

        Intent wakeLocationBrother = new Intent(this, LocationBrotherService.class);
        startService(wakeLocationBrother);

        Intent wakeAttentionBrother = new Intent(this, AttentionBrotherService.class);
        startService(wakeAttentionBrother);

        /* We need to register the device and app to Google Cloud Messaging
        *  Steps:
        *  Check if Google Play Services are available
        *  Check if we are already registered
        *  Asynchronously register to GCM and store regID in shared preferences
        *  https://www.pubnub.com/blog/2015-06-24-sending-receiving-android-push-notifications-with-gcm-google-cloud-messaging/
        *  has instructions but apparently using AsyncTask() is a deprecated design pattern
        *  Adapt for a background service, register to GCM and send regID + ANDROID_ID to the webserver
        */
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean("sentTokenToServer", false);
                if (sentToken) {
                    System.out.println("Token already sent!");
                } else {
                    System.out.println("We haven't sent the token yet.");
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST_CODE) {
            Intent wakeAttentionBrother = new Intent(this, AttentionBrotherService.class);
            startService(wakeAttentionBrother);
        }
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter("registrationComplete"));
            isReceiverRegistered = true;
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
