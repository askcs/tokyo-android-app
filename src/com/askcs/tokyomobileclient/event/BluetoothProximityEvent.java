package com.askcs.tokyomobileclient.event;

import com.askcs.commons.entity.SensorState;

public class BluetoothProximityEvent extends SensorEvent {

    public BluetoothProximityEvent(SensorState sensorState) {
        super(sensorState);
    }

}
