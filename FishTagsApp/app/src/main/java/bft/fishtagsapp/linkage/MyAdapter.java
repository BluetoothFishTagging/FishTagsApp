package bft.fishtagsapp.linkage;

/**
 * Created by jamiecho on 3/30/16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import bft.fishtagsapp.R;

public class MyAdapter extends ArrayAdapter<TagInfo> {

    static class ViewHolder{
        ImageView imageView;
        TextView tagView;
        EditText summaryView;

    }

    ViewHolder viewHolder;
    LayoutInflater inflater;

    public MyAdapter(Context context, TagInfo[] values) {
        super(context, R.layout.row_layout,values);
        inflater = LayoutInflater.from(getContext());
    }
    private Bitmap decodeBitmap(Uri uri) { // not my code; http://stackoverflow.com/questions/477572/strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object#823966
        //Log.i("BMP-URI",Uri.fromFile(f).toString());
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(uri), null, o2);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String decodeText(Uri uri){
        //Log.i("TXT-URI",Uri.fromFile(f).toString());
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return text.toString();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = inflater.inflate(R.layout.row_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tagView = (TextView) convertView.findViewById(R.id.tag);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.photo);
            viewHolder.summaryView = (EditText) convertView.findViewById(R.id.summary);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }


        //String photoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/20160211_051742.jpg"; //photo directory.
        //String tagPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/2016_01_18_21_45_735161.txt";

        //Bitmap photo = decodeBitmap(new File(photoPath)); //or something else, retrieved from app's internal memory.
        //String tag = decodeText(new File(tagPath));

        TagInfo s = getItem(position);

        Bitmap photo = decodeBitmap(s.photo);
        String tag = decodeText(s.tag);

        viewHolder.imageView.setImageBitmap(photo); //cannot set directly to URI, outofmemoryerror.
        viewHolder.tagView.setText(tag);
        viewHolder.summaryView.setText(s.summary);

        //viewHolder.imageView.setImageResource(R.drawable.placeholder);
        return  convertView;
    }
}
