package com.example.mike.geoadsmobileapp;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AdRejectionService extends BroadcastReceiver {

    private final String SERVER_URL = "http://209.2.219.212:8000";

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Ad rejected - intent.extras: " + intent.getExtras().toString());
        String adId = intent.getExtras().getString("adId");
        System.out.println("adId : " + adId);
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String rejectionUrl = SERVER_URL + "/rejectAd";
        RequestQueue volleyQueue = Volley.newRequestQueue(context);

        JSONObject rejectionParams = new JSONObject();
        try {
            rejectionParams.put("androidId", androidId);
            rejectionParams.put("adId", adId);
        } catch (JSONException e) {
            System.out.println("Unable to create rejection parameters for notification");
            return;
        }

        // Build HTTP request around JSON object
        JsonObjectRequest rejectionRequest = new JsonObjectRequest(
                Request.Method.POST,
                rejectionUrl,
                rejectionParams,
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

        volleyQueue.add(rejectionRequest);
    }
}


