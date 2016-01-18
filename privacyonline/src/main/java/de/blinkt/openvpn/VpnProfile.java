/*
 * Copyright (c) 2012-2014 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import de.blinkt.openvpn.core.Connection;
import de.blinkt.openvpn.core.NativeUtils;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import online.privacy.R;

public class VpnProfile implements Serializable, Cloneable {
    // Note that this class cannot be moved to core where it belongs since
    // the profile loading depends on it being here
    // The Serializable documentation mentions that class name change are possible
    // but the how is unclear
    //
    // Don't change this, not all parts of the program use this constant
    public static final String INLINE_TAG = "[[INLINE]]";
    public static final String DISPLAYNAME_TAG = "[[NAME]]";

    private static final long serialVersionUID = 7085688938959334563L;
    public static final int MAXLOGLEVEL = 4;
    public static final int DEFAULT_MSSFIX_SIZE = 1450;
    public static String DEFAULT_DNS1 = "8.8.8.8";
    public static String DEFAULT_DNS2 = "8.8.4.4";

    public transient String mTransientPCKS12PW = null;


    public static final int TYPE_CERTIFICATES = 0;
    public static final int TYPE_PKCS12 = 1;
    public static final int TYPE_KEYSTORE = 2;
    public static final int TYPE_USERPASS = 3;
    public static final int TYPE_STATICKEYS = 4;
    public static final int TYPE_USERPASS_CERTIFICATES = 5;
    public static final int TYPE_USERPASS_PKCS12 = 6;
    public static final int TYPE_USERPASS_KEYSTORE = 7;
    public static final int X509_VERIFY_TLSREMOTE = 0;
    public static final int X509_VERIFY_TLSREMOTE_COMPAT_NOREMAPPING = 1;
    public static final int X509_VERIFY_TLSREMOTE_DN = 2;
    public static final int X509_VERIFY_TLSREMOTE_RDN = 3;
    public static final int X509_VERIFY_TLSREMOTE_RDN_PREFIX = 4;
    // variable named wrong and should haven beeen transient
    // but needs to keep wrong name to guarante loading of old
    // profiles
    public int mAuthenticationType = TYPE_USERPASS;
    public String mName;
    public String mClientCertFilename;
    public String mTLSAuthDirection = "";
    public String mTLSAuthFilename;
    public String mClientKeyFilename;
    public String mCaFilename;
    public boolean mUseLzo = true;
    public String mPKCS12Filename;
    public boolean mUseTLSAuth = false;

    public String mDNS1 = DEFAULT_DNS1;
    public String mDNS2 = DEFAULT_DNS2;
    public String mIPv4Address;
    public boolean mOverrideDNS = false;
    public String mSearchDomain = "";
    public boolean mUseDefaultRoute = true;
    public boolean mUsePull = true;
    public String mCustomRoutes;
    public boolean mCheckRemoteCN = false;
    public boolean mExpectTLSCert = false;
    public String mRemoteCN = "";
    public String mPassword = "";
    public String mUsername = "";
    public boolean mUseRandomHostname = false;
    public boolean mUseFloat = false;
    public boolean mUseCustomConfig = false;
    public String mCustomConfigOptions = "";
    public String mVerb = "1";  //ignored
    public String mCipher = "AES-256-CBC";
    public boolean mNobind = false;
    public boolean mUseDefaultRoutev6 = true;
    public String mCustomRoutesv6 = "";
    public boolean mPersistTun = false;
    public String mConnectRetryMax = "5";
    public String mConnectRetry = "5";
    public String mAuth = "SHA256";
    public int mX509AuthType = X509_VERIFY_TLSREMOTE_RDN;
    private transient PrivateKey mPrivateKey;
    // Public attributes, since I got mad with getter/setter
    // set members to default values
    private UUID mUuid;
    public boolean mAllowLocalLAN;
    public String mExcludedRoutes;
    public int mMssFix =0; // -1 is default,
    public Connection[] mConnections = new Connection[0];
    public boolean mRemoteRandom=false;
    public HashSet<String> mAllowedAppsVpn = new HashSet<String>();
    public boolean mAllowedAppsVpnAreDisallowed = true;

    /* Options no long used in new profiles */
    public String mServerName = "openvpn.blinkt.de";
    public String mServerPort = "1194";

    public VpnProfile(String name) {
        mUuid = UUID.randomUUID();
        mName = name;

        mConnections = new Connection[1];
        mConnections[0]  = new Connection();
    }

    public static String openVpnEscape(String unescaped) {
        if (unescaped == null)
            return null;
        String escapedString = unescaped.replace("\\", "\\\\");
        escapedString = escapedString.replace("\"", "\\\"");
        escapedString = escapedString.replace("\n", "\\n");

        if (escapedString.equals(unescaped) && !escapedString.contains(" ") &&
                !escapedString.contains("#") && !escapedString.contains(";")
                && !escapedString.equals(""))
            return unescaped;
        else
            return '"' + escapedString + '"';
    }

    public void clearDefaults() {
        mServerName = "unknown";
        mUsePull = false;
        mUseLzo = false;
        mUseDefaultRoute = false;
        mUseDefaultRoutev6 = false;
        mExpectTLSCert = false;
        mCheckRemoteCN = false;
        mPersistTun = false;
        mAllowLocalLAN = true;
        mMssFix = 0;
    }

    public UUID getUUID() {
        return mUuid;

    }

    public String getName() {
        if (mName==null)
            return "No profile name";
        return mName;
    }

    public String getConfigFile(Context context, boolean configForOvpn3) {

        File cacheDir = context.getCacheDir();
        String cfg = "";

        // Enable management interface
        cfg += "# Enables connection to GUI\n";
        cfg += "management ";

        cfg += cacheDir.getAbsolutePath() + "/" + "mgmtsocket";
        cfg += " unix\n";
        cfg += "management-client\n";
        // Not needed, see updated man page in 2.3
        //cfg += "management-signal\n";
        cfg += "management-query-passwords\n";
        cfg += "management-hold\n\n";

        if (!configForOvpn3)
            cfg += String.format("setenv IV_GUI_VER %s \n", openVpnEscape(getVersionEnvString(context)));

        cfg += "machine-readable-output\n";

        // Users are confused by warnings that are misleading...
        cfg += "ifconfig-nowarn\n";

        cfg += "client\n";

        //cfg += "verb " + mVerb + "\n";
        cfg += "verb " + MAXLOGLEVEL + "\n";

        if (mConnectRetryMax == null) {
            mConnectRetryMax = "5";
        }

        if (!mConnectRetryMax.equals("-1"))
            cfg += "connect-retry-max " + mConnectRetryMax + "\n";

        if (mConnectRetry == null)
            mConnectRetry = "5";


        cfg += "connect-retry " + mConnectRetry + "\n";

        cfg += "resolv-retry 60\n";


        // We cannot use anything else than tun
        cfg += "dev tun\n";


        boolean canUsePlainRemotes = true;
        if (mConnections.length==1) {
            cfg += mConnections[0].getConnectionBlock();
        } else {
            for (Connection conn : mConnections) {
                canUsePlainRemotes = canUsePlainRemotes && conn.isOnlyRemote();
            }

            if (mRemoteRandom)
                cfg+="remote-random\n";

            if (canUsePlainRemotes) {
                for (Connection conn : mConnections) {
                    if (conn.mEnabled) {
                        cfg += conn.getConnectionBlock();
                    }
                }
            }
        }

        cfg += "auth-user-pass\n";
        cfg += insertFileData("ca", mCaFilename);
        cfg += "comp-lzo\n";

        String routes = "";
        routes += "route 0.0.0.0 0.0.0.0 vpn_gateway\n";
        cfg += "route-ipv6 ::/0\n";
        cfg += routes;

        cfg += "cipher " + mCipher + "\n";
        cfg += "auth " + mAuth + "\n";

        return cfg;
    }

    public String getVersionEnvString(Context c) {
        String version = "unknown";
        try {
            PackageInfo packageinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = packageinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            VpnStatus.logException(e);
        }
        return String.format(Locale.US, "%s %s", c.getPackageName(), version);

    }

    //! Put inline data inline and other data as normal escaped filename
    public static String insertFileData(String cfgentry, String filedata) {
        if (filedata == null) {
            // TODO: generate good error
            return String.format("%s %s\n", cfgentry, "missing");
        } else if (isEmbedded(filedata)) {
            String dataWithOutHeader = getEmbeddedContent(filedata);
            return String.format(Locale.ENGLISH, "<%s>\n%s\n</%s>\n", cfgentry, dataWithOutHeader, cfgentry);
        } else {
            return String.format(Locale.ENGLISH, "%s %s\n", cfgentry, openVpnEscape(filedata));
        }
    }

    public Intent prepareStartService(Context context) {
        Log.w("VpnProfile", "prepareStartService() called.");
        Intent intent = getStartServiceIntent(context);

        try {
            Log.w("VpnProfile", "Writing ovpn config file.");
            FileWriter cfg = new FileWriter(VPNLaunchHelper.getConfigFilePath(context));
            cfg.write(getConfigFile(context, false));
            cfg.flush();
            cfg.close();
        } catch (IOException e) {
            VpnStatus.logException(e);
            Log.w("VpnProfile", "Config writer failed: " + e.toString());
        }

        Log.w("VpnProfile", "Returning service intent to start");
        return intent;
    }

    public Intent getStartServiceIntent(Context context) {
        String prefix = context.getPackageName();

        Intent intent = new Intent(context, OpenVPNService.class);
        intent.putExtra(prefix + ".ARGV", VPNLaunchHelper.buildOpenvpnArgv(context));
        intent.putExtra(prefix + ".profileUUID", mUuid.toString());

        ApplicationInfo info = context.getApplicationInfo();
        intent.putExtra(prefix + ".nativelib", info.nativeLibraryDir);
        return intent;
    }

    public static String getEmbeddedContent(String data)
    {
        if (!data.contains(INLINE_TAG))
            return data;

        int start = data.indexOf(INLINE_TAG) + INLINE_TAG.length();
        return data.substring(start);
    }

    public static boolean isEmbedded(String data) {
        if (data==null)
            return false;
        if (data.startsWith(INLINE_TAG) || data.startsWith(DISPLAYNAME_TAG))
            return true;
        else
            return false;
    }

    /* This method is called when OpenVPNService is restarted */
    public void checkForRestart(final Context context) {}

    @Override
    protected VpnProfile clone() throws CloneNotSupportedException {
        VpnProfile copy = (VpnProfile) super.clone();
        copy.mUuid = UUID.randomUUID();
        copy.mConnections = new Connection[mConnections.length];
        int i=0;
        for (Connection conn: mConnections) {
            copy.mConnections[i++]=conn.clone();
        }
        copy.mAllowedAppsVpn = (HashSet<String>) mAllowedAppsVpn.clone();
        return copy;
    }

    public VpnProfile copy(String name) {
        try {
            VpnProfile copy = (VpnProfile) clone();
            copy.mName = name;
            return copy;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int checkProfile(Context context) {

        boolean noRemoteEnabled = true;
        for (Connection c : mConnections)
            if (c.mEnabled)
                noRemoteEnabled = false;

        if(noRemoteEnabled)
            return R.string.remote_no_server_selected;

        // Everything okay
        return R.string.no_error_found;
    }

    public String getPasswordAuth() {
        return mPassword;
    }

    // Used by the Array Adapter
    @Override
    public String toString() {
        return mName;
    }

    public String getUUIDString() {
        return mUuid.toString();
    }

    public PrivateKey getKeystoreKey() {
        return mPrivateKey;
    }

    public String getSignedData(String b64data) {
        PrivateKey privkey = getKeystoreKey();
        Exception err;

        byte[] data = Base64.decode(b64data, Base64.DEFAULT);

        // The Jelly Bean *evil* Hack
        // 4.2 implements the RSA/ECB/PKCS1PADDING in the OpenSSLprovider
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            return processSignJellyBeans(privkey, data);
        }


        try {

            /* ECB is perfectly fine in this special case, since we are using it for
               the public/private part in the TLS exchange
             */
            @SuppressLint("GetInstance")
            Cipher rsaSigner = Cipher.getInstance("RSA/ECB/PKCS1PADDING");

            rsaSigner.init(Cipher.ENCRYPT_MODE, privkey);

            byte[] signed_bytes = rsaSigner.doFinal(data);
            return Base64.encodeToString(signed_bytes, Base64.NO_WRAP);

        } catch (NoSuchAlgorithmException e) {
            err = e;
        } catch (InvalidKeyException e) {
            err = e;
        } catch (NoSuchPaddingException e) {
            err = e;
        } catch (IllegalBlockSizeException e) {
            err = e;
        } catch (BadPaddingException e) {
            err = e;
        }

        VpnStatus.logError(R.string.error_rsa_sign, err.getClass().toString(), err.getLocalizedMessage());

        return null;

    }

    private String processSignJellyBeans(PrivateKey privkey, byte[] data) {
        Exception err;
        try {
            Method getKey = privkey.getClass().getSuperclass().getDeclaredMethod("getOpenSSLKey");
            getKey.setAccessible(true);

            // Real object type is OpenSSLKey
            Object opensslkey = getKey.invoke(privkey);

            getKey.setAccessible(false);

            Method getPkeyContext = opensslkey.getClass().getDeclaredMethod("getPkeyContext");

            // integer pointer to EVP_pkey
            getPkeyContext.setAccessible(true);
            int pkey = (Integer) getPkeyContext.invoke(opensslkey);
            getPkeyContext.setAccessible(false);

            // 112 with TLS 1.2 (172 back with 4.3), 36 with TLS 1.0
            byte[] signed_bytes = NativeUtils.rsasign(data, pkey);
            return Base64.encodeToString(signed_bytes, Base64.NO_WRAP);

        } catch (NoSuchMethodException e) {
            err = e;
        } catch (IllegalArgumentException e) {
            err = e;
        } catch (IllegalAccessException e) {
            err = e;
        } catch (InvocationTargetException e) {
            err = e;
        } catch (InvalidKeyException e) {
            err = e;
        }
        VpnStatus.logError(R.string.error_rsa_sign, err.getClass().toString(), err.getLocalizedMessage());

        return null;
    }

}




