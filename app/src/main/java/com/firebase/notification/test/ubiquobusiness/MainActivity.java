package com.firebase.notification.test.ubiquobusiness;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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

    /*dove vedete scritto @BindView è perchè utilizzo una libreria che sia chiama Butterknife */
    @BindView(R.id.emailField)
    EditText emailField;
    @BindView(R.id.passwordField)
    EditText passwordField;
    @BindView(R.id.signIn)
    Button signInButton;
    @BindView(R.id.signUp)
    Button signUpButton;
    @BindView(R.id.app_name)
    TextView appName;


    //autenticatore di Firebase
    private FirebaseAuth myAuth;
    //una barra di caricamento di default
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setta l'xml per questa activity
        setContentView(R.layout.activity_main);


        //la libreria che setta la roba nel file xml come variabili di classe in automatico
        ButterKnife.bind(this);

        appName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //inizializzo l'autenticatore
        myAuth = FirebaseAuth.getInstance();
        //opzionale, setto già il messaggio che voglio far comparire nella finestra di caricamento
        dialog = UbiQuoBusinessUtils.defaultProgressBar("Login in corso", this);

        //opzionale, è un metodo che rimuove la barra che sta in alto solo per rendere la pagina a tutto schermo
        UbiQuoBusinessUtils.removeStatusBar(this);

        //Prima di fare qualunque cosa, controllo se l'utente è loggato, in quel caso lo mando alla userpage
        if (myAuth.getCurrentUser() != null) {
            Intent toUserPage = new Intent(MainActivity.this, MainUserPage.class);
            startActivity(toUserPage);
        } else {
            //se l'utente non è loggato rimane su questa pagina
        }

        //prova a loggare con email e password, il metodo sta sotto
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                loginAttempt();
            }
        });


        /*alla registrazione visto che dobbiamo registrare 3 utenti diversi apre una finestra
         (che è un fragment) che permette di scegliere il tipo di utente da registrare
        */
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TypePickerDialog typePickerDialog = TypePickerDialog.newInstance();
                typePickerDialog.show(getSupportFragmentManager(), "type_picker_dialog");
            }
        });

        /*le shared Preferences sono un contenitore (è un database SQLite) di dati riservati
          all'utente che "vivono" solo sul suo telefono di solito si tengono registrate cose
          come i dati di login  cazzi e mazzo
        */
        SharedPreferences sharedPreferences = this.getSharedPreferences("UBIQUO_BUSINESS", Context.MODE_PRIVATE);

        //prova a caricare mail e pass se l'utente li ha gia inseriti, il metodo sta sotto
        loadLoginData(sharedPreferences);

    }

    /*
    * Questo metodo triggera quando viene premuto il pulstante "Accedi"
    * */
    private void loginAttempt() {
        //legge mail e password scritti dall'utente (o caricati dalle Shared Preferences
        final String userMail = emailField.getText().toString().trim();
        final String userPassword = passwordField.getText().toString().trim();

        //se non sono nulle
        if (!userMail.isEmpty() && !userPassword.isEmpty()) {

            /*prova ad effettuare il login, notate che c'è un "addOnSuccessListener" e un "FailureListener"
              che mi permette di fare qualcosa se i dati di autenticazione sono corretti
            */
            myAuth.signInWithEmailAndPassword(userMail, userPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    /*qui dentro sono sicuro che l'utente è correttamente loggato e quindi
                      le sue credenziali sono corrette, perciò le voglio salvare nelle sue shared preferences
                    */

                    /*costruisco un oggetto SharedPreferences, devo solo passare un nome unico
                     (solitamente quello dell'app), Context.MODE_PRIVATE è il metodo di lettura e si usa
                      sempre questo onestamente

                    */
                    SharedPreferences sharedPreferences = getSharedPreferences("UBIQUO_BUSINESS", Context.MODE_PRIVATE);

                    //per scrivere nelle shared preferences serve un oggetto che si chiama editor e lo creo
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    //inserisco la mail nelle shared preferences
                    editor.putString("PLACE_MAIL", userMail);

                    //inserisco la password nelle sharedpreferences
                    editor.putString("PLACE_PASS", userPassword);

/*
                    chiamo apply() sull'editor per salvare sta roba*/
                    editor.apply();

/*
                    chiudo la finestra che dice "attendere prego"
*/
                    dialog.dismiss();

/*
                    dico all'utente che è loggato*/
                    Toasty.success(MainActivity.this, "Login effettuato !", Toast.LENGTH_SHORT, true).show();

/*
                    finalemente lo mando alla user page*/
                    Intent toUserPage = new Intent(MainActivity.this, MainUserPage.class);
                    startActivity(toUserPage);

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

/*
                    qui dentro invece qualcosa è andato storto, (mail sbagliata pass sbagliata
                    poca connessione o semplicemente utente non registrato, quindi chiudo
                    "attendere prego" e dico che il login è fallito
*/
                    dialog.dismiss();
                    Toasty.error(MainActivity.this, "Login fallito !", Toast.LENGTH_SHORT, true).show();


                }
            });
        } else {

            /*Questo è collegato all'if iniziale, cioè mail e password non devono essere null, se lo sono
            * chiedo di riepire tutti i campi e riprovare
            * */
            Toasty.error(MainActivity.this, "Riempi tutti i campi e riprova", Toast.LENGTH_SHORT, true).show();
        }
    }


    //questo è un metodo che serveper una libreria che rende facile cambiare Font alla roba (è molto utile)
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    /*questo è il metodo che carica la roba di login se è stata salvata (in generale, basta un login effettuato
    * con successo, guardate il metodo di login sopra
    * */
    private void loadLoginData(final SharedPreferences prefs) {
        /*prefs è stato inizializzato in OnCreate e viene passato al metodo*/

        /*quando si leggono gli oggetti dentro le shared preferences si passa il nome della "chiave"
        * che abbiamo messo nel metodo di login
        * ed un valore di default che in questo caso ho messo come "NA" tipo "not avaible" in modo
        * che non da nessuna NullPointerException ma allo stesso tempo so che non ho trovato un cazzo
        * */
        String userMail = prefs.getString("PLACE_MAIL", "NA");
        String userPass = prefs.getString("PLACE_PASS", "NA");
        String city = prefs.getString("PLACE_CITY", "NA");

        //se la mail è stata salvata la mette già nel campo della mail
        if (!userMail.equalsIgnoreCase("NA")) {
            emailField.setText(userMail);
        }

        //stessa cosa per la password
        if (!userPass.equalsIgnoreCase("NA")) {
            passwordField.setText(userPass);
        }


        //questo pezzo lasciatelo stare, va scritto meglio ma in generale dovrebbe assicurarsi che sia
        //stata scelta una città
        if (city.equalsIgnoreCase("NA")) {
            DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference().child("Business");

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                businessRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Business business = dataSnapshot.getValue(Business.class);
                        prefs.edit().putString("PLACE_CITY", business.getCity()).apply();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }


    }

}
