package online.privacy.privacyonline;

import android.content.Context;
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
    private int changedHeight = 0;
    private boolean isExpanded;


    // Constructors that override the ImageView ones. These are here so that we can act as a
    // bona fide ImageView and so the Android Studio design preview junk works.
    public HeaderImageView(Context context) {
        super(context);
    }
    public HeaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public HeaderImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    public void changeImageToAsset(final String assetFile) {
        final HeaderImageView us = this;

        if (us.getDrawable() == null) {
            this.setImageToAsset(assetFile);
            return;
        }

        Animation fadeOut = getAnimations().fadeOut;
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setImageToAsset(assetFile);
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
     * @param viewToCrush
     */
    public void slideOpen(final View viewToCrush) {

        int heightToGrow = viewToCrush.getHeight();
        int startHeight = this.getHeight();

        // Store the staring height in a state machine so we can slide back to our original height.
        this.changedHeight  = heightToGrow;

        ExpandAnimation expandAnimation = new ExpandAnimation(this, heightToGrow, startHeight, 800);
        expandAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isExpanded = true;
                ShrinkAnimation shrinkVpnSpinner = new ShrinkAnimation(viewToCrush, changedHeight, changedHeight, 800);
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
     * @param viewToReveal
     */
    public void slideClosed(final View viewToReveal) {

        int startHeight = this.getHeight();

        ShrinkAnimation shrinkAnimation = new ShrinkAnimation(this, changedHeight, startHeight, 1000);
        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isExpanded = false;
                setGreyScale();

                 // This Kludge is apparently required, because animating from 0 fails in a fiery ball of death.
                viewToReveal.getLayoutParams().height = 1;
                ExpandAnimation expandVpnSpinner = new ExpandAnimation(viewToReveal, (changedHeight - 1), 1, 800);
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


    // Hence follows: A bunch of private shit that no-one outside of this view class needs care about.

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