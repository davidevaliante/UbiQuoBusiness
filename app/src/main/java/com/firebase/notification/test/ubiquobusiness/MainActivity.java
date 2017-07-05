package com.firebase.notification.test.ubiquobusiness;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.emailField) EditText emailField;
    @BindView(R.id.passwordField) EditText passwordField;
    @BindView(R.id.signIn) Button signInButton;
    @BindView(R.id.signUp) Button signUpButton;
    private FirebaseAuth myAuth;
    private FirebaseAuth.AuthStateListener myAuthStateListener;
    private FirebaseUser currentUser;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);



        myAuth = FirebaseAuth.getInstance();
        dialog = UbiQuoBusinessUtils.defaultProgressBar("Login in corso",this);
        
        myAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() !=null){
                    final SharedPreferences prefs = getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
                    DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference().child("Business");
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    if(id != null){
                        businessRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Business business = dataSnapshot.getValue(Business.class);
                                prefs.edit().putString("PLACE_CITY",business.getCity()).apply();
                                Log.d("User logged In with Id:", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                Intent toMainUserPage = new Intent(MainActivity.this,MainUserPage.class);
                                startActivity(toMainUserPage);
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }


                }

            }
        };
        myAuth.addAuthStateListener(myAuthStateListener);

        UbiQuoBusinessUtils.removeStatusBar(this);

        //utente già loggato
        if(myAuth.getCurrentUser() != null){
            Intent toUserPage = new Intent(MainActivity.this,MainUserPage.class);
            startActivity(toUserPage);
        }else{
        }

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegistrationPage = new Intent(MainActivity.this,Registration.class);
                startActivity(toRegistrationPage);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                loginAttempt();
            }
        });

        //inizializzatore Shared Preferences
        Context context = this;
        SharedPreferences sharedPreferences = this.getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);

        loadLoginData(sharedPreferences);

    }

    private void loginAttempt(){
        final String userMail = emailField.getText().toString().trim();
        final String userPassword = passwordField.getText().toString().trim();
        if(!userMail.isEmpty() && !userPassword.isEmpty()){
            myAuth.signInWithEmailAndPassword(userMail,userPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    SharedPreferences sharedPreferences = getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("PLACE_MAIL",userMail);
                    editor.putString("PLACE_PASS",userPassword);
                    editor.apply();
                    dialog.dismiss();
                    Toasty.success(MainActivity.this,"Login effettuato !",Toast.LENGTH_SHORT,true).show();

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toasty.error(MainActivity.this,"Login fallito !",Toast.LENGTH_SHORT,true).show();


                }
            });
        }else{
            Toasty.error(MainActivity.this,"Riempi tutti i campi e riprova", Toast.LENGTH_SHORT,true).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myAuth.removeAuthStateListener(myAuthStateListener);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void loadLoginData(final SharedPreferences prefs){
        String userMail = prefs.getString("PLACE_MAIL","NA");
        String userPass = prefs.getString("PLACE_PASS","NA");
        String city = prefs.getString("PLACE_CITY","NA");
        if(!userMail.equalsIgnoreCase("NA")){
            emailField.setText(userMail);
        }

        if(!userPass.equalsIgnoreCase("NA")){
            passwordField.setText(userPass);
        }

        if(city.equalsIgnoreCase("NA")){
            DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference().child("Business");

            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                businessRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Business business = dataSnapshot.getValue(Business.class);
                        prefs.edit().putString("PLACE_CITY",business.getCity()).apply();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }




    }

}
