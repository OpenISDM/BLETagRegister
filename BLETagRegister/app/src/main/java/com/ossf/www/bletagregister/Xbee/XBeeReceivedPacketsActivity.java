/*
    Activity : XBeeReceivedPacketsActivity

    Description :
        Starts XBee packet listeners and shows received packets in list view.

    Author : Tiffany Chiang, modified from Digi International Inc.
    Date : 2018.08.01

 * Copyright 2017, Digi International Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


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
    public static final int ACTION_UPDATE_LIST_VIEW = 1;
    public static final int ACTION_UPDATE_LIST_TEXT = 2;
    public static final int ACTION_ADD_PACKET_TO_LIST = 3;

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
        initializeXml();

        // Render packets list view.
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
     * Initializes all the required graphic UI elements of this activity.
     */
     private void initializeXml() {
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
        handler.sendEmptyMessage(ACTION_UPDATE_LIST_VIEW);
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
        Message msg = handler.obtainMessage(ACTION_ADD_PACKET_TO_LIST);
        msg.obj = receivedPacket;
        handler.sendMessage(msg);
    }
}

