# BLE Tag Register
Android App with two main functions: BLE scanner and Zigbee packet receiver.

## Getting Started
> Android min SDK Version 22, target SDK Version 26

- BLE Scanner
1. Click on "ADD BLE" button to scan BLE device.
2. Click on a BLE device to register it with its MAC address.
3. The registered BLE devices are shown in the homepage, and are no longer able to be scanned.
3. Select the BLE devices from homepage for comparison.

- Zigbee Packet Receiver
4. Connect a Zigbee module to simulate a Gateway. 
5. Click on "RECEIVE DATA" button to show received Zigbee packet.
6. Click on the received Zigbee packet to get more details. The packet data from LBeacon should be like:
```
[MAC address 0],[timestamp 0A],[timestamp 0B];[MAC address 1],[timestamp 1A],[timestamp 1B];
```
7. Click on "COMPARE" button to get comparison results.  
8. All selected BLE devices are shown, with the extra information of "is Found: true" or "is Found: false"

## Library
XBee, Digi International Inc.
https://github.com/digidotcom/XBeeManagerSample

## Authors
- Ting Wei Chiang @tiffany85211
- Cynthia Wu @heysun0728

