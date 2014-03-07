package com.askcs.tokyomobileclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.askcs.commons.entity.SensorState;
import com.askcs.tokyomobileclient.event.BluetoothAddressSetEvent;
import com.askcs.tokyomobileclient.event.BluetoothProximityEvent;
import com.askcs.tokyomobileclient.event.BusProvider;
import com.askcs.tokyomobileclient.event.LoginEvent;
import com.askcs.tokyomobileclient.event.LogoutEvent;
import com.askcs.tokyomobileclient.event.MotionSensorEvent;
import com.askcs.tokyomobileclient.event.WifiBssidsSetEvent;
import com.askcs.tokyomobileclient.event.WifiProximityEvent;
import com.askcs.tokyomobileclient.services.TokyoMobileClientService;
import com.askcs.tokyomobileclient.util.Constants;
import com.squareup.otto.Subscribe;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class AndroidGuiActivity extends Activity {

    private Button mLoginButton;
    private Button mLogoutButton;
    private Button mSetWifiButton;
    private Button mSetBluetoothButton;
    private EditText mUsernameField;
    private EditText mPasswordField;
    private SharedPreferences mPreferences;

    /**
     * Setup the gui
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_android_gui);
        
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mUsernameField = (EditText) findViewById(R.id.username_field);
        mPasswordField = (EditText) findViewById(R.id.password_field);

        // Setup login button and its onClick behavior.
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new OnClickListener() {

            // Set GUI state, Retrieve username and password from EditText
            // fields and attempt to login.
            public void onClick(View v) {
                setGuiLoggingInState();
                String username = mUsernameField.getText().toString();
                String password = mPasswordField.getText().toString();

                if (username != null && !"".equals(username) && password != null
                        && !"".equals(password)) {

                Editor editor = mPreferences.edit();
                    editor.putString(Constants.PREF_USERNAME, username);
                    editor.putString(Constants.PREF_PASSWORD, password);
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), TokyoMobileClientService.class);
                intent.putExtra(TokyoMobileClientService.EXTRA_COMMAND_KEY,
                        TokyoMobileClientService.EXTRA_COMMAND_LOGIN);
                intent.putExtra(TokyoMobileClientService.EXTRA_LOGIN_USERNAME_KEY, username);
                intent.putExtra(TokyoMobileClientService.EXTRA_LOGIN_PASSWORD_KEY, password);

                startService(intent);
                } else {
                    Crouton.showText(getActivity(),
                            getResources().getString(R.string.login_invalid_credentials),
                            Style.ALERT);
                    setGuiLoggedOutState();
                }

            }
        });

        // Setup logout button and its onClick behavior.
        mLogoutButton = (Button) findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(new OnClickListener() {

            // Set GUI state and start logging out
            @Override
            public void onClick(View v) {
                setGuiLoggingOutState();
                Intent intent = new Intent(getApplicationContext(), TokyoMobileClientService.class);
                intent.putExtra(TokyoMobileClientService.EXTRA_COMMAND_KEY,
                        TokyoMobileClientService.EXTRA_COMMAND_LOGOUT);

                startService(intent);

            }
        });
        
        // Setup setWifi button and its onClick behavior.
        mSetWifiButton = (Button) findViewById(R.id.set_wifi_button);
        mSetWifiButton.setOnClickListener(new OnClickListener() {

            // Create and show a dialog
            @Override
            public void onClick(View v) {
               
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

                alert.setTitle(getResources().getString(R.string.set_wifi_button));
                alert.setMessage(R.string.set_wifi_input_text);

                // Set an EditText view to get user input 
                final EditText input = new EditText(v.getContext());
                String value = mPreferences.getString(Constants.PREF_WIFI_BSSIDS, null);
                if (value != null) {
                    input.setText(value);
                }
                alert.setView(input);

                alert.setPositiveButton(getResources().getString(R.string.ok_button),
                        new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                                // Gets string with bssids from the EditText
                                // split the bssids into an String array and
                                // posts them to the service
                  String value = input.getText().toString();
                  String[]values = value.split("\\s");
                        Editor editor = mPreferences.edit();
                                editor.putString(Constants.PREF_WIFI_BSSIDS, value);
                        editor.commit();
                                Intent intent = new Intent(getActivity(),
                                        TokyoMobileClientService.class);
                  intent.putExtra(TokyoMobileClientService.EXTRA_COMMAND_KEY,
                          TokyoMobileClientService.EXTRA_COMMAND_SET_WIFI_BSSIDS);
                        intent.putExtra(TokyoMobileClientService.EXTRA_SET_WIFI_BSSIDS_KEY, values);
                  startService(intent);
                  
                }
                });

                alert.setNegativeButton(getResources().getString(R.string.cancel_button),
                        new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                  }
                });

                alert.show();
                
                
            }
        });
        
        // Setup set bluetooth address button and its onClick behavior.
        mSetBluetoothButton = (Button) findViewById(R.id.set_bluetooth_button);
        mSetBluetoothButton.setOnClickListener(new OnClickListener() {

            // Builds and shows an dialog
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

                alert.setTitle(getResources().getString(R.string.set_bluetooth_button));
                alert.setMessage(getResources().getString(R.string.set_bluetooth_input_text));

                // Set an EditText view to get user input
                final EditText input = new EditText(v.getContext());
                String bluetoothAddress = mPreferences.getString(Constants.PREF_BLUETOOTH_ADDRESS, null);
                if (bluetoothAddress != null) {
                    input.setText(bluetoothAddress);
                }
                alert.setView(input);

                alert.setPositiveButton(getResources().getString(R.string.ok_button),
                        new DialogInterface.OnClickListener() {
                            // Retrieves the address from the EditText and posts
                            // it to the service
                            @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        Editor editor = mPreferences.edit();
                                editor.putString(Constants.PREF_BLUETOOTH_ADDRESS, value);
                        editor.commit();
                                Intent intent = new Intent(getActivity(),
                                TokyoMobileClientService.class);
                        intent.putExtra(TokyoMobileClientService.EXTRA_COMMAND_KEY,
                                TokyoMobileClientService.EXTRA_COMMAND_SET_BLUETOOTH_ADDRESS);
                        intent.putExtra(TokyoMobileClientService.EXTRA_SET_BLUETOOTH_ADDRESS_KEY,
                                value);
                        startService(intent);

                    }
                });

                alert.setNegativeButton(getResources().getString(R.string.cancel_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();

            }
        });
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);

        String username = mPreferences.getString(Constants.PREF_USERNAME, null);
        String password = mPreferences.getString(Constants.PREF_PASSWORD, null);
        if (username != null && password != null) {
            // logged in
            setGuiLoggedInState();
            mUsernameField.setText(username);
            mPasswordField.setText(password);

            Intent intent = new Intent(getApplicationContext(), TokyoMobileClientService.class);
            intent.putExtra(TokyoMobileClientService.EXTRA_COMMAND_KEY,
                    TokyoMobileClientService.EXTRA_COMMAND_RETRIEVE_STATES);
            startService(intent);

        } else {
            setGuiLoggedOutState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Subscribe
    public void onBluetoothProximitySensorEvent(BluetoothProximityEvent event) {
        TextView textView = (TextView) findViewById(R.id.bluetooth_state);
        SensorState sensorState = event.getSensorState();
        String stateText = getStateText(sensorState);
        textView.setText(stateText);
    }

    @Subscribe
    public void onMotionSensorEvent(MotionSensorEvent event) {
        TextView textView = (TextView) findViewById(R.id.motion_state);
        SensorState sensorState = event.getSensorState();
        String stateText = getStateText(sensorState);
        textView.setText(stateText);
    }

    @Subscribe
    public void onWifiProximitySensorEvent(WifiProximityEvent event) {
        TextView textView = (TextView) findViewById(R.id.wifi_state);
        SensorState sensorState = event.getSensorState();
        String stateText = getStateText(sensorState);
        textView.setText(stateText);
    }

    @Subscribe
    public void onLoginEvent(LoginEvent event) {
        if (event.isSuccessful()) {
            Crouton.showText(getActivity(), getResources().getString(R.string.login_successful),
                    Style.CONFIRM);
            setGuiLoggedInState();

        } else {
            Crouton.showText(getActivity(), getResources().getString(R.string.login_failed),
                    Style.ALERT);
            // remove stored settings
            Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            prefsEditor.clear();
            prefsEditor.commit();
            setGuiLoggedOutState();
        }
    }

    @Subscribe
    public void onLogoutEvent(LogoutEvent event) {
        Crouton.showText(getActivity(), getResources().getString(R.string.logged_out),
                Style.CONFIRM);
            // remove stored settings
        Editor prefsEditor = mPreferences.edit();
            prefsEditor.clear();
            prefsEditor.commit();
            setGuiLoggedOutState();

    }

    @Subscribe
    public void onBluetoothAddressSetEvent(BluetoothAddressSetEvent event) {

        if (event.isSuccessful()) {
            String text = getResources().getString(R.string.bluetooth_set_successful) + ": "
                    + event.getAddress();
            Crouton.showText(getActivity(), text, Style.CONFIRM);
        } else {
            String text = getResources().getString(R.string.bluetooth_set_unsuccessful) + ": "
                    + event.getAddress();
            Crouton.showText(getActivity(), text, Style.ALERT);
        }
    }

    @Subscribe
    public void onWifiBssidsSetEvent(WifiBssidsSetEvent event) {
        String[] bssids = event.getBssids();
        StringBuilder builder = new StringBuilder();
        for (String bssid : bssids) {
            builder.append(bssid + " ");
        }

        if (event.isSuccessful()) {
            String text = getResources().getString(R.string.wifi_set_successful) + ": "
                    + builder.toString();
            Crouton.showText(getActivity(), text, Style.CONFIRM);
        } else {
            String text = getResources().getString(R.string.wifi_set_unsuccessful) + ": "
                    + builder.toString();
            Crouton.showText(getActivity(), text, Style.ALERT);
        }
    }

    /**
     * Converts the state to user readable text
     * 
     * @param sensorstate
     * @return stateText
     */
    private String getStateText(SensorState sensorstate) {
        if (SensorState.AVAILABLE.equals(sensorstate)) {
            return getResources().getString(R.string.state_available);
        } else
            if (SensorState.UNAVAILABLE.equals(sensorstate)) {
                return getResources().getString(R.string.state_unavailable);
            } else {
                return getResources().getString(R.string.state_unknown);
            }
    }

    /**
     * change the state of all relevant GUI elements
     */
    private void setGuiLoggingInState() {
        mLoginButton.setEnabled(false);
        mLogoutButton.setEnabled(false);
        mSetWifiButton.setEnabled(false);
        mSetBluetoothButton.setEnabled(false);
        mUsernameField.setEnabled(false);
        mPasswordField.setEnabled(false);
        setProgressBarIndeterminateVisibility(true);
    }

    /**
     * change the state of all relevant GUI elements
     */
    private void setGuiLoggedInState() {
        mLoginButton.setEnabled(false);
        mLogoutButton.setEnabled(true);
        mSetWifiButton.setEnabled(true);
        mSetBluetoothButton.setEnabled(true);
        mUsernameField.setEnabled(false);
        mPasswordField.setEnabled(false);
        setProgressBarIndeterminateVisibility(false);
    }

    /**
     * change the state of all relevant GUI elements
     */
    private void setGuiLoggedOutState() {
        mLoginButton.setEnabled(true);
        mLogoutButton.setEnabled(false);
        mSetWifiButton.setEnabled(false);
        mSetBluetoothButton.setEnabled(false);
        mUsernameField.setEnabled(true);
        mPasswordField.setEnabled(true);
        setProgressBarIndeterminateVisibility(false);
    }

    /**
     * change the state of all relevant GUI elements
     */
    private void setGuiLoggingOutState() {
        mLoginButton.setEnabled(false);
        mLogoutButton.setEnabled(false);
        mSetWifiButton.setEnabled(false);
        mSetBluetoothButton.setEnabled(false);
        mUsernameField.setEnabled(false);
        mPasswordField.setEnabled(false);
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel all Croutons to avoid issues:
        // https://github.com/keyboardsurfer/Crouton#important
        Crouton.cancelAllCroutons();
    }

    private Activity getActivity() {
        return this;
    }

}
