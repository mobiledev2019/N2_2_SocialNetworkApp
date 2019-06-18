package com.tad.asannet.ui.activities.account;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.tad.asannet.R;
import com.tad.asannet.utils.Config;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Locale;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class UpdateAvailable extends AppCompatActivity {

    private Button button;
    private TextView textview;
    private String link,version,improvements;
    ArrayList<Long> list = new ArrayList<>();
    private long refid;
    public BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            list.remove(referenceId);
            if (list.isEmpty()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setupChannels(notificationManager);
                }
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        UpdateAvailable.this, Config.ADMIN_CHANNEL_ID);

                android.app.Notification notification;
                notification = mBuilder
                        .setAutoCancel(true)
                        .setContentTitle("Download success")
                        .setColorized(true)
                        .setSound(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.asannet_sound))
                        .setColor(Color.parseColor("#2591FC"))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText("File downloaded in /Downloads/asannet Updates/asannet_v" + version+".apk")
                        .build();

                notificationManager.notify(0, notification);
                Toast.makeText(ctxt, "File downloaded in /Downloads/asannet Updates/asannet_v" + version+".apk", Toast.LENGTH_LONG).show();
            }
        }

    };
    private DownloadManager downloadManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_update_available);

        textview=findViewById(R.id.textView);
        button=findViewById(R.id.button);

        link=getIntent().getStringExtra("link");
        version=getIntent().getStringExtra("version");
        improvements=getIntent().getStringExtra("improvements");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        button.setText(String.format(Locale.ENGLISH,"Download v%s", version));
        textview.setText(improvements);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onDownloadClick(View view) {

        Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(isOnline()) {

                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                            request.setAllowedOverRoaming(true);
                            request.setTitle("asannet");
                            request.setDescription("Downloading ...");
                            request.setVisibleInDownloadsUi(true);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/asannet Updates/"+ "asannet_v" + version + ".apk");

                            Toast.makeText(UpdateAvailable.this, "Downloading...", Toast.LENGTH_SHORT).show();

                            refid = downloadManager.enqueue(request);
                            list.add(refid);

                        }else{
                            Toast.makeText(UpdateAvailable.this, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            DialogOnDeniedPermissionListener.Builder
                                    .withContext(UpdateAvailable.this)
                                    .withTitle("Storage permission")
                                    .withMessage("Storage permission is needed for downloading update.")
                                    .withButtonText(android.R.string.ok)
                                    .withIcon(R.mipmap.logo_asannet)
                                    .build();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "Downloads";
        String adminChannelDescription = "Used to show the progress of downloads";
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Config.ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_DEFAULT);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

}
