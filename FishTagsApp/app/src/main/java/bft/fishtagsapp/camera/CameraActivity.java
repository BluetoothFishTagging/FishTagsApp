package bft.fishtagsapp.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import bft.fishtagsapp.Constants;
import bft.fishtagsapp.R;
import bft.fishtagsapp.Utils;

public class CameraActivity extends Activity {

    Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Boolean permitted = Utils.checkAndRequestRuntimePermissions(
                this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                Constants.REQUEST_CAMERA
        );

        if (permitted) {
            takePicture();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Log.i("PERMISSIONS", "NOT GRANTED");
                    //give up on photo
                }
                break;
        }
    }

    private File createFile(File storageDir, String extension, String dotExtension) throws IOException {
        /* Create a unique file name */
        String timeStamp = Utils.timestamp();
        String fileName = extension + "_" + timeStamp + "_";

        File file = new File(storageDir + "/" + fileName + dotExtension);
        file.createNewFile();

        return file;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_TAKE_PHOTO) {

            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();

                Intent intent_result = new Intent();
                intent_result.putExtra("photo", imageUri.toString());
                setResult(RESULT_OK, intent_result);
                finish();
            }
        }
    }

    public void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            /* Ensure that there's a camera activity to handle the intent */
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            /* Create the File where the photo should go */
            File photoFile = null;
            try {
                Log.i("Camera", "Creating file");
                photoFile = createFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "JPEG", ".jpg");
                photoUri = Uri.fromFile(photoFile);
                if (photoUri == null) {
                    Log.i("Camera", "Failed to create file");
                } else {
                    Log.i("Camera", photoUri.toString());
                }
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, Constants.REQUEST_TAKE_PHOTO);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
