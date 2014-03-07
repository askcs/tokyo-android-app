package com.askcs.tokyomobileclient.services;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.askcs.tokyomobileclient.TokyoApplication;
import com.askcs.tokyomobileclient.agent.intf.WifiProximitySensorAgentIntf;
import com.askcs.tokyomobileclient.util.Constants;

public class WifiService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String TAG = "WifiService";
    private WakeLock mWakeLock;
    private  WifiManager mWifiManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                unregisterReceiver(mReceiver);
                returnResult(mWifiManager.getScanResults());
            }

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sample() {
                mWakeLock.acquire();

        // Check if wifi is on or capable of scanning for networks
        boolean scanAlwaysEnabled = false;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // Only execute on supported android versions
            scanAlwaysEnabled = mWifiManager.isScanAlwaysAvailable();
        }

        if (mWifiManager.isWifiEnabled() || scanAlwaysEnabled) {
            registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                mWifiManager.startScan();
        } else {
            returnResult(null);
        }

    }

    private void returnResult(final List<ScanResult> scanResults) {

        Runnable r = new Runnable() {
            public void run() {
                try {
                    WifiProximitySensorAgentIntf wifiProximitySensorAgent = (WifiProximitySensorAgentIntf) TokyoApplication
                            .getAgentHost()
                            .getAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
                    wifiProximitySensorAgent.processSensorResult(scanResults);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get WifiProximitySensorAgent");
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
        super.onDestroy();
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
}
