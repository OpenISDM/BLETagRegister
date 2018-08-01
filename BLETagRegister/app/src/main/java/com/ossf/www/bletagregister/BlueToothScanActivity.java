package com.ossf.www.bletagregister;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;

/*
	activity : BlueToothScanActivity
        description : Press start button to scan ble device and press stop button to stop scanning.
        author : Cynthia
        date : 2018.08.01
 */
public class BlueToothScanActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    ListView peripheralListView;
    private final static int REQUEST_ENABLE_BT = 1;
    Boolean btScanning = false;
    int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    public Map<String, BLEdevice> deviceInfo = new HashMap<String, BLEdevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_scan);
        initializexml();
        requestBlueToothService();
    }

    private void initializexml(){
        peripheralListView=(ListView)findViewById(R.id.PeripheralListView);
        peripheralListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String content=adapterView.getItemAtPosition(i).toString();
                //get mac address from string content
                int index=content.indexOf("MAC");
                String mac=adapterView.getItemAtPosition(i).toString().substring(index+5,index+22);
                Log.v("apple","mac="+mac);
                new EditNameDialog(BlueToothScanActivity.this,deviceInfo.get(mac)).show();
            }
        });
        mNewDevicesArrayAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1);
        peripheralListView.setAdapter(mNewDevicesArrayAdapter);
        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

    }

    private void requestBlueToothService(){
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int rssi=result.getRssi();
            String mac=result.getDevice().getAddress();
            String name=result.getDevice().getName();
            BLEdevice device=deviceInfo.get(mac);
            if(regDevice_list.get(mac)==null){ // if this mac is not registered
                if(device==null){
                    BLEdevice newDev=new BLEdevice(name,mac,rssi);
                    deviceInfo.put(mac,newDev);
                } else if( rssi> device.getRssi()) {
                    device.setRssi(rssi);
                }
            }
            devicesDiscovered.add(result.getDevice());
            deviceIndex++;
        }
    };

    private void startScanning() {
        System.out.println("start scanning");
        btScanning = true;
        deviceIndex = 0;
        deviceInfo.clear();
        peripheralTextView.setText("Started Scanning. Press the stop button to stop.\n");
        // change button
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        // start scanning
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    private void stopScanning() {
        System.out.println("stopping scanning");
        peripheralTextView.setText("Stopped Scanning\n");
        btScanning = false;
        mNewDevicesArrayAdapter.clear();
        //sort deviceInfo
        List<Map.Entry<String, BLEdevice>> list = new ArrayList<Map.Entry<String,BLEdevice>>(deviceInfo.entrySet());
        Collections.sort(list,valueComparator);
        // show on listview
        for (Map.Entry<String, BLEdevice> entry : list) {
            BLEdevice device=entry.getValue();
            mNewDevicesArrayAdapter.add("name: "+device.getName()+"\nMAC: "+entry.getKey() + " rssi: " + device.getRssi());
        }
        // button change
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        // stop scanning
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    // compare function for sorting BLEdevice by rssi
    Comparator<Map.Entry<String, BLEdevice>> valueComparator = new Comparator<Map.Entry<String,BLEdevice>>() {
        @Override
        public int compare(Map.Entry<String, BLEdevice> o1,Map.Entry<String, BLEdevice> o2) {
            return o2.getValue().getRssi()-o1.getValue().getRssi();
        }
    };

}
