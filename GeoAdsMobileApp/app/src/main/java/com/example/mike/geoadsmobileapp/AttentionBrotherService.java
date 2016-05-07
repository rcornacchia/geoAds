package com.example.mike.geoadsmobileapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AttentionBrotherService extends Service {

    private String mAndroidId;
    private RequestQueue mVolleyQueue;
    private String ELASTICSEARCH_URL = "https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/userData/";
    /*
       TODO: Implement this class with various hardware listeners
       Aggregate all factors we have to decide whether the user is paying attention
       * Screen lock ( Lot of weight )
       * Major apps in the foreground
       * Time since last activity ?
       *
       * Use a broadcast receiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */

    /*
        For screen lock broadcast receiver I followed this tutorial:
        https://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
     */
    private final BroadcastReceiver screenOnOffReceiver = new BroadcastReceiver() {
        boolean screenOn = true;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                screenOn = true;
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                screenOn = false;
            }
            Intent i = new Intent(context, AttentionBrotherService.class);
            i.putExtra("screenState", screenOn);
            context.startService(i);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Get set up to receive screen on/off intents
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenOnOffReceiver, filter);

        super.onCreate();
        // Instantiate the RequestQueue.
        mVolleyQueue = Volley.newRequestQueue(this);

        // Get unique 64-bit hex string of android device
        mAndroidId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        System.out.println("AttentionBrother: Android id is: " + mAndroidId);
        ELASTICSEARCH_URL += mAndroidId + "/_update";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean screenOn = intent.getBooleanExtra("screenState", true);
        String state = "off";
        if(screenOn) {
            System.out.println("AttentionBrotherService received screenOn = true");
            state = "on";
        }
        else {
            System.out.println("AttentionBrotherService received screenOff = false");
            state = "off";
        }

        JSONObject docJSON = new JSONObject();
        try {
            docJSON.put("state", state);
        }
        catch (JSONException e) {
            System.out.println("Unable to create docJSON");
            return super.onStartCommand(intent, flags, startId);
        }

        JSONObject elasticSearchParams = new JSONObject();
        try {
            elasticSearchParams.put("doc_as_upsert", true);
            elasticSearchParams.put("doc", docJSON);
        }
        catch (JSONException e) {
            System.out.println("Unable to create elasticSearchParams");
            return super.onStartCommand(intent, flags, startId);
        }

        System.out.println("AttentionBrother request:");
        System.out.println("elasticSearchParams: " + elasticSearchParams.toString());
        System.out.println("elasticSearcUrl: " + ELASTICSEARCH_URL);

        JsonObjectRequest elasticSearchJSONreq =
                new JsonObjectRequest(Request.Method.POST,
                        ELASTICSEARCH_URL,
                        elasticSearchParams,
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

        mVolleyQueue.add(elasticSearchJSONreq);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
