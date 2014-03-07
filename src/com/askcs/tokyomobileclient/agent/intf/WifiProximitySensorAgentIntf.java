package com.askcs.tokyomobileclient.agent.intf;

import java.util.List;
import java.util.Set;

import android.net.wifi.ScanResult;

import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Optional;

public interface WifiProximitySensorAgentIntf {

    /**
     * Get a set with BSSIDs
     * 
     * @return BSSIDs
     */
    public abstract Set<String> getBSSIDs();

    /**
     * Processes the scanResults and determines the sensorState if sufficient
     * samples are provided.
     * 
     * @param scanResults
     */
    public abstract void processSensorResult(
            @Optional @Name("scanResults") List<ScanResult> scanResults);

    /**
     * Sets the BSSIDs to be checked against the scanResults
     * 
     * @param bssids
     */
    public abstract void setBSSIDs(@Name("bssids") Set<String> bssids);

}