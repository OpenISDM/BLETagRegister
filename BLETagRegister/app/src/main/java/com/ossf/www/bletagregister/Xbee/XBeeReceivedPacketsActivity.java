package com.ossf.www.bletagregister.Xbee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ossf.www.bletagregister.BLETagRegisterApplication;
import com.ossf.www.bletagregister.R;
import com.ossf.www.bletagregister.Xbee.internal.ReceivedXBeePacketsAdapter;
import com.ossf.www.bletagregister.Xbee.internal.XBeeConstants;
import com.ossf.www.bletagregister.Xbee.models.AbstractReceivedPacket;
import com.ossf.www.bletagregister.Xbee.models.ReceivedDataPacket;
import com.ossf.www.bletagregister.Xbee.models.ReceivedIOSamplePacket;
import com.ossf.www.bletagregister.Xbee.models.ReceivedModemStatusPacket;
import com.ossf.www.bletagregister.Xbee.managers.XBeeManager;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.io.IOSample;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.models.ModemStatusEvent;
import com.digi.xbee.api.models.XBeeMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class XBeeReceivedPacketsActivity extends AppCompatActivity
        implements IDataReceiveListener, IIOSampleReceiveListener, IModemStatusReceiveListener {

    // Variables.
    protected XBeeManager xbeeManager;
    private ArrayList<AbstractReceivedPacket> receivedPackets;
    private ReceivedXBeePacketsAdapter receivedPacketsAdapter;

    private TextView receivedPacketsText;

    private final Object receivedPacketsLock = new Object();

    private IncomingHandler handler = new IncomingHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xbee_received_packets);

        // set up XBee manager
        xbeeManager = BLETagRegisterApplication.getInstance().getXBeeManager();

        // Check if we have to initialize the received packets variables.
        if (receivedPackets == null) {
            receivedPackets = new ArrayList<AbstractReceivedPacket>();
            receivedPacketsAdapter = new ReceivedXBeePacketsAdapter(this, receivedPackets);
        }

        // Initialize all required UI components.
        initializeUIComponents();

        // Render initial remote devices list.
        updateListView();
    }

    // Handler used to perform actions in the UI thread.
    static class IncomingHandler extends Handler {

        private final WeakReference<XBeeReceivedPacketsActivity> wActivity;

        IncomingHandler(XBeeReceivedPacketsActivity activity) {
            wActivity = new WeakReference<XBeeReceivedPacketsActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            XBeeReceivedPacketsActivity recPacketsFragment = wActivity.get();
            if (recPacketsFragment == null)
                return;

            switch (msg.what) {
                case XBeeConstants.ACTION_UPDATE_LIST_VIEW:
                    recPacketsFragment.receivedPacketsAdapter.notifyDataSetChanged();
                    sendEmptyMessage(XBeeConstants.ACTION_UPDATE_LIST_TEXT);
                    break;
                case XBeeConstants.ACTION_UPDATE_LIST_TEXT:
                    recPacketsFragment.receivedPacketsText.setText(
                            String.format("%s %s",
                                    recPacketsFragment.receivedPackets.size(),
                                    recPacketsFragment.getResources().getString(R.string.packets_received)));
                    break;
                case XBeeConstants.ACTION_ADD_PACKET_TO_LIST:
                    synchronized (recPacketsFragment.receivedPacketsLock) {
                        recPacketsFragment.receivedPackets.add(0, (AbstractReceivedPacket)msg.obj);
                        recPacketsFragment.updateListView();

                        int sel = recPacketsFragment.receivedPacketsAdapter.getSelection();
                        if (sel != ReceivedXBeePacketsAdapter.NOTHING_SELECTED)
                            recPacketsFragment.receivedPacketsAdapter.setSelection(sel + 1);

                        recPacketsFragment.updateListView();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Subscribe listeners.
        xbeeManager.subscribeDataPacketListener(this);
        xbeeManager.subscribeIOPacketListener(this);
        xbeeManager.subscribeModemStatusPacketListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unsubscribe listeners.
        xbeeManager.unsubscribeDataPacketListener(this);
        xbeeManager.unsubscribeIOPacketListener(this);
        xbeeManager.unsubscribeModemStatusPacketListener(this);

        // Disconnect the device.
        xbeeManager.closeConnection();
    }

    @Override
    public void dataReceived(XBeeMessage xbeeMessage) {
        ReceivedDataPacket p = new ReceivedDataPacket(xbeeMessage.getDevice().get64BitAddress(), xbeeMessage.getData());
        addPacketToList(p);
    }

    @Override
    public void ioSampleReceived(RemoteXBeeDevice remoteDevice, IOSample ioSample) {
        ReceivedIOSamplePacket p = new ReceivedIOSamplePacket(remoteDevice.get64BitAddress(), ioSample);
        addPacketToList(p);
    }

    @Override
    public void modemStatusEventReceived(ModemStatusEvent modemStatusEvent) {
        ReceivedModemStatusPacket p = new ReceivedModemStatusPacket(xbeeManager.getLocalXBee64BitAddress(), modemStatusEvent);
        addPacketToList(p);
    }

    private void initializeUIComponents() {
        // XBee device mac address.
        TextView XBeeMacAddressText = (TextView)findViewById(R.id.xbee_mac_address_text);
        XBeeMacAddressText.setText(xbeeManager.getLocalXBee64BitAddress().toString());

        // XBee packet list.
        ListView receivedPacketsList = (ListView)findViewById(R.id.received_packets_list);
        receivedPacketsList.setAdapter(receivedPacketsAdapter);
        receivedPacketsList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                receivedPacketsAdapter.setSelection(i);
                updateListView();
                handlePacketSelected(receivedPackets.get(i));
            }
        });

        // Buttons.
        Button clearButton = (Button)findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClearButtonPressed();
            }
        });

        // Texts.
        receivedPacketsText = (TextView)findViewById(R.id.received_packets_text);
    }

    /**
     * Updates the list view.
     */
    public void updateListView() {
        handler.sendEmptyMessage(XBeeConstants.ACTION_UPDATE_LIST_VIEW);
    }


    /**
     * Handles what happens when a received packet is selected from the list.
     *
     * @param selectedPacket Selected received packet.
     */
    private void handlePacketSelected(AbstractReceivedPacket selectedPacket) {
        if (selectedPacket == null) {
            return;
        }

        // Create bundle for packet details.
        Bundle bundle = new Bundle();
        bundle.putString("date", selectedPacket.getDateAndTimeString());
        bundle.putString("type", String.format("%s %s",
                selectedPacket.getType().getName(),
                getResources().getString(R.string.packet_suffix)));
        bundle.putString("sourceAddress", selectedPacket.getSourceAddress().toString());
        bundle.putString("packetData", selectedPacket.getPacketData());

        // Start activity and pass over bundle.
        Intent intent = new Intent(this, XBeePacketDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Handles what happens when the clear button is pressed.
     */
    private void handleClearButtonPressed() {
        synchronized (receivedPacketsLock) {
            receivedPackets.clear();
        }
        updateListView();
    }

    /**
     * Adds the given packet to the list of packets.
     *
     * @param receivedPacket Packet to add to the list.
     */
    private void addPacketToList(AbstractReceivedPacket receivedPacket) {
        Message msg = handler.obtainMessage(XBeeConstants.ACTION_ADD_PACKET_TO_LIST);
        msg.obj = receivedPacket;
        handler.sendMessage(msg);
    }
}

