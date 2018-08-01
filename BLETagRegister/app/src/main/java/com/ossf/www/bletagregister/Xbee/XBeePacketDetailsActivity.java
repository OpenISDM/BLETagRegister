/*
    Activity : XBeePacketDetailsActivity

    Description :
        Shows details of the received packet (Date, Type, Source address, Packet data).
        Compares the MAC addresses in the received data to those of the checked devices in the HomeActivity.

    Author : Cynthia Wu, Tiffany Chiang
    Date : 2018.08.01
*/

package com.ossf.www.bletagregister.Xbee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ossf.www.bletagregister.BLEdevice;
import com.ossf.www.bletagregister.R;

import java.util.ArrayList;
import java.util.Map;

import static com.ossf.www.bletagregister.HomeActivity.regDevice_list;

public class XBeePacketDetailsActivity extends AppCompatActivity {

    // Variables.
    private TextView dateText;
    private TextView typeText;
    private TextView sourceAddressText;
    private TextView packetDataText;

    private String data;
    public static ArrayList<String> MAC_Addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xbee_packet_details);

        MAC_Addresses = new ArrayList<String>();

        // Initialize all required UI components.
        initializeXml();
    }

    private void initializeXml() {
        // Get packet details from bundle.
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

        // Parse data to get valid MAC address from received packet data.
        parseData(data);

        // Reset the "Found" parameter for all registered device as "false".
        resetAllFound();

        // Check if each received MAC address maps to a registered BLE device.
        BLEdevice device;
        for (int i = 0; i < MAC_Addresses.size(); i++) {
            device = regDevice_list.get( MAC_Addresses.get(i) );
            if(device != null) {
                device.Found();
            }
        }

        // Shows comparison result in CompareResultActivity
        startActivity(new Intent(this, CompareResultActivity.class));
    }

    private void parseData(String data) {
        // Received packet data should have the form of:
        // __MACADDR[0]__,__PROP1__,__PROP2__;__MACADDR[1]__,__PROP1__,__PROP2__;
        String[] item = data.split(";");
        for(int i = 0; i < item.length; i++) {
            String[] property = item[i].split(",");
            MAC_Addresses.add(property[0]);
        }
    }

    private void resetAllFound(){
        for (Map.Entry<String, BLEdevice> entry : regDevice_list.entrySet()) {
            entry.getValue().resetFound();
        }
    }
}
