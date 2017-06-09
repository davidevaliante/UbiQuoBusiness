package com.firebase.notification.test.ubiquobusiness;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class OpeningClosing extends Fragment {


    public OpeningClosing() {
        // Required empty public constructor
    }

    public static OpeningClosing newInstance(){
        OpeningClosing openingClosing = new OpeningClosing();
        return openingClosing;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_opening_closing,container,false);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            UbiQuoBusinessUtils.changeStatusBarColor(R.color.colorPrimary,getActivity());

        }
    }
}
