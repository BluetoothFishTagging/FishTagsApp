---
title: GPS
layout: template
filename: gps
---

## Obtaining GPS Data


### Android References:

- [Reference](http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-on-a/3145655#3145655)

The Fish Tags app needs to get the location of the user when they submit a tag form to tell researchers where the fisherman caught a fish.

Make sure you have both permissions for ACCESS_COURSE_LOCATION and ACCESS_FINE_LOCATION in your manifest.

The COURSE permission is used to get location information from the network provider.

The FINE permission is for getting location from GPS.

If you are using both GPS and the network provider, you only need to declare the FINE permission, but to be safe, we have both.

(Fedor’s answer)

This implementation uses a TimerTask (which is arguably bad, people say to use Handler), which basically creates a separate thread that will try to get the geolocation of the phone. The advantage of this method is that it takes some time to get the location, and by giving it its own thread, it won’t prevent you from using the app. It will simply return the location whenever it finds it. It also takes advantage of checking both network and GPS providers, taking the best location.

Using lastKnownLocation as many suggest if you cannot get a fresh location is not always a working option because you need to have a location that you have gotten previously.

Before trying to get a location, make sure the user has enabled GPS. This is done by creating an intent for Settings.ACTION_LOCATION_SOURCE_SETTINGS and sending the user to the Settings where they can turn on Location. After they turn it on, when they go back to the app, make sure you launch the TimerTask again to get the location.
