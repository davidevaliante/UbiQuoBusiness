package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewEventSecondPage extends Fragment {


    @BindView(R.id.eventOrganizer)
    TextView eventOrganizer;
    @BindView(R.id.buttonLayout)
    RelativeLayout buttonLayout;
    @BindView(R.id.createDatePicker)
    TextView createDatePicker;
    @BindView(R.id.createTimePicker)
    TextView createTimePicker;
    @BindView(R.id.registration_first_viewpager)
    NestedScrollView registrationFirstViewpager;
    private Geocoder mGeocoder;

    protected SupportPlaceAutocompleteFragment createEventAutoAdress,createEventAutoCity;
    private String editEventStringId;
    private Bundle proposal;
    Unbinder unbinder;

    public NewEventSecondPage() {
        // Required empty public constructor
    }

    public static NewEventSecondPage newInstance() {
        NewEventSecondPage newFragment = new NewEventSecondPage();
        return newFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_new_event_second_page, container, false);

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());

        createEventAutoAdress = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.createEventAutoAdress);
        createEventAutoCity = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.createEventAutoCity);



        createEventAutoCity.setHint("Cerca Città");
        createEventAutoAdress.setHint("Cerca Indirizzo");

        unbinder = ButterKnife.bind(this, rootView);

        editEventStringId = ((CreateEvent)getActivity()).editEventIdString;
        proposal = ((CreateEvent)getActivity()).proposal;

        createEventAutoCity.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                cityMapHandler(place);
            }

            @Override
            public void onError(Status status) {

            }
        });

        createEventAutoAdress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                adressMapHandler(place);
            }

            @Override
            public void onError(Status status) {

            }
        });

        //datePicker
        createDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog().newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Integer actualMonth = monthOfYear+1;
                        String eventDate = dayOfMonth+"/"+actualMonth+"/"+year;
                        String formattedDate = UbiQuoBusinessUtils.readableDate(eventDate);
                        SharedPreferences edit_event = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA",Context.MODE_PRIVATE);
                        if(editEventStringId == null) {
                            sharedPreferences.edit().putString("EVENT_DATE_STRING", eventDate).commit();
                        }else{
                            edit_event.edit().putString("EDIT_DATE_STRING",eventDate).commit();
                        }
                        createDatePicker.setText("Data\n"+formattedDate);
                    }
                });
                datePickerDialog.vibrate(false);
                datePickerDialog.show(getActivity().getFragmentManager(),"datepicker");
            }
        });

        //timePicker
        createTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog().newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        String starting = UbiQuoBusinessUtils.hourFormatter(hourOfDay,minute);
                        createTimePicker.setText("Orario d'inizio\n"+starting);
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA",Context.MODE_PRIVATE);
                        SharedPreferences edit_event = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
                        if(editEventStringId == null) {
                            sharedPreferences.edit().putString("EVENT_START_TIME", starting).commit();
                        }else{
                            edit_event.edit().putString("EDIT_EVENT_START_TIME",starting).commit();
                        }

                    }
                },true);
                timePickerDialog.vibrate(false);
                timePickerDialog.show(getActivity().getFragmentManager(),"minutepicker");
            }
        });

        buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editEventStringId == null) {
                    if (canGoNext()) {
                        ((CreateEvent) getActivity()).newEvenViewPager.setCurrentItem(2, true);
                    }
                }else{
                    if(canEditNext()){
                        ((CreateEvent) getActivity()).newEvenViewPager.setCurrentItem(2, true);
                    }
                }
            }
        });

        if(editEventStringId == null && proposal==null) {
            //carica i dati del locale nelle view corrispondenti
            loadPlaceData();
        }else{
            loadEditPlaceData();
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void loadPlaceData(){

            SharedPreferences placeData = getActivity().getSharedPreferences("UBIQUO_BUSINESS", Context.MODE_PRIVATE);
            String placeName = placeData.getString("PLACE_NAME", "NA");
            String placeCity = placeData.getString("PLACE_CITY", "NA");
            String placeAdress = placeData.getString("PLACE_ADRESS", "NA");
            String placeId = placeData.getString("PLACE_ID", "NA");
            Double placeLongitude = UbiQuoBusinessUtils.getDoubleFromEditor(placeData, "PLACE_LONGITUDE", 0.0);
            Double placeLatitude = UbiQuoBusinessUtils.getDoubleFromEditor(placeData, "PLACE_LATITUDE", 0.0);

            Log.d("PLACE_LAT : ", "" + placeLatitude);
            Log.d("PLACE_LONG : ", "" + placeLongitude);

            SharedPreferences eventData = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
            String startingTime = eventData.getString("EVENT_START_TIME", "NA");
            String eventDate = eventData.getString("EVENT_DATE_STRING", "NA");

            if (!placeName.equalsIgnoreCase("NA") && !placeName.isEmpty()) {
                eventOrganizer.setText(placeName);
                eventData.edit().putString("EVENT_ORGANIZER", placeName).commit();
            }

            if (!placeCity.equalsIgnoreCase("NA") && !placeCity.isEmpty()) {
                createEventAutoCity.setText(placeCity);
                eventData.edit().putString("EVENT_CITY", placeCity).commit();

            }

            if (!placeAdress.equalsIgnoreCase("NA") && !placeAdress.isEmpty()) {
                createEventAutoAdress.setText(placeAdress);
                eventData.edit().putString("EVENT_ADRESS", placeAdress).commit();
            }

            if (!startingTime.equalsIgnoreCase("NA") && !startingTime.isEmpty()) {
                createTimePicker.setText("Orario d'inizio\n" + startingTime);
            }

            if (!eventDate.equalsIgnoreCase("NA") && !eventDate.isEmpty()) {
                createDatePicker.setText("Data evento\n" + UbiQuoBusinessUtils.readableDate(eventDate));
            }

            if (placeLatitude != 0.0 && placeLongitude != 0.0) {
                UbiQuoBusinessUtils.putDoubleIntoEditor(eventData.edit(), "EVENT_LONGITUDE", placeLongitude).commit();
                UbiQuoBusinessUtils.putDoubleIntoEditor(eventData.edit(), "EVENT_LATITUDE", placeLatitude).commit();
            }

            if (!placeId.equalsIgnoreCase("NA")) {
                eventData.edit().putString("EVENT_ORGANIZER_ID", placeId).commit();
            }


    }

    private Boolean canEditNext(){
        Boolean canEditNext = true;
        SharedPreferences editPreferences = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
        String date_string = editPreferences.getString("EDIT_DATE_STRING","NA");
        String time_string = editPreferences.getString("EDIT_EVENT_START_TIME","NA");
        String city = editPreferences.getString("EDIT_CITY","NA");
        String adress = editPreferences.getString("EDIT_ADRESS","NA");

        if(date_string.equalsIgnoreCase("NA") || time_string.equalsIgnoreCase("NA") || city.equalsIgnoreCase("NA") && !adress.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Campi mancanti",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(date_string.equalsIgnoreCase("NA") || time_string.equalsIgnoreCase("NA")){
            return false;
        }else{
            editPreferences.edit().putLong("EDIT_DATE",UbiQuoBusinessUtils.getTimeMillis(date_string,time_string)).commit();
        }

        Map<String, ?> allEntries = editPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }


        return canEditNext;
    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA",Context.MODE_PRIVATE);
        String date = sharedPreferences.getString("EVENT_DATE_STRING","NA");
        String time = sharedPreferences.getString("EVENT_START_TIME","NA");
        String organizer = sharedPreferences.getString("EVENT_ORGANIZER","NA");
        String city = sharedPreferences.getString("EVENT_CITY","NA");
        String adress = sharedPreferences.getString("EVENT_ADRESS","NA");
        String id = sharedPreferences.getString("EVENT_ORGANIZER_ID","NA");
        Double latitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"EVENT_LATITUDE",0.0);
        Double longitude = UbiQuoBusinessUtils.getDoubleFromEditor(sharedPreferences,"EVENT_LONGITUDE",0.0);

        String image = sharedPreferences.getString("EVENT_IMAGE","NA");
        String title = sharedPreferences.getString("EVENT_TITLE","NA");
        String desc = sharedPreferences.getString("EVENT_DESCRIPTION","NA");
        Boolean isFree = sharedPreferences.getBoolean("EVENT_IS_FREE",true);
        Integer price = sharedPreferences.getInt("EVENT_PRICE",0);


        Log.d("latitude : ",""+latitude);
        Log.d("longitude : ",""+longitude);
        Log.d("Data evento : ",""+date);
        Log.d("Time : ",""+time);
        Log.d("City : ",""+city);
        Log.d("Organizzatore : ",""+organizer);
        Log.d("Indirizzo : ",""+adress);
        Log.d("Id organizzatore : ", id);
        Log.d("Image : ", image);
        Log.d("Titolo : ", title);
        Log.d("Desc : ",desc);

        if(isFree){
            Log.d("Event is : ", "free");
            Log.d("price : ","nulla");
        }else{
            Log.d("Event is : ", "pay");
            Log.d("price : ",""+price);
        }


        if(!date.equalsIgnoreCase("NA") && !date.isEmpty() && !time.equalsIgnoreCase("NA") && !time.isEmpty()){
            Long timeInMillis = UbiQuoBusinessUtils.getTimeMillis(date,time);
            UbiQuoBusinessUtils.putDoubleIntoEditor(sharedPreferences.edit(),"EVENT_TIME",timeInMillis);
        }else{
            Toasty.error(getActivity(),"Aggiungi data ed orario",Toast.LENGTH_SHORT,true).show();
            return false;
        }

        if(organizer.isEmpty() || city.isEmpty() || adress.isEmpty() || organizer.equalsIgnoreCase("NA") || city.equalsIgnoreCase("NA") || adress.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Riempi tutti i campi necessari", Toast.LENGTH_SHORT,true).show();
            return false;
        }
        if(latitude==0.0 && latitude==0.0){
            Toasty.error(getActivity(),"Scegli indirizzo e Città",Toast.LENGTH_SHORT).show();
            return false;
        }else{

        }



        return  canGoNext;
    }


    private void cityMapHandler(Place target_place) {
        Place place = target_place;
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;

        //update  shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        SharedPreferences edit_event = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);

        try {
            String cityName = getCityNameByCoordinates(latitude, longitude);
            createEventAutoCity.setText(cityName);

            //switcher editmode/non editmode
            if(editEventStringId == null) {
                editor.putString("EVENT_CITY", cityName).commit();
            }else{
                edit_event.edit().putString("EDIT_CITY",cityName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(editEventStringId == null) {
            UbiQuoBusinessUtils.putDoubleIntoEditor(editor, "EVENT_LATITUDE", latitude).commit();
            UbiQuoBusinessUtils.putDoubleIntoEditor(editor, "EVENT_LONGITUDE", longitude).commit();
        }else{
            UbiQuoBusinessUtils.putDoubleIntoEditor(edit_event.edit(), "EDIT_LAT", latitude).commit();
            UbiQuoBusinessUtils.putDoubleIntoEditor(edit_event.edit(), "EDIT_LNG", longitude).commit();
        }
        LatLng upper = new LatLng(latitude, longitude);
        LatLng lower = new LatLng(latitude, longitude);
        LatLngBounds latlngBounds = new LatLngBounds(upper, lower);
        createEventAutoAdress.setBoundsBias(latlngBounds);

    }

    private void adressMapHandler(Place someAdress) {
        Place place = someAdress;
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;
        String targetAdress = place.getAddress().toString();
        //update  shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        SharedPreferences edit_event = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);


        createEventAutoAdress.setText(targetAdress);
        try {
            String cityName = getCityNameByCoordinates(latitude, longitude);
            createEventAutoAdress.setText(cityName);
            if(editEventStringId == null) {
                editor.putString("EVENT_CITY", cityName).commit();
            }else{
                edit_event.edit().putString("EDIT_CITY",cityName).commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(editEventStringId == null) {
            editor.putString("EVENT_ADRESS", targetAdress);
            UbiQuoBusinessUtils.putDoubleIntoEditor(editor, "EVENT_LATITUDE", latitude).commit();
            UbiQuoBusinessUtils.putDoubleIntoEditor(editor, "EVENT_LONGITUDE", longitude).commit();
        }else{
            edit_event.edit().putString("EDIT_ADRESS",targetAdress).commit();
            UbiQuoBusinessUtils.putDoubleIntoEditor(edit_event.edit(), "EDIT_LAT", latitude).commit();
            UbiQuoBusinessUtils.putDoubleIntoEditor(edit_event.edit(), "EDIT_LNG", longitude).commit();
        }

    }

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }

    private void loadEditPlaceData(){
        SharedPreferences editPref = getActivity().getSharedPreferences("EDIT_EVENT",Context.MODE_PRIVATE);
        Long time = editPref.getLong("EDIT_DATE",0);
        Double lat = UbiQuoBusinessUtils.getDoubleFromEditor(editPref,"EDIT_LAT",0.0);
        Double lng = UbiQuoBusinessUtils.getDoubleFromEditor(editPref,"EDIT_LNG",0.0);
        String adress = editPref.getString("EDIT_ADRESS","NA");
        String city = editPref.getString("EDIT_CITY","NA");
        String organizer = editPref.getString("EDIT_ORGANIZER","NA");

        if(time != 0){
            createDatePicker.setText("Data evento\n"+UbiQuoBusinessUtils.fromMillisToStringDate(time));
            createTimePicker.setText("Orario d'inizio\n"+UbiQuoBusinessUtils.fromMillisToStringTime(time));
        }

        if(!city.equalsIgnoreCase("NA")){
            createEventAutoCity.setHint(city);
        }

        if(!adress.equalsIgnoreCase("NA")){
            createEventAutoAdress.setHint(adress);
        }

        if(!organizer.equalsIgnoreCase("NA")){
            eventOrganizer.setText(organizer);
        }

    }


}
