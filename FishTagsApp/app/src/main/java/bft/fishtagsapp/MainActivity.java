package bft.fishtagsapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import bft.fishtagsapp.client.HttpClient;
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


    private class HttpGetTask extends AsyncTask<String, Void, Boolean> {
        String response;
        @Override
        protected Boolean doInBackground(String... params) {
            String url = params[0];

            try {
                HttpClient client = new HttpClient(url);
                client.connect();
                response = client.getResponse();
                Log.i("RSP",response);
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean success) {
            //Log.i("SUCCESS", success);
            super.onPostExecute(success);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* OBTAIN EXTERAL STORAGE READ-WRITE PERMISSIONS */
        Boolean storagePermitted = Utils.checkAndRequestRuntimePermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, Constants.REQUEST_STORAGE);

        Log.i("STORAGE PERMS", storagePermitted.toString());

        Boolean networkPermitted = Utils.checkAndRequestRuntimePermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.INTERNET,
                }, Constants.REQUEST_NETWORK);

        Log.i("NETWORK PERMS", networkPermitted.toString());

        if(networkPermitted){
            new HttpGetTask().execute(Constants.DATABASE_URL + "query?name=Katya");
        }
        /* BUTTON TO GO TO FORM FOR SUBMITTING TAG DATA */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToForm(view);
            }
        });

        /* Create welcome message for returning user */
        String name = getName();
        if (name != "") {
            TextView welcome = (TextView) findViewById(R.id.welcome);
            welcome.setText(String.format("Welcome, %s!", name));
        }
        /* BLUETOOTH WATCHER FOR FILE DIRECTORY*/
        //final String DownloadDir_raw = Environment.getExternalStorageDirectory().getPath() + Constants.DEFAULT_STORE_SUBDIR; //WORKS
        final String DownloadDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//Works
        final String DEFAULT_STORE_SUBDIR = "/bft.fishtagsapp";//Check if this works
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

                            //remember recent file
                            //currently, automatically going to form doesn't work
                            recent = DownloadDir + '/' + fileName;

                            //TODO : check is valid tag file
                            //goToForm(recent);
                        }
                    });
                }
            }
        };

        observer.startWatching();

        Intent uploadIntent = new Intent(this, UploadService.class);
        bindService(uploadIntent, uploadConnection, BIND_AUTO_CREATE); // no flags
    }


    public void open(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Please ensure your rfid reader is turned on.");

        alertDialogBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(MainActivity.this, "You have rfid reader ready", Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uploadServiceBound) {
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
                    String timeStamp = Utils.timestamp();
                    String fileName = timeStamp + ".txt";
                    dataObj.put("name", fileName);//txt file extension
                    //server url placeholder
                    String url = Constants.DATABASE_URL;
                    String uri = (String) dataObj.get("photo");

                    String tagInfo = dataObj.toString(); //JSON string
                    String personInfo = Storage.read(Constants.PERSONAL_INFO);

                    uploadBinder.enqueue(url, uri, tagInfo, personInfo);

                    Toast.makeText(getApplicationContext(), "Thank you for submitting a tag!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Boolean granted = (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        //TODO : check for each permission

        switch (requestCode) {
            case Constants.REQUEST_STORAGE:
                Log.i("STORAGE PERMISSIONS", granted.toString());
                break;
            case Constants.REQUEST_NETWORK:
                new HttpGetTask().execute(Constants.DATABASE_URL + "query?name=Momo");
                break;
        }

    }

    public void goToForm(View v) {
        goToForm(recent);
    }

    public void goToForm(String fileName) {
        Intent intent = new Intent(this, FormActivity.class);
        intent.putExtra("fileName", fileName);
        startActivityForResult(intent, Constants.SUBMIT_TAG);
    }

    public void signUp(View v) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    public String getName() {
        Log.i("INFO", "YO");

        String info = Storage.read("info.txt");
        if (info != null && !info.isEmpty()) {
            try {
                JSONObject data = new JSONObject(info);
                    String value = data.getString("name");
                    return value;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}