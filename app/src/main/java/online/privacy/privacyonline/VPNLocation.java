package online.privacy.privacyonline;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *  Custom Object for holding VPN Location data.
 *
 *  Used in the custom Spinner Adapter so we don't have to split up the human name and the hostname.*
 *
 */
public class VPNLocation implements Parcelable {

    private String _hostname; // Endpoint connection hostname.
    private String _label;    // Human readable location name.

    public VPNLocation() {}
    public VPNLocation(String label, String hostname) {
        this._label = label;
        this._hostname = hostname;
    }

    public void setHostname(String hostname) {
        this._hostname = hostname;
    }

    public void setLabel(String label) {
        this._label = label;
    }

    public String getHostname() {
        return this._hostname;
    }

    public String getLabel() {
        return this._label;
    }

    public static final Parcelable.Creator<VPNLocation> CREATOR
            = new Parcelable.Creator<VPNLocation>() {

        public VPNLocation createFromParcel(Parcel in) {
            return new VPNLocation(in);
        }
        public VPNLocation[] newArray(int size) {
            return new VPNLocation[size];
        }
    };

    private VPNLocation(Parcel in) {
        this._hostname = in.readString();
        this._label    = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this._hostname);
        out.writeString(this._label);
    }
}
