---
title: Transfer
layout: template
filename: transfer 
---

## Detecting Bluetooth File Transfer

In order to automate the reporting process, it was critical to detect file transfer from the RFID readers.

To this end, we attempted a myriad of different methods:

### Extending BroadcastReceiver

We have tried different intents, but we weren't able to filter the intents even though they were broadcasted according to the android logs.

It is possible that the intents were not accessible by external application.

Here is a list of intents we have attempted:

- android.intent.action.SEND
- android.intent.action.DOWNLOAD_COMPLETE
- android.btopp.intent.action.BT_OPP_TRANSFER_DONE
- android.btopp.intent.action.TRANSFER_COMPLETE
- DownloadManager.ACTION_DOWNLOAD_COMPLETE
- BluetoothDevice.ACTION_ACL_DISCONNECTED

None of the filters were triggered except BluetoothDevice.ACTION_ACL_DISCONNECTED, but that doesn't give us the path to the received file, which is what we need.

### Implementing our own Bluetooth Agent

Unfortunately, bluetooth file transfer seems to be handled by the android OS.

### Install a FileObserver

Our last resort was to install a FileObserver in the bluetooth download directory.

[Android File Observer](https://developer.android.com/reference/android/os/FileObserver.html) observes a path and handles each events that are detected by the OS.

#### Searching for the directory	

The challenge here was determining where the bluetooth folder resides, as it differs from vendor to vendor, phone to phone.

Our solution was to recursively search for the folder named bluetooth, and if none found, default to DIRECTORY_DOWNLOAD.

This search operation was executed only once, when the app is booted first time after installation, by storing the bluetooth folder path.

```java
public List<File> folderSearchBT(File src, String folder)
        throws FileNotFoundException {

    List<File> result = new ArrayList<File>();

    File[] filesAndDirs = src.listFiles();
    List<File> filesDirs = Arrays.asList(filesAndDirs);

    for (File file : filesDirs) {
        result.add(file); // always add, even if directory
        if (!file.isFile()) {
            List<File> deeperList = folderSearchBT(file, folder);
            result.addAll(deeperList);
        }
    }

    return result;
}

public String searchForBluetoothFolder() {

    String splitchar = "/";
    File root = Environment.getExternalStorageDirectory();
    List<File> btFolder = null;
    String bt = "bluetooth";

    try {
        btFolder = folderSearchBT(root, bt);
    } catch (FileNotFoundException e) {
        Log.e("FILE: ", e.getMessage());
    }

    for (int i = 0; i < btFolder.size(); i++) {

        String g = btFolder.get(i).toString();

        String[] subf = g.split(splitchar);

        String s = subf[subf.length - 1].toUpperCase();

        boolean equals = s.equalsIgnoreCase(bt);

        if (equals)
            return g;
    }
    return null; // not found
}

public void searchBluetooth() {
    String folder = searchForBluetoothFolder();

    if (folder == null) {
        //fallback to downloads
        folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    Storage.save(Constants.BLUETOOTH_DIR, folder);
}
```

#### Installing the File Observer

```java
public void addWatcher() {
    /* BLUETOOTH WATCHER FOR FILE DIRECTORY*/
    final String bluetoothDir = Storage.read(Constants.BLUETOOTH_DIR);
    Log.i("WATCHING", bluetoothDir);

    final Handler handler = new Handler();
    observer = new FileObserver(bluetoothDir) {
        @Override
        /*DETECTING BLUETOOTH TRANSFER*/
        public void onEvent(int event, final String fileName) {
            Log.i("EVENT", String.valueOf(event));
            if (event == CLOSE_WRITE) {
                /*when transfer (write operation) is complete...*/
                Log.i("fileName", fileName);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //remember recent file
                        //currently, automatically going to form doesn't work
                        recent = bluetoothDir + '/' + fileName;

                        //TODO : check is valid tag file
                        //goToForm(recent);
                    }
                });
            }
        }
    };

    observer.startWatching();
}
```
