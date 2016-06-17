package bft.fishtagsapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import bft.fishtagsapp.gps.GPS;
import bft.fishtagsapp.parsefile.ParseFile;
import bft.fishtagsapp.signup.SignupActivity;

public class FormActivity extends AppCompatActivity {

    private ArrayList<EditText> editTexts;
    private ImageView fishPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);


        LinearLayout my_relView = (LinearLayout) findViewById(R.id.tag_submission_form);

        editTexts = new ArrayList<EditText>();
        findFields(my_relView, editTexts);
        fishPhotoView = (ImageView) findViewById(R.id.FishPhoto);

        String fileName = getIntent().getStringExtra("fileName");
        /* Auto-fill in data from the latest tag file */
        fillInInfoFromFile(fileName);
    }

    protected void fillInInfoFromFile(String fileName) {
        /* Parse the file only if the file exists. Even though one may not exist, however, still fill in time and date, etc. */
        if (fileName != null) {
            Log.i("FILENAME", fileName);
            fillInInfoFromFile(new File(fileName));
        }
        /*Get Time*/
        String time = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
        Log.i("Time", time);
        TextView timeText = (TextView) findViewById(R.id.Time);
        timeText.setText(time);

//        /*Get Location*/
//        GPS gps = new GPS(this);
//        Location location = gps.getGPS();
//        String locString;
//        if(location==null){
//            locString = "N/A";
//        }else{
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//            locString = String.format("LAT:%f,LONG:%f", latitude, longitude);
//        }
//
//        TextView locationText = (TextView) findViewById(R.id.Location);
//        locationText.setText(locString);
//        Toast.makeText(getApplicationContext(), locString, Toast.LENGTH_LONG).show();
//        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_LONG).show();
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

    private void findFields(ViewGroup v, ArrayList<EditText> editTexts) {
        /* Find EditTexts */
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
        try {
            JSONObject data = new JSONObject();

            for (EditText e : editTexts) {
                String textId = e.getResources().getResourceEntryName(e.getId());
                String value = ((EditText) e).getText().toString();
                Log.i(textId, value);
                data.put(textId, value);

            }

            ImageView p = (ImageView) findViewById(R.id.FishPhoto);
            if (p.getTag() == null) {
//                Uri uri = Uri.parse("android.resource://bft.fishtagsapp/drawable/placeholder.png");
                data.put("photo", null);
            } else {
                Uri imageUri = (Uri) p.getTag();
                data.put("photo", imageUri.toString());
            }
            //for debugging
            Toast.makeText(getApplicationContext(), data.toString(), Toast.LENGTH_LONG).show();
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

    public void goToCamera(View view) {
        Log.i("Camera", "Dispatching intent");
        dispatchTakePictureIntent();
    }

    Uri photoUri;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        /* Ensure that there's a camera activity to handle the intent */
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            /* Create the File where the photo should go */
            File photoFile = null;
            try {
                photoFile = createFile(Environment.getExternalStorageDirectory(), "JPEG", ".jpg");
                photoUri = Uri.fromFile(photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, Constants.REQUEST_TAKE_PHOTO);
            } catch (IOException ex) {

            }
        }
    }

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
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = getThumbnail(imageUri);
                    ImageView myImageView = (ImageView) findViewById(R.id.FishPhoto);
                    myImageView.setImageBitmap(bitmap);
                    myImageView.setTag(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final int THUMBNAIL_SIZE = 256;

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    private File createFile(File storageDir, String extension, String dotExtension) throws IOException {
        /* Create a unique file name */
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = extension + "_" + timeStamp + "_";

        File file = new File(storageDir + "/" + fileName + dotExtension);
        file.createNewFile();

        return file;
    }
}
