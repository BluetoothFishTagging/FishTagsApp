package bft.fishtagsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import bft.fishtagsapp.signup.SignupActivity;
import bft.fishtagsapp.storage.Storage;

public class LauncherActivity extends AppCompatActivity {

    /* LauncherActivity will prompt the user to sign up
    before launching the main application.
    If the information is already registered, it will go straight to MainActivity.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Intent intent;
        if (Storage.read(Constants.PERSONAL_INFO) != null) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(this, SignupActivity.class);
            intent.putExtra("request", Constants.REQUEST_SIGNUP);
            startActivityForResult(intent, Constants.REQUEST_SIGNUP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        }

        //terminate
    }
}
