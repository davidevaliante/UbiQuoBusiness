package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


public class NewEventFirstPage extends Fragment {


    Unbinder unbinder;
    @BindView(R.id.cameraPicker)
    ImageButton cameraPicker;
    @BindView(R.id.eventName)
    EditText eventName;
    @BindView(R.id.eventDescription)
    EditText eventDescription;
    private static final Integer GALLERY_REQUEST_CODE = 1;
    @BindView(R.id.imagePicker)
    CircularImageView imagePicker;
    @BindView(R.id.freeRadioButton)
    RadioRealButton freeRadioButton;
    @BindView(R.id.payRadioButton)
    RadioRealButton payRadioButton;
    @BindView(R.id.radioRealButtonGroup)
    RadioRealButtonGroup radioRealButtonGroup;
    @BindView(R.id.eventPrice)
    EditText eventPrice;
    @BindView(R.id.eventNextFirst)
    RelativeLayout eventNextFirst;

    private Boolean isFree = true;
    private Bundle proposal;
    private String editEventStringId;
    private DynamicData dynamicData;
    private StaticData staticData;

    public NewEventFirstPage() {
        // Required empty public constructor
    }

    public static NewEventFirstPage newInstance() {
        NewEventFirstPage newFragement = new NewEventFirstPage();
        return newFragement;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_new_event_first_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);


        //default, oscurare questi elementi
        imagePicker.setVisibility(View.GONE);
        eventPrice.setVisibility(View.GONE);

        cameraPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        radioRealButtonGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if (position == 0) {
                    eventPrice.setVisibility(View.GONE);
                    isFree = true;
                } else {
                    eventPrice.setVisibility(View.VISIBLE);
                    isFree=false;
                }
            }
        });

        eventNextFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(canGoNext()){
                   //aggiorna l'oggetto associato all'activity
                   String title = eventName.getText().toString().trim();
                   ((CreateEvent)getActivity()).getDynamicData().seteName(title);
                   ((CreateEvent)getActivity()).getDynamicData().setFree(isFree);
                   ((CreateEvent)getActivity()).mapInfo.seteName(title);

                    if(!eventPrice.getText().toString().isEmpty()){
                        Float price = Float.valueOf(eventPrice.getText().toString());
                        ((CreateEvent)getActivity()).getDynamicData().setPrice(price);
                    }else{
                        ((CreateEvent)getActivity()).getDynamicData().setPrice(0.0f);
                    }

                   String description = eventDescription.getText().toString().trim();
                   ((CreateEvent)getActivity()).getStaticData().setDesc(description);
                   if(((CreateEvent)getActivity()).eventImageUri != null) {
                       Log.d("Image : ", ((CreateEvent) getActivity()).eventImageUri);
                   }

                   ((CreateEvent)getActivity()).newEvenViewPager.setCurrentItem(1,true);
               }
            }
        });






        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);



    }

    protected void loadFirstPageDataFromBundle(Bundle proposal){
        Bundle bundle = proposal;
        String proposalTitle = proposal.getString("title");
        eventName.setText(proposalTitle);
        String proposalDescription = proposal.getString("description");
        eventDescription.setText(proposalDescription);
    }

    protected void loadFirstPageEditData(){
        //dati dinamici
        String title = dynamicData.geteName();
        eventName.setText(title);
        Boolean isFree = dynamicData.getFree();
        if(!isFree){
            radioRealButtonGroup.setPosition(1);
            Float price = dynamicData.getPrice();
            eventPrice.setVisibility(View.VISIBLE);
            eventPrice.setText(""+price);
        }

        //Dati statici
        String description = staticData.getDesc();
        eventDescription.setText(description);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //handle galleria normale
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setBorderLineColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }

        //handle cropper
        //Handle del risultato di CropImage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imagePicker.setVisibility(View.VISIBLE);
                imagePicker.setImageURI(result.getUri());
                imagePicker.setBorderColor(R.color.colorAccent);

                //se viene cambiata l'immagine
                ((CreateEvent)getActivity()).eventImageUri = result.getUri().toString();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    protected Boolean canGoNext(){
        Boolean canGonext = true;

        String name = eventName.getText().toString().trim();
        String description = eventDescription.getText().toString().trim();
        Boolean free = isFree;
        Float price = 0.0f;
        if(!eventPrice.getText().toString().isEmpty()) {
            price = Float.valueOf(eventPrice.getText().toString());
        }
        String downloadUrl = ((CreateEvent)getActivity()).getDynamicData().getiPath();
        String imageUri = ((CreateEvent)getActivity()).eventImageUri;

        if(name.length() < 6){
            Toasty.error(getActivity(),"Il titolo deve contenere almeno 6 caratteri",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(description.length() <31){
            Toasty.error(getActivity(),"La descrizione deve contenere almeno 30 caratteri",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(!free && price == 0.0f){
            Toasty.error(getActivity(),"Specifica il prezzo di ingresso",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(downloadUrl == null && imageUri == null){
            Toasty.error(getActivity(),"Aggiungi una copertina per l'evento",Toast.LENGTH_SHORT,true).show();
            return false;
        }


        return canGonext;
    }



   /* protected void saveData(){
        Bundle bundle = ((CreateEvent)getActivity()).proposal;
        //salva solo se non derivato da proposta

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("EVENT_TITLE", UbiQuoBusinessUtils.capitalize(eventName.getText().toString().trim())).commit();
            sharedPreferences.edit().putString("EVENT_DESCRIPTION", UbiQuoBusinessUtils.capitalize(eventDescription.getText().toString().trim())).commit();
            sharedPreferences.edit().putBoolean("EVENT_IS_FREE", isFree).commit();

            if (!isFree) {
                if (!eventPrice.getText().toString().trim().isEmpty()) {
                    sharedPreferences.edit().putInt("EVENT_PRICE", Integer.valueOf(eventPrice.getText().toString().trim())).commit();
                }
            }

    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }






    /*protected void loadEditDataIntoPreferences(String eventId,String city){
        loadFromDynamicData(eventId,city);
        loadFromStaticData(eventId,city);
        loadFromMapData(eventId,city);
    }

    protected void loadFromDynamicData(String id, final String city){
        DatabaseReference dynamicReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(city).child(id);

        ValueEventListener dynamicListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences edit = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);

                DynamicData data = dataSnapshot.getValue(DynamicData.class);
                edit.edit().putString("EDIT_CITY",city).commit();
                //nome luogo
                String organizer = data.getpName();
                edit.edit().putString("EDIT_ORGANIZER",organizer).commit();

                //immagine
                String image = data.getiPath();
                imagePicker.setVisibility(View.VISIBLE);
                Glide.with(getActivity()).load(image).asBitmap().into(imagePicker);
                edit.edit().putString("EDIT_IMAGE",image).commit();

                //titolo
                String title = data.geteName();
                eventName.setText(title);
                edit.edit().putString("EDIT_TITLE",title).commit();

                //orario
                Long time = data.getDate();
                edit.edit().putLong("EDIT_DATE",time).commit();

                //è gratis e prezzo
                Boolean free = data.getFree();
                if(!free){
                    isFree=false;
                    radioRealButtonGroup.setPosition(1);
                    eventPrice.setVisibility(View.VISIBLE);
                    eventPrice.setText(""+data.getPrice());
                    edit.edit().putBoolean("EDIT_ISFREE",false).commit();
                    edit.edit().putFloat("EDIT_PRICE",data.getPrice()).commit();
                }else{
                    isFree=true;
                    edit.edit().putBoolean("EDIT_ISFREE",true).commit();
                    edit.edit().putFloat("EDIT_PRICE",0.0f).commit();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dynamicReference.addListenerForSingleValueEvent(dynamicListener);
    }

    protected void loadFromStaticData(String eventId,String city){
        final DatabaseReference staticReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(city).child(eventId);

        ValueEventListener staticListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences edit = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
                StaticData data = dataSnapshot.getValue(StaticData.class);

                //descrizione
                String desc = data.getDesc();
                eventDescription.setText(desc);
                edit.edit().putString("EDIT_DESC",desc).commit();

                //array di nomi
                ArrayList<String> names = data.getNames();
                ArrayList<String> numbers = data.getNumbers();

                for(int i = 0; i < names.size();i++){
                    Integer index = i;
                    String contact = "EDIT_CONTACT_"+index;
                    String name = names.get(i);
                    edit.edit().putString(contact,name).commit();
                    Log.d(contact,name);
                }

                for(int k = 0; k< numbers.size();k++){
                    Integer index = k;
                    String number_key = "EDIT_NUMBER_"+index;
                    String number = numbers.get(k);
                    edit.edit().putString(number_key,number).commit();
                    Log.d(number_key,number);
                }

                staticReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        staticReference.addListenerForSingleValueEvent(staticListener);
    }

    protected void loadFromMapData(String eventId,String city){
        final DatabaseReference mapRef = FirebaseDatabase.getInstance().getReference().child("MapData").child(city).child(eventId);

        ValueEventListener mapListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences edit = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);


                MapInfo data = dataSnapshot.getValue(MapInfo.class);

                Double latitude = data.getLat();
                UbiQuoBusinessUtils.putDoubleIntoEditor(edit.edit(),"EDIT_LAT",latitude).commit();

                Double longitude = data.getLng();
                UbiQuoBusinessUtils.putDoubleIntoEditor(edit.edit(),"EDIT_LNG",longitude).commit();

                String adress = data.getAdress();
                edit.edit().putString("EDIT_ADRESS",adress).commit();

                String organizer_id = data.getId();
                edit.edit().putString("EDIT_ORGANIZER_ID",organizer_id).commit();

                mapRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mapRef.addListenerForSingleValueEvent(mapListener);

    }

    private Boolean canEditNext(){
        Boolean canEditNext = true;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("EDIT_EVENT", Context.MODE_PRIVATE);

        //DATI CARICATI DA DYNAMIC DATA
        String image = sharedPreferences.getString("EDIT_IMAGE","NA");
        String title = sharedPreferences.getString("EDIT_TITLE","NA");
        Long date = sharedPreferences.getLong("EDIT_DATE",0);
        Boolean free = sharedPreferences.getBoolean("EDIT_ISFREE",true);
        Float price = sharedPreferences.getFloat("EDIT_PRICE",0.0f);
        String organizer = sharedPreferences.getString("EDIT_ORGANIZER","NA");

        //DATI COMPARATIVI
        String actual_title = eventName.getText().toString().trim();
        Boolean actual_free = isFree;
        sharedPreferences.edit().putBoolean("EDIT_ISFREE",isFree).commit();
        String price_string = eventPrice.getText().toString();
        if(price_string != null && !price_string.isEmpty()) {
            Float actual_price = Float.valueOf(price);
            if (isFree) {
                sharedPreferences.edit().putFloat("EDIT_PRICE", 0.0f).commit();
            } else {
                sharedPreferences.edit().putFloat("EDIT_PRICE", actual_price).commit();
            }
        }

        if(image.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Immagine profilo mancante", Toast.LENGTH_SHORT,true).show();
            return false;
        }else{
            Log.d("image : ",image);
        }

        if(actual_title.isEmpty() || actual_title.length()<7){
            Toasty.error(getActivity(),"Il titolo deve contenere almeno 6 caratteri", Toast.LENGTH_SHORT,true).show();
            return false;
        }else{
            sharedPreferences.edit().putString("EDIT_TITLE",actual_title).commit();
            Log.d("Title : ",sharedPreferences.getString("EDIT_TITLE","NA"));

        }


        /*//******END DATI CARICATI DA DYNAMIC DATA


        //DATI CARICATI DA STATIC DATA
        String desc = sharedPreferences.getString("EDIT_DESC","NA");

        //dati comparativi
        String actual_desc = eventDescription.getText().toString().trim();
        if(actual_desc.length()<31){
            Toasty.error(getActivity(),"La descrizione deve contenere almeno 30 caratteri", Toast.LENGTH_SHORT,true).show();
            return false;
        }else{
            sharedPreferences.edit().putString("EDIT_DESC",actual_desc).commit();
            Log.d("Desc : ",sharedPreferences.getString("EDIT_DESC","NA"));

        }

        /*//******END DATI CARICATI DA STATIC DATA

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }

        return canEditNext;

    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
        String eventImage = sharedPreferences.getString("EVENT_IMAGE","NA");
        String eventTitle = eventName.getText().toString().trim();
        String description = eventDescription.getText().toString().trim();
        String price = eventPrice.getText().toString().trim();
        if(eventImage.isEmpty() || eventImage.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Scegli un immagine per l'evento", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(eventTitle.isEmpty()){
            Toasty.error(getActivity(),"Inserisci un titolo", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(description.isEmpty()){
            Toasty.error(getActivity(),"Inserisci una descrizione", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(description.length() < 30){
            Toasty.error(getActivity(),"La descrizione deve contenere almeno 30 caratteri", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(!isFree && price.isEmpty()){
            Toasty.error(getActivity(),"Hai dimenticato il prezzo di ingresso", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        return canGoNext;
    }

    private void loadLastData(){

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
        String imageUri = sharedPreferences.getString("EVENT_IMAGE", "NA");
        String title = sharedPreferences.getString("EVENT_TITLE", "NA");
        String description = sharedPreferences.getString("EVENT_DESCRIPTION", "NA");
        Boolean free = sharedPreferences.getBoolean("EVENT_IS_FREE", true);

        if (!free) {
            Integer price = sharedPreferences.getInt("EVENT_PRICE", 0);
            radioRealButtonGroup.setPosition(1);
            eventPrice.setVisibility(View.VISIBLE);
            eventPrice.setText("" + price);
            isFree = false;
        }

        if (!title.equalsIgnoreCase("NA") && !title.isEmpty()) {
            eventName.setText(title);
        }

        if (!description.equalsIgnoreCase("NA") && !description.isEmpty()) {
            eventDescription.setText(description);
        }

        if (!imageUri.equalsIgnoreCase("NA")) {
            imagePicker.setVisibility(View.VISIBLE);
            imagePicker.setBorderColor(R.color.colorAccent);
            Uri uri = Uri.parse(imageUri);
            imagePicker.setImageURI(uri);
        }
    }

    protected void loadProposalData(){
        Bundle bundle = ((CreateEvent)getActivity()).proposal;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
        String title = bundle.getString("title","NA");
        String description = bundle.getString("description","NA");
        String proposalId = bundle.getString("id","NA");
        Boolean free = sharedPreferences.getBoolean("EVENT_IS_FREE", true);



        if(!proposalId.isEmpty() && !proposalId.equalsIgnoreCase("NA")){
            sharedPreferences.edit().putString("PROPOSAL_ID",proposalId).commit();
        }

        if (!free) {
            Integer price = sharedPreferences.getInt("EVENT_PRICE", 0);
            radioRealButtonGroup.setPosition(1);
            eventPrice.setVisibility(View.VISIBLE);
            eventPrice.setText("" + price);
            isFree = false;
        }

        if (!title.equalsIgnoreCase("NA") && !title.isEmpty()) {
            eventName.setText(title);
        }

        if (!description.equalsIgnoreCase("NA") && !description.isEmpty()) {
            eventDescription.setText(description);
        }
    }*/



}