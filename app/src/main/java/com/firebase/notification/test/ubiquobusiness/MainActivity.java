package com.firebase.notification.test.ubiquobusiness;

import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.haha.perflib.Main;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.emailField) EditText emailField;
    @BindView(R.id.passwordField) EditText passwordField;
    @BindView(R.id.signIn) Button signInButton;
    @BindView(R.id.signUp) Button signUpButton;
    private FirebaseAuth myAuth;
    private FirebaseAuth.AuthStateListener myAuthStateListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        myAuth = FirebaseAuth.getInstance();
        
        myAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() !=null){
                    Log.d("User logged In with Id:", firebaseAuth.getCurrentUser().getUid());
                    Intent toMainUserPage = new Intent(MainActivity.this,MainUserPage.class);
                    startActivity(toMainUserPage);
                }

            }
        };
        myAuth.addAuthStateListener(myAuthStateListener);

        //rende la statusbar completamente invisibile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        //utente già loggato
        if(myAuth.getCurrentUser() != null){
            Intent toUserPage = new Intent(MainActivity.this,MainUserPage.class);
            startActivity(toUserPage);
        }else{
            Toast.makeText(this, "user not logged in yet", Toast.LENGTH_SHORT).show();
        }

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegistrationPage = new Intent(MainActivity.this,Registration.class);
                startActivity(toRegistrationPage);
            }
        });


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
}
