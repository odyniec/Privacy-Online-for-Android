package online.privacy.privacyonline;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ProfileManager;

/**
 * PrivacyOnlineUtility - Class holding common code used in more than one location.
 */
public class PrivacyOnlineUtility {

    final private String LOG_TAG = "p.o.utility";

    public void updateSpinnerValues(Activity activity,
                                    int spinnerID,
                                    VPNLocationAdapter locationAdapter,
                                    AdapterView.OnItemSelectedListener onItemSelectedListener) {
        Context context = activity.getApplicationContext();

        SharedPreferences preferences
                = context.getSharedPreferences(activity.getString(R.string.privacyonline_preferences), Context.MODE_PRIVATE);

        final String currentDefaultLocation = preferences.getString("default_vpn_location", "");
        Log.i(LOG_TAG, "Default VPN Location: " + currentDefaultLocation);

        final Spinner defaultVPNLocationSpinner = (Spinner) activity.findViewById(spinnerID);
        defaultVPNLocationSpinner.setAdapter(locationAdapter);

        // Set the current selected item to be the default preference.
        if (!currentDefaultLocation.equals("")) {
            int currentDefaultItemPosition
                    = locationAdapter.getEntryLocationByHostname(currentDefaultLocation);
            defaultVPNLocationSpinner.setSelection(currentDefaultItemPosition);

            // If we're on the ConnectionActivity, then we need to update the header image too.
//            ImageView headerImageView = (ImageView) activity.findViewById(R.id.header_image);
//            VPNLocation currentDefaultVPNLocation = locationAdapter.getEntryByHostname(currentDefaultLocation);
//            if (headerImageView != null && currentDefaultVPNLocation != null ) {
//                updateHeaderImage(activity, headerImageView, currentDefaultVPNLocation);
//            }
        }

        defaultVPNLocationSpinner.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void createVPNProfile(Context context, String name) {
        ProfileManager profileManager = ProfileManager.getInstance(context);
        VpnProfile openVPNProfile = new VpnProfile(name);

        profileManager.addProfile(openVPNProfile);
        profileManager.saveProfileList(context);
        profileManager.saveProfile(context, openVPNProfile);
    }

    // ImageView.setAlpha is deprecated as of API Level 16. As we're going for >14 the warning is
    // something we don't care about.
    @SuppressWarnings("deprecation")
    public void updateHeaderImage(Context context, ImageView headerImageView, VPNLocation vpnLocation) {
        final Bitmap headerImage = getBitmapFromAsset(context, vpnLocation.getHeaderImage());
        final Animation fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fadeout);
        final Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fadein);
        final ImageView imageView = headerImageView;

        if (headerImage != null) {

            if (imageView.getDrawable() == null) {
                Log.i("PrivacyOnlineUtility", "Image has no Drawable.");
                imageView.setImageBitmap(headerImage);
            } else {
                Log.i("PrivacyOnlineUtility", "Image drawable: "+imageView.getDrawable().toString());
                fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imageView.setImageBitmap(headerImage);
                        imageView.startAnimation(fadeInAnimation);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                headerImageView.startAnimation(fadeOutAnimation);
            }
        }
    }

    public Bitmap getBitmapFromAsset(Context context, String assetFileName) {
        Bitmap bitmap = null;
        try {
            InputStream assetFileStream = context.getAssets().open(assetFileName);
            bitmap = BitmapFactory.decodeStream(assetFileStream);
        } catch (IOException ioe) {
            Log.e("PrivacyOnlineUtility","Unable to read image: " + assetFileName);
        }
        return bitmap;
    }
}
