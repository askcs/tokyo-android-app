package com.askcs.tokyomobileclient.agent.intf;

import java.util.List;

import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Optional;
import com.askcs.commons.agent.intf.SensorAgentIntf;
import com.askcs.tokyomobileclient.model.BluetoothScanResult;

public interface BluetoothProximitySensorAgentIntf extends SensorAgentIntf {

    /**
     * Processes the BluetoothScanResults and determines the sensorState if
     * sufficient samples are provided.
     * 
     * @param bluetoothScanResults
     */
    public abstract void processSensorResult(
            @Optional @Name("bluetoothScanResults") List<BluetoothScanResult> bluetoothScanResults);

    /**
     * Set the bluetooth device address check against
     * 
     * @param address
     */
    public abstract void setRemoteDeviceAddress(@Name("address") String address);

    /**
     * returns the bluetooth device address
     * 
     * @return address
     */
    public abstract String getRemoteDeviceAddress();

}