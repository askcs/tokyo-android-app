package com.askcs.tokyomobileclient.services;

import java.util.Arrays;
import java.util.HashSet;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import com.almende.eve.agent.AgentHost;
import com.askcs.commons.entity.SensorState;
import com.askcs.tokyomobileclient.TokyoApplication;
import com.askcs.tokyomobileclient.agent.BluetoothProximitySensorAgent;
import com.askcs.tokyomobileclient.agent.MotionSensorAgent;
import com.askcs.tokyomobileclient.agent.WifiProximitySensorAgent;
import com.askcs.tokyomobileclient.event.BluetoothAddressSetEvent;
import com.askcs.tokyomobileclient.event.BluetoothProximityEvent;
import com.askcs.tokyomobileclient.event.BusProvider;
import com.askcs.tokyomobileclient.event.LoginEvent;
import com.askcs.tokyomobileclient.event.LogoutEvent;
import com.askcs.tokyomobileclient.event.MotionSensorEvent;
import com.askcs.tokyomobileclient.event.WifiBssidsSetEvent;
import com.askcs.tokyomobileclient.event.WifiProximityEvent;
import com.askcs.tokyomobileclient.util.Constants;
import com.squareup.otto.Bus;

public class TokyoMobileClientService extends Service {

    private static final String TAG = "TokyoMobileClientService";
    public static final String EXTRA_COMMAND_KEY = "commandKey";
    public static final int EXTRA_COMMAND_START_EVE = 1;
    public static final int EXTRA_COMMAND_LOGIN = 2;
    public static final int EXTRA_COMMAND_LOGOUT = 3;
    public static final int EXTRA_COMMAND_RETRIEVE_STATES = 4;
    public static final int EXTRA_COMMAND_SET_BLUETOOTH_ADDRESS = 5;
    public static final int EXTRA_COMMAND_SET_WIFI_BSSIDS = 6;
    public static final String EXTRA_LOGIN_USERNAME_KEY = "username";
    public static final String EXTRA_LOGIN_PASSWORD_KEY = "password";
    public static final String EXTRA_SET_BLUETOOTH_ADDRESS_KEY = "bluetoothAddress";
    public static final String EXTRA_SET_WIFI_BSSIDS_KEY = "wifiBssids";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(Process.myPid(), new Notification());

        if (intent == null) {
            // service restarted by Android making sure eve is running
            startEve();
        } else {

        switch (intent.getIntExtra(EXTRA_COMMAND_KEY, -1)) {
            case EXTRA_COMMAND_START_EVE:
                startEve();
                break;

            case EXTRA_COMMAND_LOGIN:
                String username = intent.getStringExtra(EXTRA_LOGIN_USERNAME_KEY);
                String password = intent.getStringExtra(EXTRA_LOGIN_PASSWORD_KEY);
                if (username != null && password != null) {
                    login(username, password);
                } else {
                    // failed
                    BusProvider.getBus().post(new LoginEvent(false));
                }
                break;

            case EXTRA_COMMAND_LOGOUT:
                logout(true);
                break;

            case EXTRA_COMMAND_SET_BLUETOOTH_ADDRESS:
                String address = intent.getStringExtra(EXTRA_SET_BLUETOOTH_ADDRESS_KEY);
                if (address != null) {
                    setBluetoothAddress(address);
                }
                break;

            case EXTRA_COMMAND_SET_WIFI_BSSIDS:
                String[] bssids = intent.getStringArrayExtra(EXTRA_SET_WIFI_BSSIDS_KEY);
                if (bssids != null) {
                    setWifiBssids(bssids);
                }
                break;
            case EXTRA_COMMAND_RETRIEVE_STATES:
                retrieveStates();
                break;
            default:
                Log.w(TAG, "Invalid command");
                break;
        }
        }



