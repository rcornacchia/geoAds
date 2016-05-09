package com.example.mike.geoadsmobileapp;

import android.app.ActionBar;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AttentionBrotherService extends Service implements View.OnTouchListener {

    private String mAndroidId;
    private RequestQueue mVolleyQueue;
    private String ELASTICSEARCH_URL = "https://search-adbrother-omlt2jw6gse2qvjzhcppf5myka.us-east-1.es.amazonaws.com/adbrother/userData/";
    private LinearLayout touchLayout;
    private WindowManager mWindowManager;
    private CountDownTimer sinceLastTouchTimer;
    private final int TOUCH_TIMEOUT = 5000; // touchscreen activity on/off timeout, in ms
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

        /*
            set up layouts to recognize touches
            create a dummy window that's 1px x 1px and catch all activity OUTSIDE of it
            referencing: http://www.kpbird.com/2013/03/android-detect-global-touch-event.html
         */
        touchLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(1, 1);
        touchLayout.setBackgroundColor(Color.CYAN); //diagnostic to see how big the fake layout is
        touchLayout.setLayoutParams(lp);
        touchLayout.setOnTouchListener(this);

        // create dummy window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                1, // width is 1 px
                1, // height is 1 px
                WindowManager.LayoutParams.TYPE_PHONE, // Type Phone, These are non-application windows providing user interaction with the phone (in particular incoming calls).
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,// this window won't ever get key input focus, watch outside touches
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowManager.addView(touchLayout, mParams);

        // create the global timer
        sinceLastTouchTimer = new CountDownTimer(TOUCH_TIMEOUT, TOUCH_TIMEOUT) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                sendState(false);
            }
        };
    } // end onCreate()

    // function to send the state of the device to elasticsearch
    public void sendState(boolean receivedState) {
        String state;
        if (receivedState) {
            state = "on";
        }
        else {
            state = "off";
        }

        JSONObject docJSON = new JSONObject();
        try {
            docJSON.put("state", state);
        }
        catch (JSONException e) {
            System.out.println("Unable to create docJSON");
            return;
        }

        JSONObject elasticSearchParams = new JSONObject();
        try {
            elasticSearchParams.put("doc_as_upsert", true);
            elasticSearchParams.put("doc", docJSON);
        }
        catch (JSONException e) {
            System.out.println("Unable to create elasticSearchParams");
            return;
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
    } // end sendState()

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean screenOn = intent.getBooleanExtra("screenState", true);
        String state;
        if(screenOn) {
            System.out.println("AttentionBrotherService received screenOn = true");
            sendState(true);
        }
        else {
            System.out.println("AttentionBrotherService received screenOff = false");
            sendState(false);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            System.out.println("Screen touched");
            sendState(true);
            sinceLastTouchTimer.start();
        }

        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
