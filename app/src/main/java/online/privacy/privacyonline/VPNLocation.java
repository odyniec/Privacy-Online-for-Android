package online.privacy.privacyonline;

/**
 *  Custom Object for holding VPN Location data.
 *
 *  Used in the custom Spinner Adapter so we don't have to split up the human name and the hostname.*
 *
 */
public class VPNLocation {

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
}
