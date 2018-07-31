/**
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

package com.ossf.www.bletagregister.Xbee.managers;

import android.content.Context;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IIOSampleReceiveListener;
import com.digi.xbee.api.listeners.IModemStatusReceiveListener;
import com.digi.xbee.api.models.XBee64BitAddress;


public class XBeeManager {
	
	// Variables.
	private int baudRate;
	
	private XBeeDevice localDevice;
	
	private Context context;
	
	/**
	 * Class constructor. Instances a new {@code XBeeManager} object with the
	 * given parameters.
	 * 
	 * @param context The Android context.
	 */
	public XBeeManager(Context context) {
		this.context = context;
	}
	
	/**
	 * Creates the local XBee Device using the Android USB Host API with the
	 * given baud rate.
	 * 
	 * @param baudRate Baud rate to use in the XBee Connection.
	 */
	public void createXBeeDevice(int baudRate) {
		this.baudRate = baudRate;
		localDevice = new XBeeDevice(context, baudRate);
	}
	
	/**
	 * Returns the local XBee device.
	 * 
	 * @return The local XBee device.
	 */
	public XBeeDevice getLocalXBeeDevice() {
		return localDevice;
	}
	
	/**
	 * Returns the baud rate used for the XBee device connection.
	 * 
	 * @return The baud rate used for the XBee device connection.
	 */
	public int getBaudRate() {
		return baudRate;
	}
	
	/**
	 * Retrieves the local XBee device 64-bit address.
	 * 
	 * @return The local XBee device 64-bit address.
	 */
	public XBee64BitAddress getLocalXBee64BitAddress() {
		return localDevice.get64BitAddress();
	}

	/**
	 * Subscribes the given listener to the list of listeners that will be
	 * notified when XBee data packets are received.
	 * 
	 * @param listener Listener to subscribe.
	 */
	public void subscribeDataPacketListener(IDataReceiveListener listener) {
		localDevice.addDataListener(listener);
	}
	
	/**
	 * Unsubscribes the given listener from the list of data packet listeners.
	 * 
	 * @param listener Listener to unsubscribe.
	 */
	public void unsubscribeDataPacketListener(IDataReceiveListener listener) {
		localDevice.removeDataListener(listener);
	}
	
	/**
	 * Subscribes the given listener to the list of listeners that will be
	 * notified when XBee IO packets are received.
	 * 
	 * @param listener Listener to subscribe.
	 */
	public void subscribeIOPacketListener(IIOSampleReceiveListener listener) {
		localDevice.addIOSampleListener(listener);
	}
	
	/**
	 * Unsubscribes the given listener from the list of IO packet listeners.
	 * 
	 * @param listener Listener to unsubscribe.
	 */
	public void unsubscribeIOPacketListener(IIOSampleReceiveListener listener) {
		localDevice.removeIOSampleListener(listener);
	}
	
	/**
	 * Subscribes the given listener to the list of listeners that will be
	 * notified when Modem Status events are received.
	 * 
	 * @param listener Listener to subscribe.
	 */
	public void subscribeModemStatusPacketListener(IModemStatusReceiveListener listener) {
		localDevice.addModemStatusListener(listener);
	}
	
	/**
	 * Unsubscribes the given listener from the list of Modem Status packet
	 * listeners.
	 * 
	 * @param listener Listener to unsubscribe.
	 */
	public void unsubscribeModemStatusPacketListener(IModemStatusReceiveListener listener) {
		localDevice.removeModemStatusListener(listener);
	}
	
	/**
	 * Attempts to open the local XBee Device connection.
	 * 
	 * @throws XBeeException if any error occurs during the process.
	 */
	public void openConnection() throws XBeeException {
		if (!localDevice.isOpen())
			localDevice.open();
	}
	
	/**
	 * Attempts to close the local XBee Device connection.
	 */
	public void closeConnection() {
		if (localDevice.isOpen())
			localDevice.close();
	}
}
