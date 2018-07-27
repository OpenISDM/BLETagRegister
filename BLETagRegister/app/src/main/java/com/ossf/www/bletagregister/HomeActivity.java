package com.ossf.www.bletagregister;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void onAddBLE(View view) {
        Intent intent = new Intent(this, BlueToothScan.class);
        startActivity(intent);
    }

    public void onConnectXbee(View view) {


    }
}
