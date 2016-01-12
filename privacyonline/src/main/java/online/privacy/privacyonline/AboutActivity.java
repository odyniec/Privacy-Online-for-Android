package online.privacy.privacyonline;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Version in the version string, use: BuildConfig.VERSION_NAME
        TextView versionText = (TextView) findViewById(R.id.about_text_version);
        versionText.setText(BuildConfig.VERSION_NAME);

        // Activate the links in the About Text. We need to call
        // setMovementMethod(LinkMovementMethod.getInstance()) on each, so do it iteratively in case
        // we add more links later. No-one likes copy-pasta.
        int[] textViewsToActivate = new int[] {
            R.id.about_text_sourcecode,
            R.id.about_text_openvpn_for_android
        };
        for (int textViewId : textViewsToActivate) {
            TextView textView = (TextView) findViewById(textViewId);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
