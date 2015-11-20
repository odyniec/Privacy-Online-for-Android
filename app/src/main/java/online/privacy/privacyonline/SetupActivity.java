package online.privacy.privacyonline;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class SetupActivity extends AppCompatActivity {

    final static private String LOG_TAG = "p.o.setup";

    final private Context  contextSetup  = this;
    final private Activity activitySetup = this;

    private VerifyUserAccountReceiver verifyReceiver;
    private GetLocationListReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);

        final EditText usernameInput = (EditText) findViewById(R.id.input_text_username);
        usernameInput.setText(preferences.getString("username", ""));

        final EditText passwordInput = (EditText) findViewById(R.id.input_password_password);
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

        Button buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePreferences();
                EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
                EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);

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
        IntentFilter verifyFilter = new IntentFilter(SetupActivity.VerifyUserAccountReceiver.API_RESPONSE);
        verifyFilter.addCategory(Intent.CATEGORY_DEFAULT);
        verifyReceiver = new VerifyUserAccountReceiver();
        registerReceiver(verifyReceiver, verifyFilter);

        IntentFilter locationFilter = new IntentFilter(SetupActivity.GetLocationListReceiver.API_RESPONSE);
        locationFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationReceiver = new GetLocationListReceiver();
        registerReceiver(locationReceiver, locationFilter);

        // Populate the Location list.
        Intent apiLocationIntent = new Intent(this, PrivacyOnlineAPIService.class);
        apiLocationIntent.setAction(PrivacyOnlineAPIService.ACTION_GET_LOCATIONS);
        apiLocationIntent.putExtra(PrivacyOnlineAPIService.EXTRA_CALLER, SetupActivity.GetLocationListReceiver.API_RESPONSE);
        startService(apiLocationIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(locationReceiver);
        unregisterReceiver(verifyReceiver);
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
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startLocationActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
    }

    private void updatePreferences() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = preferences.edit();

        EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
        EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);

        preferencesEditor.putString("username", inputTextUsername.getText().toString());
        preferencesEditor.putString("password", inputTextPassword.getText().toString());
        preferencesEditor.apply();
    }

    // Implement a receiver so we can use the APIService to check login details.
    public class VerifyUserAccountReceiver extends BroadcastReceiver {

        public static final String API_RESPONSE =
                "online.privacy.privacyonline.intent.action.RESPONSE_VERIFY_ACCOUNT";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Received Service Broadcast");
            boolean checkResult = intent.getBooleanExtra(PrivacyOnlineAPIService.CHECK_RESULT , false);

            // If the details were good, launch the ConnectionActivity Activity.
            if (checkResult) {
                Log.i(LOG_TAG, "User Account verified");
                finish();
            } else {
                Log.i(LOG_TAG, "User Account verification failed");
                EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
                EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);

                inputTextUsername.setBackgroundColor(Color.RED);
                inputTextPassword.setBackgroundColor(Color.RED);
            }
        }
    }

    // Implement a receiver so we can use the APIService to check login details.
    public class GetLocationListReceiver extends BroadcastReceiver {

        public static final String API_RESPONSE =
                "online.privacy.privacyonline.intent.action.RESPONSE_GET_LOCATION";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Received Get Location Service Broadcast");
            ArrayList<VPNLocation> locationList
                    = intent.getParcelableArrayListExtra(PrivacyOnlineAPIService.CHECK_RESULT);
            // Stop the App crashing if we can't get stuff from the API
            // TODO - Connectivity checking, and graceful handling.
            if (locationList == null) {
                locationList = new ArrayList<>();
            }

            final VPNLocationAdapter locationAdapter
                    = new VPNLocationAdapter(context, android.R.layout.simple_spinner_item, locationList);

            PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
            utility.updateSpinnerValues(activitySetup, R.id.input_spinner_default_vpn_location,
                    locationAdapter, new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
                    final SharedPreferences.Editor preferencesEditor = preferences.edit();
                    ;
                    VPNLocation location = locationAdapter.getItem(position);
                    preferencesEditor.putString("default_vpn_location", location.getHostname());
                    preferencesEditor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapter) {}
            });
        }
    }
}
