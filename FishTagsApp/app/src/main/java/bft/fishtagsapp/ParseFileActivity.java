package bft.fishtagsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Current RFID reader transfers:
 * CountryID:999
 * NationalID:00000000009934
 * Reserved:0
 * Current mode: Animal Tag
 * Click 'More' to change the reading type
 */
public class ParseFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse_file);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        // Do stuff
    }

    /**
     *
     * @return Path to storage where transmitted file can be found
     * External storage currently (sdcard)
     */
    private File getStoragePath(){
        return Environment.getExternalStorageDirectory();
    }

    /**
     *
     * @return name of file where transmitted data is stored.
     * Currently hardcoded to message transmitted on 3/8/2016
     */
    private String getFileName(){
        return "2016_03_08_22_08_1157809.txt";
    }

    protected HashMap<String, String> getEntries(){
        File file = new File(getStoragePath(), getFileName());
        HashMap<String, String> entries = new HashMap<>();
        String line;
        int numLine = 0;
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null){
                numLine++;
                if(numLine > 3){
                    break;
                }
                String[] l = line.split(":");
                entries.put(l[0], l[1]);
            }
        }
        catch(IOException e){
            // Do some error handling
        }

        return entries;
    }
}
