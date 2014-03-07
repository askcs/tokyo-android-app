package com.askcs.tokyomobileclient.services;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.askcs.tokyomobileclient.TokyoApplication;
import com.askcs.tokyomobileclient.agent.intf.BluetoothProximitySensorAgentIntf;
import com.askcs.tokyomobileclient.model.BluetoothScanResult;
import com.askcs.tokyomobileclient.util.Constants;

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private WakeLock mWakeLock;
    private List<BluetoothScanResult> mBluetoothScanResults;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = device.getAddress();
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                mBluetoothScanResults.add(new BluetoothScanResult(address, rssi));

            } else
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    unregisterReceiver(mReceiver);
                    returnResult(mBluetoothScanResults);
                }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    private void sample() {
        mWakeLock.acquire();
        // Check if bluetooth is enabled
                if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
            mBluetoothScanResults = new ArrayList<BluetoothScanResult>();
            // Register receiver for Bluetooth device found and finished
            // Bluetooth discovery events
            registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            registerReceiver(mReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            // Start scanning
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
                }else{
            // Bluetooth not enabled
            returnResult(null);
                }
    }

    private void returnResult(final List<BluetoothScanResult> bluetoothScanResults) {

        Runnable r = new Runnable() {
            public void run() {
                try {
                    BluetoothProximitySensorAgentIntf bluetoothProximitySensorAgent = (BluetoothProximitySensorAgentIntf) TokyoApplication
                            .getAgentHost().getAgent(
                                    Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
                    bluetoothProximitySensorAgent.processSensorResult(bluetoothScanResults);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        };
        new Thread(r).start();
    }

    @Override
    public void onDestroy() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sample();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
