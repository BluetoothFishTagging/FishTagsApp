package bft.fishtagsapp.form;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bft.fishtagsapp.R;

/**
 * Created by ksoltan on 6/27/2016.
 */
public class ForkLengthFragment extends android.support.v4.app.Fragment {
    View view;

    public ForkLengthFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fork_length, container, false);
        return view;
    }
}
