package bft.fishtagsapp.linkage;

/**
 * Created by jamiecho on 3/30/16.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import bft.fishtagsapp.R;


public class LinkageActivity extends AppCompatActivity {

    private static final int PHOTO_SELECT_CODE = 100;
    private static final int TAG_SELECT_CODE = 200;

    private Uri photoUri;
    private Uri tagUri;

    TagInfo[] linkages;
    MyAdapter adapter;

    private int sel;
    private int position;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkage);
        new ImageView(getApplicationContext()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /* Simulated Data-Loading from App's Storage*/
        String pp = "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/20160211_051742.jpg"; //photo directory.
        String tp = "file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/2016_01_18_21_45_735161.txt";

        linkages = new TagInfo[]{ // in the real app, this would be loaded from app's internal memory on startup.
                new TagInfo(Uri.parse(pp),Uri.parse(tp),"DEFAULT"),
                new TagInfo(Uri.parse(pp),Uri.parse(tp),"DEFAULT"),
        };


        /* Initializing ListView */
        adapter = new MyAdapter(this, linkages);
        ListView listview = (ListView)findViewById(R.id.linkageList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                position = pos;

                Log.i("ITEM","CLICK");
                photoUri = linkages[position].photo;
                tagUri = linkages[position].tag; //set to current path

                //String s = String.valueOf(parent.getItemAtPosition(position));
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                DialogSelectOption();
            }
        });
    }

    private void fetchPhoto(){
        fetchFile(PHOTO_SELECT_CODE);
    }
    private void fetchTag(){
        fetchFile(TAG_SELECT_CODE);
    }

    private void fetchFile(int FILE_TYPE_CODE) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"), FILE_TYPE_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(getApplicationContext(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        photoUri = data.getData();
                        linkages[position].photo = photoUri;
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case TAG_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        tagUri = data.getData();
                        linkages[position].tag = tagUri;
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }

        Log.i("PHOTO Path", photoUri.toString());
        Log.i("TAG Path", tagUri.toString());
        adapter.notifyDataSetChanged();

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void DialogSelectOption() {
        final String items[] = { "Photo", "Tag"};
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("Edit...");
        sel = 0;

        ab.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sel = whichButton;
                    }
                }).setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch(sel){
                            case 0: //Photo
                                fetchPhoto();
                                break;
                            case 1:
                                fetchTag();
                                break;
                        }
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel button, do nothing
                    }
                });

        ab.show();
        Log.i("DIALOG", "OVER");

    }

}
