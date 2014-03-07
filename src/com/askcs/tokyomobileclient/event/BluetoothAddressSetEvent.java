package com.askcs.tokyomobileclient.event;


public class BluetoothAddressSetEvent {

    boolean successful;
    String address;

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
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     *            the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @param successful
     * @param address
     */
    public BluetoothAddressSetEvent(boolean successful, String address) {
        this.successful = successful;
        this.address = address;
    }


}
