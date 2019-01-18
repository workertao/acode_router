package com.bjx.toutiao;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;

import com.bjx.router.router.BjxSuperRouter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by yt on 2019/1/15.
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        try {
            BjxSuperRouter.getInstance().init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
