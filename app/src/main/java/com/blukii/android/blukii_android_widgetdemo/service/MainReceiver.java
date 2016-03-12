package com.blukii.android.blukii_android_widgetdemo.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.blukii.android.blukii_android_widgetdemo.api.InfoApp;
import com.blukii.android.blukii_android_widgetdemo.model.InputElement;
import com.blukii.android.blukii_android_widgetdemo.util.StateValidator;
import com.blukii.android.blukii_android_widgetdemo.util.WidgetViewsHandler;

import java.util.ArrayList;

public class MainReceiver extends BroadcastReceiver {

    private static final String TAG = "MainReceiver";

    public static final String ACTION_APPSTART = "com.blukii.android.ACTION_APPSTART";
    public static final String ACTION_SEND_BLUKIILIST = "com.blukii.android.ACTION_SEND_BLUKIILIST";
    public static final String ACTION_WIDGETBUTTON_SHOW = "com.blukii.android.ACTION_WIDGETBUTTON_SHOW";
    public static final String ACTION_WIDGETBUTTON_BLE = "com.blukii.android.ACTION_WIDGETBUTTON_BLE";


    public static final String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";


    public static final String EXTRA_VALUE_ACTION_SENDER_WIDGET = "WIDGET";



    public static final String[] INTENTFITLER_ACTIONS = new String [] {
            ACTION_APPSTART,
            ACTION_SEND_BLUKIILIST,
            ACTION_WIDGETBUTTON_SHOW,
            ACTION_WIDGETBUTTON_BLE,
            ACTION_USER_PRESENT
    } ;


    DeviceDiscoveryService mDeviceDiscoveryService = null;
    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i(TAG, "onServiceConnected DeviceDiscoveryService");
            mDeviceDiscoveryService = ((DeviceDiscoveryService.ServiceBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "onServiceDisconnected DeviceDiscoveryService");
            mDeviceDiscoveryService = null;
        }
    };


    public MainReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());

        switch(intent.getAction()){

            case MainReceiver.ACTION_USER_PRESENT:
            case MainReceiver.ACTION_APPSTART:
                toggleDiscoveryService(context);
                break;


            case MainReceiver.ACTION_WIDGETBUTTON_BLE:
                // toggle Preference value
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                sharedPreferences.edit().putBoolean(InfoApp.PREF_INFOCLOUD_RUNNING,!sharedPreferences.getBoolean(InfoApp.PREF_INFOCLOUD_RUNNING,false)).apply();

                toggleDiscoveryService(context);
                break;

        }
    }


    public static IntentFilter getIntentFilterActions (){
        IntentFilter intentFilter = new IntentFilter();

        for(String action : INTENTFITLER_ACTIONS) {
            intentFilter.addAction(action);
        }

        return intentFilter;

    }

    // start/stop Service if not already done
    private void toggleDiscoveryService(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean isInfoCloudSetOn = sharedPreferences.getBoolean(InfoApp.PREF_INFOCLOUD_RUNNING, false);

        Intent intent = new Intent(context, DeviceDiscoveryService.class);
        if(isInfoCloudSetOn && !isBleRunning(context)){
            context.startService(intent);
            context.bindService(intent, mConnection, Context.BIND_IMPORTANT);
        }

        if(!isInfoCloudSetOn && isBleRunning(context)){
            if(mDeviceDiscoveryService != null) {
                context.unbindService(mConnection);
            }
            context.stopService(intent);
        }

    }


    private boolean isBleRunning(Context context){
        StateValidator stateValidator = new StateValidator(context);
        return stateValidator.serviceIsRunning(DeviceDiscoveryService.class.getName());
    }


}
