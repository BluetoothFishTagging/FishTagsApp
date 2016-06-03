package bft.fishtagsapp.Wifi;

/**
 * Created by jamiecho on 3/9/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import bft.fishtagsapp.MainActivity;

public class WifiDetector extends BroadcastReceiver {
    private static Boolean connected = false;
    private static MainActivity m = null;

    public WifiDetector() {

    }
    private static boolean checkWifiOnAndConnected(MainActivity m) {
        WifiManager wifiMgr = (WifiManager) m.getSystemService(Context.WIFI_SERVICE);
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
    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.isConnected()){
                connected = true;
                Toast.makeText(context, "WIFI_ESTABLISHED", Toast.LENGTH_LONG).show();
                m.submitReports();
            }else{
                connected = false;
                Toast.makeText(context, "WIFI_DISCONNECTED", Toast.LENGTH_LONG).show();
            }
        }
    }
    public static Boolean isConnected(){
        return connected;
    }
    public static void register(MainActivity mainActivity){
        m = mainActivity;
        connected = checkWifiOnAndConnected(m);
    }
}