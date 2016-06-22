package bft.fishtagsapp.gps;

/**
 * Created by ksoltan on 6/21/2016.
 */

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class MyLocation {
    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    boolean permission;

    public MyLocation(boolean perm) {
        permission = perm;
    }


    public boolean getLocation(Context context, LocationResult result, boolean perm) {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        permission = perm;
        locationResult=result;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // check if enabled and if not send user to the GPS settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Log.i("ENABLED GPS", "FALSE");
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        }

        Log.i("ENABLED GPS","TRUE");
        //hopefully user will enable GPS here
        //exceptions will be thrown if provider is not permitted.
        
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled)
            return false;

        if (permission) {
            try {
                if (gps_enabled)
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
                if (network_enabled)
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
                timer1 = new Timer();
                timer1.schedule(new GetLastLocation(), 20000);
                return true;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
            if (permission) {
                try {
                    lm.removeUpdates(this);
                    lm.removeUpdates(locationListenerNetwork);

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
            if (permission) {
                try {
                    lm.removeUpdates(this);
                    lm.removeUpdates(locationListenerGps);

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            if (permission) {
                try {
                    lm.removeUpdates(locationListenerGps);
                    lm.removeUpdates(locationListenerNetwork);
                    Location net_loc = null, gps_loc = null;
                    if (gps_enabled)
                        gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (network_enabled)
                        net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    //if there are both values use the latest one
                    if (gps_loc != null && net_loc != null) {
                        if (gps_loc.getTime() > net_loc.getTime())
                            locationResult.gotLocation(gps_loc);
                        else
                            locationResult.gotLocation(net_loc);
                        return;
                    }

                    if (gps_loc != null) {
                        locationResult.gotLocation(gps_loc);
                        return;
                    }
                    if (net_loc != null) {
                        locationResult.gotLocation(net_loc);
                        return;
                    }
                    locationResult.gotLocation(null);

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }
}
