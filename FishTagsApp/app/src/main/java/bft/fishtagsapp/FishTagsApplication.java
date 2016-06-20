package bft.fishtagsapp;

import android.app.Application;
import android.util.Log;

import bft.fishtagsapp.storage.Storage;
import bft.fishtagsapp.wifi.WifiDetector;

/**
 * Created by jamiecho on 6/10/16.
 */
public class FishTagsApplication extends Application {
    @Override
    public void onCreate() {
        /* Initialization before mainactivity startup*/
        super.onCreate();

        Log.i("INIT","COMPLETE");
        WifiDetector.register(this);
        Storage.register(this, Constants.APP_DIRECTORY);

    }
}
