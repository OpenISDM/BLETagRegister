package com.ossf.www.bletagregister;

import android.app.Application;

import com.ossf.www.bletagregister.Xbee.managers.XBeeManager;


public class BLETagRegisterApplication extends Application {

	private XBeeManager xbeeManager;
	
	private static BLETagRegisterApplication instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		// Save application instance.
		instance = this;
		// Initialize Managers.
		initializeManagers();
	}
	
	/**
	 * Returns the application instance.
	 * 
	 * @return The application instance.
	 */
	public static BLETagRegisterApplication getInstance() {
		return instance;
	}
	
	/**
	 * Initializes the managers used by this application.
	 */
	private void initializeManagers() {
		xbeeManager = new XBeeManager(this);
	}
	
	/**
	 * Returns the XBee Manager.
	 * 
	 * @return The application XBee Manager.
	 */
	public XBeeManager getXBeeManager() {
		return xbeeManager;
	}
}
