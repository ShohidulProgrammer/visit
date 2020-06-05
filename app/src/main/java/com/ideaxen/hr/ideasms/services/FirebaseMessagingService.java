package com.ideaxen.hr.ideasms.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.ideaxen.hr.ideasms.MyApprovalActivity;
import com.ideaxen.hr.ideasms.MyVisitsActivity;
import com.ideaxen.hr.ideasms.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

       private static final String TAG = "FCM";
    public static int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification: " + remoteMessage.getNotification().getBody());
        }

        String title, body, visitType, visitId, visitPriority;

        title = remoteMessage.getData().get("title");
        title = (title == null ? "" : title);

        body = remoteMessage.getData().get("body");
        body = (body == null ? "" : body);

        visitType = remoteMessage.getData().get("visitType");
        visitType = (visitType == null ? "" : visitType);

        visitId = remoteMessage.getData().get("visitId");
        visitId = (visitId == null ? "" : visitId);

        visitPriority = remoteMessage.getData().get("visitPriority");
        visitPriority = (visitPriority == null ? "" : visitPriority);

        if(title.equals("Visit Request Approved") || title.equals("Visit Request Declined")){
            sendNotification(title, body);
        }
        else{
            sendNotification(title, body, visitType, visitId, visitPriority);
        }
        Log.d(TAG, "onMessageReceived: " + title + ", " + body  + ", " + visitType + ", " + visitId);
    }

    public void sendNotification(String messageTitle, String messageBody){
        Intent visitIntent = new Intent(this, MyVisitsActivity.class);
        visitIntent.putExtra("FILTER_MENU","5");
        visitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent visitPendingIntent = PendingIntent.getActivity(
                this, 3, visitIntent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        initChannels(this);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"dekko")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setContentIntent(visitPendingIntent)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID , notificationBuilder.build());
            NOTIFICATION_ID++;
        }
    }

    public void sendNotification(String messageTitle, String messageBody, String visitType, String visitId, String visitPriority) {

        PendingIntent approvePendingIntent = setPendingIntent(
                "APPROVE", visitType, visitId, visitPriority,0);
        PendingIntent declinePendingIntent = setPendingIntent(
                "DECLINE", visitType, visitId, visitPriority,1);
        PendingIntent detailPendingIntent = setPendingIntent(
                "DETAIL", visitType, visitId, visitPriority,2);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        initChannels(this);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"dekko")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setOngoing(false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setContentIntent(detailPendingIntent)
                .addAction(R.drawable.ic_approve,"Approve",approvePendingIntent)
                .addAction(R.drawable.ic_decline,"Decline",declinePendingIntent)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID , notificationBuilder.build());
            NOTIFICATION_ID++;
        }
    }

    private PendingIntent setPendingIntent(String action, String visitType, String visitId, String visitPriority, int requestCode){
        Intent intent = new Intent(this, MyApprovalActivity.class);
        intent.putExtra("FROM_FCM", "FCM");
        intent.putExtra("ACTION", action);
        intent.putExtra("VISIT_TYPE", visitType);
        intent.putExtra("VISIT_ID", visitId);
        intent.putExtra("VISIT_PRIORITY", visitPriority);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("dekko",
                "Dekko Visit",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Dekko Instant Visit App");
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
