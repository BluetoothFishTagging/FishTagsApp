---
title: Runtime Permissions
layout: template
filename: runtime_permissions
---


## Requesting Runtime Permissions

[Beginning in Android 6.0 (API level 23), for certain permissions, users grant permissions to apps while the app is running, not when they install the app.](https://developer.android.com/training/permissions/requesting.html)

[List of dangerous permissions](https://developer.android.com/guide/topics/security/permissions.html#normal-dangerous), as of August 4, 2016:

| Permission Group | Permissions            |
|------------------|------------------------|
| CALENDAR         | READ_CALENDAR          |
|                  | WRITE_CALENDAR         |
| CAMERA           | CAMERA                 |
| CONTACTS         | READ_CONTACTS          |
|                  | WRITE_CONTACTS         |
|                  | GET_ACCOUNTS           |
| LOCATION         | ACCESS_FINE_LOCATION   |
|                  | ACCESS_COARSE_LOCATION |
| MICROPHONE       | RECORD_AUDIO           |
| PHONE            | READ_PHONE_STATE       |
|                  | CALL_PHONE             |
|                  | READ_CALL_LOG          |
|                  | WRITE_CALL_LOG         |
|                  | ADD_VOICEMAIL          |
|                  | USE_SIP                |
|                  | PROCESS_OUTGOING_CALLS |
| SENSORS          | BODY_SENSORS           |
| SMS              | SEND_SMS               |
|                  | RECEIVE_SMS            |
|                  | READ_SMS               |
|                  | RECEIVE_WAP_PUSH       |
|                  | RECEIVE_MMS            |
| STORAGE          | READ_EXTERNAL_STORAGE  |
|                  | WRITE_EXTERNAL_STORAGE |


Even though an Activity is required to ask for permissions, the thus obtained permissions persist throughout the application.

However, it may overwhelm the user to ask for all of the permissions when the application starts up.

Hence, it is ideal to ask for permissions only when necessary, so that the user knows why the app must be granted such permissions.

### Making the Request for multiple permissions

```java
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
```

### Handling the result:

In your Activity.java, override onRequestPermissionsResult():

```java
@Override
public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    Boolean granted = (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
    //TODO : check for each permission

    switch (requestCode) {
        case REQUEST_CODE_1:
            break;
        case REQUEST_CODE_2:
            break;
    }

}
```
