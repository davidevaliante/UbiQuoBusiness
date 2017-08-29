package com.firebase.notification.test.ubiquobusiness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindFloat;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Registration extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, DatePickerDialog.OnDateSetListener {

    protected FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Registration.ScreenSlidePagerAdapter pagerAdapter;
    private GoogleApiClient  mGoogleApiClient;
    protected static Business newBusiness = new Business();
    protected static Double cityLatitude, cityLongitude;
    protected static String croppedImagePath;
    protected static String employeeImage,employeeCity,employeeBirthday;
    protected static String accountType;
    protected List<Fragment> targetFragments;

    @BindView(R.id.registrationViewPager) CustomViewPager registrationViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        UbiQuoBusinessUtils.removeStatusBar(this);


        Integer type = getIntent().getIntExtra("ACCOUNT_TYPE",9);
        if(type==9){
            finish();
        }

        //locali
        if(type==0){
            targetFragments = initializeFragmentsForBusiness();
            pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),targetFragments);
            registrationViewPager.setOffscreenPageLimit(0);
            registrationViewPager.setPagingEnabled(false);
            registrationViewPager.setAdapter(pagerAdapter);
        }

        //employee
        if(type==1){
            targetFragments = initializeFragmentsForEmployee();
            pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),targetFragments);
            registrationViewPager.setOffscreenPageLimit(0);
            registrationViewPager.setPagingEnabled(false);
            registrationViewPager.setAdapter(pagerAdapter);
        }

        mGoogleApiClient = new GoogleApiClient
                                                .Builder(this)
                                                .addApi(Places.GEO_DATA_API)
                                                .addApi(Places.PLACE_DETECTION_API)
                                                .enableAutoManage(this, this)
                                                .build();





        mAuth = FirebaseAuth.getInstance();
        /*authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    Intent toUserPage = new Intent(Registration.this, MainUserPage.class);
                    startActivity(toUserPage);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);*/




    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toasty.error(Registration.this,"Google Maps connection error", Toast.LENGTH_SHORT, true).show();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        EmployeeFirstPageRegistration firstPage = (EmployeeFirstPageRegistration) pagerAdapter.getItem(0);
        Integer fixedMonth = monthOfYear+1;
        firstPage.employeeBirthday.setText(dayOfMonth+"/"+fixedMonth+"/"+year);
        employeeBirthday = dayOfMonth+"/"+fixedMonth+"/"+year;
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
    public void onBackPressed() {
        if (registrationViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            registrationViewPager.setCurrentItem(registrationViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    //lista di fragments per registrazione locali
    private List<Fragment> initializeFragmentsForBusiness(){
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(NamePlaceFragment.newInstance());
        fList.add(OpeningClosing.newInstance());
        fList.add(ArgumentsImageFragment.newInstance());


        return fList;
    }

    //lista di fragments per registrazione locali
    private List<Fragment> initializeFragmentsForEmployee(){
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(EmployeeFirstPageRegistration.newInstance());



        return fList;
    }


}
