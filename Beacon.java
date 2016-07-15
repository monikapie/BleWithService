package com.example.pietyszukm.blewithservice;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

/**
 * Created by pietyszukm on 14.07.2016.
 */
public class Beacon {
    String mac;
    String name;
    String uuid;
    int rssi;
    int strength;
    ArrayList<Integer> listRssi = new ArrayList<>();
    ArrayList<Integer> listStrength = new ArrayList<>();

    public Beacon(String dMac,String dName,int dRssi, byte[] scanRecord ) {
        setMac(dMac);
        setName(dName);
        setRssi(dRssi);
        setStrength(dRssi);
        setUuid(scanRecord);
    }

    public void setMac(String dMac){
        mac = dMac;
    }

    public String getMac(){
        return mac;
    }

    public void setName(String dName){
        name = dName;
    }

    public String getName(){
        return name;
    }

    public void setRssi(int dRssi){
        rssi = dRssi;
        listRssi.add(dRssi);
        listStrength.add((dRssi + 100) * 2);
    }

    public int getRssi(){
        return rssi;
    }

    public void setStrength(int dRssi){
        strength = (dRssi + 100) * 2;
        listStrength.add(strength);
    }

    public int getStrength() {
        return strength;
    }

    public ArrayList getAllRssi(){
        return listRssi;
    }

    public ArrayList getAllStrength(){
        return listStrength;
    }

    public void setUuid(byte[] scanRecord) {
        int startByte = 2;
        boolean patternFound = false;
        String uuid="";
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            //Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);
            uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
        }
    }
    public String getUuid(){
        return uuid;
    }


    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
