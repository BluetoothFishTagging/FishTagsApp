package bft.fishtagsapp.form;

import android.content.Context;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;
import java.util.HashMap;

import bft.fishtagsapp.R;
import bft.fishtagsapp.parsefile.ParseFile;

/**
 * Created by ksoltan on 6/27/2016.
 */
public class TagIDFragment extends android.support.v4.app.Fragment {
    View view;
    String filename;

    public TagIDFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the extras passed to the fragment (filename transmitted by Bluetooth
        Bundle b = getArguments();
        filename = b.getString("fileName");
        if (filename == null) {
            Log.i("BUNDLE", "Null filename");
        } else {
            Log.i("BUNDLE", filename);
        }
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tag_id, container, false);
        // If you want to display the keyboard automatically, it messes up the layout so I have for now suppressed it.
//        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        EditText e = (EditText) view.findViewById(R.id.NationalID);
        e.requestFocus();

        fillInInfoFromFile(filename);
        return view;
    }

    protected void fillInInfoFromFile(String fileName) {
        /* Parse the file only if the file exists.*/
        if (fileName != null) {
            Log.i("FILENAME", fileName);
            fillInInfoFromFile(new File(fileName));
        }
        // If the file is null, FormActivity should catch this and sshow a dialog.
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

        setTagText(entries.get("NationalID"));
    }

    public void setTagText(String id) {
        if (view != null) {
            try {
                EditText e = (EditText) view.findViewById(R.id.NationalID);
                e.setText(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i("VIEW", "NULL");
        }
    }
}
