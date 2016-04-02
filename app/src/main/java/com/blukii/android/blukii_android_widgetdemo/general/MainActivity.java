package com.blukii.android.blukii_android_widgetdemo.general;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.parse.*;

import com.blukii.android.blukii_android_widgetdemo.R;

import java.util.ArrayList;
import java.util.List;

/*
 * Activity: only start screen
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Parse.enableLocalDatastore(this);
        Parse.initialize(this);

    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
