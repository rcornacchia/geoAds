package com.example.mike.geoadsmobileapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private final String SERVER_URL = "https://search-adbrother-2mnwlo4oaulpldztks3rg362i4.us-east-1.es.amazonaws.com/adbrother/userData";
    private RequestQueue mVolleyQueue;
//    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // "438199216304" is our project number on Google cloud platform
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken("438199216304",
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);

            sharedPreferences.edit().putBoolean("sentTokenToServer", true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean("sentTokenToServer", false).apply();
        }
    }

    /**
     * Send registration token to server
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        System.out.println("Sending registration token to server.");
        // Add custom implementation, as needed.
        // Get unique 64-bit hex string of android device
        String AndroidId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String registrationUrl = SERVER_URL + '/' + AndroidId + "/_update";
        mVolleyQueue = Volley.newRequestQueue(this);

        // Build elasticSearch request JSON
        JSONObject elasticSearchUpsertJSON = new JSONObject();
        try {
            elasticSearchUpsertJSON.put("doc_as_upsert", true);
            JSONObject doc = new JSONObject();
            doc.put("gcm", token);
            elasticSearchUpsertJSON.put("doc", doc);
        }
        catch (JSONException e) {
            System.out.println("Unable to create elasticSearchUpsertJSON");
            return;
        }

        // Build HTTP request around JSON object
        JsonObjectRequest registrationRequest = new JsonObjectRequest(
            Request.Method.POST,
            registrationUrl,
            elasticSearchUpsertJSON,
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
            }
        );
        mVolleyQueue.add(registrationRequest);
    }


}