/*
 * Copyright (c) 2012-2014 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

// Integrate this lib with PrivacyOnline App.
import online.privacy.R;


public class VPNLaunchHelper {
    private static final String MININONPIEVPN = "nopie_openvpn";
    private static final String MINIPIEVPN = "pie_openvpn";
    private static final String OVPNCONFIGFILE = "android.conf";


    static private String writeMiniVPN(Context context) {
        String[] abis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            abis = getSupportedABIsLollipop();
        else
            //noinspection deprecation
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};

        String nativeAPI = NativeUtils.getNativeAPI();
        if (!nativeAPI.equals(abis[0])) {
            VpnStatus.logWarning(R.string.abi_mismatch, Arrays.toString(abis), nativeAPI);
            abis = new String[] {nativeAPI};
        }

        for (String abi: abis) {

            File vpnExecutable = new File(context.getCacheDir(), getMiniVPNExecutableName() + "." + abi);
            if ((vpnExecutable.exists() && vpnExecutable.canExecute()) || writeMiniVPNBinary(context, abi, vpnExecutable)) {
                return vpnExecutable.getPath();
            }
        }

        return null;
	}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String[] getSupportedABIsLollipop() {
        return Build.SUPPORTED_ABIS;
    }

    private static String getMiniVPNExecutableName()
    {
        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.JELLY_BEAN)
            return MINIPIEVPN;
        else
            return MININONPIEVPN;
    }


    public static String[] replacePieWithNoPie(String[] mArgv)
    {
        mArgv[0] = mArgv[0].replace(MINIPIEVPN, MININONPIEVPN);
        return mArgv;
    }


    public static String[] buildOpenvpnArgv(Context c) {
        Vector<String> args = new Vector<>();

        // Add fixed paramenters
        //args.add("/data/data/de.blinkt.openvpn/lib/openvpn");
        args.add(writeMiniVPN(c));

        args.add("--config");
        args.add(getConfigFilePath(c));

        return args.toArray(new String[args.size()]);
    }

    private static boolean writeMiniVPNBinary(Context context, String abi, File mvpnout) {
        try {
            InputStream mvpn;

            try {
                mvpn = context.getAssets().open(getMiniVPNExecutableName() + "." + abi);
            }
            catch (IOException errabi) {
                VpnStatus.logInfo("Failed getting assets for archicture " + abi);
                return false;
            }


            FileOutputStream fout = new FileOutputStream(mvpnout);

            byte buf[]= new byte[4096];

            int lenread = mvpn.read(buf);
            while(lenread> 0) {
                fout.write(buf, 0, lenread);
                lenread = mvpn.read(buf);
            }
            fout.close();

            if(!mvpnout.setExecutable(true)) {
                VpnStatus.logError("Failed to make OpenVPN executable");
                return false;
            }


            return true;
        } catch (IOException e) {
            VpnStatus.logException(e);
            return false;
        }

    }


    public static void startOpenVpn(VpnProfile startprofile, Context context) {
        if(writeMiniVPN(context)==null) {
            Log.e("VPNLaunchHelper", "Error writing minivpn binary");
            VpnStatus.logError("Error writing minivpn binary");
            return;
        }

        VpnStatus.logInfo(R.string.building_configration);
        Log.w("VPNLaunchHelper", "Building config...");

        Intent startVPN = startprofile.prepareStartService(context);
        if(startVPN!=null) {
            Log.w("VPNLaunchHelper", "Starting OpenService: "+startVPN.toString());
            context.startService(startVPN);
        } else {
            Log.e("VPNLaunchHelper", "Failed to create intent for VPN Service");
        }
    }


    public static String getConfigFilePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + "/" + OVPNCONFIGFILE;
    }

}
