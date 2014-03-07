package com.askcs.tokyomobileclient.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.askcs.tokyomobileclient.services.TokyoMobileClientService;
import com.askcs.tokyomobileclient.util.Constants;

public class BootCompleteReceiver extends BroadcastReceiver {

    /**
     * Triggers when the smartphone finished booting Checks if a user is logged
     * in and starts the eve platform in the background if logged in.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String username = prefs.getString(Constants.PREF_USERNAME, null);
        String password = prefs.getString(Constants.PREF_PASSWORD, null);

        if (username != null && !username.equals("") && password != null && !password.equals("")) {
            Intent startEveIntent = new Intent(context, TokyoMobileClientService.class);
            startEveIntent.putExtra(TokyoMobileClientService.EXTRA_COMMAND_KEY,
                    TokyoMobileClientService.EXTRA_COMMAND_START_EVE);

            context.startService(startEveIntent);
        }
    }
}
