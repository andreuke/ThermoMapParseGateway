package com.blukii.android.blukii_android_widgetdemo.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import com.blukii.android.blukii_android_widgetdemo.model.InputElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.parse.*;

/*
 * Service for BLE scanning and building blukii data object list
 */
public class DeviceDiscoveryService extends Service {

    private final String TAG = "DeviceDiscoveryService";

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<InputElement> foundDeviceElementList;
    private ArrayList<InputElement> blukiiArrayList = new ArrayList<InputElement>();
    private Handler handler;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    //DEFAULT VALUES
    public static final int DEFAULT_TIMEOUT_DURATION = 10000; // Time after blukii will be removed from blukiiArrayList
    public static final int DEFAULT_DISCOVERY_DURATION = 4000; // BLE scan intervall

    // Broadcast
    public static final String EXTRA_BLUKIILIST = "EXTRA_BLUKIILIST";



    //BLUKII PATTERN
    private static String BLUKII_PATTERN = "blukii.*";


    private final IBinder mBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        DeviceDiscoveryService getService(){
            return DeviceDiscoveryService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind Service");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind Service");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand DeviceDiscoveryService");

        //Initialze rnd elements
        foundDeviceElementList = new ArrayList<InputElement>();
        handler = new Handler(Looper.getMainLooper());

        //BT
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        startScanLeDevice();


        //Start Handler
        handler.post(handleDeviceList);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy DeviceDiscoveryService");
        try {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } catch (NullPointerException e) {
            Log.e(TAG, "Critical error - Couldnt acces BT module\n" + e.toString());
        }
        handler.removeCallbacks(handleDeviceList);

