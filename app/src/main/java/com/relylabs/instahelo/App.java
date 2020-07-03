package com.relylabs.instahelo;

import android.app.Application;
import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;


/**
 * Created by nagendra on 7/4/18.
 * * App
 */


public class App extends Application {

    public static int REQUEST_FOR_READ_PHOTO = 1;
    public static RefWatcher getRefWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        refWatcher = LeakCanary.install(this);
        Stetho.initializeWithDefaults(this);
    }

    public static String getBaseURL() {
        return "https://www.insatori.com/";
    }
}
