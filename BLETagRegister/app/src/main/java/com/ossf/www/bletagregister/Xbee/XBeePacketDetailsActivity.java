package com.ossf.www.bletagregister.Xbee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ossf.www.bletagregister.R;

import java.util.ArrayList;

public class XBeePacketDetailsActivity extends AppCompatActivity {

    // Variables.
    private TextView dateText;
    private TextView typeText;
    private TextView sourceAddressText;
    private TextView packetDataText;

    private String data;
    private ArrayList<String> addresses;

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
    }

    private void parseData(String data) {
        String[] item = data.split(";");
        for(int i = 0; i < item.length; i++) {
            String[] property = item[i].split(",");
            addresses.add(property[0]);
        }
    }
}
