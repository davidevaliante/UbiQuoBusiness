package com.firebase.notification.test.ubiquobusiness;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SnapShotFragment extends Fragment {


    public SnapShotFragment() {
        // Required empty public constructor
    }

    public static SnapShotFragment newInstance(){
        SnapShotFragment newFrag = new SnapShotFragment();
        return newFrag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_snap_shot,container,false);



        return viewGroup;
    }

}
