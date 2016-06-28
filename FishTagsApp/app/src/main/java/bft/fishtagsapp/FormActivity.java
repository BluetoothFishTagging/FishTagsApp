package bft.fishtagsapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import bft.fishtagsapp.camera.CameraActivity;
import bft.fishtagsapp.gps.MyLocation;
import bft.fishtagsapp.parsefile.ParseFile;
import bft.fishtagsapp.signup.SignupActivity;

public class FormActivity extends AppCompatActivity {
    /* HANDLES FORM INPUT FROM USER */

    private ArrayList<EditText> editTexts;
    private ImageView fishPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        LinearLayout my_linView = (LinearLayout) findViewById(R.id.tag_submission_form);

        editTexts = new ArrayList<>();
        findFields(my_linView, editTexts);
        fishPhotoView = (ImageView) findViewById(R.id.FishPhoto);

        String fileName = getIntent().getStringExtra("fileName");

        /* Auto-fill in data from the latest tag file */
        fillInInfo(fileName);
    }


    /* AUTOFILL SECTION */
    protected void fillInInfo(String fileName) {
        fillInTime();
        fillInGPS();

        /* Entries from Tag File */
        fillInInfoFromFile(fileName);
    }

    protected void fillInTime() {
        /*Get Time*/
        String timestamp = Utils.timestamp();
        TextView timeText = (TextView) findViewById(R.id.Time);
        timeText.setText(timestamp);
    }

    protected void fillInGPS() {
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

                TextView locationText = (TextView) findViewById(R.id.Location);
                locationText.setText(locString);
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
        }else{
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

    private void findFields(ViewGroup v, ArrayList<EditText> editTexts) {
        /* Recursively Find EditTexts in Viewgroups*/
        int n = v.getChildCount();
        for (int i = 0; i < n; ++i) {
            View subView = v.getChildAt(i);
            if (subView instanceof ViewGroup) {
                //recursively search for editTexts
                findFields((ViewGroup) subView, editTexts);
            } else if (subView instanceof EditText) {
                editTexts.add((EditText) subView);
            }
        }
    }

    protected JSONObject getFormMap() {
        /* Iterate through form fields */
        try {
            JSONObject data = new JSONObject();

            for (EditText e : editTexts) {
                String textId = e.getResources().getResourceEntryName(e.getId());
                String value = ((EditText) e).getText().toString();
                Log.i(textId, value);
                data.put(textId, value);

            }

            if (fishPhotoView.getTag() == null) {
                Uri imageUri = Uri.parse("android.resource://bft.fishtagsapp/" + R.drawable.placeholder);
                data.put("photo", imageUri.toString()); //empty string

            } else {
                Uri imageUri = (Uri) fishPhotoView.getTag();
                data.put("photo", imageUri.toString());
            }

            return data;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void submitForm(View view) {
        Log.i("FORM", "SUBMITTING");
        /* First verify the personal info */
        Intent intent_verify = new Intent(this, SignupActivity.class);
        intent_verify.putExtra("request", Constants.REQUEST_VERIFY_SETTINGS);
        startActivityForResult(intent_verify, Constants.REQUEST_VERIFY_SETTINGS);
    }

    /* CAMERA SECTION */
    public void goToCamera(){
        Log.i("Camera", "Dispatching intent");
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent,Constants.REQUEST_TAKE_PHOTO);
    }

    public void goToCamera(View view) {
        goToCamera();
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

                Uri imageUri = Uri.parse(data.getStringExtra("photo"));
                // Crashes right here necause there is no imageUri
                Log.i("REQUEST PHOTO", imageUri.toString());
                try {
                    Bitmap bitmap = getThumbnail(imageUri);
                    ImageView myImageView = (ImageView) findViewById(R.id.FishPhoto);
                    myImageView.setImageBitmap(bitmap);
                    myImageView.setTag(imageUri);
                    myImageView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    fillInGPS();
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
        fillInGPS();
    }

    /* THUMBNAIL CREATION SECTION */
    final int THUMBNAIL_SIZE = 256;

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            Log.i("GAH", "WHY");
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        Log.i("HELLO", "BOO");
        return bitmap;
    }
}
