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
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import bft.fishtagsapp.Camera.Camera;
import bft.fishtagsapp.GPS.GPS;
import bft.fishtagsapp.ParseFile.ParseFile;

public class FormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Auto-fill in data from the latest tag file
        fillInInfoFromFile();


    }
    protected void fillInInfoFromFile(){
        /* Call Parse File to return all of the entries in the file.
            ParseFile handles all of the storage stuff so that FormActivity only fills in the UI
         */
        HashMap<String, String> entries = ParseFile.getEntries();
        for(String key : entries.keySet()){
            // If key exists in textview, fill in corresponding text
            try {
                int textID = getResources().getIdentifier(key,
                        "id", getPackageName());
                TextView text = (TextView)findViewById(textID);
                text.setText(entries.get(key));
            }
            catch (Exception e){
                // Do error handling if the id is not found
            }
        }

        //Get Time
        String time = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
        TextView timeText = (TextView) findViewById(R.id.time);
        timeText.setText(time);

        //Get Location
        GPS gps = new GPS(this);
        Location location = gps.getGPS();
        if(location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String s = String.format("LAT:%f,LONG:%f", latitude, longitude);

            TextView locationText = (TextView) findViewById(R.id.location);
            locationText.setText(s);
        }
    }

    //TODO: Create function that collects all of the information from the boxes into a Hashmap to be able to pass it on
    protected HashMap<String, String> getFormMap(){
        GridView my_gridView = (GridView)findViewById(R.id.my_grid_view);
        for (int i = 0; i < my_gridView.getChildCount(); i++){
            View v = my_gridView.getChildAt(i);
            // DO SOMETHING
        }
        return null;
    }

    //TODO: pass along hashmap of Values
    public void submitForm(View view){
        Intent intent_result = new Intent();
        //intent_result.putExtra("map", getFormMap());
        setResult(RESULT_OK, intent_result);
        finish();
    }

    public void goToCamera(View view) {
//        Intent intent = new Intent(this, Camera.class);
//        startActivity(intent);
        dispatchTakePictureIntent();
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    Uri photoUri;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                photoUri = Uri.fromFile(photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            } catch (IOException ex) {

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            try{
                Bitmap bitmap = getThumbnail(imageUri);
                ImageView myImageView = (ImageView)findViewById(R.id.FishPhoto);
                myImageView.setImageBitmap(bitmap);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    final int THUMBNAIL_SIZE = 256;
    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
