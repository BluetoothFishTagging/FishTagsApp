package bft.fishtagsapp;

import android.app.Application;

import bft.fishtagsapp.Storage.Storage;
import bft.fishtagsapp.Wifi.WifiDetector;

/**
 * Created by jamiecho on 6/10/16.
 */
public class FishTagsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WifiDetector.register(this);
        Storage.register(this, Constants.APP_DIRECTORY);
    }
}
