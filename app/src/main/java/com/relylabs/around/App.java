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
        // Normal app init code...
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,"users-db"); //The users-db here is the name of our database.
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        Stetho.initializeWithDefaults(this);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
