package com.askcs.tokyomobileclient.agent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;

import com.almende.util.TypeUtil;
import com.askcs.commons.agent.SensorAgent;
import com.askcs.commons.entity.SensorState;
import com.askcs.tokyomobileclient.TokyoApplication;
import com.askcs.tokyomobileclient.agent.intf.WifiProximitySensorAgentIntf;
import com.askcs.tokyomobileclient.event.BusProvider;
import com.askcs.tokyomobileclient.event.WifiProximityEvent;
import com.askcs.tokyomobileclient.services.WifiService;

public class WifiProximitySensorAgent extends SensorAgent implements WifiProximitySensorAgentIntf {
    private static final String TIMES_IN_RANGE_STATE_KEY = "timesInRange";
    private static final String SAMPLE_COUNT_STATE_KEY = "sampleCount";
    private static final String BSSIDS_STATE_KEY = "bssids";
    private static final int MAX_SAMPLE_COUNT = 2;

    private boolean containsWantedBssid(List<ScanResult> scanResults) {
        Set<String> wantedBssids = getBSSIDs();
        if (wantedBssids.size() > 0) {
            for (ScanResult scanResult : scanResults) {
                for (String wantedBssid : wantedBssids) {
                    if (wantedBssid.equalsIgnoreCase(scanResult.BSSID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.askcs.tokyomobileclient.agent.WifiProximitySensorAgentIntf#getBSSIDs()
     */
    @Override
    public Set<String> getBSSIDs() {
        try {
            Set<String> bssids = getState().get(BSSIDS_STATE_KEY, new TypeUtil<Set<String>>() {
            });
            if (bssids != null) {
                return bssids;
            }
        } catch (Exception e) {
            // failed to get state return default empty HashSet
        }
        return new HashSet<String>();
    }

    private int getSampleCount() {
        try {
            return getState().get(SAMPLE_COUNT_STATE_KEY, Integer.class);
        } catch (Exception e) {
            // failed to get state return default value
            return 0;
        }
    }

    private int getTimesInRange() {
        try {
            return getState().get(TIMES_IN_RANGE_STATE_KEY, Integer.class);
        } catch (Exception e) {
            // failed to get state return default value
            return 0;
        }
    }

    @Override
    public void monitor() {
        // Start sensor service
        Context context = TokyoApplication.getContext();
        context.startService(new Intent(context, WifiService.class));
    }

    /* (non-Javadoc)
     * @see com.askcs.tokyomobileclient.agent.WifiProximitySensorAgentIntf#processSensorResult(java.util.List)
     */
    @Override
    public void processSensorResult(List<ScanResult> scanResults) {
        int timesInRange = getTimesInRange();
        int sampleCount = getSampleCount();

        sampleCount++;

        SensorState sensorState;
        if (scanResults == null || getBSSIDs().size() < 1) {
            sensorState = SensorState.UNKNOWN;
            if (processState(sensorState)) {
                BusProvider.getBus().post(new WifiProximityEvent(sensorState));
            }
        } else {
            if (containsWantedBssid(scanResults)) {
                timesInRange++;
            }

            if (sampleCount >= MAX_SAMPLE_COUNT) {
                if (timesInRange >= (MAX_SAMPLE_COUNT / 2)) {
                    sensorState = SensorState.AVAILABLE;
                } else {
                    sensorState = SensorState.UNAVAILABLE;
                }

                if (processState(sensorState)) {
                    BusProvider.getBus().post(new WifiProximityEvent(sensorState));
                }

                timesInRange = 0;
                sampleCount = 0;
            }
            setTimesInRange(timesInRange);
            setSampleCount(sampleCount);

        }
    }

    @Override
    public void sendSensorState(SensorState sensorState) {
        try {
            getSensorStateAgent().setWifiProximityState(sensorState);
            setLastSendTime(System.currentTimeMillis());
        } catch (Exception e) {
            System.out.println("failed to get SensorStateAgent");
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.askcs.tokyomobileclient.agent.WifiProximitySensorAgentIntf#setBSSIDs(java.util.Set)
     */
    @Override
    public void setBSSIDs(Set<String> bssids) {
        getState().put(BSSIDS_STATE_KEY, bssids);
    }

    private void setSampleCount(int sampleCount) {
        getState().put(SAMPLE_COUNT_STATE_KEY, sampleCount);
    }

    private void setTimesInRange(int timesInRange) {
        getState().put(TIMES_IN_RANGE_STATE_KEY, timesInRange);
    }

}
