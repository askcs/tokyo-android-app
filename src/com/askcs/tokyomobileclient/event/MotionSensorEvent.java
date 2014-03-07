package com.askcs.tokyomobileclient.event;

import com.askcs.commons.entity.SensorState;

public class MotionSensorEvent extends SensorEvent {

    public MotionSensorEvent(SensorState sensorState) {
        super(sensorState);
    }

}
