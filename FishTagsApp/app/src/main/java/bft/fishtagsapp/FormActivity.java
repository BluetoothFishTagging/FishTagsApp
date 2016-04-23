package bft.fishtagsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.HashMap;

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
    }


    protected HashMap<String, String> getFormMap(){
        return null;
    }

    public void submitForm(){
        Intent intent_result = new Intent();
        intent_result.putExtra("map", getFormMap());
        setResult(RESULT_OK, intent_result);
    }



}
