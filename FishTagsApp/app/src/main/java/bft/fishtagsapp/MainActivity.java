package bft.fishtagsapp;

import android.content.Intent;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import bft.fishtagsapp.ParseFile.ParseFile;
import bft.fishtagsapp.Storage.Storage;

public class MainActivity extends AppCompatActivity {
    private Storage storage;
    private FileObserver observer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        storage = new Storage(getApplicationContext(),"FishTagsData");

        /* BLUETOOTH WATCHER */
        String DownloadDir_raw = "/sdcard/Download"; //WORKS
        final String DownloadDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(); //WORKS
        //String BluetoothDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/bluetooth"; DOESN'T WORK

        final Handler handler = new Handler();
        observer = new FileObserver(DownloadDir) {
            @Override
            //DETECTING BLUETOOTH TRANSFER
            public void onEvent(int event, final String fileName) {
                Log.i("EVENT", String.valueOf(event));
                if(event == CLOSE_WRITE){
                    //when transfer (write operation) is complete...
                    Log.i("fileName", fileName);
                    Log.i("EVENT", String.valueOf(event));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show(); //announce filename
                            File file = new File(DownloadDir,fileName); //read from file
                            HashMap<String, String> h =  ParseFile.getEntries(file); //parse file

                            if (!h.isEmpty()){ //not an empty file, parsed content
                                String timeStamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date().getTime());
                                h.put("name", timeStamp + ".txt");//txt file extension
                                Log.i("Parsed", h.toString());


                                storage.saveReport(h);//save to report file
                                // TODO: initiate saveReport under FormActivity
                            }

                        }
                    });
                }
            }
        };
        observer.startWatching();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    static final int SUBMIT_TAG = 1;

    /**
     *
     * When FormActivity is over, MainActivity routes the data received from it (dictionary of values and Uri s)
     * to Storage, telling it to start trying to upload the info to the database.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SUBMIT_TAG){
            if (resultCode == RESULT_OK){
                //TODO: Route to Storage
                //HashMap<String, String> map = (HashMap<String, String>)data.getSerializableExtra("map");
                Toast.makeText(getApplicationContext(), "Thank you for submitting a tag!", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void goToForm(View view){
        Intent intent = new Intent(this, FormActivity.class);
        intent.putExtra("fileName","fileName");
        startActivityForResult(intent, SUBMIT_TAG);
    }
}
