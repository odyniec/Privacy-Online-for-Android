package online.privacy.privacyonline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;


public class SetupActivity extends AppCompatActivity {

    final static private int REQUEST_CODE_STANDARD_OPERATION = 1;
    final static private String LOG_TAG_HOME = "privacyonline.setup";
    final static private String PRIVACYONLINE_PREFERENCES = "online.privacy.privacyonline.PREFERENCES";
    final private Context contextHome = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        // Register the IntentService Listeners to get the response of the user check and location list.
        IntentFilter verifyFilter = new IntentFilter(SetupActivity.VerifyUserAccountReceiver.API_RESPONSE);
        verifyFilter.addCategory(Intent.CATEGORY_DEFAULT);
        VerifyUserAccountReceiver verifyReceiver = new VerifyUserAccountReceiver();
        registerReceiver(verifyReceiver, verifyFilter);

        IntentFilter locationFilter = new IntentFilter(SetupActivity.GetLocationListReceiver.API_RESPONSE);
        locationFilter.addCategory(Intent.CATEGORY_DEFAULT);
        GetLocationListReceiver locationReceiver = new GetLocationListReceiver();
        registerReceiver(locationReceiver, locationFilter);

        Button buttonLogin = (Button) findViewById(R.id.button_save);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG_HOME, "Login Clicked");
                EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
                EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);

                Intent apiIntent = new Intent(contextHome, PrivacyOnlineAPIService.class);
                apiIntent.putExtra(PrivacyOnlineAPIService.PARAM_USERNAME, inputTextUsername.getText().toString());
                apiIntent.putExtra(PrivacyOnlineAPIService.PARAM_PASSWORD, inputTextPassword.getText().toString());
                apiIntent.setAction(PrivacyOnlineAPIService.ACTION_VERIFY_USERNAME);
                startService(apiIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void startLocationActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
    }

    private SharedPreferences getPreferences() {
        return getSharedPreferences(PRIVACYONLINE_PREFERENCES, MODE_PRIVATE);
    }

    private void updateSpinnerValues(ArrayList<VPNLocation> locationList) {
        VPNLocationAdapter locationAdapter
                = new VPNLocationAdapter(SetupActivity.this, android.R.layout.simple_spinner_item, locationList);

        Spinner defaulVPNLocationSpinner = (Spinner) findViewById(R.id.input_spinner_default_vpn_location);
        defaulVPNLocationSpinner.setAdapter(locationAdapter);
        defaulVPNLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


    // Implement a receiver so we can use the APIService to check login details.
    public class VerifyUserAccountReceiver extends BroadcastReceiver {

        public static final String API_RESPONSE =
                "online.privacy.privacyonline.intent.action.RESPONSE_VERIFY_ACCOUNT";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG_HOME, "Received Service Broadcast");
            boolean checkResult = intent.getBooleanExtra(PrivacyOnlineAPIService.CHECK_RESULT , false);

            // If the details were good, launch the ConnectionActivity Activity.
            if (checkResult) {
                Log.i(LOG_TAG_HOME, "User Account verified");
                startLocationActivity();
            } else {
                Log.i(LOG_TAG_HOME, "User Account verification failed");
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
            Log.i(LOG_TAG_HOME, "Received Service Broadcast");
            ArrayList<VPNLocation> locationList
                    = intent.getParcelableArrayListExtra(PrivacyOnlineAPIService.CHECK_RESULT);
            updateSpinnerValues(locationList);
        }
    }
}
