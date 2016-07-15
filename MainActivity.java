package com.example.pietyszukm.blewithservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pietyszukm on 14.07.2016.
 */
public class MainActivity extends Activity{
    private  BeaconService beaconService;
    private boolean bound = false;
    private static final String tag = "INFO";
    private static final int REQUEST_ENABLE_BT = 1;

    private ServiceConnection connection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
                BeaconService.BeaconBinder beaconBinder = (BeaconService.BeaconBinder) service;
                beaconService = beaconBinder.getBeaconService();
                bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, BeaconService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
//        Intent enable = beaconService.bluetoothOn();
//        if(enable != null )
//            startActivityForResult(enable, REQUEST_ENABLE_BT);
//        watchBeaconList();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void watchBeaconList(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(beaconService == null){
                    ArrayList<Beacon> beacons;
                    beacons = beaconService.getBeaconList();
                    saveToFile(beacons);
                    beaconService.clearBeaconList();
                }
                handler.postDelayed(this, 3000);
            }
        });
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
                beaconDetails = b.getMac()+"\t"+b.getName()+"\t"+getString(b.getRssi())+"\n";
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

