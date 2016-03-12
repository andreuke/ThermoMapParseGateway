package com.blukii.android.blukii_android_widgetdemo.util;

import android.app.ActivityManager;
import android.content.Context;

public class StateValidator {

    private Context context;



    public StateValidator(Context context) {
        this.context = context;
    }


    public boolean serviceIsRunning(String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}
