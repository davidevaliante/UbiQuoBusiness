package com.firebase.notification.test.ubiquobusiness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateEvent extends AppCompatActivity {

    @BindView(R.id.newEvenViewPager)
    CustomViewPager newEvenViewPager;
    private PagerAdapter adapter;
    protected Bundle proposal;
    protected String editEventIdString;
    protected static Business business;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        SharedPreferences sharedPreferences = getSharedPreferences("LAST_EVENT_DATA",Context.MODE_PRIVATE);

        ButterKnife.bind(this);



        proposal = getIntent().getBundleExtra("proposal_bundle");
        editEventIdString = getIntent().getStringExtra("edit_string_id");

        List<Fragment> createEventFragments = initializeFragments();

        adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),createEventFragments);
        newEvenViewPager.setAdapter(adapter);
        newEvenViewPager.setPagingEnabled(false);
        loadUserData(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());


    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> registrationFragments;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Fragment> registrationFragments) {
            super(fm);
            this.registrationFragments = registrationFragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.registrationFragments.get(position);
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
           NewEventFirstPage firstPage = (NewEventFirstPage)getSupportFragmentManager().getFragments().get(0);
            if(editEventIdString == null && proposal == null){
                firstPage.saveData();
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
                NewEventSecondPage secondPage =(NewEventSecondPage) getSupportFragmentManager().getFragments().get(1);

                if(editEventIdString == null && proposal==null) {
                    //carica i dati del locale nelle view corrispondenti
                    secondPage.loadPlaceData();
                }else{
                    secondPage.loadEditPlaceData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
