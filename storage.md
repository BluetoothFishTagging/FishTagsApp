---
title: Storage
layout: template
filename: storage
---

## Managing In-App Storage

[Link to Github](https://github.com/BluetoothFishTagging/FishTagsApplication/tree/master/FishTagsApp/app/src/main/java/bft/fishtagsapp/storage)

[Android Reference](https://developer.android.com/guide/topics/data/data-storage.html)

There are two major storage options that we have explored:

 - [Shared Preferences](#shared-preferences)

 - [External Storage](#external-storage)


### Shared Preferences

Shared preferences are mostly used for storing key-value pairs.

1. Declaring the variables:

   ```java
   private static SharedPreferences prefStorage;
   private static SharedPreferences.Editor editor;
   ```

2. Defining the variables:

   ```java
   prefStorage = context.getSharedPreferences(YOUR_IDENTIFIER, Context.MODE_PRIVATE);
   editor = prefStorage.edit();
   ```

3. Writing to Shared Preferences:

   ```java
   private static void writeToPref(String Key, String message) {
       editor.putString(Key, message);
       editor.commit();
   }
   ```

4. Reading from Shared Preferences:

   ```java
   private static String readFromPref(String name) {
       return prefStorage.getString(name, null);
   }
   ```

5. Deleting in Shared Preferences:

   ```java
   private static Boolean deletePref(String Key) {
       editor.remove(Key);
       return editor.commit();
   }
   ```

### External Storage

### App-Private Storage

Using App-Private Storage does not require [runtime permissions](../runtime_permissions).

Application data are all stored under the directory you can obtain by context.getFilesDir().

However, when the application gets uninstalled, everything under the folder will be removed.

Your Data is **NOT** accessible by users.

### App-Public Storage

Using App-Private storage does not require [runtime permissions](../runtime_permissions).

Application data are all stored under the directory you can obtain by context.getExternalFilesDir().

However, when the application gets uninstalled, everything under the folder will be removed.

#### Global Storage

Using Global Storage requires [runtime permissions](../runtime_permissions).

Application data are all stored under the directory you can obtain by context.getExternalFilesStorage().

Data under this directory is both visible to users and persists after the app is uninstalled.

#### Usage

In this example, we're using app-public storage.

To use other options, simply swap out the getExternalFilesDir() with corresponding alternatives.

1. Declaration:

   ```java
   private static File fileStorage = null;//Main Directory File
   ```

2. Definition:

   ```java
   fileStorage = new File(context.getExternalFilesDir(null), YOUR_IDENTIFIER);
   if (!fileStorage.exists()) {
       fileStorage.mkdirs();
   }
   ```

3. Writing:

   ```java
   private static void writeToFile(String FileName, String messageBody) {
       try {
           File savedFile = new File(fileStorage.getAbsolutePath() + "/" + FileName);
   
           FileWriter fWriter;
   
           if (!savedFile.exists()) {
               Boolean res = savedFile.getParentFile().mkdirs(); /*makes parent directories*/
               savedFile.createNewFile();
           }
   
           fWriter = new FileWriter(savedFile);
   
           fWriter.write(messageBody);/*write data*/
           fWriter.flush();/*flush writer*/
           fWriter.close();/*close writer*/
   
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   ```

4. Reading:

   ```java
   private static String readFromFile(String FileName) {
           /*First check if main directory is present or not*/
           if (!fileStorage.exists()) {
               return null;
           } else {
               /*Then check if text file is present or not*/
               File savedFile = new File(fileStorage.getAbsolutePath() + "/" + FileName);
               if (!savedFile.exists())
                   return null;
               else {
                   /*Finally read data using FileReader*/
                   try {
                       FileReader rdr = new FileReader(fileStorage.getAbsolutePath() + "/" + FileName);
   
                       char[] inputBuffer = new char[READ_BLOCK_SIZE];/*get Block size as buffer*/
                       String savedData = "";
                       int charRead = rdr.read(inputBuffer);
                       /*Read all data one by one by using loop and add it to string created above*/
                       for (int k = 0; k < charRead; k++) {
                           savedData += inputBuffer[k];
                       }
                       return savedData;/*return saved data*/
   
                   } catch (Exception e) {
                       e.printStackTrace();
                       Log.e("Message", e.getLocalizedMessage());
                       return null;
                   }
               }
           }
   
       }
   ```

5. Deleting:

   ```
   private static Boolean deleteFile(String FileName) {
   
       /*Check if main directory is present or not*/
       if (!fileStorage.exists())
           return false;
       else {
           /*Now Check if text file is present or not*/
           File savedFile = new File(fileStorage.getAbsolutePath() + "/" + FileName);
           if (!savedFile.exists())
               return false;
           else {
               savedFile.delete();/*If text file is present then delete file*/
               return true;
           }
   
       }
   }
   ```
