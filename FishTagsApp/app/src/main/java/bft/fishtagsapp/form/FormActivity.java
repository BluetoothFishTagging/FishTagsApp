package bft.fishtagsapp.form;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import bft.fishtagsapp.Constants;
import bft.fishtagsapp.MainActivity;
import bft.fishtagsapp.R;
import bft.fishtagsapp.Utils;
import bft.fishtagsapp.camera.CameraActivity;
import bft.fishtagsapp.gps.MyLocation;
import bft.fishtagsapp.parsefile.ParseFile;
import bft.fishtagsapp.signup.SignupActivity;

public class FormActivity extends AppCompatActivity {
    /* HANDLES FORM INPUT FROM USER */

    // predefining all of the EditTexts because there is one in each fragment.
    private ArrayList<EditText> editTexts;
    private Uri imageUri;
    private HashMap<String, String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_form);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar); // Important piece of code that otherwise will not show menus
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // shop back button at the top
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set the first fragment to be displayed in FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            TagIDFragment firstFragment = new TagIDFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }

        imageUri = Uri.parse("android.resource://bft.fishtagsapp/" + R.drawable.placeholder);
        data = new HashMap<>();
        String fileName = getIntent().getStringExtra("fileName");
        /* Auto-fill in data from the latest tag file */
        getGPS();
        getTime();
        fillInInfoFromFile(fileName);
    }

    /**
     * Switch fragment from Tag ID to Species
     *
     * @param v
     */
    public void submitID(View v) {
        // Add data to hashmap before switching to fragment
        EditText id = (EditText) findViewById(R.id.NationalID);
        data.put("Tag ID", id.getText().toString());
        switchTo(Constants.SPECIES);
    }

    /**
     * Switch fragment from Species to Camera
     *
     * @param v
     */

    public void submitSpecies(View v) {
        Spinner spinner = (Spinner) findViewById(R.id.Species);
        data.put("Species", spinner.getSelectedItem().toString());
        switchTo(Constants.PHOTO);
    }

    /**
     * Submit form
     *
     * @param v
     */
    public void submitForkLength(View v) {
        EditText id = (EditText) findViewById(R.id.ForkLength);
        data.put("Fork Length", id.getText().toString());
        submitForm();
    }

    private void switchTo(int fragment) {
        Fragment newFragment;

        switch (fragment) {
            case Constants.SPECIES:
                newFragment = new SpeciesFragment();
                //Highlight the fishy
                ImageView fishy = (ImageView) findViewById(R.id.fish_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fishy.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_fish_on, getApplicationContext().getTheme()));
                } else {
                    fishy.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_fish_on));
                }
                // Dehighlight the tag
                ImageView tag = (ImageView) findViewById(R.id.tag_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tag.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_tag_highlighted, getApplicationContext().getTheme()));
                } else {
                    tag.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_tag_highlighted));
                }
                //Update progressbar
                ProgressBar species = (ProgressBar)findViewById(R.id.id_species);
                species.setProgress(1);
                break;
            case Constants.FORK_LENGTH:
                newFragment = new ForkLengthFragment();
                // Dehighlight camera
                ImageView camera = (ImageView) findViewById(R.id.camera_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    camera.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_camera_highlighted, getApplicationContext().getTheme()));
                } else {
                    camera.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_camera_highlighted));
                }
                // Highlight the ruler
                ImageView ruler = (ImageView) findViewById(R.id.ruler_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ruler.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_ruler_on, getApplicationContext().getTheme()));
                } else {
                    ruler.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_ruler_on));
                }
                ProgressBar length = (ProgressBar)findViewById(R.id.id_length);
                length.setProgress(1);

                break;
            case Constants.PHOTO:
                // Dehilight the fishy
                ImageView fish = (ImageView) findViewById(R.id.fish_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fish.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_fish_highlighted, getApplicationContext().getTheme()));
                } else {
                    fish.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_fish_highlighted));
                }
                ImageView camera1 = (ImageView) findViewById(R.id.camera_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    camera1.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_camera_on, getApplicationContext().getTheme()));
                } else {
                    camera1.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_camera_on));
                }
                ProgressBar photo = (ProgressBar)findViewById(R.id.id_photo);
                photo.setProgress(1);
                goToCamera();
                return;
            default:
                return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    /* AUTOFILL SECTION */
    protected void fillInInfo(String fileName) {
        //fillInTime();
        //fillInGPS();

        /* Entries from Tag File */
        fillInInfoFromFile(fileName);
    }

    protected void getTime() {
        /*Get Time*/
        String timestamp = Utils.timestamp();
        data.put("Time", timestamp);
    }

    protected void getGPS() {
        /*Get Location*/
        Boolean permission = Utils.checkAndRequestRuntimePermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                },
                Constants.REQUEST_LOCATION
        );

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                String locString;
                if (location == null) {
                    locString = "N/A";
                } else {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    locString = String.format("LAT:%f, LONG:%f", latitude, longitude);
                }
                if (data == null) {
                    data = new HashMap<>();
                }
                data.put("Location", locString);
                Log.i("MAP", data.toString());
            }
        };
        MyLocation myLocation = new MyLocation(permission);
        myLocation.getLocation(this, locationResult, permission);
    }

    protected void fillInInfoFromFile(String fileName) {
        /* Parse the file only if the file exists. Even though one may not exist, however, still fill in time and date, etc. */
        if (fileName != null) {
            Log.i("FILENAME", fileName);
            fillInInfoFromFile(new File(fileName));
        } else {
            showDialog();
        }
    }

    void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Please use RFID reader to transfer file before submitting a tag form.")
                .setTitle(R.string.rfid_dialog);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked ok button
                Intent intent = new Intent(FormActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected void fillInInfoFromFile(File file) {
        /* Call Parse File to return all of the entries in the file.
            ParseFile handles all of the storage stuff so that FormActivity only fills in the UI
         */

        HashMap<String, String> entries = ParseFile.getEntries(file);
        if (entries == null) {
            return;
        }
        Log.i("ENTRIES", entries.toString());

        for (String key : entries.keySet()) {
            /* If key exists in textview, fill in corresponding text.
             It is assumed that all of IDs of the TextViews correspond
             to the keys of the entries. */
            try {
                int textID = getResources().getIdentifier(key,
                        "id", getPackageName());
                TextView text = (TextView) findViewById(textID);
                text.setText(entries.get(key));
            } catch (Exception e) {
                // Do error handling if the id is not found
            }
        }
    }


    /* SUBMISSION SECTION */

    protected JSONObject getFormMap() {
        /* Iterate through form fields */
        try {
            JSONObject dataObj = new JSONObject();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                dataObj.put(entry.getKey(), entry.getValue());
            }

            // imageUri is defined as the placeholder. If there was an image, should be overwritten in the result of the camera activity
            dataObj.put("photo", imageUri.toString());
            return dataObj;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void submitForm() {
        Log.i("FORM", "SUBMITTING");
        /* First verify the personal info */
        Intent intent_result = new Intent();
        intent_result.putExtra("map", getFormMap().toString());
        setResult(RESULT_OK, intent_result);
        finish();
    }

    /* CAMERA SECTION */
    public void goToCamera() {
        Log.i("Camera", "Dispatching intent");
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, Constants.REQUEST_TAKE_PHOTO);
    }

    /* CALLBACK SECTION */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_VERIFY_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Intent intent_result = new Intent();
                intent_result.putExtra("map", getFormMap().toString());
                setResult(RESULT_OK, intent_result);
                finish();
            }
        }
        if (requestCode == Constants.REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                imageUri = Uri.parse(data.getStringExtra("photo"));
                // Crashes right here necause there is no imageUri
                Log.i("REQUEST PHOTO", imageUri.toString());
                // Launch Fork Length fragment
                switchTo(Constants.FORK_LENGTH);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //fillInGPS();
                } else {
                    Log.i("PERMISSIONS", "NOT GRANTED");
                    //what to do here?
                }
                return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*If the user enables GPS while in FormActivity, when they return to the form, the GPS should attempt to update*/
        getGPS();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle action bar item clicks here. The action bar will
        automatically handle clicks on the Home/Up button, so long
        as you specify a parent activity in AndroidManifest.xml. */
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
