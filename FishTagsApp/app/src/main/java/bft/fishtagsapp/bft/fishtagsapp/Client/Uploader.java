package bft.fishtagsapp.bft.fishtagsapp.Client;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jamiecho on 3/9/16.
 */
public class Uploader {

    AppCompatActivity context;

    //placeholder url for server domain
    //hardcoded, and must be replaced with real domain name
    String url = "http://192.168.16.73:8000/";

    public Uploader(AppCompatActivity context, String url) {
        this.context = context;
        this.url = url;
    }

    public void send(Uri uri, String param1, String param2){
        /* Prior to Transmit, set Files, params, etc. */
        SendHttpRequestTask t = new SendHttpRequestTask();
        String[] params = new String[]{url, uri.toString(), param1, param2};
        t.execute(params);
    }


    public byte[] convertUriToByteArray(Uri uri)
    {
        byte[] byteArray = null;
        try
        {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead =0;

            while ((bytesRead = inputStream.read(b)) != -1)
            {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return byteArray;
    }

    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            Uri uri = Uri.parse(params[1]);
            String param1 = params[2];
            String param2 = params[3];

            // FOR STORAGE FILES, construct byte array as follows:
            byte[] byteArray = convertUriToByteArray(uri); // = bytearray

            try {
                HttpClient client = new HttpClient(url);
                client.connectMultipart();
                client.addFormPart("param1", param1);
                client.addFormPart("param2", param2); //form (plain text, JSON, etc) data.

                client.addFilePart("photo","camera",byteArray);
                client.finishMultipart();
                String data = client.getResponse();
            }
            catch(Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //Log.i("RESPONSE", s);
            super.onPostExecute(s);
        }

    }
}