package com.fredrick.forapp.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.fredrick.forapp.PostCommentActivity;
import com.fredrick.forapp.R;
import com.fredrick.forapp.ui.MessagingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String saveCurrentUser = sp.getString("Current_USERID", "None");

        String notificationType = remoteMessage.getData().get("notificationType");
        assert notificationType != null;
        if (notificationType.equals("PostNotification")){
           // post notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescr = remoteMessage.getData().get("pDescr");

            //The same user dont show post notification
            assert sender != null;
            if (!sender.equals(saveCurrentUser)){
                showPostNotification(""+pId, ""+pTitle, ""+pDescr);
            }

        }else if (notificationType.equals("ChatNotification")){
           // chat notification
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            assert sent != null;
            if (firebaseUser != null && sent.equals(firebaseUser.getUid())){
                assert saveCurrentUser != null;
                if (!saveCurrentUser.equals(user)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendAboveAndroidNotification(remoteMessage);
                    } else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }



    }

    private void showPostNotification(String pId, String pTitle, String pDescr) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(300);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupPostNotificationChannel(notificationManager);
        }
        //show post detail activity using post id when notification is clicked
        Intent intent = new Intent(this, PostCommentActivity.class);
        intent.putExtra("postId", pId);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // largeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_iconics);

        //sound for notification
        Uri  notificationSoundUri =   RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ""+ADMIN_CHANNEL_ID)
                     .setSmallIcon(R.drawable.ic_iconics)
                     .setLargeIcon(largeIcon)
                     .setContentTitle(pTitle)
                     .setContentText(pDescr)
                     .setSound(notificationSoundUri)
                     .setContentIntent(pendingIntent);

        // show notification
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {
            CharSequence  channelName = "New Notification";
            String channelDescription = "Device to device post notification";
        NotificationChannel  adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.enableVibration(true);
        adminChannel.setLightColor(Color.GREEN);
        if (notificationManager !=null){
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        assert user != null;
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessagingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUids", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        assert icon != null;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                          .setSmallIcon(Integer.parseInt(icon))
                          .setContentText(body)
                          .setContentTitle(title)
                          .setAutoCancel(true)
                          .setSound(defSoundUri)
                          .setContentIntent(pIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0 ;
        if (i > 0){
            j=i;
        }
        notificationManager.notify(j, builder.build());

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void sendAboveAndroidNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        assert user != null;
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessagingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUids", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Oreo_AboveAndroidNotification  notification1 = new Oreo_AboveAndroidNotification(this);
        Notification.Builder builder = notification1.getONotification(title, body, pIntent, defSoundUri, icon);

        int j = 0 ;
        if (i >0){
            j=i;
        }
        notification1.getManager().notify(j, builder.build());

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        // update user token
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();
        if (user!=null){
            // signed in. update token
            updateTokens(tokenRefresh);
        }
    }

    private void updateTokens(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        assert user != null;
        ref.child(user.getUid()).setValue(token);
    }
}
