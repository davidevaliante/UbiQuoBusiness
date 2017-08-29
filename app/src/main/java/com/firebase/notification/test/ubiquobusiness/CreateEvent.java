package com.firebase.notification.test.ubiquobusiness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateEvent extends AppCompatActivity {

    @BindView(R.id.newEvenViewPager)
    CustomViewPager newEvenViewPager;
    public ScreenSlidePagerAdapter adapter;
    protected Bundle proposal;
    protected String editEventIdString;
    protected String editEventCity;
    protected static Business business;
    protected static DynamicData dynamicData = new DynamicData();
    protected static StaticData staticData = new StaticData();
    protected static MapInfo mapInfo = new MapInfo();
    protected static String city;
    protected static String eventImageDownload;
    protected static String eventImageUri;
    protected static String organizer, organizerId;
    protected NewEventFirstPage firstPage;
    protected NewEventSecondPage secondPage;
    protected ContactsFragment contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        SharedPreferences sharedPreferences = getSharedPreferences("LAST_EVENT_DATA",Context.MODE_PRIVATE);

        ButterKnife.bind(this);



        proposal = getIntent().getBundleExtra("proposal_bundle");
        editEventIdString = getIntent().getStringExtra("edit_string_id");
        editEventCity = getIntent().getStringExtra("edit_string_city");


        //una volta caricati i dati crea l'adapter
        List<Fragment> createEventFragments = initializeFragments();
        adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),createEventFragments);
        newEvenViewPager.setOffscreenPageLimit(3);
        newEvenViewPager.setAdapter(adapter);
        newEvenViewPager.setPagingEnabled(false);


        loadUserData(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());


        if(editEventIdString != null){
            loadEventData(editEventIdString);
        }





    }


    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> registrationFragments;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Fragment> registrationFragments) {
            super(fm);
            this.registrationFragments = registrationFragments;

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            return registrationFragments.get(position);
        }

        @Override
        public int getCount() {
            return this.registrationFragments.size();
        }




    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {
        if (newEvenViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
           NewEventFirstPage firstPage = (NewEventFirstPage)adapter.getItem(0);
            if(editEventIdString == null && proposal == null){
                //firstPage.saveData();
            }

            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            newEvenViewPager.setCurrentItem(newEvenViewPager.getCurrentItem() - 1);
        }
    }

    private List<Fragment> initializeFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(NewEventFirstPage.newInstance());
        fList.add(NewEventSecondPage.newInstance());
        fList.add(ContactsFragment.newInstance());

        return fList;
    }

    private void loadUserData(String userId){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Business").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                business = dataSnapshot.getValue(Business.class);
                city = business.getCity();
                NewEventSecondPage secondPage = (NewEventSecondPage) adapter.getItem(1);
                String placeName =  business.getName();
                String adress = business.getAdress();
                String city = business.getCity();
                Double latitude = business.getLatitude();
                Double longitude = business.getLongitude();
                String id = business.getId();
                String phone = business.getNumber();

                //di base carica nelle variabili di classe del fragment le informazioni del locale
                mapInfo.setLat(latitude);
                secondPage.latitude = latitude;
                mapInfo.setLng(longitude);
                secondPage.longitude = longitude;
                mapInfo.setAdress(adress);
                secondPage.adress = adress;
                secondPage.createEventAutoAdress.setText(adress);
                secondPage.city = city;
                secondPage.createEventAutoCity.setText(city);
                mapInfo.setId(id);
                mapInfo.setPhone(phone);
                secondPage.eventOrganizer.setText(placeName);
                SupportPlaceAutocompleteFragment autoCity = (SupportPlaceAutocompleteFragment) secondPage.getChildFragmentManager().findFragmentById(R.id.createEventAutoCity);
                SupportPlaceAutocompleteFragment autoAdress = (SupportPlaceAutocompleteFragment)secondPage.getChildFragmentManager().findFragmentById(R.id.createEventAutoAdress);
                autoCity.setText(city);
                autoAdress.setText(adress);
                organizer = business.getName();
                organizerId = business.getId();
                mapInfo.setpName(business.getName());
                dynamicData.setpName(business.getName());

                ContactsFragment contactsFragment = (ContactsFragment)adapter.getItem(2) ;
                contactsFragment.secondContactName.setText(organizer);
                contactsFragment.secondContactNumber.setText(phone);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //solo se è in editMode
    //INIZIO metodi eventi in editMode
    private void loadEventData(String eventId){
        loadDynamicData(eventId,editEventCity);
        loadStaticData(eventId,editEventCity);
        loadMapData(eventId,editEventCity);

    }



    protected void loadDynamicData(final String eventId, String city){
        final DatabaseReference dynamicReference =
              FirebaseDatabase.getInstance().getReference()
                              .child("Events").child("Dynamic").child(city).child(eventId);
        dynamicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dynamicData = dataSnapshot.getValue(DynamicData.class);

                //CARICAMENTO NELLA PRIMA PAGINA
                NewEventFirstPage firstPage = (NewEventFirstPage) adapter.getItem(0);

                String eventName = dynamicData.geteName();
                firstPage.eventName.setText(eventName);

                Boolean isFree = dynamicData.getFree();
                Float price = dynamicData.getPrice();
                if(!isFree){
                    firstPage.radioRealButtonGroup.setPosition(1);
                    firstPage.eventPrice.setVisibility(View.VISIBLE);
                    firstPage.eventPrice.setText(""+price);
                }

                String imagePath = dynamicData.getiPath();
                eventImageDownload = imagePath;
                Glide.with(CreateEvent.this).load(imagePath).into(firstPage.imagePicker);
                firstPage.imagePicker.setVisibility(View.VISIBLE);
                //FINE CARICAMENTO PRIMA PAGINA

                //CARICAMENTO SECONDA PAGINA
                NewEventSecondPage secondPage = (NewEventSecondPage)adapter.getItem(1);
                String date = UbiQuoBusinessUtils.fromMillisToStringDate(dynamicData.getDate());
                String time = UbiQuoBusinessUtils.fromMillisToStringTime(dynamicData.getDate());

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String dateString = formatter.format(new Date(dynamicData.getDate()));

                String organizer = dynamicData.getpName();

                secondPage.date = dateString;
                secondPage.createDatePicker.setText("Data\n"+UbiQuoBusinessUtils.readableDate(dateString));
                secondPage.time = time;
                secondPage.createTimePicker.setText("Orario d'inizio\n"+time);
                secondPage.eventOrganizer.setText(organizer);
                //FINE CARICAMENTO SECONDA PAGINA


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void loadStaticData(String eventId,String city){
        DatabaseReference staticRef =
                FirebaseDatabase.getInstance().getReference()
                                .child("Events").child("Static").child(city).child(eventId);

        staticRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                staticData = dataSnapshot.getValue(StaticData.class);
                NewEventFirstPage firstPage = (NewEventFirstPage)adapter.getItem(0);

                String description = staticData.getDesc();
                firstPage.eventDescription.setText(description);

                ContactsFragment contactsFragment = (ContactsFragment)adapter.getItem(2);
                ArrayList<String> names = staticData.getNames();
                ArrayList<String> numbers = staticData.getNumbers();
                if(names.size()>=1 && !names.get(0).isEmpty() && !numbers.get(0).isEmpty()){
                    contactsFragment.secondContactName.setText(names.get(0));
                    contactsFragment.secondContactNumber.setText(numbers.get(0));
                }

                if(names.size()>=2 && !names.get(1).isEmpty() && !numbers.get(1).isEmpty()){
                    contactsFragment.firstContactName.setText(names.get(1));
                    contactsFragment.firstContactNumber.setText(numbers.get(1));
                }

                if(names.size()>=3 && !names.get(2).isEmpty() && !numbers.get(2).isEmpty()){
                    contactsFragment.thirdContactName.setText(names.get(2));
                    contactsFragment.thirdContactNumber.setText(numbers.get(2));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void loadMapData(String eventId,String city){
        final DatabaseReference mapRef =
                FirebaseDatabase.getInstance().getReference()
                                .child("MapData").child(city).child(eventId);

        mapRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapInfo = dataSnapshot.getValue(MapInfo.class);
                NewEventSecondPage secondPage = (NewEventSecondPage)adapter.getItem(1);
                String adress = mapInfo.getAdress();

                secondPage.longitude = mapInfo.getLng();
                secondPage.latitude = mapInfo.getLat();
                secondPage.adress = mapInfo.getAdress();
                secondPage.createEventAutoAdress.setText(adress);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected DynamicData getDynamicData(){
        return dynamicData;
    }

    protected StaticData getStaticData(){
        return staticData;
    }

    //FINE metodi eventi in editMode

}
