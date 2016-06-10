package bft.fishtagsapp.Signup;

import android.content.Intent;
import android.content.res.Resources;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import bft.fishtagsapp.MainActivity;
import bft.fishtagsapp.R;
import bft.fishtagsapp.Storage.Storage;

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
        findFields(mLoginFormView,editTexts);

        /* Save Information */
        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        /* Autofill fields when requested*/
        Button autofillBtn = (Button) findViewById(R.id.autoFillBtn);
        autofillBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = Storage.read("info.txt");
                Log.i("INFO",info);
                if(info != null && !info.isEmpty()){
                    autofill(info);
                }
            }
        });
    }
    private void findFields(ViewGroup v, ArrayList<EditText> editTexts){
        /* Find EditTexts */
        int n = v.getChildCount();
        for(int i=0; i<n; ++i){
            View subView = v.getChildAt(i);
            if(subView instanceof ViewGroup){
                //recursively search for editTexts
                findFields((ViewGroup)subView, editTexts);
            }else if (subView instanceof EditText){
                editTexts.add((EditText)subView);
            }
        }
    }
    private void autofill(String info){
        // fill in fields from stored info
        try{
            Resources res = getResources();
            JSONObject data = new JSONObject(info);
            for(EditText e : editTexts){
                String textId = e.getResources().getResourceEntryName(e.getId());
                String value = data.getString(textId);
                e.setText(value);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    private void signup(){
        //loop through views & get data
        try{
            JSONObject data = new JSONObject();

            for(EditText e : editTexts){
                String textId = e.getResources().getResourceEntryName(e.getId());
                String value = e.getText().toString();
                data.put(textId,value);
            }

            //for debugging
            Toast.makeText(getApplicationContext(),data.toString(),Toast.LENGTH_LONG).show();

            //delete previous data (failsafe)
            Storage.delete("info.txt");

            //save new data
            Storage.save("info.txt", data.toString());

            Intent intent_result = new Intent();
            setResult(RESULT_OK, intent_result);
            finish();

        }catch(JSONException e){
            e.printStackTrace();

            Intent intent_result = new Intent();
            setResult(RESULT_CANCELED, intent_result);
            finish();
        }
    }

}

