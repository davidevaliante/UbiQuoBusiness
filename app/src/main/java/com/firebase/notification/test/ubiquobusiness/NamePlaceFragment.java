package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class NamePlaceFragment extends Fragment {
    private Geocoder mGeocoder ;

    private final static LatLng upperBound = new LatLng(46.191152,9.383675);
    private final static LatLng lowerBound = new LatLng(36.693164,14.995949);
    PlaceAutocompleteFragment autocompleteFragmentCity, autocompleteFragmentAdress;

    int PLACE_PICKER_REQUEST = 1;
    int CITY_PICKER_REQUEST = 2;
    int ADRESS_PICKER_REQUEST = 3;
    @BindView(R.id.linearLayout) LinearLayout mapButton;
    @BindView(R.id.placeName) EditText name;
    @BindView(R.id.button_next) Button nextButton;


    public NamePlaceFragment() {
        // Required empty public constructor
    }

    public static NamePlaceFragment newInstance(){
        NamePlaceFragment namePlaceFragment = new NamePlaceFragment();
        return namePlaceFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_name_place,container,false);

        ButterKnife.bind(this,rootView);

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());

        autocompleteFragmentCity = (PlaceAutocompleteFragment)
                getActivity().
                getFragmentManager().findFragmentById(R.id.place_autocomplete_city);
        autocompleteFragmentCity.setHint("Cerca la città....");

        autocompleteFragmentAdress = (PlaceAutocompleteFragment)getActivity().getFragmentManager().
                                    findFragmentById(R.id.place_autocomplete_adress);
        autocompleteFragmentAdress.setHint("Cerca l'indirizzo...");



        autocompleteFragmentCity.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                cityMapHandler(place);
            }

            @Override
            public void onError(Status status) {

            }
        });

        autocompleteFragmentAdress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                adressMapHandler(place);
            }

            @Override
            public void onError(Status status) {

            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                LatLngBounds bounds = new LatLngBounds(lowerBound,upperBound);
                builder.setLatLngBounds(bounds);
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                    Toasty.info(getActivity(),"Inserisci il nome della tua attività", Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });



        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canGoNext()) {
                    ((Registration) getActivity()).registrationViewPager.setCurrentItem(1, true);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            googleMapHandler(data);
        }


    }



    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<android.location.Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }

    private Boolean canGoNext(){
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        String mName = name.getText().toString().trim();
        String city = sharedPreferences.getString("PLACE_CITY","NO_CITY_INSERT_BY_USER");
        String adress = sharedPreferences.getString("PLACE_ADRESS","NO_ADRESS_INSERT_BY_USER");

        if(mName.equalsIgnoreCase("NO_NAME_INSERT_BY_USER") || mName.isEmpty()){
            Toasty.error(getActivity(),"Riempi tutti i campi per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }
        if(city.equalsIgnoreCase("NO_CITY_INSERT_BY_USER") || city.isEmpty()){
            Toasty.error(getActivity(),"Riempi tutti i campi per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(adress.equalsIgnoreCase("NO_ADRESS_INSERT_BY_USER") || adress.isEmpty()){
            Toasty.error(getActivity(),"Riempi tutti i campi per continuare", Toast.LENGTH_SHORT,true).show();
            return false;
        }

        return true;
    }

    private void googleMapHandler(Intent data){

            Place place = PlacePicker.getPlace(getActivity(), data);
            String placeName = place.getName().toString();
            String placeAdress = place.getAddress().toString();
            String id = place.getId();
            Double latitude = place.getLatLng().latitude;
            Double longitude = place.getLatLng().longitude;
            String phone = place.getPhoneNumber().toString();


            //update  shared preferences
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            try {
                String placeCity = getCityNameByCoordinates(latitude,longitude);
                if(!placeCity.isEmpty()){
                    autocompleteFragmentCity.setText(placeCity);
                    editor.putString("PLACE_CITY",placeCity);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            if(!placeName.isEmpty()) {
                editor.putString("PLACE_NAME", placeName);
                name.setText(placeName);
                name.requestFocus();
            }
            if(!placeAdress.isEmpty()){
                editor.putString("PLACE_ADRESS",placeAdress);
                autocompleteFragmentAdress.setText(placeAdress);
            }


            editor.putString("PLACE_ID",id);
            editor.putString("PLACE_PHONE",phone);
            UbiQuoBusinessUtils.putDoubleIntoEditor(editor,"PLACE_LATITUDE",latitude);
            UbiQuoBusinessUtils.putDoubleIntoEditor(editor,"PLACE_LONGITUDE",longitude);
            editor.commit();


    }

    private void cityMapHandler(Place target_place){
        Place place = target_place;
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;

        //update  shared preferences
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        try {
            String cityName = getCityNameByCoordinates(latitude,longitude);
            autocompleteFragmentCity.setText(cityName);
            editor.putString("PLACE_CITY",cityName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        UbiQuoBusinessUtils.putDoubleIntoEditor(editor,"PLACE_LATITUDE",latitude);
        UbiQuoBusinessUtils.putDoubleIntoEditor(editor,"PLACE_LONGITUDE",longitude);
        LatLng upper = new LatLng(latitude,longitude);
        LatLng lower = new LatLng(latitude,longitude);
        LatLngBounds latlngBounds = new LatLngBounds(upper,lower);
        autocompleteFragmentAdress.setBoundsBias(latlngBounds);

        editor.commit();
    }

    private void adressMapHandler(Place someAdress){



        Place place =  someAdress;
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;
        String targetAdress = place.getAddress().toString();
        //update  shared preferences
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        autocompleteFragmentAdress.setText(targetAdress);
        try {
            String cityName = getCityNameByCoordinates(latitude,longitude);
            autocompleteFragmentCity.setText(cityName);
            editor.putString("PLACE_CITY",cityName);

        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.putString("PLACE_ADRESS",targetAdress);
        UbiQuoBusinessUtils.putDoubleIntoEditor(editor,"PLACE_LATITUDE",latitude);
        UbiQuoBusinessUtils.putDoubleIntoEditor(editor,"PLACE_LONGITUDE",longitude);

        editor.commit();
    }
}
