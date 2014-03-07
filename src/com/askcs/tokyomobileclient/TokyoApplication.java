package com.askcs.tokyomobileclient;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.almende.eve.agent.AgentHost;
import com.almende.eve.scheduler.RunnableSchedulerFactory;
import com.almende.eve.state.FileStateFactory;
import com.almende.eve.transport.xmpp.XmppService;

public class TokyoApplication extends Application {

    private static Context mContext;
    private static AgentHost mAgentHost;
    private static final String TAG = "TokyoApplication";

    /**
     * called when the application is starting, before any activity, service or
     * receiver objects (excluding content providers) have been created.
     * http://developer.android.com/reference/android/app/Application.html
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    };

    /**
     * Returns the applicationContext. Is always save to call except within a
     * content provider
     * 
     * @return Context
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * Returns a configured agentHost
     * 
     * @return AgentHost
     */
    public static AgentHost getAgentHost() {
        if (mAgentHost == null) {
            mAgentHost = AgentHost.getInstance();
            if (mAgentHost.getStateFactory() == null) {
                Log.i(TAG, "Initialsing agenthost");
                mAgentHost.setStateFactory(new FileStateFactory(getContext().getFilesDir()
                        .getAbsolutePath() + "/.eveagents_tokyo", true));
                mAgentHost.addTransportService(new XmppService(mAgentHost, "xmpp.ask-cs.com", 5222,
                        "xmpp.ask-cs.com"));
                mAgentHost.setSchedulerFactory(new RunnableSchedulerFactory(mAgentHost,
                        "_myScheduler"));
            }
        }
        return mAgentHost;
    }
}
