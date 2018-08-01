package com.ossf.www.bletagregister.Xbee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.ossf.www.bletagregister.BLEdevice;
import com.ossf.www.bletagregister.R;
import java.util.Map;
import static com.ossf.www.bletagregister.HomeActivity.listview;
import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;

/*
    activity : CompareResultsActivity
    description : shows the device is found or not
    author : Cynthia
    date : 2018.08.01
 */
public class CompareResultsActivity extends AppCompatActivity {

    private ArrayAdapter<String> MacAddressAdapter;
    private ListView macAddrList;
    BLEdevice device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_results);
        initializeXml();
        showList();
    }

    private void initializeXml(){
        macAddrList = (ListView)findViewById(R.id.mac_addresses_list);
        MacAddressAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        macAddrList.setAdapter(MacAddressAdapter);
    }

    private void showList(){
        int index=0;
        for (Map.Entry<String, BLEdevice> entry : regDevice_list.entrySet()) {
            if(listview.isItemChecked(index)) { // only list the device which is checked
                device = entry.getValue();
                MacAddressAdapter.add("MAC: " + device.getMac() + "\nname: " + device.getName() + "\nis Found: " + device.deviceIsFound());
            }
            index++;
        }
    }
}
