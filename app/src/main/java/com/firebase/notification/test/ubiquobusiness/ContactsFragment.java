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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

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

        confirmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canSubmit()) {
                    submitEvent();
                }
            }
        });
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

                    //dati dinamici
                    String downloadUrl = task.getResult().getDownloadUrl().toString();
                    DynamicData newDinamicData = new DynamicData(0,0,0,0,0,0,timeMillies,floatPrice,title,downloadUrl,organizer,isFree,0);
                    DatabaseReference pushRef = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(city).push();
                    String pushId = pushRef.getKey();

                    pushRef.setValue(newDinamicData);

                     ArrayList<String> names = new ArrayList<>();
                     ArrayList<String> numbers = new ArrayList<>();
                    if(!phone.equalsIgnoreCase("NA")){
                        names.add(UbiQuoBusinessUtils.capitalize(organizer));
                        numbers.add(phone);
                    }

                    if(!firstName.isEmpty() && !firstContact.isEmpty()){
                        names.add(UbiQuoBusinessUtils.capitalize(firstName));
                        numbers.add(firstContact);
                    }
                    if(!secondName.isEmpty() && !secondContact.isEmpty()){
                        names.add(UbiQuoBusinessUtils.capitalize(secondName));
                        numbers.add(secondContact);
                    }
                    if(!thirdName.isEmpty() && !thirdContact.isEmpty()){
                        names.add(UbiQuoBusinessUtils.capitalize(thirdName));
                        numbers.add(thirdContact);
                    }

                    //dati statici
                    StaticData newStaticData = new StaticData(desc,names,numbers);
                    DatabaseReference staticRef = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(city);
                    staticRef.child(pushId).setValue(newStaticData);

                    MapInfo newMapInfo = new MapInfo(latitude,longitude,organizer,id,title,floatPrice,phone,0,timeMillies,adress,pushId);
                    DatabaseReference mapReference = FirebaseDatabase.getInstance().getReference().child("MapData").child(city).child(pushId);
                    mapReference.setValue(newMapInfo);

                    Toasty.success(getActivity(),"Evento aggiunto con successo !",Toast.LENGTH_SHORT,true).show();
                    getActivity().getSharedPreferences("LAST_EVENT_DATA",0).edit().clear().commit();
                    myProgressBar.dismiss();
                    getActivity().finish();
                }
                if (!task.isSuccessful()){
                    Toasty.error(getActivity(),"C'è stato un errore durante il caricamento del nuovo evento",Toast.LENGTH_SHORT,true).show();
                }
            }
        });

    }
}
