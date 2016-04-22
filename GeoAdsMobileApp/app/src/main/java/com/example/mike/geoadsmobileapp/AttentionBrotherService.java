package com.example.mike.geoadsmobileapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

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

    /*
        Temporary AWS access keys for development, replace this with Amazon Cognito Identity:
        http://docs.aws.amazon.com/mobile/sdkforandroid/developerguide/cognito-auth.html
     */
    private final String SQS_ACCESS_KEY_ID = "AKIAIM7MKGE5ZKGJYKPQ";
    private final String SQS_SECRET_ACCESS_KEY = "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";

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

        // set up AWS credentials for SQS
        AWSCredentials credentials = new BasicAWSCredentials( SQS_ACCESS_KEY_ID, SQS_SECRET_ACCESS_KEY );
        AmazonSQSClient sqsClient = new AmazonSQSClient( credentials );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean screenOn = intent.getBooleanExtra("screenState", true);
        if(screenOn) {
            System.out.println("AttentionBrotherService received screenOn = true");
        }
        else {
            System.out.println("AttentionBrotherService received screenOff = false");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
