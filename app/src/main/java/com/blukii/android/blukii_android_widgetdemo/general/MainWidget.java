package com.blukii.android.blukii_android_widgetdemo.general;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.blukii.android.blukii_android_widgetdemo.model.InputElement;
import com.blukii.android.blukii_android_widgetdemo.service.DeviceDiscoveryService;
import com.blukii.android.blukii_android_widgetdemo.service.MainReceiver;
import com.blukii.android.blukii_android_widgetdemo.util.WidgetViewsHandler;

import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 */
public class MainWidget extends AppWidgetProvider {

    private static final String TAG = "MainWidget";

    private InputElement mNearestBlukii = null;


    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.i(TAG, "updateAppWidget");

        WidgetViewsHandler widgetViewsHandler = new WidgetViewsHandler(context);
        RemoteViews views = widgetViewsHandler.create(mNearestBlukii, MainReceiver.EXTRA_VALUE_ACTION_SENDER_WIDGET);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(TAG, "onUpdate");
        // There may be multiple widgets active, so update all of them

        updateAllAppWidgets(context);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());

        super.onReceive(context, intent);

        switch(intent.getAction()){
            case MainReceiver.ACTION_SEND_BLUKIILIST:

                ArrayList<InputElement> blukiiArrayList = intent.getParcelableArrayListExtra(DeviceDiscoveryService.EXTRA_BLUKIILIST);
                int nearestRssi = Integer.MIN_VALUE;

                // show data of the nearest blukii
                for(InputElement element : blukiiArrayList){

                    if(element.getIsConnected()) {
                        if (element.getRssi() > nearestRssi) {
                            mNearestBlukii = element;
                            nearestRssi = element.getRssi();
                        }
                    }

                }

                updateAllAppWidgets(context);
                break;

            case MainReceiver.ACTION_WIDGETBUTTON_BLE:
                mNearestBlukii = null;
                updateAllAppWidgets(context);
                break;


        }


    }

    private void updateAllAppWidgets(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    @Override
    public void onEnabled(Context context) {
        Log.i(TAG, "onEnabled");
        // Enter relevant functionality for when the first widget is created
        updateAllAppWidgets(context);

    }

    @Override
    public void onDisabled(Context context) {
        Log.i(TAG, "onDisabled");
        // Enter relevant functionality for when the last widget is disabled

    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Log.i(TAG, "onRestored");
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }



}

