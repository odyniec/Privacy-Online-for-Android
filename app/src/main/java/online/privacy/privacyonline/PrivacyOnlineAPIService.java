package online.privacy.privacyonline;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PrivacyOnlineAPIService extends IntentService {

    private static final String LOG_TAG_API_SERVICE = "online.privacy.privacyonline.service.API";
    public static final String ACTION_VERIFY_USERNAME
            = "online.privacy.privacyonline.action.VERIFY_USERNAME";

    public static final String PARAM_USERNAME = "online.privacy.privacyonline.extra.USERNAME";
    public static final String PARAM_PASSWORD = "online.privacy.privacyonline.extra.PASSWORD";
    public static final String CHECK_RESULT   = "online.privacy.privacyonline.extra.CHECK_RESULT";
    public static final String QUERY_TYPE     = "online.privacy.privacyonline.extra.QUERY_TYPE";

    public PrivacyOnlineAPIService() {

        super("PrivacyOnlineAPIService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionVerifyUserAccount(Context context, String username, String password) {
        Intent intent = new Intent(context, PrivacyOnlineAPIService.class);
        intent.setAction(ACTION_VERIFY_USERNAME);
        intent.putExtra(PARAM_USERNAME, username);
        intent.putExtra(PARAM_PASSWORD, password);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.i(LOG_TAG_API_SERVICE, "Attempting to Handle Service");
            final String action = intent.getAction();
            Log.i(LOG_TAG_API_SERVICE, "Service action: "+action);
            if (ACTION_VERIFY_USERNAME.equals(action)) {
                Log.i(LOG_TAG_API_SERVICE, "Action is to verify user account");
                final String username = intent.getStringExtra(PARAM_USERNAME);
                final String password = intent.getStringExtra(PARAM_PASSWORD);
                handleActionVerifyUserAccount(username, password);
            }
        }
    }

    private void handleActionVerifyUserAccount(String username, String password) {
        Log.i(LOG_TAG_API_SERVICE, "Verify User handler executed.");
        PrivacyOnlineApiRequest requestPrivacyAPI = new PrivacyOnlineApiRequest();
        boolean checkResult = requestPrivacyAPI.verifyUserAccount(username, password);
        broadcastVerifyAccountResult(checkResult);
    }

    private void handleActionGetLocationList() {
        Log.i(LOG_TAG_API_SERVICE, "Get Location List handler executed.");
        PrivacyOnlineApiRequest requestPrivacyAPI = new PrivacyOnlineApiRequest();
        HashMap<String,String> locationList = requestPrivacyAPI.getLocationList();
        broadcastGetLocationListResult(locationList);
    }

    private void broadcastVerifyAccountResult(boolean checkResult) {
        Log.i(LOG_TAG_API_SERVICE, "Broadcasting response.");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SetupActivity.ApiResponseReceiver.API_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CHECK_RESULT, checkResult);
        sendBroadcast(broadcastIntent);
    }
    private void broadcastGetLocationListResult(HashMap<String,String> checkResult) {
        Log.i(LOG_TAG_API_SERVICE, "Broadcasting response.");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SetupActivity.ApiResponseReceiver.API_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CHECK_RESULT, );
        sendBroadcast(broadcastIntent);
    }
}
