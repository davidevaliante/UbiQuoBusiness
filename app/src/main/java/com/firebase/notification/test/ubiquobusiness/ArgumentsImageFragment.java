package com.firebase.notification.test.ubiquobusiness;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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

import java.io.File;

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
    @BindView(R.id.validationCode)
    EditText validationCode;
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

        dialog = new ProgressDialog(getActivity(),android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        dialog.setMessage("Attendere prego");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Log.d("LOGGED ID : ", firebaseAuth.getCurrentUser().getUid());

                } else {
                    Log.d("USER LOGGED OUT", "NO USER LOGGED IN");
                }
            }
        };

        mAuth.addAuthStateListener(authStateListener);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (businessHasRequiredFields()) {
                    dialog.show();
                    final String mail = email.getText().toString().trim();
                    final String password = confirmPassword.getText().toString().trim();
                    final String code = validationCode.getText().toString().trim();
                    DatabaseReference codeReference = FirebaseDatabase.getInstance().getReference().child("Codes");
                    codeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(code)) {
                                mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String userId = task.getResult().getUser().getUid();
                                            upDateDatabaseWithNewPlace(userId);

                                        }
                                    }
                                });
                            }else{

                                dialog.dismiss();
                                Toasty.error(getActivity(),"Codice non valido, contatta il team di UbiQuo su Facebook per averne uno valido",Toast.LENGTH_SHORT,true).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("Error : ",databaseError.getDetails());
                        }
                    });




                }
            }
        });


        return rootView;
    }

    private Boolean businessHasRequiredFields() {
        Boolean isOk = true;
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String passConfirm = confirmPassword.getText().toString().trim();
        String code = validationCode.getText().toString().trim();

        if (mail.isEmpty() || pass.isEmpty() || passConfirm.isEmpty() || pass.isEmpty()) {
            Toasty.error(getActivity(), "Riempi tutti i campi", Toast.LENGTH_SHORT).show();

            isOk = false;
            return false;
        }

        if (!pass.equalsIgnoreCase(passConfirm)) {
            Toasty.error(getActivity(), "Le password non coincidono", Toast.LENGTH_SHORT).show();
            isOk = false;
            return false;
        }

        if(code.isEmpty()){
            Toasty.error(getActivity(), "Al momento è necessario un codice per registrarsi, contatta l'amministrazione di UbiQuo su Facebook per averne uno gratuitamente", Toast.LENGTH_SHORT).show();
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



    private void upDateDatabaseWithNewPlace(final String userId) {

        //prende l'immagine la comprime e tenta l'upload
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS", Context.MODE_PRIVATE);
        String imageUri = sharedPreferences.getString("PLACE_AVATAR", "NA");
        Uri avatar = Uri.parse(imageUri);
        final File compressedFile = Compressor.getDefault(getActivity()).compressToFile(new File(avatar.getPath()));
        //per rendere il path unico
        final String uniqueStoragePath = avatar.getLastPathSegment() + userId;
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Business_avatar");

        //inizializzatore di base per alcuni counter
        ((Registration) getActivity()).newBusiness.setContacts("NA");
        ((Registration) getActivity()).newBusiness.setPhones("NA");
        ((Registration) getActivity()).newBusiness.setRating(0);
        ((Registration) getActivity()).newBusiness.setLikes(0);
        ((Registration) getActivity()).newBusiness.setArguments("NA");
        ((Registration) getActivity()).newBusiness.setToken("no_token");
        if(((Registration)getActivity()).newBusiness.getId() == null ){
            ((Registration)getActivity()).newBusiness.setId(userId);
        }

        //aggiorna le shared preferences per far in modo che nella MainUserPage venga current_city sia sempre uguale a placeCity
        SharedPreferences preferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS", Context.MODE_PRIVATE);
        String placeCity = ((Registration) getActivity()).newBusiness.getCity();
        preferences.edit().putString("PLACE_CITY", placeCity).apply();


        storageReference.child(uniqueStoragePath).putFile(Uri.fromFile(compressedFile)).addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                String placeCity = ((Registration) getActivity()).newBusiness.getCity();
                ((Registration) getActivity()).newBusiness.setImage(downloadUrl);
                Long registrationTime = System.currentTimeMillis();
                ((Registration) getActivity()).newBusiness.setIscrizione(registrationTime);

                Business business = ((Registration) getActivity()).newBusiness;

                //Nodi nel quale fare l'upload
                DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference().child("Business").child(userId);
                DatabaseReference cityBusinessRef = FirebaseDatabase.getInstance().getReference().child("City-Businesses").child(placeCity).child(userId);
                businessRef.setValue(business);
                cityBusinessRef.setValue(business);
                dialog.dismiss();

                Intent toUserPage = new Intent(getActivity(), MainUserPage.class);
                getActivity().startActivity(toUserPage);

            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Problemi durante il caricamento dell'immagine", Toast.LENGTH_SHORT).show();
            }
        });








    }


}