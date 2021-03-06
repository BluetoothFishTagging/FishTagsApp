package bft.fishtagsapp.parsefile;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Current RFID reader transfers:
 * CountryID:999
 * NationalID:00000000009934
 * Reserved:0
 * Current mode: Animal Tag
 * Click 'More' to change the reading type
 */
public class ParseFile {
    /**
     * @return Path to storage where transmitted file can be found
     * External storage currently (sdcard)
     */
    private static File getStoragePath() {
        //return Environment.getExternalStorageDirectory();
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * @return name of file where transmitted data is stored.
     * Currently hardcoded to message transmitted on 3/8/2016
     */

    public static HashMap<String, String> getEntries(String fileName) {
        File file = new File(fileName);
        return getEntries(file);
    }

    public static HashMap<String, String> getEntries(File file) {
        HashMap<String, String> entries = new HashMap<>();
        String line;
        int numLine = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                numLine++;
                if (numLine > 3) {
                    break;
                }
                Log.i("Reading", line);
                String[] l = line.split(":");
                entries.put(l[0], l[1]);

            }
        } catch (IOException e) {
            // Do some error handling
            //TextView test = (TextView)findViewById(R.id.testText);
            //test.setText("Error occurred");
        }

        return entries;
    }
}