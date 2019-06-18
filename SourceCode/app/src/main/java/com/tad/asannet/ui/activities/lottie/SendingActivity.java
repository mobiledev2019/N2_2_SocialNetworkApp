package com.tad.asannet.ui.activities.lottie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.tad.asannet.R;
import com.tad.asannet.ui.activities.friends.SendActivity;
import com.tad.asannet.utils.AnimationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.tad.asannet.ui.activities.lottie.FestivalActivity.activity;
import static com.tad.asannet.utils.Config.random;

public class SendingActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;

    public static void startActivity(Context context,String reason,String message_,Uri imageUri,String c_name,String c_image,String current_id,String user_id){
        context.startActivity(new Intent(context,SendingActivity.class)
                .putExtra("message_",message_)
                .putExtra("imageUri",imageUri.toString())
                .putExtra("c_name",c_name)
                .putExtra("reason",reason)
                .putExtra("c_image",c_image)
                .putExtra("current_id",current_id)
                .putExtra("user_id",user_id));
    }
    public static void startActivity(Context context,String reason,String message_,String c_name,String c_image,String current_id,String user_id){
        context.startActivity(new Intent(context,SendingActivity.class)
                .putExtra("message_",message_)
                .putExtra("reason",reason)
                .putExtra("c_name",c_name)
                .putExtra("c_image",c_image)
                .putExtra("current_id",current_id)
                .putExtra("user_id",user_id));
    }

    public static void startActivity(Context context,String reason,String dev_id){
        context.startActivity(new Intent(context,SendingActivity.class)
                .putExtra("reason",reason)
        .putExtra("dev_id",dev_id));
    }

    FirebaseFirestore mFirestore;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        mFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("notification").child(random()+".jpg");

        lottieAnimationView=findViewById(R.id.lottie);
        lottieAnimationView.useHardwareAcceleration(true);

        String reason=getIntent().getStringExtra("reason");
        if(reason.equals("normal_message")) {
            if (StringUtils.isNotEmpty(getIntent().getStringExtra("imageUri"))) {
                sendMessage(
                        "",
                        getIntent().getStringExtra("message_"),
                        Uri.parse(getIntent().getStringExtra("imageUri")),
                        getIntent().getStringExtra("c_name"),
                        getIntent().getStringExtra("c_image"),
                        getIntent().getStringExtra("current_id"),
                        getIntent().getStringExtra("user_id"));
            } else {
                sendMessage(
                        "",
                        getIntent().getStringExtra("message_"),
                        null,
                        getIntent().getStringExtra("c_name"),
                        getIntent().getStringExtra("c_image"),
                        getIntent().getStringExtra("current_id"),
                        getIntent().getStringExtra("user_id"));
            }
        }else if(reason.equals("wish_back")){
            FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            sendMessage(
                                    "festival",
                                    documentSnapshot.getString("name") + " wished you back for your wishes."
                                    ,null
                                    ,documentSnapshot.getString("username")
                                    ,documentSnapshot.getString("image")
                                    ,documentSnapshot.getString("id")
                                    ,getIntent().getStringExtra("dev_id"));
                        }
                    });
        }
        else if(reason.equals("thank_back")){
            FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            sendMessage(
                                    "festival",
                                    documentSnapshot.getString("name") + " thanked you back for your wishes."
                                    ,null
                                    ,documentSnapshot.getString("username")
                                    ,documentSnapshot.getString("image")
                                    ,documentSnapshot.getString("id")
                                    ,getIntent().getStringExtra("dev_id"));
                        }
                    });
        }

    }

    private void pauseAnimation(int delay) {
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lottieAnimationView.pauseAnimation();
            }
        },delay);
    }

    private void sendMessage(final String type, final String message_, Uri imageUri, final String c_name, final String c_image, final String current_id, final String user_id) {

        if(!TextUtils.isEmpty(message_)){

            if(imageUri==null){

                //Send only message

                Map<String,Object> notificationMessage=new HashMap<>();
                notificationMessage.put("username",c_name);
                notificationMessage.put("userimage",c_image);
                notificationMessage.put("message",message_);
                notificationMessage.put("from",current_id);
                notificationMessage.put("notification_id", String.valueOf(System.currentTimeMillis()));
                notificationMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
                notificationMessage.put("read","false");

                mFirestore.collection("Users/"+user_id+"/Notifications").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        lottieAnimationView.playAnimation();
                        lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                Toast.makeText(SendingActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                if(type.equals("festival")) {
                                    activity.finish();
                                }else{
                                    finish();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SendingActivity.this, "Error sending message :(", Toast.LENGTH_LONG).show();

                    }
                });
            }else{
                //Send message with Image


                storageReference.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful() &&task.getResult().toString()!=null){

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Map<String,Object> notificationMessage=new HashMap<>();
                                    notificationMessage.put("username",c_name);
                                    notificationMessage.put("userimage",c_image);
                                    notificationMessage.put("image",uri.toString());
                                    notificationMessage.put("message",message_);
                                    notificationMessage.put("from",current_id);
                                    notificationMessage.put("notification_id", String.valueOf(System.currentTimeMillis()));
                                    notificationMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
                                    notificationMessage.put("read","false");

                                    mFirestore.collection("Users/"+user_id+"/Notifications_image").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            lottieAnimationView.playAnimation();
                                            lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                    Toast.makeText(SendingActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SendingActivity.this, "Error sending message :(", Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SendingActivity.this, "Error sending message :(", Toast.LENGTH_LONG).show();

                                }
                            });

                        }

                    }
                });

            }


        }else{
            Toast.makeText(this, "Empty message", Toast.LENGTH_SHORT).show();
            finish();
        }

    }


}
