package com.blukii.android.blukii_android_widgetdemo.api;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blukii.android.blukii_android_widgetdemo.service.MainReceiver;

/**
 * Application class
 * - Initialization
 */
public class InfoApp extends Application {

    private static final String TAG = "InfoApp";

    public static final String PREF_INFOCLOUD_RUNNING = "PREF_INFOCLOUD_RUNNING";

    BroadcastReceiver mMainReceiver = null;

    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        initPreferences();

        registerMainReceiver() ;

        // tell receivers after init
        Intent intent = new Intent(MainReceiver.ACTION_APPSTART);
        sendBroadcast(intent);

    }

    // init preference default values
    private void initPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPreferences.contains(PREF_INFOCLOUD_RUNNING)){
            Log.i(TAG, "initPreferences: create new pref");
            sharedPreferences.edit().putBoolean(PREF_INFOCLOUD_RUNNING,false).apply();
        }
    }


    @Override
    public void onTerminate() {
        Log.i(TAG, "onTerminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory");
        super.onLowMemory();
    }

    // register main broadcast receiver
    private void registerMainReceiver() {
        // set receiving filter action
        IntentFilter intentFilter = MainReceiver.getIntentFilterActions();

        mMainReceiver = new MainReceiver();
        registerReceiver(mMainReceiver, intentFilter);
    }



}
