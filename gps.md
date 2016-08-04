---
title: GPS
layout: template
filename: gps
---

## Obtaining Location Data

[Link to Github](https://github.com/BluetoothFishTagging/FishTagsApplication/tree/master/FishTagsApp/app/src/main/java/bft/fishtagsapp/gps)

Accessing GPS Data requires [runtime permissions](../runtime_permissions).

If you haven't, read how you request runtime permissions on the link above.

### Android References:

- [Reference](http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-on-a/3145655#3145655) (Fedor’s answer)

### Implementation

The Fish Tags app needs to get the location of the user when they submit a tag form to tell researchers where the fisherman caught a fish.

1. In your AndroidManifest.xml:

   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
   ```

   The coarse location is obatined from the network provider.
   
   The fine location is obatined from GPS.

   If you are using both GPS and the network provider, you only need to declare the permission for fine location, but to be safe, we have both.

2. GPS Listener must extend LocationListener, which means the following methods must be overridden:

   ```java
   @Override
   public void onLocationChanged(Location location) {
       this.location = location;
   }
   
   @Override
   public void onStatusChanged(String provider, int status, Bundle extras) {
       //nothing yet
   }
   
   @Override
   public void onProviderEnabled(String provider) {
        //indicated provider enabled
   }
   
   @Override
   public void onProviderDisabled(String provider) {
        //indicate provider disabled
   }
   ```

3. To enable GPS:

   ```java
   public void enableGPS() {
       locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
       boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
   
       // check if enabled and if not send user to the GSP settings
   
       if (!enabled) {
           Log.i("ENABLED GPS", "FALSE");
           Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
           context.startActivity(intent);
       }
   
       //hopefully user will enable GPS here
   }
   ```

4. To obtain GPS Data:

   ```java
   public Location getGPS(Boolean permitted) {

       if (!permitted) {
           return null;
       }

       if (!enabled){
           enableGPS(); //in case not enabled
       }

       Criteria criteria = new Criteria();
       provider = locationManager.getBestProvider(criteria, true);

       //You may also query for all providers:
       //List<String> providers_all = locationManager.getAllProviders();

       try {
           // to be safe
           locationManager.requestLocationUpdates(provider, 0, 0, this);
           location = locationManager.getLastKnownLocation(provider);
       } catch (SecurityException e) {
           e.printStackTrace();
       }
   
       if (location != null) {
           System.out.println("Provider " + provider + " has been selected.");
           onLocationChanged(location);
       }
   
       return location;
   }
   ```

5. Example:

[Direct Github Link](https://github.com/BluetoothFishTagging/FishTagsApplication/blob/master/FishTagsApp/app/src/main/java/bft/fishtagsapp/gps/GPS.java)
