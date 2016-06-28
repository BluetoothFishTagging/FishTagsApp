package bft.fishtagsapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bft.fishtagsapp.client.HttpClient;

/**
 * Created by ksoltan on 6/27/2016.
 */
public class HomeFragment extends android.support.v4.app.Fragment {
    View view;
    String response;

    public HomeFragment() {
        // Required empty public constructor
    }

    protected class HttpGetTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String url = params[0];

            try {
                HttpClient client = new HttpClient(url);
                client.connect();
                response = client.getResponse();

                Log.i("RSP", response);
                Log.i("RSP", "In HomeFragment");
                //publishProgress();
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            updateView();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //Log.i("SUCCESS", success);
            super.onPostExecute(success);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* OBTAIN NETWORK-ACCESS RELATED PERMISSIONS */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        new HttpGetTask().execute(Constants.DATABASE_URL + "query?name=jamie");
        return view;
    }

    private String getLocCatches(String res) {
        String a = "";
        try {
            JSONArray results = new JSONArray(res);
            for (int i = 0; i < results.length(); i++){
                JSONObject tag = new JSONObject((String) results.get(i));
                String loc = (String)tag.get("Location");
                a += "\n" + loc;
                Log.i("Array", a);
            }
            return a;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void updateView() {
        if (view != null) {
            TextView text = (TextView) view.findViewById(R.id.numCatches);
            if (text != null) {
                String r = getLocCatches(response);
                Log.i("String", r);
                text.setText(r);
            }else{
                Log.i("Text", "is null");
            }
        } else {
            Log.i("HomeFragment", "null view");
        }
    }
}
