package com.firebase.notification.test.ubiquobusiness;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
    private String editString;
    private Bundle proposal;
    public static Map<String,String> provinces = new HashMap<>();


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

        editString = ((CreateEvent)getActivity()).editEventIdString;
        proposal = ((CreateEvent)getActivity()).proposal;

        //prepara Map delle province
        initProvinces();

        confirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editString == null){
                    if (canSubmit()) {
                        submitEvent();
                    }
                }else{
                    if(canEdit()){
                        editEvent();
                    }
                }
            }
        });

        if(editString != null){
            loadContacts();
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private  Boolean canSubmit(){
        Boolean canSubmit = true;
        String firstName = firstContactName.getText().toString().trim();
        String secondName = secondContactName.getText().toString().trim();
        String thirdName = thirdContactName.getText().toString().trim();

        String firstContact = firstContactNumber.getText().toString().trim();
        String secondContact = secondContactNumber.getText().toString().trim();
        String thirdContact = thirdContactNumber.getText().toString().trim();


        if(firstName.isEmpty() && secondName.isEmpty() && thirdName.isEmpty()){
            Toasty.error(getActivity(),"Inserisci almeno un contatto", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(firstContact.isEmpty() && secondContact.isEmpty() && thirdContact.isEmpty()){
            Toasty.error(getActivity(),"Inserisci almeno un numero", Toast.LENGTH_SHORT).show();
            return false;
        }

        return canSubmit;
    }




    private void submitEvent(){
        //prima di tutto si stabilisce se è un evento ex novo oppure derivato da una proposta
        final Bundle bundle = ((CreateEvent)getActivity()).proposal;
        //progressBar
        myProgressBar = new ProgressDialog(getActivity(),android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        myProgressBar.setMessage("Caricamento in corso");

        myProgressBar.show();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
        String date = sharedPreferences.getString("EVENT_DATE_STRING","NA");
        final String time = sharedPreferences.getString("EVENT_START_TIME","NA");
        final String organizer = sharedPreferences.getString("EVENT_ORGANIZER","NA");
        final String city = sharedPreferences.getString("EVENT_CITY","NA");
        final String adress = sharedPreferences.getString("EVENT_ADRESS","NA");
        final String id = sharedPreferences.getString("EVENT_ORGANIZER_ID","NA");


        final Double latitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"EVENT_LATITUDE",0.0);
        final Double longitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"EVENT_LONGITUDE",0.0);
        final Long timeMillies = UbiQuoBusinessUtils.getTimeMillis(date,time);

        String image = sharedPreferences.getString("EVENT_IMAGE","NA");
        final String title = sharedPreferences.getString("EVENT_TITLE","NA");
        final String desc = sharedPreferences.getString("EVENT_DESCRIPTION","NA");
        final Boolean isFree = sharedPreferences.getBoolean("EVENT_IS_FREE",true);
        final Integer price = sharedPreferences.getInt("EVENT_PRICE",0);


        final String firstName = firstContactName.getText().toString().trim();
        final String secondName = secondContactName.getText().toString().trim();
        final String thirdName = thirdContactName.getText().toString().trim();

        final String firstContact = firstContactNumber.getText().toString().trim();
        final String secondContact = secondContactNumber.getText().toString().trim();
        final String thirdContact = thirdContactNumber.getText().toString().trim();

        SharedPreferences userPref = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
        final String phone = userPref.getString("PLACE_PHONE","NA");
        final String place_id = userPref.getString("PLACE_ID","NA");
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();


        //caricamento immagine
        Uri imageUri = Uri.parse(image);
        String imagePath = imageUri.getLastPathSegment()+organizer;

        final File compressedFile = Compressor.getDefault(getActivity()).compressToFile(new File(imageUri.getPath()));

        StorageReference myReference = FirebaseStorage.getInstance().getReference().child("Event_Images").child(imagePath);

        final float floatPrice = (float)price;

        myReference.putFile(Uri.fromFile(compressedFile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isComplete() && task.isSuccessful()){

                    //non creato a partire da una proposta
                    if(bundle == null) {
                        //dati dinamici
                        String downloadUrl = task.getResult().getDownloadUrl().toString();
                        DynamicData newDinamicData = new DynamicData(0, 0, 0, 0, 0, 0, timeMillies, floatPrice, title, downloadUrl, organizer, isFree, 0);
                        DatabaseReference pushRef = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(city).push();
                        String pushId = pushRef.getKey();

                        pushRef.setValue(newDinamicData);

                        ArrayList<String> names = new ArrayList<>();
                        ArrayList<String> numbers = new ArrayList<>();
                        if (!phone.equalsIgnoreCase("NA")) {
                            names.add(UbiQuoBusinessUtils.capitalize(organizer));
                            numbers.add(phone);
                        }

                        if (!firstName.isEmpty() && !firstContact.isEmpty()) {
                            names.add(UbiQuoBusinessUtils.capitalize(firstName));
                            numbers.add(firstContact);
                        }
                        if (!secondName.isEmpty() && !secondContact.isEmpty()) {
                            names.add(UbiQuoBusinessUtils.capitalize(secondName));
                            numbers.add(secondContact);
                        }
                        if (!thirdName.isEmpty() && !thirdContact.isEmpty()) {
                            names.add(UbiQuoBusinessUtils.capitalize(thirdName));
                            numbers.add(thirdContact);
                        }

                        //dati statici
                        StaticData newStaticData = new StaticData(desc, names, numbers);
                        DatabaseReference staticRef = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(city);
                        staticRef.child(pushId).setValue(newStaticData);

                        MapInfo newMapInfo = new MapInfo(latitude, longitude, organizer, id, title, floatPrice, phone, 0, timeMillies, adress, pushId);
                        DatabaseReference mapReference = FirebaseDatabase.getInstance().getReference().child("MapData").child(city).child(pushId);
                        mapReference.setValue(newMapInfo);

                        FirebaseDatabase.getInstance().getReference().child("BusinessesEvents").child(city).child(userId).child(pushId).setValue(true);

                        Toasty.success(getActivity(), "Evento aggiunto con successo !", Toast.LENGTH_SHORT, true).show();
                        getActivity().getSharedPreferences("LAST_EVENT_DATA", 0).edit().clear().commit();
                        myProgressBar.dismiss();
                        getActivity().finish();


                    }else{

                        //creato a partire da una proposta
                        final String proposalId = bundle.getString("id", "NA");
                        //dati dinamici
                        String downloadUrl = task.getResult().getDownloadUrl().toString();
                        DynamicData newDinamicData = new DynamicData(0, 0, 0, 0, 0, 0, timeMillies, floatPrice, title, downloadUrl, organizer, isFree, 0);
                        String pushId = proposalId;
                        DatabaseReference pushRef = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(city).child(proposalId);


                        pushRef.setValue(newDinamicData);

                        ArrayList<String> names = new ArrayList<>();
                        ArrayList<String> numbers = new ArrayList<>();
                        if (!phone.equalsIgnoreCase("NA")) {
                            names.add(UbiQuoBusinessUtils.capitalize(organizer));
                            numbers.add(phone);
                        }

                        if (!firstName.isEmpty() && !firstContact.isEmpty()) {
                            names.add(UbiQuoBusinessUtils.capitalize(firstName));
                            numbers.add(firstContact);
                        }
                        if (!secondName.isEmpty() && !secondContact.isEmpty()) {
                            names.add(UbiQuoBusinessUtils.capitalize(secondName));
                            numbers.add(secondContact);
                        }
                        if (!thirdName.isEmpty() && !thirdContact.isEmpty()) {
                            names.add(UbiQuoBusinessUtils.capitalize(thirdName));
                            numbers.add(thirdContact);
                        }

                        //dati statici
                        StaticData newStaticData = new StaticData(desc, names, numbers);
                        DatabaseReference staticRef = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(city);
                        staticRef.child(pushId).setValue(newStaticData);

                        MapInfo newMapInfo = new MapInfo(latitude, longitude, organizer, id, title, floatPrice, phone, 0, timeMillies, adress, pushId);
                        DatabaseReference mapReference = FirebaseDatabase.getInstance().getReference().child("MapData").child(city).child(pushId);
                        mapReference.setValue(newMapInfo);

                        FirebaseDatabase.getInstance().getReference().child("BusinessesEvents").child(city).child(place_id).child(pushId).setValue(true);


                        Toasty.success(getActivity(), "Evento aggiunto con successo !", Toast.LENGTH_SHORT, true).show();
                        getActivity().getSharedPreferences("LAST_EVENT_DATA", 0).edit().clear().commit();
                        ProposalBuiltPush pushNotification = new ProposalBuiltPush(proposalId, FirebaseAuth.getInstance().getCurrentUser().getUid(),organizer);
                        FirebaseDatabase.getInstance().getReference().child("NotificationForProposal").child(city).child(proposalId).setValue(pushNotification);
                        myProgressBar.dismiss();
                        getActivity().finish();
                    }
                }
                if (!task.isSuccessful()){
                    Toasty.error(getActivity(),"C'è stato un errore durante il caricamento del nuovo evento",Toast.LENGTH_SHORT,true).show();
                }
            }
        });

    }

    private void editEvent(){
        String event_key = editString;
        if(!event_key.isEmpty()){
            SharedPreferences editPreferences = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
            String city = editPreferences.getString("EDIT_CITY","NA");
            DatabaseReference dynamicReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(city).child(event_key);

            //Dati dinamici
            Long date = editPreferences.getLong("EDIT_DATE",0);
            Float price = editPreferences.getFloat("EDIT_PRICE",0.0f);
            String title = editPreferences.getString("EDIT_TITLE","NA");
            String image = editPreferences.getString("EDIT_IMAGE","NA");
            String placeName = editPreferences.getString("EDIT_ORGANIZER","NA");
            Boolean isFree = editPreferences.getBoolean("EDIT_ISFREE",true);
            DynamicData newDynamicData = new DynamicData(0,0,0,0,0,0,date,price,title,image,placeName,isFree,0);
            dynamicReference.setValue(newDynamicData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toasty.success(getActivity(),"Success !",Toast.LENGTH_SHORT,true).show();
                }
            });

            //dati statici
            DatabaseReference staticReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(city).child(event_key);
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> numbers = new ArrayList<>();
            String desc = editPreferences.getString("EDIT_DESC","NA");

            String name_one = editPreferences.getString("EDIT_CONTACT_1","NA");
            String number_one = editPreferences.getString("EDIT_NUMBER_1","NA");

            //switchati perchè nel layout il secondo viene prima
            if(!name_one.equalsIgnoreCase("NA") && !number_one.equalsIgnoreCase("NA")){
                names.add(name_one);
                numbers.add(number_one);
            }

            String name_two = editPreferences.getString("EDIT_CONTACT_2","NA");
            String number_two = editPreferences.getString("EDIT_NUMBER_2","NA");
            if(!name_two.equalsIgnoreCase("NA") && !number_two.equalsIgnoreCase("NA")){
                names.add(name_two);
                numbers.add(number_two);
            }

            String name_three = editPreferences.getString("EDIT_CONTACT_3","NA");
            String number_three = editPreferences.getString("EDIT_NUMBER_3","NA");
            if(!name_three.equalsIgnoreCase("NA") && !number_three.equalsIgnoreCase("NA")){
                names.add(name_three);
                numbers.add(number_three);
            }

            StaticData newStaticData = new StaticData(desc,names,numbers);
            staticReference.setValue(newStaticData);

            //dati mappa
            DatabaseReference mapReference = FirebaseDatabase.getInstance().getReference().child("MapData").child(city).child(event_key);

            Double lat = UbiQuoBusinessUtils.getDoubleFromEditor(editPreferences,"EDIT_LAT",0.0);
            Double lng = UbiQuoBusinessUtils.getDoubleFromEditor(editPreferences,"EDIT_LNG",0.0);
            String organizer_id = editPreferences.getString("EDIT_ORGANIZER_ID","NA");
            String phone = editPreferences.getString("EDIT_NUMBER_0","Non disponibile");
            String adress = editPreferences.getString("EDIT_ADRESS","Non disponibile");
            MapInfo newMapInfo = new MapInfo(lat,lng,placeName,organizer_id,title,price,phone,0,date,adress,event_key);
            mapReference.setValue(newMapInfo);

        }else{
            Toasty.error(getActivity(),"Errore del server, riprova", Toast.LENGTH_SHORT,true).show();
        }
    }

    private Boolean canEdit(){
        Boolean canEdit = true;
        SharedPreferences editor = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
        String name_one = secondContactName.getText().toString().trim();
        String name_two = firstContactName.getText().toString().trim();
        String name_three = thirdContactName.getText().toString().trim();
        String number_one = secondContactNumber.getText().toString().trim();
        String number_two = firstContactNumber.getText().toString().trim();
        String number_three = thirdContactNumber.getText().toString().trim();

        if(!name_one.isEmpty() && !number_one.isEmpty()){
            editor.edit().putString("EDIT_CONTACT_1",name_one).commit();
            editor.edit().putString("EDIT_NUMBER_1",number_one).commit();
        }

        if(!name_two.isEmpty() && !number_two.isEmpty()){
            editor.edit().putString("EDIT_CONTACT_2",name_two).commit();
            editor.edit().putString("EDIT_NUMBER_2",number_two).commit();
        }

        if(!name_three.isEmpty() && !name_three.isEmpty()){
            editor.edit().putString("EDIT_CONTACT_3",name_three).commit();
            editor.edit().putString("EDIT_NUMBER_3",number_three).commit();
        }

        return canEdit;
    }

    private void loadContacts(){
        SharedPreferences editor = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
        String name_one = editor.getString("EDIT_CONTACT_1","NA");
        String number_one = editor.getString("EDIT_NUMBER_1","NA");

        //switchati perchè nel layout il secondo viene prima
        if(!name_one.equalsIgnoreCase("NA") && !number_one.equalsIgnoreCase("NA")){
            secondContactName.setText(name_one);
            secondContactNumber.setText(number_one);
        }

        String name_two = editor.getString("EDIT_CONTACT_2","NA");
        String number_two = editor.getString("EDIT_NUMBER_2","NA");
        if(!name_two.equalsIgnoreCase("NA") && !number_two.equalsIgnoreCase("NA")){
            firstContactName.setText(name_two);
            firstContactNumber.setText(number_two);
        }

        String name_three = editor.getString("EDIT_CONTACT_3","NA");
        String number_three = editor.getString("EDIT_NUMBER_3","NA");
        if(!name_three.equalsIgnoreCase("NA") && !number_three.equalsIgnoreCase("NA")){
            thirdContactName.setText(name_three);
            thirdContactNumber.setText(number_three);
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
