package com.firebase.notification.test.ubiquobusiness;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArgumentsImageFragment extends Fragment {


    public ArgumentsImageFragment() {
        // Required empty public constructor
    }

    public static ArgumentsImageFragment newInstance(){
        ArgumentsImageFragment argumentsImageFragment = new ArgumentsImageFragment();
        return argumentsImageFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_arguments_image,container,false);



        return rootView;
    }

}
