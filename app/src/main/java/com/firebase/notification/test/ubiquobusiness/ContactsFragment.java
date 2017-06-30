package com.firebase.notification.test.ubiquobusiness;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
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
public class ContactsFragment extends Fragment {


    @BindView(R.id.firstContactName)
    EditText firstContactName;
    @BindView(R.id.firstContactNumber)
    EditText firstContactNumber;
    @BindView(R.id.secondContactName)
    EditText secondContactName;
    @BindView(R.id.secondContactNumber)
    EditText secondContactNumber;
    @BindView(R.id.thirdContactName)
    EditText thirdContactName;
    @BindView(R.id.thirdContactNumber)
    EditText thirdContactNumber;
    @BindView(R.id.confirmLayout)
    RelativeLayout confirmLayout;
    Unbinder unbinder;

    private ProgressDialog myProgressBar;
    private String editEventId,editEventCity;
    private Bundle proposal;
    public static Map<String,String> provinces = new HashMap<>();
    private ProgressDialog dialog;


    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance() {
        ContactsFragment newFragment = new ContactsFragment();
        return newFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_contacts, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        editEventId = ((CreateEvent)getActivity()).editEventIdString;
        editEventCity = ((CreateEvent) getActivity()).editEventCity;
        proposal = ((CreateEvent)getActivity()).proposal;


        //prepara Map delle province
        initProvinces();

        confirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //non in editMode e no proposal
                if (editEventId == null && proposal==null){
                    if(canGoNext()){
                        dialog = UbiQuoBusinessUtils.defaultProgressBar("Caricamento in corso",getActivity());
                        dialog.show();
                        updateStaticData();
                        submitEvent();
                    }
                }

                if(editEventId != null){
                    dialog = UbiQuoBusinessUtils.defaultProgressBar("Caricamento in corso",getActivity());
                    dialog.show();
                    updateStaticData();
                    editEvent(editEventId,editEventCity);
                }

                if(proposal != null){
                    //submitProposal(proposal.getString("id"));
                }
            }
        });



        return rootView;
    }

    private void submitEvent(){
        String city = ((CreateEvent)getActivity()).city;
        String organizerId = ((CreateEvent)getActivity()).organizerId;
        String image = ((CreateEvent)getActivity()).eventImageUri;
        String businessUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference dynamicReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(city);
        final DatabaseReference staticReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(city);
        final DatabaseReference mapReference = FirebaseDatabase.getInstance().getReference().child("MapData").child(city);
        final DatabaseReference BusinessEvents = FirebaseDatabase.getInstance().getReference().child("BusinessesEvents").child(city).child(businessUserId);
        StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("Events_Images");
        //chiave universale di riferimento
        final String pushId = BusinessEvents.push().getKey();

        //caricamento immagine
        Uri imageUri = Uri.parse(image);

        final File compressedFile = Compressor.getDefault(getActivity()).compressToFile(new File(imageUri.getPath()));



        imageReference.child(pushId).putFile(Uri.fromFile(compressedFile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    BusinessEvents.child(pushId).setValue(true);


                    DynamicData dynamicData = ((CreateEvent) getActivity()).getDynamicData();
                    StaticData staticData = ((CreateEvent) getActivity()).getStaticData();
                    MapInfo mapInfo = ((CreateEvent) getActivity()).mapInfo;
                    mapInfo.setReferenceKey(pushId);
                    dynamicData.setiPath(task.getResult().getDownloadUrl().toString());

                    dynamicReference.child(pushId).setValue(dynamicData);
                    staticReference.child(pushId).setValue(staticData);
                    mapReference.child(pushId).setValue(mapInfo);
                    dialog.dismiss();
                    Toasty.success(getActivity(), "Evento aggiunto con successo", Toast.LENGTH_SHORT, true).show();
                    getActivity().finish();
                }else{
                    Toasty.success(getActivity(), "Errore nel caricamento dell'evento", Toast.LENGTH_SHORT, true).show();

                }

            }


        });





    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void updateStaticData(){
        String first_name = firstContactName.getText().toString().trim();
        String second_name = secondContactName.getText().toString().trim();
        String third_name = thirdContactName.getText().toString().trim();
        String first_number = firstContactNumber.getText().toString().trim();
        String second_number = secondContactNumber.getText().toString().trim();
        String third_number = thirdContactNumber.getText().toString().trim();

        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> numbers = new ArrayList<>();

        if(!second_name.isEmpty() && !second_number.isEmpty()){
            names.add(second_name);
            numbers.add(second_number);
        }

        if(!first_name.isEmpty() && !first_number.isEmpty()){
            names.add(first_name);
            numbers.add(first_number);
        }

        if(!third_name.isEmpty() && !third_number.isEmpty()){
            names.add(third_name);
            numbers.add(third_number);
        }

        ((CreateEvent)getActivity()).getStaticData().setNames(names);
        ((CreateEvent)getActivity()).getStaticData().setNumbers(numbers);

    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;
        String first_name = firstContactName.getText().toString().trim();
        String second_name = secondContactName.getText().toString().trim();
        String third_name = thirdContactName.getText().toString().trim();
        String first_number = firstContactNumber.getText().toString().trim();
        String second_number = secondContactNumber.getText().toString().trim();
        String third_number = thirdContactNumber.getText().toString().trim();

        if(first_name.isEmpty() && second_name.isEmpty() & third_name.isEmpty() && first_number.isEmpty() && second_number.isEmpty() && third_number.isEmpty()){
            Toasty.error(getActivity(),"Inserisci almeno un contatto telefonico", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(first_name.isEmpty() && !first_number.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il nome del secondo contatto", Toast.LENGTH_SHORT,true).show();
            return false;
        }
        if(!first_name.isEmpty() && first_number.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il numero del secondo contatto", Toast.LENGTH_SHORT,true).show();
            return false;
        }



        if(second_name.isEmpty() && !second_number.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il nome del primo contatto", Toast.LENGTH_SHORT,true).show();
            return false;
        }
        if(!second_name.isEmpty() && second_number.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il numero del primo contatto", Toast.LENGTH_SHORT,true).show();
            return false;
        }



        if(third_name.isEmpty() && !third_number.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il nome del terzo contatto", Toast.LENGTH_SHORT,true).show();
            return false;
        }
        if(!third_name.isEmpty() && third_number.isEmpty()){
            Toasty.error(getActivity(),"Inserisci il numero del terzo contatto", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        return canGoNext;
    }

    private void editEvent(String eventId,String editEventCity){
        String city = ((CreateEvent)getActivity()).city;
        String image = ((CreateEvent)getActivity()).eventImageUri;
        String businessUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference dynamicReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(city);
        final DatabaseReference staticReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(city);
        final DatabaseReference mapReference = FirebaseDatabase.getInstance().getReference().child("MapData").child(city);
        final DatabaseReference BusinessEvents = FirebaseDatabase.getInstance().getReference().child("BusinessesEvents").child(city).child(businessUserId);
        StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("Events_Images");
        //chiave universale di riferimento
        final String pushId = eventId;


        if(image != null) {
            //se l'immagine è stata cambiata allora questa stringa non è null
            Uri imageUri = Uri.parse(image);

            final File compressedFile = Compressor.getDefault(getActivity()).compressToFile(new File(imageUri.getPath()));


            imageReference.child(pushId).putFile(Uri.fromFile(compressedFile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        BusinessEvents.child(pushId).setValue(true);


                        DynamicData dynamicData = ((CreateEvent) getActivity()).getDynamicData();
                        StaticData staticData = ((CreateEvent) getActivity()).getStaticData();
                        MapInfo mapInfo = ((CreateEvent) getActivity()).mapInfo;
                        mapInfo.setReferenceKey(pushId);
                        dynamicData.setiPath(task.getResult().getDownloadUrl().toString());

                        dynamicReference.child(pushId).setValue(dynamicData);
                        staticReference.child(pushId).setValue(staticData);
                        mapReference.child(pushId).setValue(mapInfo);
                        dialog.dismiss();
                        Toasty.success(getActivity(), "Evento modificato con successo", Toast.LENGTH_SHORT, true).show();
                        getActivity().finish();
                    } else {
                        Toasty.success(getActivity(), "Errore nel caricamento dell'evento", Toast.LENGTH_SHORT, true).show();

                    }

                }
            });
        }else{
            BusinessEvents.child(pushId).setValue(true);
            DynamicData dynamicData = ((CreateEvent) getActivity()).getDynamicData();
            StaticData staticData = ((CreateEvent) getActivity()).getStaticData();
            MapInfo mapInfo = ((CreateEvent) getActivity()).mapInfo;
            mapInfo.setReferenceKey(pushId);

            dynamicReference.child(pushId).setValue(dynamicData);
            staticReference.child(pushId).setValue(staticData);
            mapReference.child(pushId).setValue(mapInfo);
            dialog.dismiss();
            Toasty.success(getActivity(), "Evento modificato con successo", Toast.LENGTH_SHORT, true).show();
            getActivity().finish();
        }
    }


    private void initProvinces() {
        provinces.put("AG", "Agrigento");
        provinces.put("AL", "Alessandria");
        provinces.put("AN", "Ancona");
        provinces.put("AO", "Aosta");
        provinces.put("AR", "Arezzo");
        provinces.put("AP", "Ascoli Piceno");
        provinces.put("AT", "Asti");
        provinces.put("AG", "Agrigento");
        provinces.put("AV", "Avellino");
        provinces.put("BA", "Bari");
        provinces.put("BT", "Barletta-Andria-Trani");
        provinces.put("BL", "Belluno");
        provinces.put("BN", "Benevento");
        provinces.put("BG", "Bergamo");
        provinces.put("BI", "Biella");
        provinces.put("BO", "Bologna");
        provinces.put("BZ", "Bolzano");
        provinces.put("BS", "Brescia");
        provinces.put("BR", "Brindisi");
        provinces.put("CA", "Cagliari");
        provinces.put("CL", "Caltanissetta");
        provinces.put("CB", "Campobasso");
        provinces.put("CI", "Carbonia-Iglesias");
        provinces.put("CE", "Caserta");
        provinces.put("CT", "Catania");
        provinces.put("CZ", "Catanzaro");
        provinces.put("CH", "Chieti");
        provinces.put("CO", "Como");
        provinces.put("CS", "Cosenza");
        provinces.put("CR", "Cremona");
        provinces.put("KR", "Crotone");
        provinces.put("CN", "Cuneo");
        provinces.put("EN", "Enna");
        provinces.put("FM", "Fermo");
        provinces.put("FE", "Ferrara");
        provinces.put("FI", "Firenze");
        provinces.put("FG", "Foggia");
        provinces.put("FC", "Forlì-Cesena");
        provinces.put("FR", "Frosinone");
        provinces.put("GE", "Genova");
        provinces.put("GO", "Gorizia");
        provinces.put("GR", "Grosseto");
        provinces.put("IM", "Imperia");
        provinces.put("IS", "Isernia");
        provinces.put("SP", "La Spezia");
        provinces.put("AQ", "L'Aquila");
        provinces.put("LT", "Latina");
        provinces.put("LE", "Lecce");
        provinces.put("LC", "Lecco");
        provinces.put("LI", "Livorno");
        provinces.put("LO", "Lodi");
        provinces.put("LU", "Lucca");
        provinces.put("MC", "Macerata");
        provinces.put("MN", "Mantova");
        provinces.put("MS", "Massa Carrara");
        provinces.put("MT", "Matera");
        provinces.put("VS", "Medio Campidano");
        provinces.put("ME", "Messina");
        provinces.put("MI", "Milano");
        provinces.put("MO", "Modena");
        provinces.put("MB", "Monza e Brianza");
        provinces.put("NA", "Napoli");
        provinces.put("NO", "Novara");
        provinces.put("NU", "Nuoro");
        provinces.put("OG", "Ogliastra");
        provinces.put("OT", "Olbia-Tempio");
        provinces.put("OR", "Oristano");
        provinces.put("PD", "Padova");
        provinces.put("PA", "Palermo");
        provinces.put("PR", "Parma");
        provinces.put("PV", "Pavia");
        provinces.put("PG", "Perugia");
        provinces.put("PU", "Pesaro Urbino");
        provinces.put("PE", "Pescara");
        provinces.put("PC", "Piacenza");
        provinces.put("PI", "Pisa");
        provinces.put("PT", "Pistoia");
        provinces.put("PN", "Pordenone");
        provinces.put("PZ", "Potenza");
        provinces.put("PO", "Prato");
        provinces.put("RG", "Ragusa");
        provinces.put("RA", "Ravenna");
        provinces.put("RC", "Reggio Calabria");
        provinces.put("RE", "Reggio Emilia");
        provinces.put("RI", "Rieti");
        provinces.put("RN", "Rimini");
        provinces.put("RM", "Roma");
        provinces.put("RO", "Rovigo");
        provinces.put("SA", "Salerno");
        provinces.put("SS", "Sassari");
        provinces.put("SV", "Savona");
        provinces.put("SI", "Siena");
        provinces.put("SO", "Sondrio");
        provinces.put("SR", "Siracusa");
        provinces.put("TA", "Taranto");
        provinces.put("TE", "Teramo");
        provinces.put("TR", "Terni");
        provinces.put("TP", "Trapani");
        provinces.put("TN", "Trentino");
        provinces.put("TV", "Treviso");
        provinces.put("TS", "Trieste");
        provinces.put("TO", "Torino");
        provinces.put("UD", "Udine");
        provinces.put("VA", "Varese");
        provinces.put("VE", "Venezia");
        provinces.put("VB", "Verbano-Cusio-Ossola");
        provinces.put("VE", "Vercelli");
        provinces.put("VR", "Verona");
        provinces.put("VV", "Vibo Valentia");
        provinces.put("VI", "Vicenza");
        provinces.put("VT", "Viterbo");
    }

    }
