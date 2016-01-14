/*
 * Copyright (c) 2012-2014 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

//import online.privacy.privacyonline.R;
import online.privacy.privacyonline.R;
import de.blinkt.openvpn.VpnProfile;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;


import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

public class X509Utils {
	public static Certificate getCertificateFromFile(String certfilename) throws FileNotFoundException, CertificateException {
		CertificateFactory certFact = CertificateFactory.getInstance("X.509");

		InputStream inStream;

		if(VpnProfile.isEmbedded(certfilename)) {
            // The java certifcate reader is ... kind of stupid
            // It does NOT ignore chars before the --BEGIN ...
            int subIndex = certfilename.indexOf("-----BEGIN CERTIFICATE-----");
            subIndex = Math.max(0,subIndex);
			inStream = new ByteArrayInputStream(certfilename.substring(subIndex).getBytes());


        } else {
			inStream = new FileInputStream(certfilename);
        }


		return certFact.generateCertificate(inStream);
	}
}
