package com.tad.asannet.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.tad.asannet.R;
import com.tad.asannet.ui.activities.account.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if(FirebaseAuth.getInstance().getCurrentUser()==null){
                    startActivity(new Intent(Splash.this, LoginActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(Splash.this, MainActivity.class));
                    finish();
                }

            }
        },1200);

    }
}
