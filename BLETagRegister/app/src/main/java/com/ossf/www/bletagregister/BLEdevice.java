package com.ossf.www.bletagregister;

public class BLEdevice {
    private int rssi;
    private String name;
    private String mac;
    private String reg_name;
    boolean isReg;
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
    }
    public BLEdevice(String n,String m,int r){
        rssi=r;
        name=n;
        mac=m;
        reg_name=null;
        isReg=false;
    }
    public void register(String n){ reg_name=n; }
    public void setRssi(int r){ rssi=r; }
    public int getRssi(){ return rssi; }
    public String getName(){ return name; }
    public String getMac(){ return mac; }
    public String getRegName(){ return reg_name; }
}
