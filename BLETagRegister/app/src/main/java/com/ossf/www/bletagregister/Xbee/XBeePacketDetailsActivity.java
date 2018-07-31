package com.ossf.www.bletagregister.Xbee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ossf.www.bletagregister.BLEdevice;
import com.ossf.www.bletagregister.R;

import java.util.ArrayList;
import java.util.Map;

import static com.ossf.www.bletagregister.HomeActivity.DevicesArrayAdapter;
import static com.ossf.www.bletagregister.HomeActivity.listview;
import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;

public class XBeePacketDetailsActivity extends AppCompatActivity {

    // Variables.
    private TextView dateText;
    private TextView typeText;
    private TextView sourceAddressText;
    private TextView packetDataText;

    private String data;
    private ArrayList<String> MAC_Addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xbee_packet_details);
        initializeUIComponents();
    }

    private void initializeUIComponents() {
        Bundle bundle = this.getIntent().getExtras();

        // Texts.
        dateText = (TextView)findViewById(R.id.date_text);
        typeText = (TextView)findViewById(R.id.packet_type_text);
        sourceAddressText = (TextView)findViewById(R.id.source_address_text);
        packetDataText = (TextView)findViewById(R.id.packet_data_text);

        dateText.setText(bundle.getString("date"));
        typeText.setText(bundle.getString("type"));
        sourceAddressText.setText(bundle.getString("sourceAddress"));
        data = bundle.getString("packetData");
        packetDataText.setText(data);

        // Buttons.
        Button compareButton = (Button)findViewById(R.id.compare_button);
        compareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCompareButtonPressed();
            }
        });
    }

    private void handleCompareButtonPressed() {
        parseData(data);
        String mac;
        BLEdevice device;
        resetAllFound();
        for (int i=0; i<MAC_Addresses.size(); i++) {
            mac=MAC_Addresses.get(i);
            Toast.makeText(XBeePacketDetailsActivity.this,mac,Toast.LENGTH_SHORT);
            device=regDevice_list.get(mac);
            if(device!=null){
                device.Found();
            }
        }
    }
    private void resetAllFound(){
        for (Map.Entry<String, BLEdevice> entry : regDevice_list.entrySet()) {
            BLEdevice device = entry.getValue();
            device.resetFound();
        }
    }

    private void parseData(String data) {
        String[] item = data.split(";");
        for(int i = 0; i < item.length; i++) {
            String[] property = item[i].split(",");
            MAC_Addresses.add(property[0]);
        }
    }
}
