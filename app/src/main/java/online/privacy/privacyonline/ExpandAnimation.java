package online.privacy.privacyonline;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Animation
 */
public class ExpandAnimation extends Animation {
    final int heightChange;
    View view;
    int startHeight;

    public ExpandAnimation(View view, int heightChange, int startHeight, int duration) {
        this.view = view;
        this.heightChange = heightChange;
        this.startHeight = startHeight;
        this.setDuration(duration);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight = (int) (startHeight + (heightChange * interpolatedTime));
        view.getLayoutParams().height = newHeight;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
