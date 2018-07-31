package com.ossf.www.bletagregister.Xbee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ossf.www.bletagregister.R;

import java.io.Serializable;

public class XBeePacketDetailsActivity extends AppCompatActivity {
    private TextView dateText;
    private TextView typeText;
    private TextView sourceAddressText;
    private TextView packetDataText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xbee_packet_details);

        Bundle bundle = this.getIntent().getExtras();

        dateText = (TextView)findViewById(R.id.date_text);
        typeText = (TextView)findViewById(R.id.packet_type_text);
        sourceAddressText = (TextView)findViewById(R.id.source_address_text);
        packetDataText = (TextView)findViewById(R.id.packet_data_text);

        dateText.setText(bundle.getString("date"));
        typeText.setText(bundle.getString("type"));
        sourceAddressText.setText(bundle.getString("sourceAddress"));
        packetDataText.setText(bundle.getString("packetData"));
    }
}
