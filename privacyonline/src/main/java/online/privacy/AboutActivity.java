package online.privacy;
/**
 * Simple UI activity for displaying relevant info on licencing for the different libs used within
 * the Privacy Online for Android app.
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
 *
 * @author James Ronan <jim@dev.uk2.net>
 */
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

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
