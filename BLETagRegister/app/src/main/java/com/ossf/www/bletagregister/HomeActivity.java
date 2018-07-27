package com.ossf.www.bletagregister;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ossf.www.bletagregister.Xbee.XbeeConnectActivity;
import com.ossf.www.bletagregister.Xbee.managers.XBeeManager;

public class HomeActivity extends AppCompatActivity {

    private static XBeeManager xbeeManager;
    private static HomeActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        instance = this;
        xbeeManager = new XBeeManager(this);
    }

    public void onAddBLE(View view) {
        Intent intent = new Intent(this, BlueToothScanActivity.class);
        startActivity(intent);
    }

    public void onConnectXbee(View view) {
        Intent intent = new Intent(this, XbeeConnectActivity.class);
        startActivity(intent);
    }

    public static HomeActivity getInstance() { return instance; }
    public XBeeManager getXBeeManager() {
        return xbeeManager;
    }
}
