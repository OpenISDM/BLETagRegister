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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.digi.xbee.api.exceptions.XBeeException;
import com.ossf.www.bletagregister.Xbee.XBeeReceivedPacketsActivity;
import com.ossf.www.bletagregister.Xbee.internal.XBeeConstants;
import com.ossf.www.bletagregister.Xbee.managers.XBeeManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    public static Map<String, BLEdevice> regDevice_list;
    public static ListView listview;
    public static ArrayAdapter<String> DevicesArrayAdapter;
    private XBeeManager xbeeManager;
    private boolean connecting = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        requestPermission(); //權限要求
        regDevice_list=new HashMap<String, BLEdevice>();

        // Get Xbee manager
        xbeeManager = BLETagRegisterApplication.getInstance().getXBeeManager();
        // Instantiate the XBeeDevice object.
        xbeeManager.createXBeeDevice(XBeeConstants.BAUDRATE);

        // initiallize xml
        listview=(ListView)findViewById(R.id.lv_regDevice);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DevicesArrayAdapter.notifyDataSetChanged();
                if(!listview.isItemChecked(i)) {
                    listview.setItemChecked(i,false);
                }else{
                    listview.setItemChecked(i,true);
                }
            }
        });
        DevicesArrayAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice);
        listview.setAdapter(DevicesArrayAdapter);
        // show list
        getRegList();
        listInitiallize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Avoid accesses while connecting. Check connection status.
        if (connecting || xbeeManager.getLocalXBeeDevice().isOpen())
            return;

        // Create the connection thread.
        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                connecting = true;
                try {
                    // Open device connection
                    xbeeManager.openConnection();
                    showToastMessage("Device open: " + xbeeManager.getLocalXBeeDevice().toString());
                } catch (XBeeException e) {
                    showToastMessage("Could not open device: " + e.getMessage());
                }
                connecting = false;
            }
        });
        connectThread.start();

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

    public void onReceiveXBeeData(View view) {
        if(xbeeManager.getLocalXBeeDevice().isOpen()) {
            Intent intent = new Intent(this, XBeeReceivedPacketsActivity.class);
            startActivity(intent);
        }
        else {
            showToastMessage("XBee is not connected. Please connect XBee and restart the app.");
        }
    }

    /**
     * Displays the given message.
     *
     * @param message The message to show.
     */
    private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
