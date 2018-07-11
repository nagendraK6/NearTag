package com.relylabs.around;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.relylabs.around.db.DaoMaster;
import com.relylabs.around.db.DaoSession;
import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.greendao.database.Database;

/**
 * Created by nagendra on 7/4/18.
 */


public class App extends Application {

    public static final boolean ENCRYPTED = true;
    private DaoSession daoSession;

    @Override public void onCreate() {
        super.onCreate();
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
