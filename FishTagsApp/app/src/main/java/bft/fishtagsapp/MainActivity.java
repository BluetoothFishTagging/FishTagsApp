package bft.fishtagsapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bft.fishtagsapp.client.UploadService;
import bft.fishtagsapp.form.FormActivity;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar); // Important piece of code that otherwise will not show menus

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        /* OBTAIN EXTERAL STORAGE READ-WRITE PERMISSIONS */
        Boolean storagePermitted = Utils.checkAndRequestRuntimePermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, Constants.REQUEST_STORAGE);

        Log.i("STORAGE PERMS", storagePermitted.toString());

        if (storagePermitted) {
            if (Storage.read(Constants.BLUETOOTH_DIR) == null) {
                searchBluetooth();
            }
            addWatcher();
        }

        /* OBTAIN NETWORK-ACCESS RELATED PERMISSIONS */
        Boolean networkPermitted = Utils.checkAndRequestRuntimePermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.INTERNET,
                }, Constants.REQUEST_NETWORK);

        Log.i("NETWORK PERMS", networkPermitted.toString());

        if (networkPermitted) {
//            new HttpGetTask().execute(Constants.DATABASE_URL + "query?name=jamie");
        }

        Boolean bluetoothPermitted = Utils.checkAndRequestRuntimePermissions(this,
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_PRIVILEGED,
                        Manifest.permission.BLUETOOTH_ADMIN,
                }, Constants.REQUEST_NETWORK);

        Log.i("BLUETOOTH PERMS", bluetoothPermitted.toString());

        //showDialog();

        /* BUTTON TO GO TO FORM FOR SUBMITTING TAG DATA */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToForm(view);
            }
        });

        Utils.setName(findViewById(R.id.main_view));

        Intent uploadIntent = new Intent(this, UploadService.class);

        startService(uploadIntent);
        bindService(uploadIntent, uploadConnection, BIND_AUTO_CREATE); // no flags
    }

    /* SET UP FRAGMENTS AS SLIDING TABS */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), "HOME");
        adapter.addFragment(new MyCatchesFragment(), "My Catches");
        adapter.addFragment(new ScoreboardFragment(), "Scoreboard");
        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("MAINACVITIY", "DESTROYED");
        // --> perhaps handle "save pending" stuff here...
        super.onDestroy();

        if (uploadServiceBound) {
            uploadBinder.alertSave(); //save any pending stuff
            unbindService(uploadConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present.*/
        getMenuInflater().inflate(R.menu.fish_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle action bar item clicks here. The action bar will
        automatically handle clicks on the Home/Up button, so long
        as you specify a parent activity in AndroidManifest.xml. */
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SignupActivity.class);
                intent.putExtra("request", Constants.REQUEST_EDIT_SETTINGS);
                startActivityForResult(intent, Constants.REQUEST_SIGNUP);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
                if (Storage.read(Constants.BLUETOOTH_DIR) == null) {
                    searchBluetooth();
                }
                addWatcher();
                break;
            case Constants.REQUEST_NETWORK:
                //new HttpGetTask().execute(Constants.DATABASE_URL + "query?name=" + getName());
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*If the user changes their name, the app should update to reflect this*/
        Utils.setName(findViewById(R.id.main_view));
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

    /* BLUETOOTH SET UP */
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

        Log.i("BTDIR", folder);
        Storage.save(Constants.BLUETOOTH_DIR, folder);
    }


    public void addWatcher() {
        /* BLUETOOTH WATCHER FOR FILE DIRECTORY*/
        //final String DownloadDir_raw = Environment.getExternalStorageDirectory().getPath() + Constants.DEFAULT_STORE_SUBDIR; //WORKS
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
}
