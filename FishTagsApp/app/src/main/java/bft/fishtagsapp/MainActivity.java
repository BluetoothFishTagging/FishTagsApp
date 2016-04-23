package bft.fishtagsapp;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;

import bft.fishtagsapp.Camera.Camera;
import bft.fishtagsapp.GPS.GPS;
import bft.fishtagsapp.Client.Uploader;
import bft.fishtagsapp.Linkage.LinkageActivity;
import bft.fishtagsapp.Storage.StorageActivty;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    static final int SUBMIT_TAG = 1;

    /**
     *
     * When formActivity is over, MainActivity routes the data received from it (dictionary of values and Uri s)
     * to Storage, telling it to start tyring to upload the info to the database.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SUBMIT_TAG){
            if (resultCode == RESULT_OK){
                //TODO: Route to Storage
                HashMap<String, String> map = (HashMap<String, String>)data.getSerializableExtra("map");
                Toast.makeText(getApplicationContext(), "Thank you for submitting a tag!", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void goToForm(View view){
        Intent intent = new Intent(this, FormActivity.class);
        startActivityForResult(intent, SUBMIT_TAG);
    }
}
