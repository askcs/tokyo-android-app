package com.askcs.tokyomobileclient.agent;

import android.content.Context;
import android.content.Intent;

import com.askcs.commons.agent.SensorAgent;
import com.askcs.commons.entity.SensorState;
import com.askcs.tokyomobileclient.TokyoApplication;
import com.askcs.tokyomobileclient.agent.intf.MotionSensorAgentIntf;
import com.askcs.tokyomobileclient.event.BusProvider;
import com.askcs.tokyomobileclient.event.MotionSensorEvent;
import com.askcs.tokyomobileclient.services.MotionService;

public class MotionSensorAgent extends SensorAgent implements MotionSensorAgentIntf {

    private static final String AVERAGE_VALUES_STATE_KEY = "averageValues";
    private static final String ADDED_VALUES_STATE_KEY = "addedValues";
    private static final String SAMPLE_COUNT_STATE_KEY = "sampleCount";
    private static final int MAX_SAMPLE_COUNT = 10;

    @Override
    public void monitor() {
        // Start sensor service
        Context context = TokyoApplication.getContext();
        context.startService(new Intent(context, MotionService.class));
    }

    @Override
    public void sendSensorState(SensorState sensorState) {
        try {
            getSensorStateAgent().setMotionState(sensorState);
            setLastSendTime(System.currentTimeMillis());
        } catch (Exception e) {
            System.out.println("failed to get SensorStateAgent");
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.askcs.tokyomobileclient.agent.MotionSensorAgentIntf#processSensorResult(float[])
     */
    @Override
    public void processSensorResult(float[] values) {

        Float[] addedValues = getAddedValues();
        for (int i = 0; i < addedValues.length; i++) {
            addedValues[i] = addedValues[i] + values[i];
        }

        int sampleCount = getSampleCount();
        sampleCount++;

        if (sampleCount >= MAX_SAMPLE_COUNT) {
            Float[] averageValues = new Float[3];

            for (int i = 0; i < averageValues.length; i++) {
                averageValues[i] = addedValues[i] / MAX_SAMPLE_COUNT;
            }

            Float[] oldAverageValues = getAverageValues();
            boolean moved = moved(averageValues, oldAverageValues);

            // store average
            setAverageValues(averageValues);

            // reset counter and addedValues
            setSampleCount(0);
            setAddedValues(new Float[] { 0.0f, 0.0f, 0.0f });

            SensorState sensorState;
            if (moved) {
                sensorState = SensorState.AVAILABLE;
            } else {
                sensorState = SensorState.UNAVAILABLE;
            }
            if (processState(sensorState)) {
                BusProvider.getBus().post(new MotionSensorEvent(sensorState));
            }

        } else {
        setSampleCount(sampleCount);
        setAddedValues(addedValues);
        }
    }

    private boolean moved(Float[] value, Float[] oldValue) {
        float noiseTreshold = 0.025f;

        for (int i = 0; i < value.length; i++) {

            float noiseUp = oldValue[i] + noiseTreshold;
            float noiseDown = oldValue[i] - noiseTreshold;

            if (value[i] > noiseDown && value[i] < noiseUp) {
            } else {
            return true;
        }
        }
        return false;
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

    private void setAddedValues(Float[] values) {
        getState().put(ADDED_VALUES_STATE_KEY, values);
    }

    private Float[] getAddedValues() {
        try {
        Float[] values = getState().get(ADDED_VALUES_STATE_KEY, Float[].class);
        if (values != null) {
            return values;
        }
        } catch (Exception e) {
            // failed to get state return default value
        }

        return new Float[] { 0.0f, 0.0f, 0.0f };
    }

    private void setAverageValues(Float[] values) {
        getState().put(AVERAGE_VALUES_STATE_KEY, values);
    }

    private Float[] getAverageValues() {
        try {
            Float[] values = getState().get(AVERAGE_VALUES_STATE_KEY, Float[].class);
            if (values != null) {
                return values;
            }
        } catch (Exception e) {
            // failed to get state return default value
        }
        return new Float[] { 0.0f, 0.0f, 0.0f };
    }

}
