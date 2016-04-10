package com.example.mike.geoadsmobileapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AttentionBrotherService extends Service {
    /*
       TODO: Implement this class with various hardware listeners
       Aggregate all factors we have to decide whether the user is paying attention
       * Screen lock ( Lot of weight )
       * Major apps in the foreground
       * Time since last activity ?
       *
       * Use a broadcast receiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
