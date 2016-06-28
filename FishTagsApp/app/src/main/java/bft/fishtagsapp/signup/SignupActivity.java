package bft.fishtagsapp.signup;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import bft.fishtagsapp.Constants;
import bft.fishtagsapp.R;
import bft.fishtagsapp.storage.Storage;

public class SignupActivity extends AppCompatActivity {
    // UI references.
    private ScrollView mLoginFormView;
    private ArrayList<EditText> editTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mLoginFormView = (ScrollView) findViewById(R.id.login_form);

        editTexts = new ArrayList<EditText>();
        findFields(mLoginFormView, editTexts);


        /* Decide which buttons to display depending on request*/

        int request = getIntent().getIntExtra("request", 0);
        Log.i("HEY", "" + request);
        if (request == Constants.REQUEST_SIGNUP) {
            View b = findViewById(R.id.newInfo);
            b.setVisibility(View.VISIBLE);
        } else {
            /* Autofill if possible */
            String info = Storage.read("info.txt");
            Log.i("INFO", info);
            if (info != null && !info.isEmpty()) {
                autofill(info);
            }

            if (request == Constants.REQUEST_EDIT_SETTINGS) {
                Log.i("SIGNUP", "Edit settings");
                View b = findViewById(R.id.modifyInfo);
                b.setVisibility(View.VISIBLE);
            } else {
                View b = findViewById(R.id.verifyInfo);
                b.setVisibility(View.VISIBLE);
                Button b2 = (Button) findViewById(R.id.signUpBtn);
                b2.setText("Submit");
            }
        }

        /* Must send a result OK back to formActivity to allow it to submit*/
        if (request == Constants.REQUEST_VERIFY_SETTINGS) {
            Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
            signUpBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    signup(); // save in case the info was editted
                    /* We probably want to return the latest info.txt stuff*/
                    Intent intent_result = new Intent();
                    intent_result.putExtra("info", 0);
                    setResult(RESULT_OK, intent_result);
                    finish();
                }
            });
        } else {
        /* Save Information */
            Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
            signUpBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    signup();
                }
            });
        }
    }

    private void findFields(ViewGroup v, ArrayList<EditText> editTexts) {
        /* Find EditTexts */
        int n = v.getChildCount();
        for (int i = 0; i < n; ++i) {
            View subView = v.getChildAt(i);
            if (subView instanceof ViewGroup) {
                //recursively search for editTexts
                findFields((ViewGroup) subView, editTexts);
            } else if (subView instanceof EditText) {
                editTexts.add((EditText) subView);
            }
        }
    }

    private void autofill(String info) {
        // fill in fields from stored info
        try {
            Resources res = getResources();
            JSONObject data = new JSONObject(info);
            for (EditText e : editTexts) {
                String textId = e.getResources().getResourceEntryName(e.getId());
                String value = data.getString(textId);
                e.setText(value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void signup() {
        //loop through views & get data
        try {
            JSONObject data = new JSONObject();

            for (EditText e : editTexts) {
                String textId = e.getResources().getResourceEntryName(e.getId());
                String value = e.getText().toString();
                data.put(textId, value);
            }

            //delete previous data (failsafe)
            Storage.delete(Constants.PERSONAL_INFO);

            //save new data
            Storage.save(Constants.PERSONAL_INFO, data.toString());

            Intent intent_result = new Intent();
            setResult(RESULT_OK, intent_result);
            finish();

        } catch (JSONException e) {
            e.printStackTrace();

            Intent intent_result = new Intent();
            setResult(RESULT_CANCELED, intent_result);
            finish();
        }
    }
}
