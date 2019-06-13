package com.tad.asannet.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tad.asannet.R;
import com.tad.asannet.ui.activities.MainActivity;
import com.tad.asannet.ui.activities.friends.FriendProfile;
import com.tad.asannet.ui.activities.notification.NotificationActivity;
import com.tad.asannet.ui.activities.notification.NotificationImage;
import com.tad.asannet.ui.activities.notification.NotificationImageReply;
import com.tad.asannet.ui.activities.notification.NotificationReplyActivity;
import com.tad.asannet.utils.database.NotificationsHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tad on 10/3/18.
 */

public class NotificationUtil {

    private static String TAG = NotificationUtil.class.getSimpleName();
    private static String TAGPush = "Push Notification";

    private Context mContext;
    public static boolean read=true;

    public NotificationUtil(Context mContext) {
        this.mContext = mContext;
    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void clearNotificationsById(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static long getTimeMilliSec(String timeStamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void showNotificationMessage(int id, String timeStamp, String click_action, String user_image, String title, String message, Intent intent) {
        showNotificationMessage(id, timeStamp, click_action, user_image, title, message, intent, null);
    }

    @RequiresApi(api =   Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Config.ADMIN_CHANNEL_ID, Config.ADMIN_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(Config.ADMIN_CHANNEL_DESCPT);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.BLUE);
        adminChannel.canShowBadge();
        adminChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public void showNotificationMessage(int id, String timeStamp, String click_action, final String user_image, final String title, final String message, Intent intent, String imageUrl) {

        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        // notification icon
        final int icon = R.mipmap.ic_launcher;

        int requestID = (int) System.currentTimeMillis();

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        requestID,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT //PendingIntent.FLAG_CANCEL_CURRENT
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext, Config.ADMIN_CHANNEL_ID);


        try {
            if (!TextUtils.isEmpty(imageUrl)) {

                Bitmap bitmap = getBitmapFromURL(imageUrl);

                if (bitmap != null) {
                    showBigNotification(id, timeStamp, click_action, user_image, bitmap, mBuilder, icon, title, message, resultPendingIntent);
                } else {
                    showSmallNotification(id, timeStamp, click_action, user_image, mBuilder, icon, title, message, resultPendingIntent);
                }

            } else {
                showSmallNotification(id, timeStamp, click_action, user_image, mBuilder, icon, title, message, resultPendingIntent);
            }
        } catch (Exception e) {
            Log.e("showNotificationMessage", e.getMessage() == null ? "" : e.getMessage());
        }
    }

    public Intent getIntent(String click_action) {

        Intent resultIntent;

        switch (click_action) {
            case "com.tad.asannet.TARGETNOTIFICATION":
                resultIntent = new Intent(mContext, NotificationActivity.class);
                break;
            case "com.tad.asannet.TARGETNOTIFICATIONREPLY":
                resultIntent = new Intent(mContext, NotificationReplyActivity.class);
                break;
            case "com.tad.asannet.TARGETNOTIFICATION_IMAGE":
                resultIntent = new Intent(mContext, NotificationImage.class);
                break;
            case "com.tad.asannet.TARGETNOTIFICATIONREPLY_IMAGE":
                resultIntent = new Intent(mContext, NotificationImageReply.class);
                break;
            case "com.tad.asannet.TARGET_FRIENDREQUEST":
                resultIntent = new Intent(mContext, FriendProfile.class);
                break;
            case "com.tad.asannet.TARGET_ACCEPTED":
                resultIntent = new Intent(mContext, FriendProfile.class);
                break;
            case "com.tad.asannet.TARGET_LIKE":
                resultIntent = new Intent(mContext, MainActivity.class).putExtra("openFragment","forLike");
                break;
            case "com.tad.asannet.TARGET_COMMENT":
                resultIntent = new Intent(mContext, MainActivity.class).putExtra("openFragment","forComment");
                break;
            default:
                resultIntent = new Intent(mContext, MainActivity.class);
                break;
        }
        return resultIntent;

    }

    private void showSmallNotification(int id, String timeStamp, String click_action, String user_image, NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent) {

        Log.d(TAGPush, "showSmallNotification Push Notification. Click_action: " + click_action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            setupChannels(notificationManager);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);

        Notification notification;

        notification = mBuilder
                .setAutoCancel(true)
                .setContentTitle(title)
                .setTicker(title)
                .setContentIntent(resultPendingIntent)
                .setColorized(true)
                .setWhen(getTimeMilliSec(timeStamp))
                .setShowWhen(true)
                .setSound(Uri.parse("android.resource://"+mContext.getPackageName()+"/"+R.raw.asannet_sound))
                .setColor(Color.parseColor("#2591FC"))
                .setStyle(bigTextStyle)
                .setLargeIcon(getCircularBitmap(getBitmapFromURL(user_image)))
                .setSmallIcon(icon)
                .setContentText(message)
                .build();

        NotificationsHelper notificationsHelper=new NotificationsHelper(mContext);
        notificationsHelper.insertContact(user_image,title,message,timeStamp);
        read=false;
        notificationsHelper.close();

        notificationManagerCompat.notify(id, notification);
    }

    private void showBigNotification(int id, String timeStamp, String click_action, String user_image, Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title, String message, PendingIntent resultPendingIntent) {

        Log.d(TAGPush, "showBigNotification Push Notification. Click_action: " + click_action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            setupChannels(notificationManager);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);
        Notification notification;

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.bigPicture(bitmap);

        notification = mBuilder
                .setAutoCancel(true)
                .setContentTitle(title)
                .setTicker(title)
                .setContentIntent(resultPendingIntent)
                .setColorized(true)
                .setShowWhen(true)
                .setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.asannet_sound))
                .setWhen(getTimeMilliSec(timeStamp))
                .setColor(Color.parseColor("#2591FC"))
                .setStyle(bigPictureStyle) //bigPictureStyle
                .setLargeIcon(getCircularBitmap(getBitmapFromURL(user_image)))
                .setSmallIcon(icon)
                .setContentText(message)
                .build();

        NotificationsHelper notificationsHelper=new NotificationsHelper(mContext);
        notificationsHelper.insertContact(user_image,title,message,timeStamp);
        read=false;
        notificationsHelper.close();

        notificationManagerCompat.notify(id, notification);
    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap){
        final Bitmap output=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        final Canvas canvas=new Canvas(output);
        final int color=Color.RED;
        final Paint paint=new Paint();
        final Rect rect=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        final RectF rectF=new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);
        canvas.drawOval(rectF,paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        bitmap.recycle();
        return output;

    }

}