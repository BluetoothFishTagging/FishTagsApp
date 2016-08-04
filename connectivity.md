---
title: Connectivity
layout: template
filename: connectivity
---

## Checking Network Connectivity

Checking Network Connectivity is crucial to the HI-Tag application, as we don't want to use up the user's phone resources by constantly trying to upload to the database even though there is no network connection. It is ideal to take note of the network status and submit the collected data when WiFi connection is established.

### Setting up

1. In your AndroidManifest.xml:

   Declare the permissions:

   ```java
   <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   ```

2. Import the relevant packages in your java file:

   ```
   import android.net.NetworkInfo;
   import android.net.wifi.WifiInfo;
   import android.net.wifi.WifiManager;
   ```

### Checking the current status

In order to check the current wifi status, it is possible to query the system service. 

```java
private static boolean checkWifiOnAndConnected(Context context) {
    WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    if (wifiMgr.isWifiEnabled()) { // WiFi adapter is ON
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        if( wifiInfo.getNetworkId() == -1 ){
            return false; // Not connected to an access-Point
        }
        return true;      // Connected to an Access Point
    } else {
        return false; // WiFi adapter is OFF
    }
}
```

### Extending a Broadcast Receiver

A [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver.html) listens to intents. In this case, system services broadcast network-related intents so that the developer can take appropriate actions every time a network status is changed.

1. In your AndroidManifest.xml, declare the receiver and install the intent-filter.

   ```
   <receiver
       android:name="WifiDetector"
       android:enabled="true"
       android:exported="true">
       <intent-filter>
           <action android:name="android.net.wifi.STATE_CHANGE" />
       </intent-filter>
   </receiver>
   ```

2. Implement OnReceive() in order to handle the wifi state-change event.

   ```
   public class WifiDetector extends BroadcastReceiver {
      protected static Boolean connected = false;
      
      @Override
      public void onReceive(Context context, Intent intent) {
          final String action = intent.getAction();
          if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
              NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
              if(info.isConnected()){
                  connected = true;
              }else{
                  connected = false;
              }
          }
      }
   }
   ```
