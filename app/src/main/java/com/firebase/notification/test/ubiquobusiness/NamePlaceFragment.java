package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.model.CountrySpecification;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
    @BindView(R.id.buttonLayout)
    RelativeLayout buttonLayout;
    private Geocoder mGeocoder;
    private String pickedCity;

    private final static LatLng upperBound = new LatLng(46.92025531537451, 5.712890625);
    private final static LatLng lowerBound = new LatLng(39.16414104768743, 19.072265625);

    private final static LatLng UpperBound = new LatLng(47.754097979680026, 17.75390625);
    private final static LatLng LowerBound = new LatLng(35.02999636902566, 6.6796875);


    SupportPlaceAutocompleteFragment autocompleteFragmentCity, autocompleteFragmentAdress;

    int PLACE_PICKER_REQUEST = 1;

    @BindView(R.id.linearLayout)
    LinearLayout mapButton;
    @BindView(R.id.placeName)
    EditText name;



    public NamePlaceFragment() {
        // Required empty public constructor
    }

    public static NamePlaceFragment newInstance() {
        NamePlaceFragment namePlaceFragment = new NamePlaceFragment();
        return namePlaceFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_name_place, container, false);

        ButterKnife.bind(this, rootView);


        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
        AutocompleteFilter filter_only_italy_cities =
                new AutocompleteFilter.Builder().setCountry("IT").setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();

        AutocompleteFilter filter_only_adress = new AutocompleteFilter.Builder().setCountry("IT").setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();


        autocompleteFragmentCity = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.place_autocomplete_city);
        autocompleteFragmentCity.setFilter(filter_only_italy_cities);
        autocompleteFragmentCity.setHint("Cerca la città");

        autocompleteFragmentAdress = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.place_autocomplete_adress);
        autocompleteFragmentAdress.setFilter(filter_only_adress);
        autocompleteFragmentAdress.setHint("Cerca l'indirizzo");






        autocompleteFragmentCity.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //resistuisce il nome dalle coordinate e lo scrive nelle shared preferences
                cityMapHandler(place);
                //se la città è stata gia selezionata allora restringe gli indirizzi alla città
                AutocompleteFilter filter_only_italy_cities =
                        new AutocompleteFilter.Builder().setCountry("IT").setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();            }

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
                LatLngBounds bounds = new LatLngBounds(LowerBound, UpperBound);

                builder.setLatLngBounds(bounds);
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                    Toasty.info(getActivity(), "Inserisci il nome della tua attività", Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canGoNext()) {
                    ((Registration) getActivity()).registrationViewPager.setCurrentItem(1, true);
                    Log.d("Nome : ", ((Registration)getActivity()).newBusiness.getName());
                    Log.d("Città : ", ((Registration)getActivity()).newBusiness.getCity());
                    Log.d("Indirizzo : ", ((Registration)getActivity()).newBusiness.getAdress());

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

    //restituisce il nome della città direttamente dallae coordinate di Geolocation
    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }


    //check se si può passare al prossimo fragment
    private Boolean canGoNext() {
        Business newBusiness = ((Registration)getActivity()).newBusiness;

        String adress = newBusiness.getAdress();
        String city = newBusiness.getCity();

        if(name.getText().toString().trim().isEmpty()){
            Toasty.error(getActivity(),"Nome mancante",Toast.LENGTH_SHORT,true).show();
            return false;
        }else{
            ((Registration)getActivity()).newBusiness.setName(name.getText().toString().trim());
        }

        if(city==null || city.isEmpty()){
            Toasty.error(getActivity(),"Città mancante",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(adress == null || adress.isEmpty()){
            Toasty.error(getActivity(),"Indirizzo mancante",Toast.LENGTH_SHORT,true).show();
            return false;
        }



        return true;
    }


    private void googleMapHandler(Intent data) {

        Place place = PlacePicker.getPlace(getActivity(), data);
        String placeName = place.getName().toString();
        String placeAdress = place.getAddress().toString();
        String id = place.getId();
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;
        String phone = place.getPhoneNumber().toString();

        try {
            String placeCity = getCityNameByCoordinates(latitude, longitude);
            if (!placeCity.isEmpty()) {
                autocompleteFragmentCity.setText(placeCity);
                ((Registration)getActivity()).newBusiness.setCity(placeCity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (!placeName.isEmpty()) {
            ((Registration)getActivity()).newBusiness.setName(placeName);
            name.setText(placeName);
            name.requestFocus();
        }
        if (!placeAdress.isEmpty()) {
            ((Registration)getActivity()).newBusiness.setAdress(placeAdress);
            autocompleteFragmentAdress.setText(placeAdress);
        }


        ((Registration)getActivity()).newBusiness.setId(id);
        ((Registration)getActivity()).newBusiness.setNumber(phone);
        ((Registration)getActivity()).newBusiness.setLatitude(latitude);
        ((Registration)getActivity()).newBusiness.setLongitude(longitude);


    }


    //selezionata una città gestisce il Places restituito per aggiornare il Business Object ed applicare
    //una restrizione ulteriore all'autocomplete per l'indirizzo
    private void cityMapHandler(Place target_place) {
        Place place = target_place;

        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;

        //prova a reperire il nome a partire dalle coordinate usando il geocoding inverso
        try {
            String cityName = getCityNameByCoordinates(latitude, longitude);
            autocompleteFragmentCity.setText(cityName);

            //referenza all'activty che mantiene i fragment
            ((Registration)getActivity()).newBusiness.setCity(cityName);
            ((Registration)getActivity()).cityLatitude = latitude;
            ((Registration)getActivity()).cityLongitude = longitude;

            pickedCity = cityName;
        } catch (IOException e) {
            e.printStackTrace();
        }


        LatLng upper = new LatLng(latitude, longitude);
        LatLng lower = new LatLng(latitude, longitude);
        LatLngBounds latlngBounds = new LatLngBounds(upper, lower);
        autocompleteFragmentAdress.setBoundsBias(latlngBounds);


    }

    private void adressMapHandler(Place someAdress) {


        Place place = someAdress;
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;
        String targetAdress = place.getAddress().toString();

        autocompleteFragmentAdress.setText(targetAdress);

        //non si può scegliere un indirizzo non appartenente alla città, se viene fatto viene aggiornata anche la città
        try {
            String cityName = getCityNameByCoordinates(latitude, longitude);
            autocompleteFragmentCity.setText(cityName);
            ((Registration)getActivity()).newBusiness.setCity(cityName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //aggiorna l'oggetto nella classe
        ((Registration)getActivity()).newBusiness.setAdress(targetAdress);
        ((Registration)getActivity()).newBusiness.setLatitude(latitude);
        ((Registration)getActivity()).newBusiness.setLongitude(longitude);
    }

    private void loadRegisterData(){

        //carica informazioni già presenti
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
        String lastName = sharedPreferences.getString("PLACE_NAME","NA");
        String lastAdress = sharedPreferences.getString("PLACE_ADRESS","NA");
        String lastCity = sharedPreferences.getString("PLACE_CITY","NA");

        if(!lastName.isEmpty() && !lastName.equalsIgnoreCase("NA")){
            name.setText(lastName);
        }

        if(!lastAdress.isEmpty() && !lastName.equalsIgnoreCase("NA")){
            autocompleteFragmentAdress.setText(lastAdress);
        }

        if(!lastCity.isEmpty() && !lastName.equalsIgnoreCase("NA")){
            autocompleteFragmentCity.setText(lastCity);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


}
