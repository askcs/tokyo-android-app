package com.askcs.tokyomobileclient.agent;

import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.askcs.commons.agent.SensorAgent;
import com.askcs.commons.entity.SensorState;
import com.askcs.tokyomobileclient.TokyoApplication;
import com.askcs.tokyomobileclient.agent.intf.BluetoothProximitySensorAgentIntf;
import com.askcs.tokyomobileclient.event.BluetoothProximityEvent;
import com.askcs.tokyomobileclient.event.BusProvider;
import com.askcs.tokyomobileclient.model.BluetoothScanResult;
import com.askcs.tokyomobileclient.services.BluetoothService;

public class BluetoothProximitySensorAgent extends SensorAgent implements BluetoothProximitySensorAgentIntf {
    private static final String ADDED_RSSI_VALUE_STATE_KEY = "addedRssiValue";
    private static final String REMOTE_DEVICE_ADDRESS_STATE_KEY = "remoteDeviceAddress";
    private static final String SAMPLE_COUNT_STATE_KEY = "sampleCount";
    private static final int MAX_SAMPLE_COUNT = 3;
    private static final int RSSI_THRESHOLD = -75;

    @Override
    public void monitor() {
        // Start sensor service
        Context context = TokyoApplication.getContext();
        context.startService(new Intent(context, BluetoothService.class));
    }

    @Override
    public void sendSensorState(SensorState sensorState) {
        try {
            getSensorStateAgent().setBluetoothProximityState(sensorState);
            setLastSendTime(System.currentTimeMillis());
        } catch (Exception e) {
            System.out.println("failed to get SensorStateAgent");
            e.printStackTrace();
        }
    }

    private BluetoothScanResult getResultWithAddress(
            List<BluetoothScanResult> bluetoothScanResults, String address) {
        for (BluetoothScanResult bluetoothScanResult : bluetoothScanResults) {
            if (address.equalsIgnoreCase(bluetoothScanResult.getAddress())) {
                return bluetoothScanResult;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.askcs.tokyomobileclient.agents.BluetoothProximitySensorAgentIntf#processSensorResult(java.util.List)
     */
    @Override
    public void processSensorResult(List<BluetoothScanResult> bluetoothScanResults) {
        String remoteDeviceAddress = getRemoteDeviceAddress();
        int sampleCount = getSampleCount();
        sampleCount++;
        int addedRssiValue = getAddedRssiValue();
        if (remoteDeviceAddress == null || bluetoothScanResults == null) {
            if (processState(SensorState.UNKNOWN)) {
                BusProvider.getBus().post(new BluetoothProximityEvent(SensorState.UNKNOWN));
            }
        } else {
            BluetoothScanResult foundBluetoothScanResult = getResultWithAddress(
                    bluetoothScanResults, remoteDeviceAddress);
            if (foundBluetoothScanResult != null) {
                addedRssiValue = addedRssiValue + foundBluetoothScanResult.getRssi();
            } else {
                // Add out of range value
                addedRssiValue = addedRssiValue + (RSSI_THRESHOLD * 2);
            }

            if (sampleCount >= MAX_SAMPLE_COUNT) {
                SensorState sensorState = SensorState.UNKNOWN;
                double averageRssi = addedRssiValue / MAX_SAMPLE_COUNT;
                if (averageRssi > RSSI_THRESHOLD) {
                    sensorState = SensorState.AVAILABLE;
                } else {
                    sensorState = SensorState.UNAVAILABLE;
                }
                if (processState(sensorState)) {
                    BusProvider.getBus().post(new BluetoothProximityEvent(sensorState));
                }
                sampleCount = 0;
                addedRssiValue = 0;
            }
        }

        setSampleCount(sampleCount);
        setAddedRssiValue(addedRssiValue);

    }


    /* (non-Javadoc)
     * @see com.askcs.tokyomobileclient.agents.BluetoothProximitySensorAgentIntf#setRemoteDeviceAddress(java.lang.String)
     */
    @Override
    public void setRemoteDeviceAddress(String address) {
        getState().put(REMOTE_DEVICE_ADDRESS_STATE_KEY, address);
    }

    /* (non-Javadoc)
     * @see com.askcs.tokyomobileclient.agents.BluetoothProximitySensorAgentIntf#getRemoteDeviceAddress()
     */
    @Override
    public String getRemoteDeviceAddress() {
        try {
            return getState().get(REMOTE_DEVICE_ADDRESS_STATE_KEY, String.class);
        } catch (Exception e) {
            // failed to get state return default value
            return null;
        }
    }

    private void setSampleCount(int sampleCount) {
        getState().put(SAMPLE_COUNT_STATE_KEY, sampleCount);
    }

    private int getSampleCount() {
        try {
            return getState().get(SAMPLE_COUNT_STATE_KEY, Integer.class);
        } catch (Exception e) {
            // failed to get state return default value
            return 0;
        }
    }

    private void setAddedRssiValue(int addedRssiValue) {
        getState().put(ADDED_RSSI_VALUE_STATE_KEY, addedRssiValue);
    }

    private int getAddedRssiValue() {
        try {
            return getState().get(ADDED_RSSI_VALUE_STATE_KEY, Integer.class);
        } catch (Exception e) {
            // failed to get state return default value
            return 0;
        }
    }

}
