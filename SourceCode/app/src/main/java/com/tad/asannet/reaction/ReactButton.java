package com.tad.asannet.reaction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;

import com.tad.asannet.R;

@SuppressLint("AppCompatCustomView")
public class ReactButton
        extends Button
        implements View.OnClickListener, View.OnLongClickListener {

    /**
     * ReactButton custom view object to make easy to change attribute
     */
    private ReactButton mReactButton = this;

    /**
     * Reaction Alert Dialog to show Reaction layout with 6 Reactions
     */
    private AlertDialog mReactAlertDialog;


    /**
     * react current state as boolean variable
     * is false in default state and true in all other states
     */
    private boolean mCurrentReactState;

    /**
     * ImagesButton one for every Reaction
     */
    private ImageButton mImgButtonOne;
    private ImageButton mImgButtonTwo;
    private ImageButton mImgButtonThree;
    private ImageButton mImgButtonFour;
    private ImageButton mImgButtonFive;
    private ImageButton mImgButtonSix;

    private OnFavoriteAnimationEndListener mOnFavoriteAnimationEndListener;

    /**
     * Number of Valid Reactions
     */
    private static final int REACTIONS_NUMBER = 6;

    /**
     * Array of ImagesButton to set any action for all
     */
    private final ImageButton[] mReactImgArray = new ImageButton[REACTIONS_NUMBER];

    /**
     * Reaction Object to save default Reaction
     */
    private Reaction mDefaultReaction = FbReactions.getDefaultReact();

    /**
     * Reaction Object to save the current Reaction
     */
    private Reaction mCurrentReaction = mDefaultReaction;

    /**
     * Array of six Reaction one for every ImageButton Icon
     */
    private Reaction[] mReactionPack = FbReactions.getReactions();

    /**
     * Integer variable to change react dialog shape
     * Default value is react_dialog_shape
     */
    private int mReactDialogShape = R.drawable.react_dialog_shape;

    /**
     * onClickListener interface implementation object
     */
    private OnClickListener onClickListener;

    /**
     * OnLongClickListener interface implementation object
     */
    private OnLongClickListener onDismissListener;

    public ReactButton(Context context) {
        super(context);
        reactButtonDefaultSetting();
    }

    public ReactButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        reactButtonDefaultSetting();
    }

    public ReactButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        reactButtonDefaultSetting();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ReactButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        reactButtonDefaultSetting();
    }

    /**
     * Method with 2 state set first React or back to default state
     */
    private void onClickLikeAndDisLike() {
        //Code When User Click On Button
        //If State is true , dislike The Button And Return To Default State
        if (mCurrentReactState) {
            updateReactButtonByReaction(mDefaultReaction);
        } else {
            updateReactButtonByReaction(mReactionPack[0]);
        }
    }

    /**
     * Show Reaction dialog when user long click on react button
     */
    private void onLongClickDialog() {
        //Show Dialog With 6 React
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        //Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.react_dialog_layout, null);

        initializingReactImages(dialogView);
        setReactionsArray();
        resetReactionsIcons();
        onClickImageButtons();

        dialogBuilder.setView(dialogView);
        mReactAlertDialog = dialogBuilder.create();
        mReactAlertDialog.getWindow().setBackgroundDrawableResource(mReactDialogShape);

        Window window = mReactAlertDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        mReactAlertDialog.show();
    }

    /**
     * @param view : View Object to initialize all ImagesButton
     */
    private void initializingReactImages(View view) {
        mImgButtonOne = view.findViewById(R.id.imgButtonOne);
        mImgButtonTwo = view.findViewById(R.id.imgButtonTwo);
        mImgButtonThree = view.findViewById(R.id.imgButtonThree);
        mImgButtonFour = view.findViewById(R.id.imgButtonFour);
        mImgButtonFive = view.findViewById(R.id.imgButtonFive);
        mImgButtonSix = view.findViewById(R.id.imgButtonSix);
    }

    /**
     * Put all ImagesButton on Array
     */
    private void setReactionsArray() {
        mReactImgArray[0] = mImgButtonOne;
        mReactImgArray[1] = mImgButtonTwo;
        mReactImgArray[2] = mImgButtonThree;
        mReactImgArray[3] = mImgButtonFour;
        mReactImgArray[4] = mImgButtonFive;
        mReactImgArray[5] = mImgButtonSix;
    }

    /**
     * Set onClickListener For every Image Buttons on Reaction Dialog
     */
    private void onClickImageButtons() {
        imgButtonSetListener(mImgButtonOne, 0);
        imgButtonSetListener(mImgButtonTwo, 1);
        imgButtonSetListener(mImgButtonThree, 2);
        imgButtonSetListener(mImgButtonFour, 3);
        imgButtonSetListener(mImgButtonFive, 4);
        imgButtonSetListener(mImgButtonSix, 5);
    }

    /**
     * @param imgButton  : ImageButton view to set onClickListener
     * @param reactIndex : Index of Reaction to take it from ReactionPack
     */
    private void imgButtonSetListener(ImageButton imgButton, final int reactIndex) {
        imgButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReactButtonByReaction(mReactionPack[reactIndex]);
                mReactAlertDialog.dismiss();
            }
        });
    }

    /**
     * Update All Reaction ImageButton one by one from Reactions array
     */
    private void resetReactionsIcons() {
        for (int index = 0; index < REACTIONS_NUMBER; index++) {
            mReactImgArray[index].setImageResource(mReactionPack[index].getReactIconId());
        }
    }

    /**
     * Simple Method to set default settings for ReactButton Constructors
     * - Default Text Is Like
     * - set onClick And onLongClick
     * - set Default image is Dark Like
     */
    private void reactButtonDefaultSetting() {
        mReactButton.setText(mDefaultReaction.getReactText());
        mReactButton.setOnClickListener(this);
        mReactButton.setOnLongClickListener(this);
        mReactButton.setCompoundDrawablesWithIntrinsicBounds(mDefaultReaction.getReactIconId(), 0, 0, 0);
    }

    /**
     * @param shapeId : Get xml Shape for react dialog layout
     */
    public void setReactionDialogShape(int shapeId) {
        //Set Shape for react dialog layout
        this.mReactDialogShape = shapeId;
    }

    /**
     * @param react : Reaction to update UI by take attribute from it
     */
    private void updateReactButtonByReaction(Reaction react) {
        mCurrentReaction = react;
        mReactButton.setText(react.getReactText());
        mReactButton.setTextColor(Color.parseColor(react.getReactTextColor()));
        mReactButton.setCompoundDrawablesWithIntrinsicBounds(react.getReactIconId(), 0, 0, 0);
        mCurrentReactState = !react.getReactType().equals(mDefaultReaction.getReactType());
    }

    /**
     * @param reactions : Array of six Reactions to update default six Reactions
     */
    public void setReactions(Reaction... reactions) {
        //Assert that Reactions number is six
        if (reactions.length != REACTIONS_NUMBER)
            return;
        //Update array of library default reactions
        mReactionPack = reactions;
    }

    /**
     * @param reaction : set This Reaction as current Reaction
     */
    public void setCurrentReaction(Reaction reaction) {
        updateReactButtonByReaction(reaction);
    }

    /**
     * @return : The Current reaction Object
     */
    public Reaction getCurrentReaction() {
        return mCurrentReaction;
    }

    /**
     * @param reaction : Update library default Reaction by other Reaction
     */
    public void setDefaultReaction(Reaction reaction) {
        mDefaultReaction = reaction;
        updateReactButtonByReaction(mDefaultReaction);
    }

    /**
     * @return : The current default Reaction object
     */
    public Reaction getDefaultReaction() {
        return mDefaultReaction;
    }

    /**
     * @param onClickListener : get OnClickListener implementation from developer
     *                        ans update onClickListener Value in this class
     */
    public void setReactClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * @param onDismissListener : get OnLongClickListener implementation
     *                          and update onDismissListener value in this class
     */
    public void setReactDismissListener(OnLongClickListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    /**
     * @return : true if current reaction type is default
     */
    public boolean isDefaultReaction() {
        return mCurrentReaction.equals(mDefaultReaction);
    }

    @Override
    public void onClick(View view) {
        //The Library OnClick
        onClickLikeAndDisLike();
        //If User Set OnClick Using it After Native Library OnClick
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        final View currentView = view;
        //First Using My Native OnLongClick
        onLongClickDialog();
        //Implement on Dismiss Listener to call Developer Method
        mReactAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (onDismissListener != null) {
                    //User OnLongClick Implementation
                    onDismissListener.onLongClick(currentView);
                }
            }
        });
        return true;
    }

    public interface OnFavoriteChangeListener {
        /**
         * Called when the favorite state is changed.
         *
         * @param buttonView the button view whose state has changed
         * @param favorite the favorite state
         */
        void onFavoriteChanged(ReactButton buttonView, boolean favorite);
    }

    public interface OnFavoriteAnimationEndListener {
        /**
         * Called when the favorite animation ended.
         *
         * @param buttonView the button view whose animation ended
         * @param favorite the favorite state
         */
        void onAnimationEnd(ReactButton buttonView, boolean favorite);
    }

    public void setOnFavoriteAnimationEndListener(OnFavoriteAnimationEndListener listener) {
        mOnFavoriteAnimationEndListener = listener;
    }

    private boolean mAnimateFavorite;
    private boolean mAnimateUnfavorite;

    public void setFavorite(boolean favorite, boolean animated) {
        if (favorite) {
            boolean orig = mAnimateFavorite;
            mAnimateFavorite = animated;
            setFavorite(favorite);
            mAnimateFavorite = orig;
        } else {
            boolean orig = mAnimateUnfavorite;
            mAnimateUnfavorite = animated;
            setFavorite(favorite);
            mAnimateUnfavorite = orig;
        }
    }

    public Reaction getRactionFromText(String favorite) {
       for (int i=0 ;i<mReactionPack.length; i++){
           if (mReactionPack[i].getReactText().equals(favorite)){
               return mReactionPack[i];
           }
       }
       return mReactionPack[0];
    }

    private boolean mFavorite;
    private boolean mBroadcasting;
    private OnFavoriteChangeListener mOnFavoriteChangeListener;

    public void setFavorite(boolean favorite) {
        if (mFavorite != favorite) {
            mFavorite = favorite;
            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnFavoriteChangeListener != null) {
                mOnFavoriteChangeListener.onFavoriteChanged(this, mFavorite);
            }
            updateFavoriteButton(favorite);
            mBroadcasting = false;
        }
    }

    private int mFavoriteResource;
    private int mNotFavoriteResource;

    private void updateFavoriteButton(boolean favorite) {
        if (favorite) {
            if (mAnimateFavorite) {
                animateButton(favorite);
            } else {
                setBackgroundResource(mFavoriteResource);
                if (mOnFavoriteAnimationEndListener != null) {
                    mOnFavoriteAnimationEndListener.onAnimationEnd(this, mFavorite);
                }
            }
        } else {
            if (mAnimateUnfavorite) {
                animateButton(favorite);
            } else {
                setBackgroundResource(mNotFavoriteResource);
                if (mOnFavoriteAnimationEndListener != null) {
                    mOnFavoriteAnimationEndListener.onAnimationEnd(this, mFavorite);
                }
            }
        }
    }

    private int mRotationAngle;
    private int mBounceDuration;
    private int mRotationDuration;
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR =
            new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private void animateButton(boolean toFavorite) {
        final int startAngle = 0;
        int endAngle;
        float startBounce;
        float endBounce;
        if (toFavorite) {
            endAngle = mRotationAngle;
            startBounce = 0.2f;
            endBounce = 1.0f;
        } else {
            endAngle = -mRotationAngle;
            startBounce = 1.3f;
            endBounce = 1.0f;
        }

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(this, "rotation", startAngle, endAngle);
        rotationAnim.setDuration(mRotationDuration);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(this, "scaleX", startBounce, endBounce);
        bounceAnimX.setDuration(mBounceDuration);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(this, "scaleY", startBounce, endBounce);
        bounceAnimY.setDuration(mBounceDuration);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                if (mFavorite) {
                    setBackgroundResource(mFavoriteResource);
                } else {
                    setBackgroundResource(mNotFavoriteResource);
                }
            }
        });

        animatorSet.play(rotationAnim);
        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (mOnFavoriteAnimationEndListener != null) {
                    mOnFavoriteAnimationEndListener.onAnimationEnd(ReactButton.this, mFavorite);
                }
            }
        });

        animatorSet.start();
    }

    public void setOnFavoriteChangeListener(OnFavoriteChangeListener listener) {
        mOnFavoriteChangeListener = listener;
    }


}
