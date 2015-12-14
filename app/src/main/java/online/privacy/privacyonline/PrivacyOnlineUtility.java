package online.privacy.privacyonline;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

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
    public void updateHeaderImage(final Context context,
                                  final ImageView headerImageView,
                                  VPNLocation vpnLocation)
    {
        final Bitmap headerImage = getBitmapFromAsset(context, vpnLocation.getHeaderImage());
        final Animation fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fadeout);
        final Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fadein);

        if (VpnStatus.getVpnStatus().equals("CONNECTED")) {
            headerImageView.setImageBitmap(headerImage);
            unsetGreyScale(headerImageView);
            return;
        }

        if (headerImage != null) {

            if (headerImageView.getDrawable() == null) {
                Log.i("PrivacyOnlineUtility", "Image has no Drawable.");
                headerImageView.setImageBitmap(headerImage);
                setGreyScale(headerImageView);
            } else {
                Log.i("PrivacyOnlineUtility", "Image drawable: " + headerImageView.getDrawable().toString());
                fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        headerImageView.setImageBitmap(headerImage);
                        headerImageView.startAnimation(fadeInAnimation);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                headerImageView.startAnimation(fadeOutAnimation);
            }
        }
    }

    public void setHeaderImageToCurrentConnected(Activity activity) {
        Spinner vpnLocationSpinner = (Spinner) activity.findViewById(R.id.input_spinner_vpn_location);
        if (vpnLocationSpinner.getVisibility() != View.VISIBLE) {
            vpnLocationSpinner.setVisibility(View.VISIBLE);
        }

        ImageView headerImage = (ImageView) activity.findViewById(R.id.header_image);
        if (headerImage.getVisibility() != View.VISIBLE) {
            headerImage.setVisibility(View.VISIBLE);
        }

        int vpnLocationSpinnerHeight = vpnLocationSpinner.getHeight();
        int headerImageHeight = headerImage.getHeight();
        Log.e("Utility", "spinner height: "+vpnLocationSpinnerHeight);
        Log.e("Utility", "image height: "+headerImageHeight);

        VPNLocation currentLocation = (VPNLocation) vpnLocationSpinner.getSelectedItem();
        Bitmap headerImageBitmap = getBitmapFromAsset(activity, currentLocation.getHeaderImage());
        Log.e("Utility", "image to use: "+currentLocation.getHeaderImage());

        headerImage.getLayoutParams().height = (headerImageHeight + vpnLocationSpinnerHeight);
        vpnLocationSpinner.getLayoutParams().height = 0;
        headerImage.setImageBitmap(headerImageBitmap);
        unsetGreyScale(headerImage);
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

    public void setGreyScale(ImageView imageView) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(cf);
    }

    public void unsetGreyScale(ImageView imageView) {
        imageView.setColorFilter(null);
    }
}
