---
title: Camera
layout: template
filename: camera
---

## Managing Photography

### Android References:

- [Camera](https://developer.android.com/guide/topics/media/camera.html)
- [Requesting Runtime Permissions](https://developer.android.com/training/permissions/requesting.html)

Fish Tags uses the camera app already installed on the user’s phone. When the user gets to the point in the app where they are asked to take a picture, the program creates a temporary file to store their photo, uses the existing camera app to take a picture, and finally saves the picture taken to the file it created before.

Make sure you declare everything (permissions, features) you will be using for the camera in the Android Manifest (AndroidManifest.xml) above the <application> tag but within the <manifest> tag. The declarations you need are well documented in the Android Developers documentation (linked at the top). Starting from Android API Level 23 (Android 6.0 Marshmallow), you are required to ask permission from the user at runtime (when your app is running and the user can be interacting with it) to allow your app to access things like the location, camera, storage, etc. These permissions, in addition to declaring that you will be using certain features of the phone, must be declared in the manifest.

To use the existing camera app, create an intent for the camera. Check that this intent can be handled (that there is a camera app that you can request use of). Create a blank file in the storage system. This is where you will save the photo the camera takes. We used a helper function called createFile to take in where you want to create a new file and what kind of extension it has. The function generates a time-stamped name for the file, making it almost impossible for any name collisions to happen with existing files.
MAKE SURE THIS FUNCTION ACTUALLY CREATES THE FILE. If the URI generated from the file is null, you have problems, and you will get nasty bugs later.
Make sure to add the URI as an extra to the camera intent like so:

```java
takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
```

We made the URI of the file for the picture a global variable in the class because after you pass the URI to the camera intent, it will automatically save the photo to this file location and does not return it. You need to know the URI of the file to later access it to send the photo to the cloud database by passing it back to the activity that called the camera activity as a String extra.

The first prototype of the Fish Tags app included creating a bitmap from the photo to display to the user in the form. This feature was taken out because while on a boat, no one has time or energy to review the form. Every step of the process should be a do it and move on step for the user.
