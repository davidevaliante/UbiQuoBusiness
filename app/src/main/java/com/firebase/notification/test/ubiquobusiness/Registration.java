package com.firebase.notification.test.ubiquobusiness;

import android.content.Intent;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindFloat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class Registration extends AppCompatActivity {
    @BindView(R.id.registration_email) EditText emailField;
    @BindView(R.id.registration_password) EditText passwordField;
    @BindView(R.id.registrationButton) Button registrationButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        mAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    Intent toUserPage = new Intent(Registration.this, MainUserPage.class);
                    startActivity(toUserPage);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInWithEmailAndPassword(emailField.getText().toString().trim(),passwordField.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(Registration.this, "Registrazione effettuata", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(Registration.this, "Registrazione fallita", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(authStateListener);
    }
}
