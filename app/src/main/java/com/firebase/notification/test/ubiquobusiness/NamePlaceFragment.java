package com.firebase.notification.test.ubiquobusiness;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class NamePlaceFragment extends Fragment {

    @BindView(R.id.registration_email)    EditText emailField;
    @BindView(R.id.registration_password) EditText passwordField;
    @BindView(R.id.registrationButton)    Button registrationButton;


    public NamePlaceFragment() {
        // Required empty public constructor
    }

    public static NamePlaceFragment newInstance(){
        NamePlaceFragment namePlaceFragment = new NamePlaceFragment();
        return namePlaceFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_name_place,container,false);
        ButterKnife.bind(this,rootView);



        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Registration)getActivity()).mAuth.createUserWithEmailAndPassword(emailField.getText().toString().trim(),passwordField.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Registrazione effettuata", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), "Registrazione fallita", Toast.LENGTH_SHORT).show();
                            Log.d("Failure cause :", task.getException().toString());
                        }
                    }
                });
            }
        });


        return rootView;
    }

}
