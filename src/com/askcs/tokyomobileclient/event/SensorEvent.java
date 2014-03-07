package com.askcs.tokyomobileclient.event;

import com.askcs.commons.entity.SensorState;

public class SensorEvent {

    private SensorState sensorState;

    /**
     * @param state
     */
    public SensorEvent(SensorState sensorState) {
        this.sensorState = sensorState;
    }




    /**
     * @return the state
     */
    public SensorState getSensorState() {
        return sensorState;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setSensorState(SensorState sensorState) {
        this.sensorState = sensorState;
    }
}
