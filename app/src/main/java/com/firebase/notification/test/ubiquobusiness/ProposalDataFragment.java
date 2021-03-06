package com.firebase.notification.test.ubiquobusiness;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;

public class ProposalDataFragment extends Fragment {


    protected EditText title,description;
    protected SupportPlaceAutocompleteFragment autocCompleteCity;
    protected RadioRealButtonGroup anonGroup, argumentsGroup;
    protected RelativeLayout nextButton;
    protected Geocoder mGeocoder;
    protected Boolean isAnonymous = true;

    public ProposalDataFragment() {
        // Required empty public constructor
    }

    public static ProposalDataFragment newInstance(){
        ProposalDataFragment newFrag = new ProposalDataFragment();
        return newFrag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_proposal_data,container,false);

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());


        title = (EditText)rootView.findViewById(R.id.mTitle);
        description = (EditText)rootView.findViewById(R.id.mDescription);
        autocCompleteCity = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.place_autocomplete_city);
        anonGroup = (RadioRealButtonGroup)rootView.findViewById(R.id.anonGroup);
        argumentsGroup = (RadioRealButtonGroup)rootView.findViewById(R.id.argumentsGroup);
        nextButton = (RelativeLayout)rootView.findViewById(R.id.mNextButton);

        autocCompleteCity.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                cityMapHandler(place);
            }

            @Override
            public void onError(Status status) {

            }
        });

        anonGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                SharedPreferences proposalPref = getActivity().getSharedPreferences("NEWPROPOSAL_PREF", Context.MODE_PRIVATE);
                switch(position){
                    case 0:
                        proposalPref.edit().putBoolean("PROP_ISANON",true).commit();
                        break;
                    case 1:
                        proposalPref.edit().putBoolean("PROP_ISANON",false).commit();
                        break;
                    default:
                        proposalPref.edit().putBoolean("PROP_ISANON",false).commit();
                        break;
                }
            }
        });



        argumentsGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                SharedPreferences proposalPref = getActivity().getSharedPreferences("NEWPROPOSAL_PREF", Context.MODE_PRIVATE);

                switch (position){
                    case 0:
                        proposalPref.edit().putString("PROP_ARG","party").commit();
                        break;

                    case 1:
                        proposalPref.edit().putString("PROP_ARG","cocktail").commit();
                        break;

                    case 2:
                        proposalPref.edit().putString("PROP_ARG","dance").commit();
                        break;

                    case 3:
                        proposalPref.edit().putString("PROP_ARG","themed").commit();
                        break;

                    case 4:
                        proposalPref.edit().putString("PROP_ARG","music").commit();
                        break;

                    default:
                        proposalPref.edit().putString("PROP_ARG","party").commit();
                        break;
                }
            }
        });



        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canGoNext()){
                    saveData();
                    submitProposal();
                }
            }
        });

        autocCompleteCity.setHint("Imposta la città");

        loadUserData();

        return rootView;
    }

    private void loadUserData(){
        //preference utente
        SharedPreferences userData = getActivity().getSharedPreferences("UBIQUO_BUSINESS", Context.MODE_PRIVATE);
        String userCity = userData.getString("PLACE_CITY","NA");

        //preference activity
        SharedPreferences proposalPref = getActivity().getSharedPreferences("NEWPROPOSAL_PREF", Context.MODE_PRIVATE);
        String lastTitle = proposalPref.getString("PROP_TITLE","NA");
        String lastCity =  proposalPref.getString("PROP_CITY","NA");
        Boolean isAnon = proposalPref.getBoolean("PROP_ISANON",true);
        String argument = proposalPref.getString("PROP_ARG","NA");
        String last_description = proposalPref.getString("PROP_DESC","NA");

        if(!userCity.equalsIgnoreCase("NA")){
            autocCompleteCity.setHint(userCity);
            proposalPref.edit().putString("PROP_CITY",userCity).commit();
        }



        if(!lastCity.equalsIgnoreCase("NA")){
            autocCompleteCity.setHint(lastCity);
            proposalPref.edit().putString("PROP_CITY",lastCity).commit();

        }

        if(isAnon){
            anonGroup.setPosition(0);
        }else{
            anonGroup.setPosition(1);
        }

        if(!argument.equalsIgnoreCase("NA")){
            switch(argument){
                case "party":
                    argumentsGroup.setPosition(0);
                    break;

                case "cocktail":
                    argumentsGroup.setPosition(1);
                    break;

                case "dance":
                    argumentsGroup.setPosition(2);
                    break;

                case "themed":
                    argumentsGroup.setPosition(3);
                    break;

                case "music":
                    argumentsGroup.setPosition(4);
                    break;

                default:
                    argumentsGroup.setPosition(0);
                    break;
            }
        }

        if(!lastTitle.isEmpty() && !lastTitle.equalsIgnoreCase("NA")){
            title.setText(lastTitle);
        }

        if(!last_description.equalsIgnoreCase("NA")){
            description.setText(last_description);
        }
    }

    //controlla solo che le condizioni minime siano soddisfatte
    protected Boolean canGoNext(){
        Boolean canGoNext = true;
        if(title.getText().toString().trim().isEmpty() || description.getText().toString().isEmpty()){
            Toasty.error(getActivity(),"Riempi tutti i campi di testo", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(description.getText().toString().length() <31){
            Toasty.error(getActivity(),"La descrizione deve contenere almeno 30 caratteri",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(title.getText().toString().length() < 9){
            Toasty.error(getActivity(),"Il titolo deve contenere almeno 8 caratteri",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        return canGoNext;
    }

    protected void saveData(){
        SharedPreferences proposalPref = getActivity().getSharedPreferences("NEWPROPOSAL_PREF", Context.MODE_PRIVATE);
        proposalPref.edit().putString("PROP_TITLE",title.getText().toString().trim()).commit();
        proposalPref.edit().putString("PROP_DESC",description.getText().toString().trim()).commit();
        Log.d("CITY :", proposalPref.getString("PROP_CITY","NA"));
        Log.d("TITLE :", proposalPref.getString("PROP_TITLE","NA"));
        Log.d("DESC :", proposalPref.getString("PROP_DESC","NA"));
        Log.d("ARG :", proposalPref.getString("PROP_ARG","party"));
        if(proposalPref.getBoolean("PROP_ISANON",true)) {
            Log.d("IS_ANON :", "vero");
        }else {
            Log.d("IS_ANON :", "falso");

        }
        Toast.makeText(getActivity(), "saved", Toast.LENGTH_SHORT).show();





    }

    private void submitProposal(){
        SharedPreferences userPref = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);

        final String creatorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final SharedPreferences proposalPref = getActivity().getSharedPreferences("NEWPROPOSAL_PREF", Context.MODE_PRIVATE);
        final String title = proposalPref.getString("PROP_TITLE","NA");
        final String description = proposalPref.getString("PROP_DESC","NA");
        final Boolean isAnon = proposalPref.getBoolean("PROP_ISANON",true);
        final String city = proposalPref.getString("PROP_CITY","NA");
        final String argument = proposalPref.getString("PROP_ARG","party");
        final Long currentTime = System.currentTimeMillis();

        if(isAnon==false){
             DatabaseReference businessRef =  FirebaseDatabase.getInstance().getReference().child("Business").child(creatorId);

            businessRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Business business = dataSnapshot.getValue(Business.class);
                    String creatorName = business.getName();
                    DatabaseReference newPropRef = FirebaseDatabase.getInstance().getReference().child("Proposals").child(city);
                    Proposal proposal = new Proposal(title,"",description,argument,creatorName,0,currentTime,city,isAnon, creatorId);
                    newPropRef.push().setValue(proposal);
                    proposalPref.edit().clear().commit();
                    getActivity().finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else {
            String creatorName = "Non disponibile";
            DatabaseReference newPropRef = FirebaseDatabase.getInstance().getReference().child("Proposals").child(city);
            Proposal proposal = new Proposal(title, "", description, argument, creatorName, 0, currentTime, city, isAnon, FirebaseAuth.getInstance().getCurrentUser().getUid());
            newPropRef.push().setValue(proposal);
            proposalPref.edit().clear().commit();
            getActivity().finish();
        }
    }


    private void cityMapHandler(Place target_place) {
        Place place = target_place;
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;

        //update  shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("NEWPROPOSAL_PREF",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            String cityName = getCityNameByCoordinates(latitude, longitude);
            autocCompleteCity.setText(cityName);
            editor.putString("PROP_CITY", cityName);

        } catch (IOException e) {
            e.printStackTrace();
        }


        editor.commit();
    }

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }


}