package com.ossf.www.bletagregister.Xbee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.digi.xbee.api.exceptions.XBeeException;
import com.ossf.www.bletagregister.BLETagRegisterApplication;
import com.ossf.www.bletagregister.HomeActivity;
import com.ossf.www.bletagregister.R;
import com.ossf.www.bletagregister.Xbee.managers.XBeeManager;

public class XbeeConnectActivity extends AppCompatActivity {

    private boolean connecting = false;
    private XBeeManager xbeeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xbee_connect);

        xbeeManager = BLETagRegisterApplication.getInstance().getXBeeManager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Avoid accesses while connecting.
        if (connecting)
            return;

        // Instantiate the XBeeDevice object.
        xbeeManager.createXBeeDevice(XBeeConstants.BAUDRATE);

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

    /**
     * Displays the given message.
     *
     * @param message The message to show.
     */
    private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(XbeeConnectActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
