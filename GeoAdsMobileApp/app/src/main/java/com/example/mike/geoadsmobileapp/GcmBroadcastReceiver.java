package com.example.mike.geoadsmobileapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Code adapted from:
        // https://www.pubnub.com/blog/2015-06-24-sending-receiving-android-push-notifications-with-gcm-google-cloud-messaging/
        ComponentName comp = new ComponentName(context.getPackageName(),
                AdNotificationService.class.getName());

        startWakefulService(context, (intent.setComponent(comp)));
        // setResultCode(Activity.RESULT_OK); // Who would we be notifying?
    }
}
