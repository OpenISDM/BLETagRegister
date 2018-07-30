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

    // Constants.
    protected static final int ACTION_CLEAR_ERROR_MESSAGE = 0;
    protected static final int ACTION_SET_ERROR_MESSAGE = 1;
    protected static final int ACTION_SET_SUCCESS_MESSAGE = 2;
    protected static final int ACTION_SHOW_READ_PROGRESS_DIALOG = 3;
    protected static final int ACTION_SHOW_WRITE_PROGRESS_DIALOG = 4;
    protected static final int ACTION_SHOW_DISCOVER_PROGRESS_DIALOG = 5;
    protected static final int ACTION_SHOW_SEND_PROGRESS_DIALOG = 6;
    protected static final int ACTION_HIDE_PROGRESS_DIALOG = 7;
    protected static final int ACTION_UPDATE_LIST_VIEW = 8;
    protected static final int ACTION_UPDATE_LIST_TEXT = 9;
    protected static final int ACTION_ENABLE_PARAMETERS_BUTTONS = 10;
    protected static final int ACTION_DISABLE_PARAMETERS_BUTTONS = 11;
    protected static final int ACTION_CLEAR_VALUES = 12;
    protected static final int ACTION_ENABLE_DISCOVER_BUTTONS = 13;
    protected static final int ACTION_DISABLE_DISCOVER_BUTTONS = 14;
    protected static final int ACTION_ADD_PACKET_TO_LIST = 15;
    protected static final int ACTION_ADD_DEVICE_TO_LIST = 16;

    // Variables.
    protected XBeeManager xbeeManager;
    private ArrayList<AbstractReceivedPacket> receivedPackets;
    private ReceivedXBeePacketsAdapter receivedPacketsAdapter;

    private TextView receivedPacketsText;
    private TextView dateText;
    private TextView typeText;
    private TextView sourceAddressText;
    private TextView packetDataText;

    private final Object receivedPacketsLock = new Object();

    private IncomingHandler handler = new IncomingHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xbee_received_data);

        // xbeeManager = XbeeConnectActivity.xbeeManager;
        // xbeeManager = (XBeeManager) getIntent().getSerializableExtra("XBeeManager");
        xbeeManager = BLETagRegisterApplication.getInstance().getXBeeManager();
        TextView XBeeMacAddress = (TextView)findViewById(R.id.xbee_mac_address);
        XBeeMacAddress.setText(xbeeManager.getLocalXBeeDevice().get64BitAddress().toString());

        // Check if we have to initialize the received packets variables.
        if (receivedPackets == null) {
            receivedPackets = new ArrayList<AbstractReceivedPacket>();
            receivedPacketsAdapter = new ReceivedXBeePacketsAdapter(this, receivedPackets);
        }

        // Initialize all required UI elements.
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
        dateText = (TextView)findViewById(R.id.date_text);
        typeText = (TextView)findViewById(R.id.packet_type_text);
        sourceAddressText = (TextView)findViewById(R.id.source_address_text);
        packetDataText = (TextView)findViewById(R.id.packet_data_text);

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
                case ACTION_UPDATE_LIST_VIEW:
                    recPacketsFragment.receivedPacketsAdapter.notifyDataSetChanged();
                    sendEmptyMessage(ACTION_UPDATE_LIST_TEXT);
                    break;
                case ACTION_UPDATE_LIST_TEXT:
                    recPacketsFragment.receivedPacketsText.setText(
                            String.format("%s %s",
                                    recPacketsFragment.receivedPackets.size(),
                                    recPacketsFragment.getResources().getString(R.string.packets_received)));
                    break;
                case ACTION_CLEAR_VALUES:
                    recPacketsFragment.dateText.setText("");
                    recPacketsFragment.typeText.setText("");
                    recPacketsFragment.sourceAddressText.setText("");
                    recPacketsFragment.packetDataText.setText("");
                    break;
                case ACTION_ADD_PACKET_TO_LIST:
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

    /**
     * Updates the list view.
     */
    public void updateListView() {
        handler.sendEmptyMessage(ACTION_UPDATE_LIST_VIEW);
    }


    /**
     * Handles what happens when a received packet is selected from the list.
     *
     * @param selectedPacket Selected received packet.
     */
    private void handlePacketSelected(AbstractReceivedPacket selectedPacket) {
        if (selectedPacket == null) {
            clearValues();
            return;
        }
        dateText.setText(selectedPacket.getDateAndTimeString());
        typeText.setText(String.format("%s %s",
                selectedPacket.getType().getName(),
                getResources().getString(R.string.packet_suffix)));
        sourceAddressText.setText(selectedPacket.getSourceAddress().toString());
        packetDataText.setText(selectedPacket.getPacketData());
    }

    /**
     * Handles what happens when the clear button is pressed.
     */
    private void handleClearButtonPressed() {
        synchronized (receivedPacketsLock) {
            receivedPackets.clear();
        }
        updateListView();
        handlePacketSelected(null);
    }

    /**
     * Clears the text values.
     */
    private void clearValues() {
        handler.sendEmptyMessage(ACTION_CLEAR_VALUES);
    }

    /**
     * Adds the given packet to the list of packets.
     *
     * @param receivedPacket Packet to add to the list.
     */
    private void addPacketToList(AbstractReceivedPacket receivedPacket) {
        Message msg = handler.obtainMessage(ACTION_ADD_PACKET_TO_LIST);
        msg.obj = receivedPacket;
        handler.sendMessage(msg);
    }
}

