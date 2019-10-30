package com.amgoing.measureslave;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/*
 * @author AmGoing
 * @time 2019-10-30 17:42
 * @declare Mark Here...
 */
public class MeasureSlaveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
