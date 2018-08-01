package com.ossf.www.bletagregister;
/*
    class : BLEdevice
    description : the class that records information of ble devices
    author : Cynthia
    date : 2018.08.01
*/
public class BLEdevice {

    private int rssi;
    private String name;
    private String mac;
    private String reg_name;
    boolean isReg;
    boolean isFound;
    //copy constructor
    public BLEdevice(BLEdevice copyDevice){
        this(copyDevice.getName(),copyDevice.getMac(),copyDevice.getRssi());
    }
    public BLEdevice(String rn,String m){
        rssi=0;
        name=null;
        mac=m;
        reg_name=rn;
        isReg=true;
        isFound=false;
    }
    public BLEdevice(String n,String m,int r){
        rssi=r;
        name=n;
        mac=m;
        reg_name=null;
        isReg=false;
        isFound=false;
    }
    public void register(String n){ reg_name=n; }
    public void setRssi(int r){ rssi=r; }
    public void Found(){ isFound=true; }
    public void resetFound(){ isFound=false; }
    public int getRssi(){ return rssi; }
    public String getName(){ return name; }
    public String getMac(){ return mac; }
    public String getRegName(){ return reg_name; }
    public Boolean deviceIsFound(){ return isFound; }

}
