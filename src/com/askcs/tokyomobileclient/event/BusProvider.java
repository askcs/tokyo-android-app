package com.askcs.tokyomobileclient.event;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Maintains a singleton instance for obtaining the event bus over which
 * messages are passed from UI components (such as Activities and Fragments) to
 * Services, and back.
 */
public final class BusProvider {

    // The singleton of the Bus instance which can be used from
    // any thread in the app.
    private static final Bus BUS = new Bus() {

        private final Handler uiHandler = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        post(event);
                    }
                });
            }
        }
    };

    public static Bus getBus() {
        return BUS;
    }

    // No need to instantiate this class.
    private BusProvider() {
    }
}