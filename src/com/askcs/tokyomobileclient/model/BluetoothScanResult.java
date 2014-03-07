package com.askcs.tokyomobileclient.model;

public class BluetoothScanResult {
    /**
     * @param address
     * @param rssi
     */
    public BluetoothScanResult(String address, short rssi) {
        this.address = address;
        this.rssi = rssi;
    }

    private String address;
    private short rssi;

    /**
     * @return the rssi
     */
    public short getRssi() {
        return rssi;
    }

    /**
     * @param rssi
     *            the rssi to set
     */
    public void setRssi(short rssi) {
        this.rssi = rssi;
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
}
