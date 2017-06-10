package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.haha.perflib.Main;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArgumentsImageFragment extends Fragment {


    @BindView(R.id.placeEmail)
    EditText email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.confirmPassword)
    EditText confirmPassword;
    @BindView(R.id.registration_second_viewpager)
    ScrollView registrationSecondViewpager;
    @BindView(R.id.submitButton)
    RelativeLayout submitButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    Unbinder unbinder;

    public ArgumentsImageFragment() {
        // Required empty public constructor
    }

    public static ArgumentsImageFragment newInstance() {
        ArgumentsImageFragment argumentsImageFragment = new ArgumentsImageFragment();
        return argumentsImageFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_arguments_image, container, false);


        unbinder = ButterKnife.bind(this, rootView);

        mAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    Intent toUserPage = new Intent(getActivity(), MainUserPage.class);
                    toUserPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(toUserPage);
                }else{

                }
            }
        };

        mAuth.addAuthStateListener(authStateListener);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataHasBeenSaved()){
                    String mail = email.getText().toString().trim();
                    String password = confirmPassword.getText().toString().trim();
                    Toasty.success(getActivity(),"Cool shit dude", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("EMAIL", mail);
                    mAuth.createUserWithEmailAndPassword(mail,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toasty.success(getActivity(),"Registrazione effettuata !",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toasty.error(getActivity(),"Registrazione fallita !",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{

                }
            }
        });


        return rootView;
    }

    private Boolean dataHasBeenSaved(){
        Boolean isOk = true;
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String passConfirm = confirmPassword.getText().toString().trim();

        if(mail.isEmpty() || pass.isEmpty() || passConfirm.isEmpty() || pass.isEmpty()){
            Toasty.error(getActivity(),"Riempi tutti i campi", Toast.LENGTH_SHORT).show();

            isOk = false;
            return false;
        }

        if(!pass.equalsIgnoreCase(passConfirm)){
            Toasty.error(getActivity(),"Le password non coincidono", Toast.LENGTH_SHORT).show();
            isOk = false;
            return false;
        }

        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String placeName = sharedPreferences.getString("PLACE_NAME","NA");
        String placeCity = sharedPreferences.getString("PLACE_CITY","NA");
        String placeAdress = sharedPreferences.getString("PLACE_ADRESS","NA");
        String placeId = sharedPreferences.getString("PLACE_ID","NA");
        String placePhone = sharedPreferences.getString("PLACE_PHONE","NA");
        Double latitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"PLACE_LATITUDE",0.0);
        Double longitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"PLACE_LONGITUDE",0.0);

        if(placeName.equalsIgnoreCase("NA") || placeCity.equalsIgnoreCase("NA") || placeAdress.equalsIgnoreCase("NA")
              || latitude == 0.0 || longitude == 0.0){

            Toasty.error(getActivity(),"Errore: alcuni dati precedenti risultano mancanti", Toast.LENGTH_SHORT).show();
            isOk = false;
            return false;

        }

        return isOk;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAuth.removeAuthStateListener(authStateListener);
        unbinder.unbind();
    }
}
