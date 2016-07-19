package bft.fishtagsapp.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import bft.fishtagsapp.Constants;
import bft.fishtagsapp.Utils;
import bft.fishtagsapp.storage.Storage;
import bft.fishtagsapp.wifi.WifiDetector;

/**
 * Created by jamiecho on 3/9/16.
 */
public class UploadService extends Service {

    private final WifiDetector networkChecker = new WifiDetector() {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if (isConnected()) {
                UploadService.this.unregisterReceiver(this);
                UploadService.this.uploadOne();
            }
            //otherwise continue to listen
        }
    };

    public class UploadBinder extends Binder {

        public UploadService getService() {
            return UploadService.this;
        }

        public void enqueue(String... params) {

            uploadQueue.add(params);
            //try to upload
            uploadOne();
        }

        public void alertSave() {
            savePending();
        }
    }

    private final IBinder mBinder = new UploadBinder();
    private LinkedList<String[]> uploadQueue = new LinkedList<>();
    private Boolean bound;

    String url;

    public UploadService() {

    }

    /* DEPRECATED */
    public void send(Uri uri, String param1, String param2) {

        // Prior to Transmit, set Files, params, etc.
        SendHttpRequestTask t = new SendHttpRequestTask();
        String[] params = new String[]{url, uri.toString(), param1, param2};
        t.execute(params);
    }


    public byte[] convertUriToByteArray(Uri uri) {
        byte[] byteArray = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 8];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    private class SendHttpRequestTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String url = params[0];
            Uri uri = Uri.parse(params[1]);
            String tagInfo = params[2];
            String personInfo = params[3];
            try {
                byte[] byteArray = convertUriToByteArray(uri); // = Image Bytearray

                HttpClient client = new HttpClient(url);
                client.connectMultipart();
                client.addFormPart("tagInfo", tagInfo);
                client.addFormPart("personInfo", personInfo); //form (plain text, JSON, etc) data.

                String timeStamp = Utils.timestamp();
                String photoName = timeStamp + ".jpg";

                client.addFilePart("photo", photoName, byteArray);
                client.finishMultipart();
                String response = client.getResponse();

                if (response != null) {
                    //TODO : check if valid response
                    return true;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false; //debug : for now
            //return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            //Log.i("RESPONSE", s);
            super.onPostExecute(success);
            if (success) {
                //remove from queue
                uploadQueue.remove();
                //handle next one up in queue
                uploadOne();
            } else {
                //failed
                if (networkChecker.isConnected()) {
                    //try again
                    uploadOne();
                } else {
                    //disconnected - wait for Wifi connection
                    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                    registerReceiver(networkChecker, filter);
                }
            }
        }

    }

    @Override
    public void onCreate() {
        Storage.register(this, Constants.APP_DIRECTORY);
        /* JUST IN CASE ... */
        /* IN FACT, THE PERMISSION IS NOT NECESSARY ANYMORE */

        /*PermissionEverywhere.getPermission(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                Constants.REQUEST_STORAGE,
                "FishTags - Upload Service",
                "This app needs a read permission",
                R.mipmap.ic_launcher)
                .enqueue(new PermissionResultCallback() {
                    @Override
                    public void onComplete(PermissionResponse permissionResponse) {
                        Toast.makeText(UploadService.this, "is Granted " + permissionResponse.isGranted(), Toast.LENGTH_SHORT).show();
                    }
                });
        */

    }
    //SERVICE-RELATED IMPLEMENTATION

    @Override
    public void onDestroy() {
        Log.i("UPLOADSERVICE", "DESTROYED");
        savePending();
    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //just in case
        Storage.register(this, Constants.APP_DIRECTORY);

        Log.i("UPLOADSERVICE", "STARTING");
        //load pending data -- i.e. those that didn't quite get delivered
        String pendingString = Storage.read("pending.txt");

        if (pendingString != null && !pendingString.isEmpty()) {
            try {
                JSONObject pending = new JSONObject(Storage.read("pending.txt"));
                int count = pending.getInt("count");
                Log.i("UPLOADSERVICE-COUNT", String.valueOf(count));

                for (int i = 0; i < count; ++i) {
                    //numbering as identifiers
                    JSONObject one = pending.getJSONObject(String.valueOf(i));

                    String url = one.getString("url");
                    String uri = one.getString("uri");
                    String tagInfo = one.getString("tagInfo");
                    String personInfo = one.getString("personInfo");
                    String params[] = {url, uri, tagInfo, personInfo};

                    uploadQueue.add(params);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //try to upload if pending was leftover
        if (uploadQueue.size() > 0) {
            uploadOne();
        }
        //load from pending...?
        return START_STICKY;
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("UPLOADSERVICE", "BINDING");
        bound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("UPLOADSERVICE", "UNBINDING");
        bound = false;
        return super.onUnbind(intent);
    }


    private void uploadOne() {
        Log.i("UPLOADSERVICE", "UPLOADING");
        String[] params = uploadQueue.peek();
        if (params == null) {
            if (bound == false) {
                //not bound && nothing pending
                stopSelf();
            }
        } else {
            //has pending upload
            new SendHttpRequestTask().execute(params);
        }
    }

    public void savePending() {
        try {
            //try to save queue to pending files
            JSONObject pending = new JSONObject();
            int count = uploadQueue.size();
            Log.i("UPLOADSERVICE-COUNT", String.valueOf(count));

            pending.put("count", count);
            int i = 0;
            for (String[] params : uploadQueue) {
                JSONObject one = new JSONObject();

                one.put("url", params[0]);
                one.put("uri", params[1]);
                one.put("tagInfo", params[2]);
                one.put("personInfo", params[3]);

                pending.put(String.valueOf(i), one); //put JSON object
                ++i;
            }
            Storage.save("pending.txt", pending.toString());

        } catch (JSONException e) {
            //well... have to give up here
            e.printStackTrace();
        }
    }
}
