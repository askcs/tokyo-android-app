package com.askcs.tokyomobileclient.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.askcs.tokyomobileclient.TokyoApplication;
import com.askcs.tokyomobileclient.agent.intf.MotionSensorAgentIntf;
import com.askcs.tokyomobileclient.util.Constants;

public class MotionService extends Service implements SensorEventListener {

    private static final String TAG = "MotionService";
    private WakeLock mWakeLock;
    private SensorManager mSensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }



    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    /**
     * Receives sensorEvent, stops listening for new events and return the
     * result.
     */
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mSensorManager.unregisterListener(this);
            returnResult(event);
        }

    }

    /**
     * Returns the SensorEvent to the motionSensorAgent
     * 
     * @param event
     */
    private void returnResult(final SensorEvent event) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    MotionSensorAgentIntf motionSensorAgent = (MotionSensorAgentIntf) TokyoApplication
                            .getAgentHost().getAgent(Constants.MOTION_SENSOR_AGENT_RESOURCE);
                    motionSensorAgent.processSensorResult(event.values);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get SensorMonitorAgent");
        }

                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
        }
            }
        };
        new Thread(r).start();

    }

    /**
     * Acquire wakelock and start listening for sensor events
     */
    private void sample() {
                mWakeLock.acquire();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Release the wakelock before the service is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    /**
     * Start getting a sample after the service has been started via and Intent
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sample();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // No binder implemented
        return null;
    }

}
