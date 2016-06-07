package bft.fishtagsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import bft.fishtagsapp.Client.Uploader;
import bft.fishtagsapp.Storage.Storage;
import bft.fishtagsapp.Wifi.WifiDetector;

public class MainActivity extends AppCompatActivity {
    private FileObserver observer;
    private Uploader uploader;
    private String recent; //recent file

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /* BLUETOOTH WATCHER */
        String DownloadDir_raw = "/sdcard/Download"; //WORKS
        final String DownloadDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(); //WORK
        //String BluetoothDir = getExternalFilesDir(Environment.DIRECTORY_).getPath() + "/bluetooth"; DOESN'T WORK

        final Handler handler = new Handler();
        observer = new FileObserver(DownloadDir) {
            @Override
            /*DETECTING BLUETOOTH TRANSFER*/
            public void onEvent(int event, final String fileName) {
                Log.i("EVENT", String.valueOf(event));
                if (event == CLOSE_WRITE) {
                    /*when transfer (write operation) is complete...*/
                    Log.i("fileName", fileName);
                    Log.i("EVENT", String.valueOf(event));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show(); //announce filename

                            //if(new File(fileName).length() > 0) // has content
                            //goToForm(DownloadDir + '/' + fileName);
                            recent = DownloadDir + '/' + fileName;
                            //TODO : create fallback when there is no tag
                            //TODO : check is valid tag file
                            //goToForm(recent);
                        }
                    });
                }
            }
        };
        observer.startWatching();
        WifiDetector.register(this);
        Storage.register(this,"FishTagsData");
        uploader = new Uploader(this,"http://192.168.16.73:8000/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present.*/
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle action bar item clicks here. The action bar will
        automatically handle clicks on the Home/Up button, so long
        as you specify a parent activity in AndroidManifest.xml. */
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    static final int SUBMIT_TAG = 1;

    /**
     * When FormActivity is over, MainActivity routes the data received from it (dictionary of values and Uri s)
     * to Storage, telling it to start trying to upload the info to the database.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.i("resultCode", String.valueOf(resultCode));
        Log.i("requestCode", String.valueOf(requestCode));

        if (requestCode == SUBMIT_TAG) {
            if (resultCode == RESULT_OK) {
                //TODO: Route to Storage
                HashMap<String, String> map = (HashMap<String, String>) data.getSerializableExtra("map");

                String timeStamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date().getTime());
                String fileName = timeStamp + ".txt";
                map.put("name", fileName);//txt file extension

                if (WifiDetector.isConnected()) {
                    Log.i("WIFI", "CONNECTED");
                    submitReport(new JSONObject(map));
                    //submit directly
                } else {
                    Log.i("WIFI", "NOT CONNECTED");
                    storage.saveReport(map);
                    storage.save("pending.txt", fileName);
                }

                Toast.makeText(getApplicationContext(), "Thank you for submitting a tag!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void submitReports() {
        //check reports pending upload...
        //if(pending != null) ...

        /*Indicate main activity that wifi has been connected*/

        String fileName = Storage.read("pending.txt");
        //TODO : protect against multiple pending files
        String fileContent = Storage.read(fileName); //JSON String
        if(fileContent != null){
            try{
                JSONObject content = new JSONObject(fileContent);
                submitReport(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void submitReport(JSONObject content) {
        Log.i("SUBMITTING", "REPORT");
        try {

            Uri imageuri = Uri.parse((String) content.get("photo"));
            uploader.send(imageuri, content.toString(), "PARAM2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //TODO : pass other information
    }

    public void goToForm(View v) {
        goToForm(recent);
    }

    public void goToForm(String fileName) {
        Intent intent = new Intent(this, FormActivity.class);
        intent.putExtra("fileName", fileName);
        startActivityForResult(intent, SUBMIT_TAG);
    }
}
