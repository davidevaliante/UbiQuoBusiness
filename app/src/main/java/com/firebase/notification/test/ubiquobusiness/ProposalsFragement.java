package com.firebase.notification.test.ubiquobusiness;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProposalsFragement extends Fragment {


    public ProposalsFragement() {
        // Required empty public constructor
    }

    public static ProposalsFragement newInstance(){
        ProposalsFragement newFragment = new ProposalsFragement();
        return  newFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_proposals_fragement,container,false);


        return rootView;
    }

}