        super.onDestroy();
        stopSelf();

    }




    // Start scanning => register callback mLeScanCallback
    private void startScanLeDevice() {
        try {
            Log.i(TAG, "startScanLeDevice");
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if(mBluetoothAdapter.startLeScan(mLeScanCallback)){
                Log.i(TAG, "startLeScan successful");
            } else {
                Log.e(TAG, "startLeScan NOT successfull");
            }


        } catch (NullPointerException e) {
            Log.e(TAG, "Critical error - Couldnt acces BT module");
        }

    }

    // Start scanning => unregister callback mLeScanCallback
    private void stopScanLeDevice() {
        try {
            Log.i(TAG, "stopScanLeDevice");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } catch (NullPointerException e) {
            Log.e(TAG, "Critical error - Couldnt acces BT module");
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

                    try {
                        String deviceName = device.getName();
                        Log.i(TAG, "onLeScan: " + deviceName);


                        if (deviceName != null && deviceName.matches(BLUKII_PATTERN)) {
                            Log.i(TAG,"Device found: " + device.getAddress() + " Devicename: " + device.getName() + " : " + rssi + " ");

                            String parsedTagName = deviceName;

                            if (parsedTagName != null) {
                                // Create Data object
                                InputElement currentElement = new InputElement(parsedTagName.toString());
                                currentElement.setRssi(Short.parseShort(rssi + ""));
                                currentElement.setDeviceFoundDate(System.currentTimeMillis());

                                currentElement = parseScanRecord(scanRecord,currentElement);

                                addFoundDevice(currentElement);

                                sendToParse(currentElement);
                            } else {
                                //Log.e(TAG,"Bad formated Blukii transmitter found");
                                return;
                            }
                        } else if (deviceName != null) {
                            Log.i(TAG, "Non Blukii device found: " + device.getName());
                        } else {
                            Log.w(TAG, "Non Blukii device found: devicename unknown");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception @ LeScanCallBack: " + e.toString());
                    }

                }
            };


    // interpreting scanRecord to parse blukii sensor values
    private InputElement parseScanRecord(final byte[] scanRecord, InputElement inputElement){

        try {
            if(scanRecord == null || scanRecord.length < 32){
                throw new Exception("scanRecord null or not long enough");
            }

            if(scanRecord[0] != 0x02) {
                throw new Exception("first block has invalid length");
            }

            if(scanRecord[3] != 0x03) {
                throw new Exception("first block has invalid length");
            }

            if(scanRecord[7] != 0x17) {
                throw new Exception("third block has invalid length");
            }

            if((scanRecord[12] & 0x0F) != 0x04) {
                throw new Exception("device is not a beacon");
            }

            if((scanRecord[24] & 0xFF) != 0xFF) {
                int airPressure = doubleByteToInt(scanRecord[24], scanRecord[25]);
                inputElement.setAirPressure(airPressure);
            }

            if((scanRecord[26] & 0xFF) != 0xFF) {
                int light = doubleByteToInt(scanRecord[26], scanRecord[27]);
                inputElement.setLight(light);
            }

            if((scanRecord[28] & 0xFF) != 0xFF) {
                byte b0 = 0;
                int humidity = doubleByteToInt(b0, scanRecord[28]);
                inputElement.setHumidity(humidity);
            }

            if((scanRecord[29] & 0xFF) != 0x80) {
                short shortTemp = (short)doubleByteToInt(scanRecord[29], scanRecord[30]);
                float temperature = (float) shortTemp / 10;
                inputElement.setTemperature(temperature);
            }


        } catch (Exception ex){
            Log.w(TAG, "parseScanRecord failed: " + ex.getMessage());
        }

        return inputElement;
    }

    private int doubleByteToInt(byte b1, byte b2){
        int i1 = b1 & 0xFF;
        int i2 = b2 & 0xFF;
        return (i1 * 0x100) + i2;
    }

    // add blukii to foundDeviceElementList
    private void addFoundDevice(InputElement currentElement) {
        for (int i = 0; i < foundDeviceElementList.size(); i++) {
            if (currentElement.getTagID().equalsIgnoreCase(foundDeviceElementList.get(i).getTagID())) {
                foundDeviceElementList.get(i).copy(currentElement);
                //Log.i(TAG,"Updating RSSI + Date for: " + foundDeviceElementList.get(i).getTagID());
                return;
            }
        }
        foundDeviceElementList.add(currentElement);
        Log.i(TAG, "Adding Device to List: " + currentElement.getTagID());
    }


    // loop BLE scan every DEFAULT_DISCOVERY_DURATION seconds
    private final Runnable handleDeviceList = new Runnable() {
        @Override
        public void run() {

            try {
                stopScanLeDevice();
                startScanLeDevice();
                Log.i(TAG, "LE Scan restarted");

                // remove blukiis out of range
                long currentDate = System.currentTimeMillis();
                long timeOut = DEFAULT_TIMEOUT_DURATION;
                for (Iterator<InputElement> iterator = foundDeviceElementList.iterator(); iterator.hasNext(); ) {
                    InputElement currentElement = iterator.next();

                    if (currentDate - currentElement.getDeviceFoundDate() > timeOut) {
                        Log.i(TAG, currentElement.getTagID() + " wasnt seen for " + timeOut + " ms. Removing from list");
                        iterator.remove();
                    }
                }

                if (!foundDeviceElementList.isEmpty()) {
                    Log.i(TAG, "--- DeviceList---");
                    for (InputElement currentElement : foundDeviceElementList) {
                        Log.i(TAG, "Device:" + currentElement.getTagID() + " -> " + currentElement.getRssi() + " -> " + currentElement.getDeviceFoundDate());
                    }

                    //FoundDeviceElement -> BlukiiElement -> ResolveByBackend
                    updateBlukiiList(foundDeviceElementList);

                } else {
                    Log.i(TAG, "--- No Devices found ---");
                }

            } finally {
                handler.postDelayed(handleDeviceList, DEFAULT_DISCOVERY_DURATION);
            }

        }
    };


    // update blukiiArrayList and send list to widget
    private void updateBlukiiList(ArrayList<InputElement> currentFoundDeviceElementList) {
        Log.d(TAG,"updateBlukiiList()");
        addNewBlukiis(currentFoundDeviceElementList);
        updateBlukiiStatus(currentFoundDeviceElementList);

        sendBlukiiArrayList();

    }

    // send list to widget
    private void sendBlukiiArrayList() {

        Intent intent = new Intent(MainReceiver.ACTION_SEND_BLUKIILIST);
        intent.putParcelableArrayListExtra(EXTRA_BLUKIILIST, blukiiArrayList);
        sendBroadcast(intent);
    }

    // add to blukiiArrayList
    private void addNewBlukiis(ArrayList<InputElement> foundDeviceArrayList) {
        Log.d(TAG,"addNewBlukiis()");
        for (InputElement currentDeviceElement : foundDeviceArrayList) {
            boolean blukiiAlreadyInList = false;

            for (InputElement currentBlukiiElement : blukiiArrayList) {
                if (currentDeviceElement.getTagID().equals(currentBlukiiElement.getTagID())) {
                    blukiiAlreadyInList = true;
                    break;
                }
            }
            if (!blukiiAlreadyInList) {
                InputElement newBlukiiElement = new InputElement(currentDeviceElement);
                blukiiArrayList.add(newBlukiiElement);
            }
        }
    }

    // actualize blukiiArrayList item params
    private void updateBlukiiStatus(ArrayList<InputElement> foundDeviceArrayList) {
        Log.d(TAG,"updateBlukiiStatus()");
        for (int i = 0; i < blukiiArrayList.size(); i++) {
            boolean deviceIsConnected = false;
            for (InputElement currentDeviceElement : foundDeviceArrayList) {
                if (currentDeviceElement.getTagID().equals(blukiiArrayList.get(i).getTagID())) {
                    blukiiArrayList.get(i).copy(currentDeviceElement);
                    blukiiArrayList.get(i).setIsConnected(true);
                    deviceIsConnected = true;
                    break;
                }
            }
            if (!deviceIsConnected) {
                blukiiArrayList.get(i).setIsConnected(false);
                blukiiArrayList.get(i).setRssi(Short.MIN_VALUE);
            }
        }
    }

    private void sendToParse(InputElement sensorData) {
        ParseObject sensorScore = new ParseObject("SensorRecord");
        sensorScore.put("id", sensorData.getTagID());
        sensorScore.put("temperature", sensorData.getTemperature());
        sensorScore.put("timestamp", new Date(sensorData.getDeviceFoundDate()));
        sensorScore.saveEventually();
    }




}