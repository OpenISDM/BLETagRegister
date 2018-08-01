/*
    Activity : CompareResultsActivity

    Description : Shows if the checked devices are found or not.

    Author : Cynthia Wu, Tiffany Chiang
    Date : 2018.08.01
 */

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


public class CompareResultsActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
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
        macAddrList = (ListView)findViewById(R.id.compare_result_listview);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        macAddrList.setAdapter(adapter);
    }

    private void showList(){
        int index=0;
        for (Map.Entry<String, BLEdevice> entry : regDevice_list.entrySet()) {
            if(listview.isItemChecked(index)) { // only list the device which is checked
                device = entry.getValue();
                adapter.add("name: " + device.getRegName() + "\nMAC: " + device.getMac() + "\nis Found: " + device.deviceIsFound());
            }
            index++;
        }
    }
}
