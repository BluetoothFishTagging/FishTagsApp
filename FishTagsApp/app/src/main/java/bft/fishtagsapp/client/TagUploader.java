package bft.fishtagsapp.client;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import bft.fishtagsapp.Constants;

/**
 * TagUploader will receive data in the form of a hasmap from each tag report submission and attempt to upload it to the database.
 * And IntentService was chosen for this purpose because the uploads do not have to be uploaded in parallel and therefore simply
 * need to be uploaded one after the other. The IntentService also terminates when it itself done, independent of the lifecycle
 * of the activity that calls it, ensuring that the uploading will continue until all pending requests are submitted.
 */

public class TagUploader extends IntentService {
    //placeholder url
    private static String url = "http://192.168.16.73:8000";
    private static Context context;

    public TagUploader() {
        super("TagUploader");
    }

    public static void startActionUpload(Context context, String uri, String personInfo, String tagInfo) {
        TagUploader.context = context;
        Intent intent = new Intent(context, TagUploader.class);

        intent.putExtra("uri", uri);
        intent.putExtra("personInfo", personInfo);
        intent.putExtra("tagInfo", tagInfo);

        intent.setAction(Constants.ACTION_UPLOAD);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_UPLOAD.equals(action)) {
                String uri = intent.getStringExtra("uri");
                String personInfo = intent.getStringExtra("personInfo");
                String tagInfo = intent.getStringExtra("tagInfo");

                handleActionUpload(uri, personInfo, tagInfo);
            }
        }
    }

    public byte[] convertUriToByteArray(Uri uri) {
        byte[] byteArray = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 8];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    private void handleActionUpload(String uri, String tagInfo, String personInfo) {
        /* Wait For Wifi*/
        try {
            byte[] byteArray = convertUriToByteArray(Uri.parse(uri)); // = bytearray

            HttpClient client = new HttpClient(url);
            client.connectMultipart();
            client.addFormPart("tagInfo", tagInfo);
            client.addFormPart("personInfo", personInfo); //form (plain text, JSON, etc) data.
            client.addFilePart("photo", "camera", byteArray);
            client.finishMultipart();
            String response = client.getResponse();

            if (response.equals("RESPONSE_OK")) {
                //TODO : change response string
                return;
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        //if it reaches here something bad happened

    }
}