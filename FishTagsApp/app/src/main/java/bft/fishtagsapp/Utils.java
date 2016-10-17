package bft.fishtagsapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import bft.fishtagsapp.storage.Storage;

/**
 * Created by jamiecho on 6/20/16.
 */
public final class Utils {
    public static Boolean checkAndRequestRuntimePermissions(Activity activity, String[] permissions, int requestCode){
        ArrayList<String> permissionsToRequest = new ArrayList<>();

        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)){
                    //do something here
                }else{
                    permissionsToRequest.add(permission);
                }
            }
        }

        if(permissionsToRequest.size() == 0){
            return true; //no need to get new permissions
        }else{
            String[] permArr = new String[permissionsToRequest.size()];
            permArr = permissionsToRequest.toArray(permArr);
            ActivityCompat.requestPermissions(activity, permArr, requestCode);
            return false;
        }
    }

    public static String timestamp(){
        return new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date().getTime());
    }


    public static String getName() {
        String info = Storage.read("info.txt");
        if (info != null && !info.isEmpty()) {
            try {
                JSONObject data = new JSONObject(info);
                String value = data.getString("name");
                Log.i("INFO", value);
                return value;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }


    public static void setName(View v) {
        /* Create welcome message for returning user */
        String name = Utils.getName();

        if (!name.equals("")) {
            TextView welcome = (TextView) v.findViewById(R.id.userName);
            if (welcome != null) {
                welcome.setText(String.format("Hi, %s!", name));
            }
        }
    }
}
