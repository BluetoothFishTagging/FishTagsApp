package bft.fishtagsapp.Storage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bft.fishtagsapp.MainActivity;

/**
 * Created by jamiecho on 4/14/16.
 */

public final class Storage {

    private static final int READ_BLOCK_SIZE = 512;//Block Size for text File
    private static Boolean useSDCard;

    //FileStorage
    private static File fileStorage = null;//Main Directory File

    private static String latest; //latest entry

    //Preferences to Store Data when SDCard is not present
    private static SharedPreferences prefStorage;
    private static SharedPreferences.Editor editor;

    private static Context context;

    private Storage(){

    }

    public static boolean isSDCardPresent() {
        //can be simpler but leaving it this way to be explicit
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static Boolean save(String name, String data){
        if(useSDCard){
            return saveToFile(name, data);
        }else{
            //return saveToPref(name,data);
        }
        return false;
    }

    public static String read(String name){
        if(useSDCard){
            return readFromFile(name);
        }else{
            return readFromPref(name);
        }
    }

    public static Boolean delete(String name){
        if(useSDCard){
            return deleteFile(name);
        }else{
            return deletePref(name);
        }
    }

    private static Boolean saveToFile(String FileName, String message) {
        //Check if it is null or not

        if (message.length() == 0 && message.equals(""))
            return false;
        else {

                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    Log.d("Test", "sdcard mounted and writable");
                }
                else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    Log.d("Test", "sdcard mounted readonly");
                }
                else {
                    Log.d("Test", "sdcard state: " + state);
                }

            try {

                if(Environment.getExternalStorageDirectory().canWrite()){
                    Log.i("write","can write");
                }else{
                    Log.i("write","can't-write");
                }
                //Create Main Directory if not present
                if (!fileStorage.exists()){
                    Log.i("fileStorage","Nonexistent");
                    Boolean res = fileStorage.mkdirs();
                    Log.i("fileStorageMkdirs",res.toString());
                }

                //Make File Name under main Directory
                File savedFile = new File(fileStorage, FileName);

                //If Saved File exists then check if its size is greater than 0
                if (savedFile.exists()) {
                    if (savedFile.length() > 0)
                        appendOrOverrideSavedFile(FileName, message);//if file present then show alert for appending and overriding
                    else {
                        //else save data into file
                        writeToFile(FileName, message);
                        return true;
                    }
                } else {
                    //If Saved file doesn't exists then create new file and save file
                    writeToFile(FileName, message);
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Message", e.getLocalizedMessage());

            }
        }
        return false;
    }


