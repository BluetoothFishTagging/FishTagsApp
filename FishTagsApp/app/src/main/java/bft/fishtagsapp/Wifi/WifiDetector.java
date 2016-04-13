package bft.fishtagsapp.Wifi;

/**
 * Created by jamiecho on 3/9/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class WifiDetector extends BroadcastReceiver {

    public WifiDetector() {

    }
    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.isConnected()){
                Toast.makeText(context, "WIFI_ESTABLISHED", Toast.LENGTH_LONG).show();
                //Indicate main activity that wifi has been connected
            }
        }
    }
}
