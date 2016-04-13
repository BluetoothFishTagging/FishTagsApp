package bft.fishtagsapp;

import android.content.Intent;
import android.location.Location;
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

import bft.fishtagsapp.GPS.GPS;

import bft.fishtagsapp.ParseFile.ParseFileActivity;

public class MainActivity extends AppCompatActivity {
    private GPS gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        /* How To Invoke GPS */
        gps = new GPS(this);
        Button GPSBtn = (Button) findViewById(R.id.GPS_GPSBtn);
        GPSBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Location location = gps.getGPS();
                if(location != null){
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String s = String.format("LAT:%f,LONG:%f",latitude,longitude);
                    Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                }
            }
        });
        /* GPS End */

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

    /**
     * Called when user clicks Parse File button
     * @param view
     */
    public void parseFile(View view){
        Intent intent = new Intent(this, ParseFileActivity.class);
        startActivity(intent);
    }
}
