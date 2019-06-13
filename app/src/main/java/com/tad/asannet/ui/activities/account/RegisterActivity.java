package com.tad.asannet.ui.activities.account;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.tad.asannet.utils.database.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE =100 ;
    private static final int PIC_CROP = 1;
    public Uri imageUri;
    public StorageReference storageReference;
    public ProgressDialog mDialog;
    public String name_, pass_, email_,username_,location_;
    private EditText name,email,password,location,username;
    private CircleImageView profile_image;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private static final String TAG = RegisterActivity.class.getSimpleName();

    public static void startActivity(Activity activity, Context context, View view) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }


    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.isAnyPermissionPermanentlyDenied()){
                            Toast.makeText(RegisterActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

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

        setContentView(R.layout.activity_register);

        askPermission();

        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("images");
        firebaseFirestore=FirebaseFirestore.getInstance();
        imageUri=null;
        UserHelper userHelper = new UserHelper(this);


        name=(EditText)findViewById(R.id.name);
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        location=(EditText)findViewById(R.id.location);
        username=(EditText)findViewById(R.id.username);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        Button register = findViewById(R.id.button);

        profile_image=findViewById(R.id.profile_image);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            fade.excludeTarget(findViewById(R.id.layout), true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(imageUri!=null){
                    username_=username.getText().toString();
                    name_=name.getText().toString();
                    email_=email.getText().toString();
                    pass_=password.getText().toString();
                    location_=location.getText().toString();

                    mDialog.show();

                    if (TextUtils.isEmpty(username_)) {

                        AnimationUtil.shakeView(username, RegisterActivity.this);
                        mDialog.dismiss();

                    }

                    if (TextUtils.isEmpty(name_)) {

                        AnimationUtil.shakeView(name, RegisterActivity.this);
                        mDialog.dismiss();

                    }
                    if (TextUtils.isEmpty(email_)) {

                        AnimationUtil.shakeView(email, RegisterActivity.this);
                        mDialog.dismiss();

                    }
                    if (TextUtils.isEmpty(pass_)) {

                        AnimationUtil.shakeView(password, RegisterActivity.this);
                        mDialog.dismiss();

                    }

                    if (TextUtils.isEmpty(location_)) {

                        AnimationUtil.shakeView(location, RegisterActivity.this);
                        mDialog.dismiss();

                    }

                    if (!TextUtils.isEmpty(name_) || !TextUtils.isEmpty(email_) ||
                            !TextUtils.isEmpty(pass_) || !TextUtils.isEmpty(username_) || !TextUtils.isEmpty(location_)) {

                        firebaseFirestore.collection("Usernames")
                                .document(username_)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(!documentSnapshot.exists()){
                                            registerUser();
                                        }else{
                                            Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                                            AnimationUtil.shakeView(username, RegisterActivity.this);
                                            mDialog.dismiss();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error",e.getMessage());
                                    }
                                });

                    }else{

                        AnimationUtil.shakeView(username, RegisterActivity.this);
                        AnimationUtil.shakeView(name, RegisterActivity.this);
                        AnimationUtil.shakeView(email, RegisterActivity.this);
                        AnimationUtil.shakeView(password, RegisterActivity.this);
                        AnimationUtil.shakeView(location, RegisterActivity.this);
                        mDialog.dismiss();

                    }

                }else{
                    AnimationUtil.shakeView(profile_image, RegisterActivity.this);
                    Toast.makeText(RegisterActivity.this, "We recommend you to set a profile picture", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }

            }
        });


    }

    private void registerUser() {

        mAuth.createUserWithEmailAndPassword(email_, pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Map<String,Object> usernameMap=new HashMap<String, Object>();
                    usernameMap.put("username",username_);

                    firebaseFirestore.collection("Usernames")
                            .document(username_)
                            .set(usernameMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    task.getResult()
                                            .getUser()
                                            .sendEmailVerification()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    final String userUid = task.getResult().getUser().getUid();
                                                    final StorageReference user_profile = storageReference.child(userUid + ".png");
                                                    user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                                            if (task.isSuccessful()) {

                                                                user_profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(final Uri uri) {

                                                                        // TODO https://firebase.google.com/docs/cloud-messaging/android/client#retrieve-the-current-registration-token.

                                                                        Map<String, Object> userMap = new HashMap<>();
                                                                        userMap.put("id", userUid);
                                                                        userMap.put("name", name_);
                                                                        userMap.put("image", uri.toString());
                                                                        userMap.put("email", email_);
                                                                        userMap.put("bio",getString(R.string.default_bio));
                                                                        userMap.put("username", username_);
                                                                        userMap.put("location", location_);

                                                                        firebaseFirestore.collection("Users").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                mDialog.dismiss();
                                                                                Toast.makeText(RegisterActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                                                                finish();

                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                mDialog.dismiss();
                                                                                Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });


                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        mDialog.dismiss();
                                                                    }
                                                                });


                                                            } else {
                                                                mDialog.dismiss();
                                                            }
                                                        }
                                                    });

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    task.getResult().getUser().delete();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error",e.getMessage());
                                }
                            });


                } else {
                    mDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();
                // start crop activity
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                options.setCompressionQuality(100);
                options.setShowCropGrid(true);

                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "sparks_user_profile_picture.png")))
                        .withAspectRatio(1, 1)
                        .withOptions(options)
                        .start(this);

            }
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                imageUri = UCrop.getOutput(data);
                try {
                    File compressedFile= new Compressor(this).setCompressFormat(Bitmap.CompressFormat.PNG).setQuality(50).setMaxHeight(96).setMaxWidth(96).compressToFile(new File(imageUri.getPath()));
                    profile_image.setImageURI(Uri.fromFile(compressedFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to compress: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    profile_image.setImageURI(imageUri);
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
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

    public void setProfilepic(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE);
    }

}
