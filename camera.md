---
title: Camera
layout: template
filename: camera
---

## Managing Photography

[Link to Github](https://github.com/BluetoothFishTagging/FishTagsApplication/tree/master/FishTagsApp/app/src/main/java/bft/fishtagsapp/camera)

### Android References:

- [Camera](https://developer.android.com/guide/topics/media/camera.html)

Fish Tags currently uses the camera app already installed on the user’s phone.

When the user gets to the point in the app where they are asked to take a picture, the program creates a file to store the photo, uses the existing camera app to take a picture, and finally write the picture to the said file.

In your AndroidManifest.xml, declare that you will be using the camera:

```xml
<uses-permission android:name="android.permission.CAMERA" />

<uses-feature
    android:name="android.hardware.camera"
    android:required="true" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

Make sure you declare everything (permissions, features) you will be using for the camera in the Android Manifest (AndroidManifest.xml) above the <application> tag but within the <manifest> tag.

Starting from Android API Level 23 (Android 6.0 Marshmallow), you are required to ask [runtime permissions](../runtime_permissions) to interact with the camera.

Example : 

1. To use the existing camera app, create an intent for the camera: 

   ```java
   Intent tackPictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
   ```

2. Check that there is a camera app that you can request the use of:

   ```java
   if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      //code
   }
   ```

3. Create the file to write the image data, and obtain its URI.

   ```java
   private File createFile(File storageDir, String extension, String dotExtension) throws IOException {
       /* Create a unique file name */
       String timeStamp = Utils.timestamp();
       String fileName = extension + "_" + timeStamp + "_";

       File file = new File(storageDir + "/" + fileName + dotExtension);
       file.createNewFile();

       return file;
   }
   
   // ... code ...

   photoFile = createFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "JPEG", ".jpg");
   photoUri = Uri.fromFile(photoFile);
   ```

   createFile() generates a time-stamped name for the file, making it almost impossible for any name collisions to happen with existing files.

   MAKE SURE THIS FUNCTION ACTUALLY CREATES THE FILE. If the URI generated from the file is null, you have problems, and you will get nasty bugs later.

4. Pass the thus obtained URI to the intent, and start the camera activity.

   ```java
   takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
   startActivityForResult(takePictureIntent, Constants.REQUEST_TAKE_PHOTO);
   ```

5. All together:

   ```java
   public void takePicture() {
       Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
   
           /* Ensure that there's a camera activity to handle the intent */
   
       if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
   
           /* Create the File where the photo should go */
           File photoFile = null;
           try {
               photoFile = createFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "JPEG", ".jpg");
               photoUri = Uri.fromFile(photoFile);
   
               if (photoUri) {
                   Log.i("Camera", photoUri.toString());
   				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
   				startActivityForResult(takePictureIntent, Constants.REQUEST_TAKE_PHOTO);
               } else {
                   Log.i("Camera", "Failed to create file");
               }
           } catch (IOException ex) {
               ex.printStackTrace();
           }
       }
   }
   ```

We made the URI of the file for the picture a global variable in the class because after you pass the URI to the camera intent, it will automatically save the photo to this file location and does not return it. You need to know the URI of the file to later access it to send the photo to the cloud database by passing it back to the activity that called the camera activity as a String extra.

The first prototype of the Fish Tags app included creating a bitmap from the photo to display to the user in the form. This feature was taken out because while on a boat, no one has time or energy to review the form. Every step of the process should be a do it and move on step for the user.
