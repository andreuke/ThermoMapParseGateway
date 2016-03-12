package com.blukii.android.blukii_android_widgetdemo.util;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.blukii.android.blukii_android_widgetdemo.R;
import com.blukii.android.blukii_android_widgetdemo.api.InfoApp;
import com.blukii.android.blukii_android_widgetdemo.general.MainActivity;
import com.blukii.android.blukii_android_widgetdemo.model.InputElement;
import com.blukii.android.blukii_android_widgetdemo.service.MainReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper for creating widget user interface and click handler
 */
public class WidgetViewsHandler {

    private static final String TAG = "WidgetViewsHandler";
    
    private Context mContext = null;
    public WidgetViewsHandler(Context context){
        mContext = context;
    }


    public RemoteViews create(InputElement inputElement, String viewName) {
        Log.i(TAG, "create");

        RemoteViews views = getRemoteViews();

        if(views == null){
            return null;
        }


        float floatValue;
        int intValue;
        String txtValue;

        if(inputElement != null){

            floatValue = inputElement.getTemperature();
            if(floatValue != Float.MIN_VALUE){
                txtValue = String.valueOf(floatValue) + mContext.getString(R.string.unit_temperature);
            } else {
                txtValue = mContext.getString(R.string.text_off);
            }
            views.setTextViewText(R.id.temperature, mContext.getString(R.string.text_temperature) + ": " + txtValue);

            intValue = inputElement.getHumidity();
            if(intValue != Integer.MIN_VALUE){
                txtValue = String.valueOf(intValue) + mContext.getString(R.string.unit_humidity);
            } else {
                txtValue = mContext.getString(R.string.text_off);
            }
            views.setTextViewText(R.id.humidity, mContext.getString(R.string.text_humidity) + ": " + txtValue);

            intValue = inputElement.getAirPressure();
            if(intValue != Integer.MIN_VALUE){
                txtValue = String.valueOf(intValue) + mContext.getString(R.string.unit_airpressure);
            } else {
                txtValue = mContext.getString(R.string.text_off);
            }
            views.setTextViewText(R.id.airpressure, mContext.getString(R.string.text_airpressure) + ": " + txtValue);

            intValue = inputElement.getLight();
            if(intValue != Integer.MIN_VALUE){
                txtValue = String.valueOf(intValue) + mContext.getString(R.string.unit_light);
            } else {
                txtValue = mContext.getString(R.string.text_off);
            }
            views.setTextViewText(R.id.light, mContext.getString(R.string.text_light) + ": " + txtValue);

            views.setTextViewText(R.id.blukii, inputElement.getTagID());

        } else{

            txtValue = mContext.getString(R.string.text_off);
            views.setTextViewText(R.id.temperature, mContext.getString(R.string.text_temperature) + ": " + txtValue);
            views.setTextViewText(R.id.humidity, mContext.getString(R.string.text_humidity) + ": " + txtValue);
            views.setTextViewText(R.id.airpressure, mContext.getString(R.string.text_airpressure) + ": " + txtValue);
            views.setTextViewText(R.id.light, mContext.getString(R.string.text_light) + ": " + txtValue);

            views.setTextViewText(R.id.blukii, mContext.getString(R.string.text_no_blukii));
        }

        views.setTextViewText(R.id.time, mContext.getString(R.string.text_time) + ": " + new SimpleDateFormat("HH:mm:ss").format(new Date()));

        // Set Button Text (simulate "Toggle" state)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        boolean isInfoCloudRunning = sharedPreferences.getBoolean(InfoApp.PREF_INFOCLOUD_RUNNING, false);
        String bleState = isInfoCloudRunning ? mContext.getString(R.string.text_off) : mContext.getString(R.string.text_on);
        views.setTextViewText(R.id.buttonBle, mContext.getString(R.string.text_ble) + " " + bleState);

        // BLE Button Action
        PendingIntent piBle = PendingIntent.getBroadcast(mContext, 0, new Intent(MainReceiver.ACTION_WIDGETBUTTON_BLE), PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.buttonBle, piBle);


        // Show Button Action
        Intent activityIntent = new Intent(mContext, MainActivity.class);
        PendingIntent piShow = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.buttonShow, piShow);




        return views;

    }


    private RemoteViews getRemoteViews(){
        return new RemoteViews(mContext.getPackageName(), R.layout.main_widget);
    }


}
