package com.example.jason.switchbar;

import android.app.Application;

/**
 * Created by jason on 2018/2/1.
 */

public class CCApplication extends Application {

    private static CCApplication INSTANCE;

    public static CCApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

}
