/*
    Activity : CompareResultActivity

    Description :
        Lists the MAC address comparison results between the checked devices and the received data,
        and shows if the checked devices are found or not.

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
import static com.ossf.www.bletagregister.HomeActivity.regDeviceListView;
import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;


public class CompareResultActivity extends AppCompatActivity {

    private ArrayAdapter<String> ResultsAdapter;
    private ListView compareResultListView;
    BLEdevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_result);

        // Initialize all required UI components.
        initializeXml();

        // Render results list view.
        updateListView();
    }

    private void initializeXml() {
        compareResultListView = (ListView)findViewById(R.id.compare_result_list);
        ResultsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        compareResultListView.setAdapter(ResultsAdapter);
    }

    private void updateListView() {
        int index = 0;

        for (Map.Entry<String, BLEdevice> entry : regDevice_list.entrySet()) {
            // only list the devices which are checked
            if(regDeviceListView.isItemChecked(index)) {
                device = entry.getValue();
                ResultsAdapter.add("name: " + device.getRegName()
                        + "\nMAC: " + device.getMac()
                        + "\nis Found: " + device.deviceIsFound());
            }
            index++;
        }
    }
}