        return START_STICKY;
    }

    private void startEve() {
        Runnable r = new Runnable() {
            public void run() {
                TokyoApplication.getAgentHost();
            }
        };
        new Thread(r).start();
    }

    /**
     * Create and connects each agent with the provided username and password.
     * 
     * @param username
     * @param password
     */
    private void login(final String username, final String password) {
        Runnable r = new Runnable() {
            public void run() {
                boolean successful;
                try {
                    createMotionSensorAgent(username, password);
                    createWifiProximitySensorAgent(username, password);
                    createBluetoothProximitySensorAgent(username, password);
                    successful = true;
                } catch (Exception e) {
                    // failed to login
                    e.printStackTrace();
                    successful = false;
                }
                if (!successful) {
                    logout(false);
                }
                BusProvider.getBus().post(new LoginEvent(successful));
            }
        };
        new Thread(r).start();

    }

    /**
     * Retrieve the storedState from each agent and post each state to the Bus.
     */
    private void retrieveStates() {

        Runnable r = new Runnable() {
            public void run() {
                Bus bus = BusProvider.getBus();

                AgentHost agentHost = TokyoApplication.getAgentHost();
                try {
                    if (agentHost.hasAgent(Constants.MOTION_SENSOR_AGENT_RESOURCE)) {
                        MotionSensorAgent agent = (MotionSensorAgent) agentHost
                                .getAgent(Constants.MOTION_SENSOR_AGENT_RESOURCE);
                        bus.post(new MotionSensorEvent(agent.getStoredSensorState()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "failed to retrieve state from MotionSensorAgent");
                    bus.post(new MotionSensorEvent(SensorState.UNKNOWN));
                }
                try {
                    if (agentHost.hasAgent(Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE)) {
                        BluetoothProximitySensorAgent agent = (BluetoothProximitySensorAgent) agentHost
                                .getAgent(Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
                        bus.post(new BluetoothProximityEvent(agent.getStoredSensorState()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "failed to retrieve state from BluetoothProximitySensorAgent");
                    bus.post(new BluetoothProximityEvent(SensorState.UNKNOWN));
                }
                try {
                    if (agentHost.hasAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE)) {
                        WifiProximitySensorAgent agent = (WifiProximitySensorAgent) agentHost
                                .getAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
                        bus.post(new WifiProximityEvent(agent.getStoredSensorState()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "failed to retrieve state from WifiProximitySensorAgent");
                    bus.post(new WifiProximityEvent(SensorState.UNKNOWN));
                }
            }
        };
        new Thread(r).start();

    }

    private void setBluetoothAddress(final String address) {
        Runnable r = new Runnable() {
            public void run() {
                AgentHost agentHost = TokyoApplication.getAgentHost();
                try {
                    if (agentHost.hasAgent(Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE)) {
                        BluetoothProximitySensorAgent agent = (BluetoothProximitySensorAgent) agentHost
                                .getAgent(Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
                        agent.setRemoteDeviceAddress(address);
                        BusProvider.getBus().post(new BluetoothAddressSetEvent(true, address));
                    }
                } catch (Exception e) {
                    BusProvider.getBus().post(new BluetoothAddressSetEvent(false, address));
                }
            }
        };
        new Thread(r).start();

    }

    private void setWifiBssids(final String[] bssids) {
        Runnable r = new Runnable() {
            public void run() {
                AgentHost agentHost = TokyoApplication.getAgentHost();
                try {
                    if (agentHost.hasAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE)) {
                        WifiProximitySensorAgent agent = (WifiProximitySensorAgent) agentHost
                                .getAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
                        agent.setBSSIDs(new HashSet<String>(Arrays.asList(bssids)));
                        BusProvider.getBus().post(new WifiBssidsSetEvent(true, bssids));
                    }
                } catch (Exception e) {
                    BusProvider.getBus().post(new WifiBssidsSetEvent(false, bssids));
                }
            }
        };
        new Thread(r).start();

    }

    private void logout(final boolean sendLogoutEvent) {
        Runnable r = new Runnable() {
            public void run() {
                AgentHost agentHost = TokyoApplication.getAgentHost();
                try {
                    if (agentHost.hasAgent(Constants.MOTION_SENSOR_AGENT_RESOURCE)) {
                        MotionSensorAgent agent = (MotionSensorAgent) agentHost
                                .getAgent(Constants.MOTION_SENSOR_AGENT_RESOURCE);
                        agent.purge();
                    }
                } catch (Exception e) {
                    System.out
                            .println("Failed to logout " + Constants.MOTION_SENSOR_AGENT_RESOURCE);
                    e.printStackTrace();
                    // try removing the agent directly
                    agentHost.deleteAgent(Constants.MOTION_SENSOR_AGENT_RESOURCE);
                }
                try {
                    if (agentHost.hasAgent(Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE)) {
                        BluetoothProximitySensorAgent agent = (BluetoothProximitySensorAgent) agentHost
                                .getAgent(Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
                        agent.purge();
                    }
                } catch (Exception e) {
                    System.out.println("Failed to logout "
                            + Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
                    e.printStackTrace();
                    // try removing the agent directly
                    agentHost.deleteAgent(Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
                }
                try {
                    if (agentHost.hasAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE)) {
                        WifiProximitySensorAgent agent = (WifiProximitySensorAgent) agentHost
                                .getAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
                        agent.purge();
                    }
                } catch (Exception e) {
                    System.out.println("Failed to logout "
                            + Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
                    e.printStackTrace();
                    // try removing the agent directly
                    agentHost.deleteAgent(Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
                }
                Bus bus = BusProvider.getBus();
                bus.post(new WifiProximityEvent(SensorState.UNKNOWN));
                bus.post(new BluetoothProximityEvent(SensorState.UNKNOWN));
                bus.post(new MotionSensorEvent(SensorState.UNKNOWN));
                if (sendLogoutEvent) {
                bus.post(new LogoutEvent());
                }
            }
        };
        new Thread(r).start();

    }

    private void createMotionSensorAgent(String username, String password) throws Exception {
        AgentHost agentHost = TokyoApplication.getAgentHost();
        MotionSensorAgent agent = agentHost.createAgent(MotionSensorAgent.class,
                Constants.MOTION_SENSOR_AGENT_RESOURCE);
        agent.setAccount(username, password, Constants.MOTION_SENSOR_AGENT_RESOURCE);
        agent.startAutoMonitor(5000);
    }

    private void createBluetoothProximitySensorAgent(String username, String password)
            throws Exception {
        AgentHost agentHost = TokyoApplication.getAgentHost();
        BluetoothProximitySensorAgent agent = agentHost.createAgent(
                BluetoothProximitySensorAgent.class,
                Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
        agent.setAccount(username, password, Constants.BLUETOOTH_PROXIMITY_SENSOR_AGENT_RESOURCE);
        agent.startAutoMonitor(20000);

    }

    private void createWifiProximitySensorAgent(String username, String password) throws Exception {
        AgentHost agentHost = TokyoApplication.getAgentHost();
        WifiProximitySensorAgent agent = agentHost.createAgent(WifiProximitySensorAgent.class,
                Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
        agent.setAccount(username, password, Constants.WIFI_PROXIMITY_SENSOR_AGENT_RESOURCE);
        agent.startAutoMonitor(30000);
    }

    /**
     * Service does not support binding
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
