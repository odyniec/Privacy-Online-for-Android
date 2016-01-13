package online.privacy.privacyonline;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Let's see if we can encapsulate the header image controls into a Customer ImageView class.
 * Let that manage it's own state etc.
 */

public class HeaderImageView extends ImageView {

    // Member vars
    private Activity activity; // This View's Activity.

    /**
     * companionViewId
     *
     * This is the Android Resource ID of the view that will disappear when the header expands.
     */
    private int companionViewId = 0;

    /**
     * changedHeight
     *
     * The height of the companion view as displayed. This will act as a benchmark for the
     * animations to know how far to grow/shrink.
     */
    private int heightChange = 0;

    /**
     * isExpanded
     *
     * Indicates the current status of the view.
     */
    private boolean isExpanded = false;


    // Constructors that override the ImageView ones. These are here so that we can act as a
    // bona fide ImageView and so the Android Studio design preview junk works.
    public HeaderImageView(Context context) { super(context);
        this.activity = (Activity) context;
    }
    public HeaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.activity = (Activity) context;
    }
    public HeaderImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.activity = (Activity) context;
    }

    /**
     * setImageToAsset - Set this view's src asset bitmap image to the specified file.
     *
     * Takes a filename that exists in the Android Assets folder for this app, and changes this
     * "ImageView" src to it. Also sets this image to grey-scale.
     *
     * @param assetFile
     */

    public void setImageToAsset(String assetFile) {
        this.setImageBitmap(getBitmapFromAsset(assetFile));
        this.setGreyScale();
    }

    /**
     * changeImageToAsset - Swap this view's src asset bitmap image to the supplied file by way of fade.
     *
     * Takes a filename that exists in the Android Assets folder for this app and changes this
     * "ImageView" src to it, by way of fading out the old image, and fading in the new one.
     *
     * If this ImageView currently has no Drawable, no animation takes place, instead setImageToAsset
     * is invoked alone to set the image in the first instance.
     *
     * @param assetFile
     */
    public void changeImageToAsset(final String assetFile, boolean vpnIsConnected) {
        final HeaderImageView us = this;

        if (us.getDrawable() == null) {
            this.setImageToAsset(assetFile);
            return;
        }

        if (vpnIsConnected) {
            this.setImageToAsset(assetFile);
            this.unsetGreyScale();
            return;
        }

        Animation fadeOut = getAnimations().fadeOut;
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                us.setImageToAsset(assetFile);
                us.startAnimation(getAnimations().fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.startAnimation(fadeOut);
    }

    /**
     * getIsExpanded = public accessor for the isExpanded state member.
     *
     * @return
     */
    public boolean getIsExpanded() {
        return isExpanded;
    }

    /**
     * setGreyScale - Changes the image drawable in this view to black and white.
     *
     * This is presuming the image is colour to begin with. The ColorFilter won't do a lot if
     * it's already black and white.
     */
    public void setGreyScale() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  // 0 means grey-scale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        this.setColorFilter(cf);
    }

    /**
     * unsetGreyScale - Changes the image drawable in this view back to colour.
     *
     * This is presuming the image is a colour one, and has been made grey-scale by way of
     * setGreyScale(). Won't do a lot otherwise, as all it does is call this.setColorFilter(null);
     */
    public void unsetGreyScale() {
        this.setColorFilter(null);
    }

    /**
     * slideOpen - Increases the image height by amount of height of supplied view, using animation.
     *
     * Increases the image height in amount equal to the height of the specified view object, using
     * an animated transition.
     *
     */
    public void slideOpen() {
        loadExpandedState();
        if (isExpanded) {
            return;
        }

        final View viewToCrush = this.activity.findViewById(this.companionViewId);
        int startHeight   = this.getHeight();
        this.heightChange = getCompanionViewHeight();

        // Store the staring height in a state machine so we can slide back to our original height.
        //int heightToGrow = viewToCrush.getHeight();
        //this.heightChange  = heightToGrow;

        ExpandAnimation expandAnimation = new ExpandAnimation(this, this.heightChange, startHeight, 800);
        expandAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isExpanded = true;
                saveExpandedState();
                ShrinkAnimation shrinkVpnSpinner = new ShrinkAnimation(viewToCrush, heightChange, heightChange, 800);
                viewToCrush.startAnimation(shrinkVpnSpinner);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                unsetGreyScale();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.startAnimation(expandAnimation);
    }


    /**
     * slideClosed - Reduces the image height by amount of height of supplied view, using animation.
     *
     * Reduces the image height in amount equal to the height of the specified view object, using
     * an animated transition.
     *
     * Also expands the given view to return it to it's previous state.
     *
     */
    public void slideClosed() {

        // Don't do it if we're already closed.
        loadExpandedState();
        loadHeightChange();

        if (!isExpanded) {
            return;
        }

        final View viewToReveal = this.activity.findViewById(this.companionViewId);
        int startHeight = this.getHeight();

        ShrinkAnimation shrinkAnimation = new ShrinkAnimation(this, heightChange, startHeight, 1000);
        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isExpanded = false;
                saveExpandedState();
                setGreyScale();

                 // This Kludge is apparently required, because animating from 0 fails in a fiery ball of death.
                viewToReveal.getLayoutParams().height = 1;
                ExpandAnimation expandVpnSpinner = new ExpandAnimation(viewToReveal, (heightChange - 1), 1, 800);
                viewToReveal.startAnimation(expandVpnSpinner);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.startAnimation(shrinkAnimation);
    }

    /**
     * setClosed
     *
     * Sets the HeaderImageView to the slid-in, greyscaled configuration. No animation takes place.
     *
     */
    public void setClosed() {

        int startHeight = this.getHeight();
        this.getLayoutParams().height = (startHeight - this.heightChange);
        this.setGreyScale();

        View viewToReveal = this.activity.findViewById(this.companionViewId);
        viewToReveal.getLayoutParams().height = this.heightChange;
    }

    /**
     * setOpen
     *
     * Sets the HeaderImageView to the slid-out, non-greyscaled configuration. No animation takes
     * place.
     *
     */
    public void setOpen() {
        Log.e("HeaderImageView", "setOpen called()");
        loadHeightChange();

        this.unsetGreyScale();
        int otherViewHeight = this.heightChange;
        this.getLayoutParams().height = this.getLayoutParams().height + otherViewHeight;

        View viewToCrush = this.activity.findViewById(this.companionViewId);
        viewToCrush.getLayoutParams().height = 0;
    }


    /**
     * setCompanionView
     *
     * Used to pre-configure the HeaderImageView. Tells it which view resource should be manipulated
     * along with the image itself. Also stores the height of said companion view, so we have an
     * animation parameter.
     *
     * @param viewResourceId
     */
    public void setCompanionView(int viewResourceId) {
        this.companionViewId = viewResourceId;
    }


    /**
     * clearExpandedState
     *
     * If - for whatever reason - the app is killed while connected, the state will be wrong.
     * We need to clear this state onDestroy. So implement a call for that here.
     *
     */
    public void clearExpandedState() {
        this.isExpanded = false;
        saveExpandedState();
    }



    // Hence follows: A bunch of private shit that no-one outside of this view class needs care about.

    // Because this instance doesn't persist across activities, we need to store the state in
    // sharedpreferences as that persists.
    private SharedPreferences getOurPreferences() {
        return this.activity.getSharedPreferences(
                this.activity.getString(R.string.privacyonline_preferences), Context.MODE_PRIVATE);
    }

    private void saveHeightChange() {
        SharedPreferences preferences = getOurPreferences();
        int storedHeightChange = preferences.getInt("header-height-change", 0);
        if (storedHeightChange == 0) {
            SharedPreferences.Editor prefsEditor = preferences.edit();
            prefsEditor.putInt("header-height-change", this.heightChange);
            prefsEditor.apply();
        }
    }

    private void saveExpandedState() {
        SharedPreferences preferences = getOurPreferences();
        SharedPreferences.Editor prefsEditor = preferences.edit();
        prefsEditor.putBoolean("header-is-expanded", this.isExpanded);
        prefsEditor.apply();
    }

    // Likewise, we need to pull the stored state,
    private void loadHeightChange() {
        SharedPreferences preferences = getOurPreferences();
        int storedHeightChange = preferences.getInt("header-height-change", 0);
        if (storedHeightChange != 0) {
            this.heightChange = storedHeightChange;
        }
    }

    private void loadExpandedState() {
        SharedPreferences preferences = getOurPreferences();
        this.isExpanded = preferences.getBoolean("header-is-expanded", false);
    }

    private int getCompanionViewHeight() {
        this.loadHeightChange();
        if (this.heightChange != 0) {
            return this.heightChange;
        }

        this.heightChange = this.activity.findViewById(this.companionViewId).getHeight();
        this.saveHeightChange();
        return this.heightChange;
    }


    private Bitmap getBitmapFromAsset(String assetFileName) {
        Bitmap bitmap = null;
        try {
            InputStream assetFileStream = getContext().getAssets().open(assetFileName);
            bitmap = BitmapFactory.decodeStream(assetFileStream);
        } catch (IOException ioe) {
            Log.e("HeaderImage", "Unable to read image: " + assetFileName);
        }
        return bitmap;
    }

    private Animations getAnimations() {
        return new Animations();
    }

    private class Animations {
        final public Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
        final public Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
    }
}
