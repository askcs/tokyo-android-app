package com.askcs.tokyomobileclient.event;


public class WifiBssidsSetEvent {

    boolean successful;
    String[] bssids;

    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * @param successful
     *            the successful to set
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * @param successful
     * @param ssids
     */
    public WifiBssidsSetEvent(boolean successful, String[] ssids) {
        super();
        this.successful = successful;
        this.bssids = ssids;
    }

    /**
     * @return the bssids
     */
    public String[] getBssids() {
        return bssids;
    }

    /**
     * @param bssids
     *            the bssids to set
     */
    public void setBssids(String[] bssids) {
        this.bssids = bssids;
    }



}
