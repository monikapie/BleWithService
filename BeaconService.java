package com.example.pietyszukm.blewithservice;

import android.annotation.TargetApi;
import android.app.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by pietyszukm on 14.07.2016.
 */
@TargetApi(18)
public class BeaconService extends Service implements BluetoothAdapter.LeScanCallback{
    private final IBinder binder = new BeaconBinder();
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler();
    DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy, HH:mm");
    private static final String tag = "INFO";
    private ArrayList<Beacon> beaconList = new ArrayList<Beacon>();
    private ArrayList<String> timeList = new ArrayList<String>();
    int i = 0;

    public class BeaconBinder extends Binder{
        BeaconService getBeaconService(){
            return BeaconService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        bleSupported();
        bluetoothManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();
        bluetoothSupported(bluetoothAdapter);
        handler.post(startRunnable);
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        handler.post(stopRunnable);
    }

    public void bleSupported(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            this.stopSelf();
        }
    }

    public void bluetoothSupported(BluetoothAdapter bA){
        if (bA == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            this.stopSelf();
        }
    }

    public Intent bluetoothOn(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            return enableBtIntent;
        }
        return null;
    }

    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    public void startScan() {
        bluetoothAdapter.startLeScan(this);
    }

    public void stopScan() {
        bluetoothAdapter.stopLeScan(this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(tag,"scanning"+"\t"+device.getAddress());
        Beacon beacon = new Beacon(device.getAddress(),device.getName(),rssi,scanRecord);
        String date = dateFormat.format(Calendar.getInstance().getTime());
        timeList.add(date);
        beaconList.add(beacon);
        i++;
        Log.i(tag, String.valueOf(timeList.get(i-1)+"\t"+beaconList.get(i-1).getName())+"\t"+String.valueOf(beaconList.get(i-1).getRssi()));
    }

//    public String senCostam(){
//        return "Haloo";
//    }

    public ArrayList<Beacon> getBeaconList(){
        return beaconList;
    }

    public void clearBeaconList(){
        beaconList.clear();
    }

    private void saveToFile(ArrayList<Beacon> beaconArrayList){
        Log.i(tag,getString(R.string.saving_file));
        try {
            File traceFile = new File(((Context)this).getExternalFilesDir(null),"TraceFile.txt");
            if (!traceFile.exists()){
                traceFile.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile, true));
            String beaconDetails;
            for(Beacon b:beaconArrayList){
                beaconDetails = timeList.get(timeList.indexOf(b))+"\t"+b.getMac()+"\t"+b.getName()+"\t"+getString(b.getRssi())+"\n";
                writer.write(beaconDetails);
            }
            writer.close();

            MediaScannerConnection.scanFile((Context) (this),
                    new String[]{traceFile.toString()}, null, null);
        } catch (IOException e) {
            Log.e("FileTestError", "Unable to write to the TraceFile.txt file.");
        }
    }


}
