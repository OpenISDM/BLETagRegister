package com.ossf.www.bletagregister;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueToothScanActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    ListView peripheralListView;
    private final static int REQUEST_ENABLE_BT = 1;
    public static FileOutputStream fos;
    Boolean btScanning = false;
    int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    BluetoothGatt bluetoothGatt;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public Map<String, String> uuids = new HashMap<String, String>();
    public Map<String, BLEdevice> deviceInfo = new HashMap<String, BLEdevice>();

    // Stops scanning after 5 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_scan);

        peripheralListView=(ListView)findViewById(R.id.PeripheralListView);
        peripheralListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String content=adapterView.getItemAtPosition(i).toString();
                //從字串抽取mac位址
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
            if(device==null){
                BLEdevice newDev=new BLEdevice(name,mac,rssi);
                deviceInfo.put(mac,newDev);
            } else if( rssi> device.getRssi()) {
                device.setRssi(rssi);
            }
            //mNewDevicesArrayAdapter.add("MAC: " + result.getDevice().getAddress() + " rssi: " + result.getRssi() + "\n");
            //peripheralTextView.append("Index: " + deviceIndex + ", Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
            devicesDiscovered.add(result.getDevice());
            deviceIndex++;
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            BlueToothScanActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.setText("device read or wrote to\n");
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    BlueToothScanActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.setText("device disconnected\n");
                        }
                    });
                    break;
                case 2:
                    BlueToothScanActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.setText("device connected\n");
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    BlueToothScanActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.setText("we encounterned an unknown state, uh oh\n");
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            BlueToothScanActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.setText("device services have been discovered\n");
                }
            });
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        System.out.println(characteristic.getUuid());
    }

    public void startScanning() {
        System.out.println("start scanning");
        btScanning = true;
        deviceIndex = 0;
        deviceInfo.clear();
        peripheralTextView.setText("Started Scanning\n");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        }, SCAN_PERIOD);*/
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        peripheralTextView.setText("Stopped Scanning\n");
        btScanning = false;
        mNewDevicesArrayAdapter.clear();
        //sort deviceInfo and print
        List<Map.Entry<String, BLEdevice>> list = new ArrayList<Map.Entry<String,BLEdevice>>(deviceInfo.entrySet());
        Collections.sort(list,valueComparator);
        for (Map.Entry<String, BLEdevice> entry : list) {
            BLEdevice device=entry.getValue();
            mNewDevicesArrayAdapter.add("name: "+device.getName()+"\nMAC: "+entry.getKey() + " rssi: " + device.getRssi());
        }
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    Comparator<Map.Entry<String, BLEdevice>> valueComparator = new Comparator<Map.Entry<String,BLEdevice>>() {
        @Override
        public int compare(Map.Entry<String, BLEdevice> o1,Map.Entry<String, BLEdevice> o2) {
            return o2.getValue().getRssi()-o1.getValue().getRssi();
        }
    };


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
            BlueToothScanActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.setText("Service disovered: "+uuid+"\n");
                }
            });
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                System.out.println("Characteristic discovered for service: " + charUuid);
                BlueToothScanActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        peripheralTextView.setText("Characteristic discovered for service: "+charUuid+"\n");
                    }
                });

            }
        }
    }

}
