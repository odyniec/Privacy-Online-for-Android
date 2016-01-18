package online.privacy;
/**
 * SetupActivity
 *
 * Configuration UI activity for the Privacy Online for Android app.
 *
 * Takes the username / password credentials for the Privacy Online account and validates them
 * against the Privacy Online API. As well as the default VPN location to use.
 *
 * Stores preferences using Android's builtin preferences manager which is private to this app.
 *
 * Copyright © 2016, privacy.online
 * All rights reserved.
 *
 * This file is part of Privacy Online for Android.
 *
 * Privacy Online for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Online for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Online for Android.  If not, see <http://www.gnu.org/licenses/>.
 */
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ProfileManager;

public class SetupActivity extends AppCompatActivity {

    final static private String LOG_TAG = "p.o.setup";

    final private Context contextSetup = this;
    final private Activity activitySetup = this;

    private VerifyUserAccountReceiver verifyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);

        final EditText usernameInput = (EditText) findViewById(R.id.input_text_username);
        final EditText passwordInput = (EditText) findViewById(R.id.input_password_password);

        usernameInput.setText(preferences.getString("username", ""));
        usernameInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (   (event.getAction() == KeyEvent.ACTION_DOWN)
                    && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    usernameInput.clearFocus();
                    passwordInput.requestFocus();
                    return true;
                }
                return false;
            }
        });

        passwordInput.setText(preferences.getString("password", ""));


        // Ugh, apparently we can't define text on buttons to have the underlined property from
        // within the XML, so we'll do it here we have to set the intent chooser here anyway.
        Button buttonSignUp = (Button) findViewById(R.id.button_cta_signup);
        buttonSignUp.setPaintFlags(buttonSignUp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ctaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_online_signup)));
                startActivity(ctaIntent);
            }
        });

        final Button buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
                EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);
                clearErrorState(inputTextUsername);
                clearErrorState(inputTextPassword);
                setErrorInfoVisibility(View.INVISIBLE);

                setWorkingState(true);

                Intent apiIntent = new Intent(contextSetup, PrivacyOnlineAPIService.class);
                apiIntent.putExtra(PrivacyOnlineAPIService.PARAM_USERNAME, inputTextUsername.getText().toString());
                apiIntent.putExtra(PrivacyOnlineAPIService.PARAM_PASSWORD, inputTextPassword.getText().toString());
                apiIntent.setAction(PrivacyOnlineAPIService.ACTION_VERIFY_USERNAME);
                startService(apiIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register the IntentService Listeners to get the response of the user check and location list.
        IntentFilter verifyFilter = new IntentFilter(VerifyUserAccountReceiver.API_RESPONSE);
        verifyFilter.addCategory(Intent.CATEGORY_DEFAULT);
        verifyReceiver = new VerifyUserAccountReceiver();
        registerReceiver(verifyReceiver, verifyFilter);

        // Populate the Location list.
        VPNLocations vpnLocations = new VPNLocations(this);
        ArrayList<VPNLocation> locationList = vpnLocations.getArrayList();
        final VPNLocationAdapter locationAdapter
                = new VPNLocationAdapter(this, R.layout.spinner_layout_full, locationList);
        PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
        utility.updateSpinnerValues(activitySetup, R.id.input_spinner_default_vpn_location,
                locationAdapter, false, new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
                        final SharedPreferences.Editor preferencesEditor = preferences.edit();

                        VPNLocation location = locationAdapter.getItem(position);
                        preferencesEditor.putString("default_vpn_location", location.getHostname());
                        preferencesEditor.apply();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(verifyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Private method to handle storing the supplied preferences.
    private void updatePreferences() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = preferences.edit();

        EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
        String username = inputTextUsername.getText().toString();

        EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);
        String password = inputTextPassword.getText().toString();

        preferencesEditor.putString("username", username);
        preferencesEditor.putString("password", password);
        preferencesEditor.apply();
    }

    // Used to change the UI. This gives the user indication that the button was pressed.
    private void setWorkingState(boolean isWorking) {
        Button buttonSave = (Button) findViewById(R.id.button_save);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_save);

        if (isWorking) {
            buttonSave.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonSave.setVisibility(View.VISIBLE);
        }
    }

    // Updates the visibility of the Error info container.
    private void setErrorInfoVisibility(int visibility) {
        GridLayout errorInfo = (GridLayout) findViewById(R.id.credential_error_info);
        errorInfo.setVisibility(visibility);
    }


    // View.setBackgroundDrawable is deprecated from api16, it's now just setBackground(Drawable)
    // but we can't use that because we're using api14 as a min.
    @SuppressWarnings("deprecation")
    private void setErrorState(View view) {
        ColorStateList stateList = ContextCompat.getColorStateList(activitySetup, R.color.input_text_error);
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTintList(wrappedDrawable, stateList);
        view.setBackgroundDrawable(wrappedDrawable);
    }

    // View.setBackgroundDrawable is deprecated from api16, it's now just setBackground(Drawable)
    // but we can't use that because we're using api14 as a min.
    @SuppressWarnings("deprecation")
    private void clearErrorState(View view) {
        ColorStateList stateList = ContextCompat.getColorStateList(activitySetup, R.color.input_text_normal);
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTintList(wrappedDrawable, stateList);
        view.setBackgroundDrawable(wrappedDrawable);
    }


    /**
     * VerifyUserAccountReceiver
     *
     * Public sub-class responsible for handling the result Broadcast from the PrivacyOnlineAPIService
     * service.
     */
    public class VerifyUserAccountReceiver extends BroadcastReceiver {

        public static final String API_RESPONSE =
                "online.privacy.privacyonline.intent.action.RESPONSE_VERIFY_ACCOUNT";

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean checkResult = intent.getBooleanExtra(PrivacyOnlineAPIService.CHECK_RESULT, false);

            // Update the progressbar/button as we got a response.
            setWorkingState(false);

            // If the details were good, save the details and launch the ConnectionActivity Activity.
            if (checkResult) {
                Log.i(LOG_TAG, "User Account verified");
                updatePreferences();
                finish();
            } else {
                Log.i(LOG_TAG, "User Account verification failed");
                EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
                EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);

                setErrorState(inputTextUsername);
                setErrorState(inputTextPassword);
                setErrorInfoVisibility(View.VISIBLE);
            }
        }
    }
}
