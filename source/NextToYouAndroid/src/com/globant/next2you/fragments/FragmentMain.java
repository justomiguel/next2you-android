package com.globant.next2you.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.globant.next2you.R;


public class FragmentMain extends Fragment {
    public static final String ARG_SCREEN_NUMBER = "number";

    public FragmentMain() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        
        int i = getArguments().getInt(ARG_SCREEN_NUMBER);
        String screen = (getResources().getStringArray(R.array.screens_array)[i]).toString();
        ((TextView) rootView.findViewById(R.id.fragment_textview)).setText(screen);
        
        getActivity().setTitle(screen);
        return rootView;
    }
}