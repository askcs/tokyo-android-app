package com.askcs.tokyomobileclient.agent.intf;

import com.almende.eve.rpc.annotation.Name;
import com.almende.eve.rpc.annotation.Optional;
import com.askcs.commons.agent.intf.SensorAgentIntf;

public interface MotionSensorAgentIntf extends SensorAgentIntf {

    /**
     * Processes the motionSensor values and determines the sensorState if
     * sufficient samples are provided.
     * 
     * @param values
     */
    public abstract void processSensorResult(@Optional @Name("values") float[] values);

}