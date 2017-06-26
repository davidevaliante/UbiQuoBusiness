package com.firebase.notification.test.ubiquobusiness;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.haha.perflib.Main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;


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
    private ProgressDialog dialog;

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

        dialog = UbiQuoBusinessUtils.defaultProgressBar("Registrazione in corso",getActivity());

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    Log.d("LOGGED ID : ",firebaseAuth.getCurrentUser().getUid());

                }else{
                    Log.d("USER LOGGED OUT","NO USER LOGGED IN");
                }
            }
        };

        mAuth.addAuthStateListener(authStateListener);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                if(dataHasBeenSaved()){
                    String mail = email.getText().toString().trim();
                    String password = confirmPassword.getText().toString().trim();
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("EMAIL", mail);

                    mAuth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                writeNewPlace();
                            }
                        }
                    });


                }else{
                    dialog.dismiss();
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

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
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

    //TODO finire metodo per scrivere nuovo locale con tutti i callback necessari per il fallimento ed i dati mancanti
    private void writeNewPlace(){

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
        final String placeName = sharedPreferences.getString("PLACE_NAME","NA");
        final String placeCity = sharedPreferences.getString("PLACE_CITY","NA");
        final String placeAdress = sharedPreferences.getString("PLACE_ADRESS","NA");
        final String placeId = sharedPreferences.getString("PLACE_ID","NA");
        final String placePhone = sharedPreferences.getString("PLACE_PHONE","NA");
        String imageUri = sharedPreferences.getString("PLACE_AVATAR","NA");
        final Double latitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"PLACE_LATITUDE",0.0);
        final Double longitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"PLACE_LONGITUDE",0.0);
        final String openingTime = sharedPreferences.getString("PLACE_OPENING_TIME","NA");
        final String closingTime = sharedPreferences.getString("PLACE_CLOSING_TIME","NA");
        final Long iscrizione = System.currentTimeMillis();
        String mail = email.getText().toString().trim();
        String password = confirmPassword.getText().toString().trim();
        Uri avatar = Uri.parse(imageUri);

        //recupera l'immagine croppata e la e la comprime in un file
        final File compressedFile = Compressor.getDefault(getActivity()).compressToFile(new File(avatar.getPath()));
        final String uniqueStoragePath = avatar.getLastPathSegment()+placeName+placeCity;

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Business_avatar");


        storageReference.child(uniqueStoragePath).putFile(Uri.fromFile(compressedFile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {

                    String imagePath = task.getResult().getDownloadUrl().toString();
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Business newBusiness = new Business(placeId, "NA", "NA", placeName, placeAdress, placePhone, placeCity, latitude, longitude, 0, 0, openingTime, closingTime, imagePath, "NA", "no_token", iscrizione);

                    //nodo che salva locali per ogni città
                    FirebaseDatabase.getInstance().getReference().child("City-Businesses").child(placeCity).child(id).setValue(newBusiness);
                    //nodo che salva i locali in un contenitore comune
                    final DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference().child("Businesses");

                    businessRef.child(id).setValue(newBusiness);


                }else {
                    Toasty.error(getActivity(),"Ci sono stati problemi con l'upload",Toast.LENGTH_SHORT,true).show();

                }
            }
        });

        dialog.dismiss();
        Toasty.success(getActivity(), "Registrazione effettuata !", Toast.LENGTH_SHORT,true).show();

        Intent toUserPage = new Intent(getContext(), MainUserPage.class);
        startActivity(toUserPage);
        //getActivity().finish();



    }
}