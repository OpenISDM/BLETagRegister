package com.ossf.www.bletagregister.Xbee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ossf.www.bletagregister.BLEdevice;
import com.ossf.www.bletagregister.R;

import java.util.ArrayList;
import java.util.Map;

import static com.ossf.www.bletagregister.HomeActivity.listview;
import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;

public class CompareResultsActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private ListView compareResultListView;
    BLEdevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_results);

        compareResultListView = (ListView)findViewById(R.id.compare_result_listview);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        compareResultListView.setAdapter(adapter);
        int i = 0;
        for (Map.Entry<String, BLEdevice> entry : regDevice_list.entrySet()) {
            if(listview.isItemChecked(i)) {
                device = entry.getValue();
                adapter.add("MAC: " + device.getMac() + "\nname: " + device.getName() + "\nis Found: " + device.deviceIsFound());
            }
            i++;
        }

    }
}
