package bft.fishtagsapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
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

import bft.fishtagsapp.client.UploadService;
import bft.fishtagsapp.signup.SignupActivity;
import bft.fishtagsapp.storage.Storage;

public class MainActivity extends AppCompatActivity {
    private FileObserver observer;
    private String recent; //recent file
    private UploadService uploadService;
    private UploadService.UploadBinder uploadBinder;
    private Boolean uploadServiceBound;

    private ServiceConnection uploadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uploadBinder = (UploadService.UploadBinder) service;
            uploadService = uploadBinder.getService();
            uploadServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            uploadServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToForm(view);
            }
        });

        /* BLUETOOTH WATCHER */
        //final String DownloadDir_raw = Environment.getExternalStorageDirectory().getPath() + Constants.DEFAULT_STORE_SUBDIR; //WORKS
        final String DownloadDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//Works
        final String DEFAULT_STORE_SUBDIR = "/FishTagsData";//Check if this works
        //String BluetoothDir = getExternalFilesDir(Environment.DIRECTORY_).getPath() + "/bluetooth"; DOESN'T WORK

        final Handler handler = new Handler();
        observer = new FileObserver(DownloadDir) {
        //observer = new FileObserver(DownloadDir) {
        //observer = new FileObserver(DownloadDir) {
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
							//
                            recent = DownloadDir + '/' + fileName;

                            //TODO : check is valid tag file
                            //goToForm(recent);
                        }
                    });
                }
            }
        };
        observer.startWatching();

        Intent uploadIntent = new Intent(this,UploadService.class);

        bindService(uploadIntent,uploadConnection,BIND_AUTO_CREATE); // no flags
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(uploadServiceBound){
            unbindService(uploadConnection);
        }
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

        if (requestCode == Constants.SUBMIT_TAG) {
            if (resultCode == RESULT_OK) {
                JSONObject dataObj = null;

                try {
                    dataObj = new JSONObject(data.getStringExtra("map"));
                    String timeStamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date().getTime());
                    String fileName = timeStamp + ".txt";
                    dataObj.put("name", fileName);//txt file extension
                    //server url placeholder
                    String url = Constants.DATABASE_URL;
                    String uri = (String) dataObj.get("photo");
                    String tagInfo = dataObj.toString(); //JSON string
                    String personInfo = Storage.read(Constants.PERSONAL_INFO);

                    uploadBinder.enqueue(url,uri,tagInfo,personInfo);

                    Toast.makeText(getApplicationContext(), "Thank you for submitting a tag!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Deprecated by UploadService
//    public void submitReports() {
//        //check reports pending upload...
//        //if(pending != null) ...
//
//        /*Indicate main activity that wifi has been connected*/
//
//        String fileName = Storage.read("pending.txt");
//        //TODO : protect against multiple pending files
//        String fileContent = Storage.read(fileName); //JSON String
//        if(fileContent != null){
//            try{
//                JSONObject content = new JSONObject(fileContent);
//                submitReport(content);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    public void submitReport(JSONObject tagInfo) {
//        Log.i("SUBMITTING", "REPORT");
//        try {
//            String personInfo = Storage.read(Constants.PERSONAL_INFO);
//            Uri imageuri = Uri.parse((String) tagInfo.get("photo"));
//
//            Log.i("PERSONINFO",personInfo);
//            Log.i("TAGINFO",tagInfo.toString());
//            Log.i("IMAGEURI",imageuri.toString());
//
//            TagUploader.startActionUpload(this,imageuri.toString(),personInfo, tagInfo.toString());
//            //uploadService.send(imageuri, tagInfo.toString(), "PARAM2");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        //TODO : pass other information
//    }

    public void goToForm(View v) {
        goToForm(recent);
    }

    public void goToForm(String fileName) {
        Intent intent = new Intent(this, FormActivity.class);
        intent.putExtra("fileName", fileName);
        startActivityForResult(intent, Constants.SUBMIT_TAG);
    }

    public void signUp(View v){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}
