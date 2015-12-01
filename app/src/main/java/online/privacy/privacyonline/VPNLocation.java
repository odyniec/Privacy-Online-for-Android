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

    private String _hostname;    // Endpoint connection hostname.
    private String _label;       // Human readable location name.
    private String _flag;        // Asset filename for flag icon.
    private String _headerImage; // Asset filename for the header image.

    public VPNLocation() {}
    public VPNLocation(String label, String hostname) {
        this._label = label;
        this._hostname = hostname;
    }
    public VPNLocation(String label, String hostname, String flag, String headerImage) {
        this._label = label;
        this._hostname = hostname;
        this._flag = flag;
        this._headerImage = headerImage;
    }


    public void setHostname(String hostname) {
        this._hostname = hostname;
    }
    public void setLabel(String label) {
        this._label = label;
    }
    public void setFlag(String flagFile) {
        this._flag = flagFile;
    }
    public void setHeaderImage(String headerImageFile) {
        this._headerImage = headerImageFile;
    }


    public String getHostname() {
        return this._hostname;
    }
    public String getLabel() {
        return this._label;
    }
    public String getFlag() {
        return this._flag;
    }
    public String getHeaderImage() {
        return this._headerImage;
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
        this._hostname    = in.readString();
        this._label       = in.readString();
        this._flag        = in.readString();
        this._headerImage = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this._hostname);
        out.writeString(this._label);
        out.writeString(this._flag);
        out.writeString(this._headerImage);
    }
}
