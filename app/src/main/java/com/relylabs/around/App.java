package com.relylabs.around;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;


/**
 * Created by nagendra on 7/4/18.
 */


public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        LeakCanary.install(this);
        Stetho.initializeWithDefaults(this);
    }

    public static String getBaseURL() {
        return "https://www.rely.ai/";
    }
}
