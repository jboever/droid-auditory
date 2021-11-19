package com.droid.auditory.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.droid.auditory.BuildConfig;

import timber.log.Timber;

public class Auditory extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Dagger.setDagger(DaggerAuditoryComponent.builder().auditoryModule(new AuditoryModule(this)).build());
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
