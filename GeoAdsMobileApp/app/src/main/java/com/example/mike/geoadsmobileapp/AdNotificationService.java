package com.example.mike.geoadsmobileapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;


public class AdNotificationService extends IntentService {

    private final static NotificationID mNID = new NotificationID();
    private final String SERVER_URL = "http://209.2.219.212:8000";

    public AdNotificationService() {
        super("AdNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            String adId = extras.getString("id");
            System.out.println("adId received from gcm: " + adId);
            String adTitle = extras.getString("title");
            String adMsg = extras.getString("msg");
            String adUrl = extras.getString("url");
            sendNotification(adId, adTitle, adMsg, adUrl);
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String adId, String adTitle, String adMsg, String adUrl) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_community)
                        .setContentTitle(adTitle)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(adMsg))
                        .setContentText(adMsg);

        // Set up contentIntent for when the user clicks the ad
        String androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        adUrl = SERVER_URL + "/redirect?adId=" + adId + "&androidId=" + androidId + "&link=" + adUrl;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(adUrl));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                browserIntent, 0);

        // Set up deleteIntent for when the user rejects the ad
        Intent notifyDeletionIntent = new Intent(this, AdRejectionService.class);
        notifyDeletionIntent.putExtra("adId", adId);
        notifyDeletionIntent.setAction("notification_cancelled");
        PendingIntent deleteIntent = PendingIntent.getBroadcast(this, 0,
                notifyDeletionIntent, 0);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDeleteIntent(deleteIntent);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(mNID.getID(), mBuilder.build());
    }
}
