package com.tad.asannet.receivers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.tad.asannet.ui.activities.account.UpdateAvailable;
import com.tad.asannet.ui.activities.MainActivity;
import com.tad.asannet.ui.activities.friends.FriendProfile;
import com.tad.asannet.ui.activities.notification.NotificationActivity;
import com.tad.asannet.ui.activities.notification.NotificationImage;
import com.tad.asannet.ui.activities.notification.NotificationImageReply;
import com.tad.asannet.ui.activities.notification.NotificationReplyActivity;
import com.tad.asannet.ui.activities.post.CommentsActivity;
import com.tad.asannet.ui.activities.post.SinglePostView;
import com.tad.asannet.utils.Config;
import com.tad.asannet.utils.NotificationUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by tad on 10/3/18.
 */

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = FCMService.class.getSimpleName();

    private NotificationUtil notificationUtils;


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("NEW_TOKEN",token);

        storeRegIdInPref(token);
        sendRegistrationToServer(token);

        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", token);

        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }


    private void sendRegistrationToServer(final String token) {
        Log.i(TAG, "sendRegistrationToServer:" + token);
    }

    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", token);
        editor.apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        handleDataMessage(remoteMessage);
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {


        //Map<String, String> Friend_tokens = remoteMessage.getData("friend_tokesn");
        final Map<String, String> data = remoteMessage.getData();

        final String title    = remoteMessage.getData().get("title");
        final String body     = remoteMessage.getData().get("body");
        String click_action   = remoteMessage.getData().get("click_action");
        String message        = remoteMessage.getData().get("message");
        String from_name      = remoteMessage.getData().get("from_name");
        String from_image     = remoteMessage.getData().get("from_image");
        String from_id        = remoteMessage.getData().get("from_id");
        final String imageUrl = remoteMessage.getData().get("image");
        String reply_for      = remoteMessage.getData().get("reply_for");
        long id               = System.currentTimeMillis();
        String timeStamp      = String.valueOf(remoteMessage.getData().get("timestamp"));

        //Friend Request Message data
        String friend_id    = remoteMessage.getData().get("friend_id");
        String friend_name  = remoteMessage.getData().get("friend_name");
        String friend_email = remoteMessage.getData().get("friend_email");
        String friend_image = remoteMessage.getData().get("friend_image");

        //String friend_tokens = remoteMessage.getData().get("friend_tokens");

        //CommentData and Like Data
        String post_id   = remoteMessage.getData().get("post_id");
        String admin_id  = remoteMessage.getData().get("admin_id");
        String post_desc = remoteMessage.getData().get("post_desc");
        String channel   = remoteMessage.getData().get("channel");

        //UpdateData
        String version      = remoteMessage.getData().get("version");
        String improvements = remoteMessage.getData().get("improvements");
        String link         = remoteMessage.getData().get("link");

        String question_id=remoteMessage.getData().get("question_id");

        final Intent resultIntent;

        switch (click_action) {
            case "com.tad.asannet.TARGETNOTIFICATION":

                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    // do stuff that is allowed on main thread
                    Toast.makeText(getApplicationContext(),"New Message Ready",Toast.LENGTH_LONG).show(); // MainActivity.context
                    Log.d("onMessageReceived", "onMessageReceived: " + click_action);
                    //MainActivity.setChatUnreadCount(MainActivity.chatUnreadCount++);
                } else {
                    // do stuff that is allowed on background thread
                }

                resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);

                break;
            case "com.tad.asannet.TARGETNOTIFICATIONREPLY":

                resultIntent = new Intent(getApplicationContext(), NotificationReplyActivity.class);

                break;
            case "com.tad.asannet.TARGETNOTIFICATION_IMAGE":

                resultIntent = new Intent(getApplicationContext(), NotificationImage.class);

                break;
            case "com.tad.asannet.TARGETNOTIFICATIONREPLY_IMAGE":

                resultIntent = new Intent(getApplicationContext(), NotificationImageReply.class);

                break;
            case "com.tad.asannet.TARGET_FRIENDREQUEST":

                Log.d("TARGET_FRIENDREQUEST", "onMessageReceived: " + click_action);
                //MainActivity.setManageFriendsUnreadCount(MainActivity.manageFriendsUnreadCount++);
                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.tad.asannet.TARGET_ACCEPTED":

                Log.d("TARGET_ACCEPTED", "onMessageReceived: " + click_action);
                //MainActivity.setManageFriendsUnreadCount(MainActivity.manageFriendsUnreadCount++);
                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.tad.asannet.TARGET_DECLINED":

                Log.d("TARGET_DECLINED", "onMessageReceived: " + click_action);
                resultIntent = new Intent(getApplicationContext(), FriendProfile.class);

                break;
            case "com.tad.asannet.TARGET_COMMENT":

                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    // do stuff that is allowed on main thread
                    Toast.makeText(getApplicationContext(),"New Message Ready",Toast.LENGTH_LONG).show(); // MainActivity.context
                }
                else {
                    // do stuff that is allowed on background thread
                }

                //MainActivity.setDashboardUnreadCount(MainActivity.dashboardUnreadCount++);
                Log.d("TARGET_COMMENT", "onMessageReceived: " + click_action);
                //TODO B4 resultIntent = new Intent(getApplicationContext(), MainActivity.class).putExtra("openFragment","forComment"); // WHY EVEN DO THIS?
                resultIntent = new Intent(getApplicationContext(), CommentsActivity.class);

                break;
            case "com.tad.asannet.TARGET_UPDATE":

                resultIntent = new Intent(getApplicationContext(), UpdateAvailable.class);

                break;
            case "com.tad.asannet.TARGET_LIKE":

                Log.d("TARGET_LIKE", "onMessageReceived: " + click_action);
                //TODO B4 resultIntent = new Intent(getApplicationContext(), MainActivity.class).putExtra("openFragment","forLike"); // WHY EVEN DO THIS?
                resultIntent = new Intent(getApplicationContext(), SinglePostView.class);

                break;

            default:

                resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                break;
        }

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", from_name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("notification_id", id);
        resultIntent.putExtra("timestamp", timeStamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", imageUrl);
        resultIntent.putExtra("reply_image", from_image);

        resultIntent.putExtra("f_id", friend_id);
        resultIntent.putExtra("f_name", friend_name);
        resultIntent.putExtra("f_email", friend_email);
        resultIntent.putExtra("f_image", friend_image);
        //resultIntent.putExtra("f_token", friend_token);

        resultIntent.putExtra("user_id", admin_id);
        resultIntent.putExtra("post_id", post_id);
        resultIntent.putExtra("post_desc", post_desc);

        resultIntent.putExtra("channel", channel);
        resultIntent.putExtra("version", version);
        resultIntent.putExtra("improvements", improvements);
        resultIntent.putExtra("link", link);

        resultIntent.putExtra("question_id",question_id);


        try {

            Log.d("FCM_LOGIC", "BEFORE BOOLEAN FOREGROUND: ");

            boolean foreground = new ForegroundCheckTask().execute(getApplicationContext()).get();

            Log.d("FCM_LOGIC", "FOREGROUND: " + foreground);

            if(!foreground){

                Log.d("FCM_LOGIC", "AFTER if(!foreground)");
                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {

                    Log.d("FCM_LOGIC", "AFTER if(TextUtils.isEmpty(imageUrl))");

                    if (!TextUtils.isEmpty(from_image)) {

                        Log.d("FCM_LOGIC", "showNotificationMessage AFTER if(!TextUtils.isEmpty(from_image)) ");
                        showNotificationMessage((int) id, timeStamp, click_action, from_image, getApplicationContext(), title, body, resultIntent);
                    } else {

                        Log.d("FCM_LOGIC", "showNotificationMessage AFTER if(!TextUtils.isEmpty(from_image)) ELSE ");
                        showNotificationMessage((int) id, timeStamp, click_action, friend_image, getApplicationContext(), title, body, resultIntent);
                    }

                } else {

                    Log.d("FCM_LOGIC", "AFTER if(TextUtils.isEmpty(imageUrl)) ELSE ");
                    // image is present, show notification with image
                    if (!TextUtils.isEmpty(from_image)) {

                        Log.d("FCM_LOGIC", "showNotificationMessageWithBigImage AFTER if(!TextUtils.isEmpty(from_image)) ");
                        showNotificationMessageWithBigImage((int) id, timeStamp, click_action, from_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                    } else {

                        Log.d("FCM_LOGIC", "showNotificationMessageWithBigImage AFTER if(!TextUtils.isEmpty(from_image)) ELSE ");
                        showNotificationMessageWithBigImage((int) id, timeStamp, click_action, friend_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                    }
                }

            }else{


                Log.d("FCM_LOGIC", "AFTER if(!foreground) ELSE");

                boolean active = getSharedPreferences("fcm_activity",MODE_PRIVATE).getBoolean("active",true);

                if(active) {

                    Log.d("FCM_LOGIC", "AFTER if(active) ");

                    Intent intent = new Intent(Config.PUSH_NOTIFICATION);

                    intent.putExtra("title", title);
                    intent.putExtra("body", body);
                    intent.putExtra("name", from_name);
                    intent.putExtra("from_image", from_image);
                    intent.putExtra("message", message);
                    intent.putExtra("from_id", from_id);
                    intent.putExtra("notification_id", id);
                    intent.putExtra("timestamp", timeStamp);
                    intent.putExtra("reply_for", reply_for);
                    intent.putExtra("image", imageUrl);
                    intent.putExtra("reply_image", from_image);

                    intent.putExtra("f_id", friend_id);
                    intent.putExtra("f_name", friend_name);
                    intent.putExtra("f_email", friend_email);
                    intent.putExtra("f_image", friend_image);
                    //intent.putExtra("f_token", friend_token);

                    intent.putExtra("user_id", admin_id);
                    intent.putExtra("post_id", post_id);
                    intent.putExtra("post_desc", post_desc);
                    intent.putExtra("click_action", click_action);

                    intent.putExtra("version", version);
                    intent.putExtra("improvements", improvements);
                    intent.putExtra("link", link);
                    intent.putExtra("channel",channel);
                    intent.putExtra("question_id",question_id);

                    if (title.toLowerCase().contains("update")) {

                        Log.d("FCM_LOGIC", "showNotificationMessage if(title.toLowerCase().contains(updatePer)) ");
                        showNotificationMessage((int) id, timeStamp, click_action, from_image, getApplicationContext(), title, body, resultIntent);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    } else {

                        Log.d("FCM_LOGIC", "showNotificationMessage if(title.toLowerCase().contains(updatePer)) ELSE ");
                        showNotificationMessage((int) id, timeStamp, click_action, from_image, getApplicationContext(), title, body, resultIntent);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }else{

                    Log.d("FCM_LOGIC", "AFTER if(active) ELSE");

                    if (TextUtils.isEmpty(imageUrl)) {

                        Log.d("FCM_LOGIC", "AFTER if(TextUtils.isEmpty(imageUrl)) ");

                        if (!TextUtils.isEmpty(from_image)) {

                            Log.d("FCM_LOGIC", "showNotificationMessage AFTER if(!TextUtils.isEmpty(from_image)) ");
                            showNotificationMessage((int) id, timeStamp, click_action, from_image, getApplicationContext(), title, body, resultIntent);
                        } else {

                            Log.d("FCM_LOGIC", "showNotificationMessage AFTER if(TextUtils.isEmpty(from_image)) ELSE ");
                            showNotificationMessage((int) id, timeStamp, click_action, friend_image, getApplicationContext(), title, body, resultIntent);
                        }

                    } else {

                        Log.d("FCM_LOGIC", "AFTER if(TextUtils.isEmpty(imageUrl)) ELSE ");
                        // image is present, show notification with image
                        if (!TextUtils.isEmpty(from_image)) {

                            Log.d("FCM_LOGIC", "showNotificationMessageWithBigImage AFTER if(!TextUtils.isEmpty(from_image)) ");
                            showNotificationMessageWithBigImage((int) id, timeStamp, click_action, from_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                        } else {

                            Log.d("FCM_LOGIC", "showNotificationMessageWithBigImage AFTER if(!TextUtils.isEmpty(from_image)) ELSE ");
                            showNotificationMessageWithBigImage((int) id, timeStamp, click_action, friend_image, getApplicationContext(), title, body, resultIntent, imageUrl);
                        }
                    }


                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



    }

    private void showNotificationMessage(int id, String timeStamp, String click_action, String user_image, Context context, String title, String message, Intent intent) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, click_action, user_image, title, message, intent, null);
    }

    private void showNotificationMessageWithBigImage(int id, String timeStamp, String click_action, String user_image, Context context, String title, String message, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(id, timeStamp, click_action, user_image, title, message, intent, imageUrl);
    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

}
