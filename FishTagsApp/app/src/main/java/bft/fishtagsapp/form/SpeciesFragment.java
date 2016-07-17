package bft.fishtagsapp.form;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import bft.fishtagsapp.R;

/**
 * Created by ksoltan on 6/27/2016.
 */
public class SpeciesFragment extends android.support.v4.app.Fragment {
    View view;

    public SpeciesFragment() {
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
        view = inflater.inflate(R.layout.fragment_species, container, false);
        Spinner dropdown = (Spinner) view.findViewById(R.id.Species);
        String[] items = new String[]{"SELECT SPECIES","Atlantic Bluefin", "Striped Marlin", "Yellowfin Tuna"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        return view;
    }
}
