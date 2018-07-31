package com.ossf.www.bletagregister.Xbee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ossf.www.bletagregister.R;

import java.util.ArrayList;

public class CompareResultsActivity extends AppCompatActivity {

    private ArrayAdapter<String> MacAddressAdapter;
    private ListView macAddrList;
    private ArrayList<String> mac_addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_results);

        macAddrList = (ListView)findViewById(R.id.mac_addresses_list);

        MacAddressAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        macAddrList.setAdapter(MacAddressAdapter);

        mac_addresses = XBeePacketDetailsActivity.MAC_Addresses;
        for(int i=0; i < mac_addresses.size(); i++) {
            MacAddressAdapter.add(mac_addresses.get(i));
        }
    }
}