    //Method to show alert if file already present
    private static void appendOrOverrideSavedFile(final String FileName, final String message) {
        File savedFile = new File(fileStorage.getAbsolutePath() + "/" + FileName);

        final String savedMessage = readFromFile(FileName);//Read saved message

        //Check if saved message not null
        if (savedMessage != null && savedMessage.length() > 0 && !savedMessage.equals("")) {

            //If not null then show alert

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle("Choose..");
            builder.setMessage("What do you want to do with message?\n\nAppend data to current saved file or override saved data.");
            builder.setPositiveButton("OVERRIDE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //If Override is selected then textfile data is overrided
                    writeToFile(FileName, message);
                    Toast.makeText(context, "File Overrided Successfully!",
                            Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("APPEND", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //If Append is selected then textfile data is appended with new data
                    writeToFile(FileName, savedMessage + "\n\n" + message);
                    Toast.makeText(context, "File Appended Successfully!",
                            Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            AlertDialog ad = builder.create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(true);
            ad.show();

        } else
            //If there is nothing to read from saved fie then simply add data to file
            writeToFile(FileName, message);
    }


    //Method for writing data to text file
    private static void writeToFile(String FileName, String messageBody) {
        try {
            File savedFile = new File(fileStorage.getAbsolutePath() + "/" + FileName);
            Log.i("dir",savedFile.getAbsolutePath());
            FileWriter fWriter;

            if(!savedFile.exists()){
                Boolean res = savedFile.getParentFile().mkdirs(); //makes parent directories
                Log.i("mkdirs",res.toString());
                res = savedFile.createNewFile();
                Log.i("createFile",res.toString());
            }

            fWriter = new FileWriter(savedFile);

            //File writer is used for writing data
            fWriter.write(messageBody);//write data
            fWriter.flush();//flush writer
            fWriter.close();//close writer

            //Get Current Date and put it in Shared Preferences
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeToPref(String Key, String message){
        editor.putString(Key, message);
        editor.commit();

    }

    //Method that will return saved text file data after reading
    private static String readFromFile(String FileName) {

        //First check if main directory is present or not
        if (!fileStorage.exists()) {
            return null;
        }
        else {
            //Then check if text file is present or not
            File savedFile = new File(fileStorage.getAbsolutePath() + "/" + FileName);
            if (!savedFile.exists())
                return null;
            else {
                //Finally read data using FileReader
                try {
                    FileReader rdr = new FileReader(fileStorage.getAbsolutePath() + "/" + FileName);

                    char[] inputBuffer = new char[READ_BLOCK_SIZE];//get Block size as buffer
                    String savedData = "";
                    int charRead = rdr.read(inputBuffer);
                    //Read all data one by one by using loop and add it to string created above
                    for (int k = 0; k < charRead; k++) {
                        savedData += inputBuffer[k];
                    }
                    return savedData;//return saved data

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Message", e.getLocalizedMessage());
                    return null;
                }
            }
        }

    }

    private static String readFromPref(String name){
        return prefStorage.getString(name,null);
    }
    //Show Saved data
    /* Temporarily Disabled
    private void showSavedFileData(String savedMessage) {

        //Check if saved data is not null
        if (savedMessage != null && savedMessage.length() > 0 && !savedMessage.equals("")) {

            //Check if TimeStamp is present or not
            if (prefStorage.contains(TimeKeyValue))
                savedMessageTime.setText(prefStorage.getString(TimeKeyValue, ""));//Dispaly saved timestamp

            savedMessageFileNameandPath.setText(FileName + " - inside '" + fileStorage.getAbsolutePath() + "'");//Display file path
            saveMessageData.setText(savedMessage);//display saved data
        }
    }
    */

    //Delete text file method
    private static Boolean deleteFile(String FileName) {

        //Check if main directory is present or not
        if (!fileStorage.exists())
            return false;
        else {
            //Now Check if text file is present or not
            File savedFile = new File(fileStorage.getAbsolutePath() + "/" + FileName);
            if (!savedFile.exists())
                return false;
            else {
                savedFile.delete();//If text file is present then delete file
                return true;
            }

        }
    }

    private static Boolean deletePref(String Key){
        editor.remove(Key);
        return editor.commit();
    }

    public static Boolean saveReport(HashMap<String,String> data){
        JSONObject jsonData = new JSONObject(data);
        writeToFile("reports" + '/' + data.get("name"), jsonData.toString());
        return true;
    }

    public static HashMap<String,String> parseReport(File f){
        HashMap<String,String> map = new HashMap<>();

        // TODO: parse report from file

        return map;
    }

    private static List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".csv")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    public static ArrayList<HashMap<String,String>> getReports(){
        ArrayList<HashMap<String,String>> list = new ArrayList<>();

        for(File file : getListFiles(fileStorage)){
            list.add(parseReport(file));
        }

        return list;
    }
    public static void removeReports(){
        //remove all reports
        String[] children = fileStorage.list();
        for (int i = 0; i < children.length; i++)
        {
            new File(fileStorage, children[i]).delete();
        }
    }

    public static void register(Context context, String name){
        Storage.context = context;

        if(isSDCardPresent()){
            Log.i("sdcard","present");
            useSDCard = true;
            fileStorage = new File(Environment.getExternalStorageDirectory(),name);
            if(!fileStorage.exists()){
                fileStorage.mkdirs();
            }

            File reportStorage = new File(fileStorage,"reports");
            if(!reportStorage.exists()){
                reportStorage.mkdirs();
            }

            //TODO : create subdirectory "report"
        }else{

            Log.i("sdcard","not present");
            useSDCard = false;
            prefStorage = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            editor = prefStorage.edit();
            //alternatively, use SharedPreferences
        }
    }

    public static String latest(){ //getter
        return latest;
    }

    public static void latest(String l){ //setter
        latest = l;
    }
}
