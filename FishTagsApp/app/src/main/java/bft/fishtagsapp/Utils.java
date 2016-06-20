package bft.fishtagsapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

}
