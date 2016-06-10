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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import bft.fishtagsapp.GPS.GPS;
import bft.fishtagsapp.ParseFile.ParseFile;

public class FormActivity extends AppCompatActivity {

    private ArrayList<EditText> editTexts;
    private ImageView fishPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        RelativeLayout my_relView = (RelativeLayout) findViewById(R.id.my_rel_view);

        editTexts = new ArrayList<EditText>();
        findFields(my_relView,editTexts);
        fishPhotoView = (ImageView) findViewById(R.id.FishPhoto);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        String fileName = getIntent().getStringExtra("fileName");
        /* Auto-fill in data from the latest tag file */
        fillInInfoFromFile(fileName);

    }

    protected void fillInInfoFromFile(String fileName) {
        if (fileName != null) {
            Log.i("FILENAME", fileName);
            fillInInfoFromFile(new File(fileName));
        }
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

        /*Get Time*/
        String time = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
        TextView timeText = (TextView) findViewById(R.id.Time);
        timeText.setText(time);

        /*Get Location*/
        GPS gps = new GPS(this);
        Location location = gps.getGPS();
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String s = String.format("LAT:%f,LONG:%f", latitude, longitude);

            TextView locationText = (TextView) findViewById(R.id.Location);
            locationText.setText(s);
        }
    }
    private void findFields(ViewGroup v, ArrayList<EditText> editTexts){
        /* Find EditTexts */
        int n = v.getChildCount();
        for(int i=0; i<n; ++i){
            View subView = v.getChildAt(i);
            if(subView instanceof ViewGroup){
                //recursively search for editTexts
                findFields((ViewGroup)subView, editTexts);
            }else if (subView instanceof EditText){
                editTexts.add((EditText)subView);
            }
        }
    }

    //TODO: Create function that collects all of the information from the boxes into a Hashmap to be able to pass it on
    protected HashMap<String, String> getFormMap() {
        HashMap<String, String> map = new HashMap<>();

        RelativeLayout my_relView = (RelativeLayout) findViewById(R.id.my_rel_view);
        for(EditText e : editTexts){
            String textId = e.getResources().getResourceEntryName(e.getId());
            String value = ((EditText) e).getText().toString();
            map.put(textId, value);
        }
        Uri imageUri = (Uri) fishPhotoView.getTag();
        if(imageUri != null){
            map.put("photo", imageUri.toString());
        }else{
            //fallback photo - placeholder
            Uri uri = Uri.parse("android.resource://bft.fishtagsapp/drawable/placeholder.png");
            map.put("photo", uri.toString());
        }
        return map;
    }

    //TODO: pass along hashmap of Values
    public void submitForm(View view) {
        Log.i("FORM", "SUBMITTING");
        Intent intent_result = new Intent();
        intent_result.putExtra("map", getFormMap());
        setResult(RESULT_OK, intent_result);
        finish();
    }

    public void goToCamera(View view) {
//        Intent intent = new Intent(this, Camera.class);
//        startActivity(intent);
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
        super.onActivityResult(requestCode, resultCode, data);
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
