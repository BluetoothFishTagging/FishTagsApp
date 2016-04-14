package bft.fishtagsapp.Linkage;

/**
 * Created by jamiecho on 3/30/16.
 */
import android.net.Uri;


public class TagInfo {
    private final String delim="/@@/";

    Uri photo; //photo file path, if it exists.
    Uri tag; //tag info file path, if it exists
    String summary; //concise, one-liner description of this info

    public TagInfo(Uri photo, Uri tag, String summary){
        this.photo=photo;
        this.tag=tag;
        this.summary=summary;
    }
    public TagInfo(String s){ //convert from String
        String[] info = s.split(delim);
        photo = Uri.parse(info[0]);
        tag = Uri.parse(info[1]);
        summary = info[2];
    }
    public String toString(){
        return photo.toString() + delim + tag.toString() + delim + summary;
    }

}
