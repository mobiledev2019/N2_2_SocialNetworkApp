package com.tad.asannet.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.tad.asannet.R;
import com.tad.asannet.models.MultipleImage;
import com.tad.asannet.reaction.ReactButton;
import com.tad.asannet.ui.activities.notification.ImagePreview;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tad.asannet.ui.views.AsannetImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;


public class PostPhotosAdapter extends PagerAdapter {


    private ArrayList<MultipleImage> IMAGES;
    private boolean local;
    private LayoutInflater inflater;
    private Context context;
    private File compressedFile;
    private Activity activity;
    private String postId;
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    //private MaterialFavoriteButton like_btn;
    private ReactButton like_btn;

    public PostPhotosAdapter(Context context, Activity activity, ArrayList<MultipleImage> IMAGES, boolean local, String postId, ReactButton like_btn) {
        this.context = context;
        this.IMAGES =IMAGES;
        this.local=local;
        this.activity=activity;
        inflater = LayoutInflater.from(context);
        this.postId=postId;
        this.like_btn=like_btn;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }


    private void animatePhotoLike(final View vBgLike, final ImageView ivLike) {
        vBgLike.setVisibility(View.VISIBLE);
        ivLike.setVisibility(View.VISIBLE);

        vBgLike.setScaleY(0.1f);
        vBgLike.setScaleX(0.1f);
        vBgLike.setAlpha(1f);
        ivLike.setScaleY(0.1f);
        ivLike.setScaleX(0.1f);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(vBgLike, "scaleY", 0.1f, 1f);
        bgScaleYAnim.setDuration(300);
        bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(vBgLike, "scaleX", 0.1f, 1f);
        bgScaleXAnim.setDuration(300);
        bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(vBgLike, "alpha", 1f, 0f);
        bgAlphaAnim.setDuration(300);
        bgAlphaAnim.setStartDelay(150);
        bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 0.1f, 1f);
        imgScaleUpYAnim.setDuration(300);
        imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 0.1f, 1f);
        imgScaleUpXAnim.setDuration(300);
        imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 1f, 0f);
        imgScaleDownYAnim.setDuration(300);
        imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 1f, 0f);
        imgScaleDownXAnim.setDuration(300);
        imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
        animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetLikeAnimationState(vBgLike,ivLike);
                like_btn.setFavorite(true,true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put("liked", true);

                try {

                    FirebaseFirestore.getInstance().collection("Posts")
                            .document(postId)
                            .collection("Liked_Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .set(likeMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i("post", "liked");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error like", e.getMessage());
                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        animatorSet.start();

    }

    private void resetLikeAnimationState(View vBgLike,ImageView ivLike) {
        vBgLike.setVisibility(View.INVISIBLE);
        ivLike.setVisibility(View.INVISIBLE);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, final int position) {
        final View imageLayout = inflater.inflate(R.layout.item_viewpager_image, view, false);

        assert imageLayout!=null;
        AsannetImageView imageView = imageLayout.findViewById(R.id.image);
        final View vBgLike = imageLayout.findViewById(R.id.vBgLike);
        final ImageView ivLike = imageLayout.findViewById(R.id.ivLike);

        if(!local) {

            final GestureDetector detector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    Intent intent=new Intent(context, ImagePreview.class)
                            .putExtra("uri","")
                            //.putExtra("sender_name","Posts")
                            .putExtra("url",IMAGES.get(position).getUrl());
                    context.startActivity(intent);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if(isOnline()) {
                        animatePhotoLike(vBgLike, ivLike);
                    }

                    return true;
                }
            }
            );

            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return detector.onTouchEvent(event);
                }
            });


            Glide.with(context)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder2))
                    .load(IMAGES.get(position).getUrl())
                    .into(imageView);

        }else{

            try {
                compressedFile= new Compressor(context).setCompressFormat(Bitmap.CompressFormat.PNG).setQuality(75).setMaxHeight(350).compressToFile(new File(IMAGES.get(position).getLocal_path()));
                imageView.setImageURI(Uri.fromFile(compressedFile));


            } catch (IOException e) {
                e.printStackTrace();
            }




        }

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
