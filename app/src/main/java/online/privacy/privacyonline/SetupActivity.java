package online.privacy.privacyonline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;


public class SetupActivity extends AppCompatActivity {

    final static private int REQUEST_CODE_STANDARD_OPERATION = 1;
    final static private String LOG_TAG_HOME = "online.privacy.privacyonline.SetupActivity";
    final private Context contextHome = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LoginDetailsCheckReceiver receiver;

        // Register the IntentService Listener.
        IntentFilter filter = new IntentFilter(SetupActivity.LoginDetailsCheckReceiver.ACTION_REPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new LoginDetailsCheckReceiver();
        registerReceiver(receiver, filter);

        Button buttonLogin = (Button) findViewById(R.id.login_button);
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

    private void startLocationActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
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

    // Implement a receiver so we can use the APIService to check login details.
    public class LoginDetailsCheckReceiver extends BroadcastReceiver {

        public static final String ACTION_REPONSE =
                "online.privacy.privacyonline.intent.action.USERCHECK_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG_HOME, "Received Service Broadcast");
            boolean checkResult = intent.getBooleanExtra(PrivacyOnlineAPIService.CHECK_RESULT, false);

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


}
