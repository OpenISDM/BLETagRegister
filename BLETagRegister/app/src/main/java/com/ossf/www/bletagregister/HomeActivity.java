package com.ossf.www.bletagregister;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ossf.www.bletagregister.Xbee.XbeeConnectActivity;
import com.ossf.www.bletagregister.Xbee.managers.XBeeManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    public static Map<String, BLEdevice> regDevice_list;
    private ListView listview;
    private static ArrayAdapter<String> DevicesArrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        requestPermission(); //權限要求
        regDevice_list=new HashMap<String, BLEdevice>();
        // initiallize xml
        listview=(ListView)findViewById(R.id.lv_regDevice);
        DevicesArrayAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice);
        listview.setAdapter(DevicesArrayAdapter);
        // show list
        getRegList();
        listInitiallize();
    }
    // 將 ble.txt資料丟進regDevice_list裡面
    public void getRegList(){
        regDevice_list.clear();
        FileStream fs;
        try {
            fs = new FileStream();
            fs.readRegList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //list refresh
    public static void listInitiallize(){
        DevicesArrayAdapter.clear();
        for (Map.Entry<String, BLEdevice> entry : regDevice_list.entrySet()) {
            BLEdevice device=entry.getValue();
            DevicesArrayAdapter.add("name: "+device.getRegName()+"\nMAC: "+device.getMac());
        }
    }

    public void requestPermission(){
        // write external storage enaled
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //  access coarse location enabled
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            });
            builder.show();
        }
    }

    public void onAddBLE(View view) {
        Intent intent = new Intent(this, BlueToothScanActivity.class);
        startActivity(intent);
    }

    public void onConnectXbee(View view) {
        Intent intent = new Intent(this, XbeeConnectActivity.class);
        startActivity(intent);
    }

}
