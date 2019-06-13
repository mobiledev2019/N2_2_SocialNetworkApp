package com.tad.asannet.ui.activities.post;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tad.asannet.R;
import com.tad.asannet.adapters.CommentsAdapter;
import com.tad.asannet.models.Comment;
import com.tad.asannet.models.Post;
import com.tad.asannet.utils.AnimationUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class CommentsActivity extends AppCompatActivity {

    String user_id, post_id;
    private FirebaseFirestore mFirestore;
    private CommentsAdapter mAdapter;
    private List<Comment> commentList;
    private ProgressBar mProgress;
    private RecyclerView mCommentsRecycler;
    private EditText mCommentText;
    private MaterialButton mCommentsSend;
    private FirebaseUser mCurrentUser;
    private boolean owner;
    private CircleImageView user_image;
    private TextView post_desc;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static void startActivity(Context context, List<Post> post, String desc, int pos, boolean owner) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra("user_id", post.get(pos).getUserId());
        intent.putExtra("post_desc", desc);
        intent.putExtra("post_id", post.get(pos).postId);
        intent.putExtra("owner",owner);
        context.startActivity(intent);
    }


    private void sendNotification() {

        Map<String,Object> commentNotification=new HashMap<>();
        commentNotification.put("post_desc",post_desc.getText().toString());
        commentNotification.put("owner",owner);
        commentNotification.put("post_id",post_id);
        commentNotification.put("admin_id",user_id);
        commentNotification.put("notification_id",String.valueOf(System.currentTimeMillis()));
        commentNotification.put("timestamp",String.valueOf(System.currentTimeMillis()));

        mFirestore.collection("Notifications")
                .document(mCurrentUser.getUid())
                .collection("Comment")
                .add(commentNotification)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("Comment Message","success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Comment Message","failure",e);
                    }
                });

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

        setContentView(R.layout.activity_post_comments);

        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Bình luận");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Bình luận");

        user_image=findViewById(R.id.comment_admin);
        post_desc=findViewById(R.id.comment_post_desc);

        setupCommentView();

    }


    private void setupCommentView() {

        user_id = getIntent().getStringExtra("user_id");
        post_id = getIntent().getStringExtra("post_id");
        post_desc.setText(Html.fromHtml(getIntent().getStringExtra("post_desc")));
        owner=getIntent().getBooleanExtra("owner",false);

        mFirestore.collection("Users")
                .document(user_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                       Glide.with(CommentsActivity.this)
                               .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.gradient_2))
                               .load( documentSnapshot.getString("image"))
                               .into(user_image);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error",e.getLocalizedMessage());
                    }
                });

        mCommentsRecycler = findViewById(R.id.recyclerView);
        mCommentText = findViewById(R.id.text);
        mCommentsSend = findViewById(R.id.send);
        mProgress = findViewById(R.id.progressBar);

        commentList = new ArrayList<>();
        mAdapter = new CommentsAdapter(commentList, this,owner);

        mCommentText.setHint("Thêm bình luận..");
        mCommentsSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = mCommentText.getText().toString();
                if (!TextUtils.isEmpty(comment))
                    sendComment(comment, mCommentText, mProgress);
                else
                    AnimationUtil.shakeView(mCommentText, CommentsActivity.this);
            }
        });

        mCommentsRecycler.setItemAnimator(new DefaultItemAnimator());
        mCommentsRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecycler.setHasFixedSize(true);
        mCommentsRecycler.setAdapter(mAdapter);

        getComments(mProgress);

    }


    private void sendComment(final String comment, final EditText comment_text, final ProgressBar mProgress) {

        mProgress.setVisibility(View.VISIBLE);

        mFirestore.collection("Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Map<String, Object> commentMap = new HashMap<>();
                        commentMap.put("id", documentSnapshot.getString("id"));
                        commentMap.put("username", documentSnapshot.getString("username"));
                        commentMap.put("image", documentSnapshot.getString("image"));
                        commentMap.put("post_id", post_id);
                        commentMap.put("comment", comment);
                        commentMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

                        mFirestore.collection("Posts")
                                .document(post_id)
                                .collection("Comments")
                                .add(commentMap)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        mProgress.setVisibility(View.GONE);
                                        sendNotification();
                                        mCommentText.setHint("Thêm bình luận..");
                                        Toast.makeText(CommentsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                                        commentList.clear();
                                        getComments(mProgress);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mProgress.setVisibility(View.GONE);
                                        Toast.makeText(CommentsActivity.this, "Error sending comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Lỗi gửi bình luận", e.getMessage());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error getuser", e.getMessage());
                    }
                });

    }


    private void getComments(final ProgressBar mProgress) {
        mProgress.setVisibility(View.VISIBLE);
        mFirestore.collection("Posts")
                .document(post_id)
                .collection("Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {

                        if(e!=null){
                            mProgress.setVisibility(View.GONE);
                            e.printStackTrace();
                            return;
                        }

                        for (DocumentChange doc : querySnapshot.getDocumentChanges()) {

                            if (doc.getDocument().exists()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Comment comment = doc.getDocument().toObject(Comment.class).withId(doc.getDocument().getId());
                                    commentList.add(comment);
                                    mAdapter.notifyDataSetChanged();

                                }

                                if (querySnapshot.getDocuments().size() == commentList.size()) {
                                    mProgress.setVisibility(View.GONE);
                                }
                            } else {
                                mProgress.setVisibility(View.GONE);
                            }

                        }

                    }
                });
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

    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }



}
