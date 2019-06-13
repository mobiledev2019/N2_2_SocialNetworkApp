package com.tad.asannet.ui.activities.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.tad.asannet.R;
import com.tad.asannet.ui.activities.MainActivity;
import com.tad.asannet.utils.AnimationUtil;
import com.tad.asannet.utils.Config;
import com.tad.asannet.utils.database.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static Activity activity;
    private EditText email,password;
    private Button login,register;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private UserHelper userHelper;
    private ProgressDialog mDialog;

    public static void startActivityy(Context context) {
        Intent intent=new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }

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

        setContentView(R.layout.activity_login);

        activity = this;
        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();
        userHelper = new UserHelper(this);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(findViewById(R.id.layout), true);
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

    }


    public void performLogin(final boolean override) {

        final String email_, pass_;
        email_ = email.getText().toString();
        pass_ = password.getText().toString();

        if (!TextUtils.isEmpty(email_) && !TextUtils.isEmpty(pass_)) {
            mDialog.show();

            mAuth.signInWithEmailAndPassword(email_, pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        Log.i(TAG, "Đăng nhập thành công, tiếp tục xác minh email");

                        if (task.getResult().getUser().isEmailVerified()) {

                            Log.i(TAG, "Email được xác minh thành công, tiếp tục nhận mã thông báo");
                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> taskInstanceToken) {
                                    if (!taskInstanceToken.isSuccessful()) {
                                        Log.w(TAG, "getInstanceId thất bại", taskInstanceToken.getException());
                                        return;
                                    }

                                    // TODO Get new Instance ID token
                                    final String token_id = taskInstanceToken.getResult().getToken();

                                    Log.i(TAG, "Nhận mã thông báo, Token ID (token_id): " + token_id);

                                    final String current_id = task.getResult().getUser().getUid();


                                    mFirestore.collection("Users").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                // TODO How to update only one field that is list of string.
                                                //https://firebase.google.com/docs/firestore/manage-data/add-data#update-data

                                                final Map<String, Object> tokenMap = new HashMap<>();
                                                tokenMap.put("token_ids", FieldValue.arrayUnion(token_id));

                                                mFirestore.collection("Users")
                                                        .document(current_id)
                                                        .update(tokenMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                FirebaseFirestore.getInstance().collection("Users").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                                        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);
                                                                        SharedPreferences.Editor editor = pref.edit();
                                                                        editor.putString("regId", token_id);
                                                                        editor.apply();

                                                                        String username = documentSnapshot.getString("username");
                                                                        String name = documentSnapshot.getString("name");
                                                                        String email = documentSnapshot.getString("email");
                                                                        String image = documentSnapshot.getString("image");
                                                                        String password = pass_;
                                                                        String location = documentSnapshot.getString("location");
                                                                        String bio = documentSnapshot.getString("bio");

                                                                        userHelper.insertContact(username, name, email, image, password, location, bio);

                                                                        mDialog.dismiss();
                                                                        MainActivity.startActivity(LoginActivity.this);
                                                                        finish();

                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Lỗi", ".." + e.getMessage());
                                                                        mDialog.dismiss();
                                                                    }
                                                                });

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mDialog.dismiss();
                                                        Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                        }


                                    });

                                }

                            });


                        }

                        else{

                            mDialog.dismiss();
                            new DialogSheet(LoginActivity.this)
                                    .setTitle("Thông tin")
                                    .setCancelable(true)
                                    .setRoundedCorners(true)
                                    .setColoredNavigationBar(true)
                                    .setMessage("Email chưa được xác minh, vui lòng xác minh và tiếp tục.")
                                    .setPositiveButton("Gửi lại", v -> task.getResult()
                                            .getUser()
                                            .sendEmailVerification()
                                            .addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Log.e("Lỗi",e.getMessage())))
                                    .setNegativeButton("Ok", v -> {

                                    })
                                    .show();

                            if (mAuth.getCurrentUser() != null) {
                                mAuth.signOut();
                            }

                        }

                    } else {
                        if (task.getException().getMessage().contains("Mật khẩu không hợp lệ")) {
                            Toast.makeText(LoginActivity.this, "Lỗi: Mật khẩu bạn đã nhập không hợp lệ.", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        } else if (task.getException().getMessage().contains("Không có hồ sơ người dùng")) {
                            Toast.makeText(LoginActivity.this, "Lỗi: Người dùng không hợp lệ, Vui lòng đăng ký bằng nút bên dưới.", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        } else {
                            Toast.makeText(LoginActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }

                    }
                }
            });

        } else if (TextUtils.isEmpty(email_)) {

            AnimationUtil.shakeView(email, this);

        } else if (TextUtils.isEmpty(pass_)) {

            AnimationUtil.shakeView(password, this);

        } else {

            AnimationUtil.shakeView(email, this);
            AnimationUtil.shakeView(password, this);

        }

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void onLogin(View view) {
        performLogin(false);
    }

    public void onRegister(View view) {
        RegisterActivity.startActivity(this, this, findViewById(R.id.button));
    }

    public void onForgotPassword(View view) {

        if(TextUtils.isEmpty(email.getText().toString())) {
            Toast.makeText(activity, "Nhập email của bạn để gửi lại mật khẩu mail.", Toast.LENGTH_SHORT).show();
            AnimationUtil.shakeView(email, this);
        }else{

            mDialog.show();

            FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Đặt lại mật khẩu mail đã gửi", Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Lỗi gửi mail : "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}
