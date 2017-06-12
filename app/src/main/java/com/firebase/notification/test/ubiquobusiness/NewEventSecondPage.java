package com.firebase.notification.test.ubiquobusiness;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewEventSecondPage extends Fragment {


    public NewEventSecondPage() {
        // Required empty public constructor
    }

    public static NewEventSecondPage newInstance(){
        NewEventSecondPage newFragment = new NewEventSecondPage();
        return newFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_new_event_second_page,container,false);

        //prendere dati luogo dati tempo

        return rootView;
    }

}
