package com.firebase.notification.test.ubiquobusiness;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class Prova extends DialogFragment {

    protected Button testButton;

    public Prova() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup a = (ViewGroup)inflater.inflate(R.layout.fragment_prova, container, false);

        testButton = (Button)a.findViewById(R.id.btn_base);

        return inflater.inflate(R.layout.fragment_prova, container, false);
    }

}
