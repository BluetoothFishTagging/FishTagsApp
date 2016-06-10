package bft.fishtagsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import bft.fishtagsapp.Signup.SignupActivity;
import bft.fishtagsapp.Storage.Storage;
import bft.fishtagsapp.Wifi.WifiDetector;

public class LauncherActivity extends AppCompatActivity {

    /* LauncherActivity will prompt the user to sign up
    before launching the main application.
    If the information is already registered, it will go straight to MainActivity.
    */

    private static final int REQUEST_SIGNUP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Intent intent;
        Storage.register(this, "FishTagsData");
        if (Storage.read("info.txt") != null) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(this, SignupActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        }

        //terminate
    }
}
