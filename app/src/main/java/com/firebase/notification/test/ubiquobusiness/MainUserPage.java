package com.firebase.notification.test.ubiquobusiness;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.haha.perflib.Main;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainUserPage extends AppCompatActivity {


    @BindView(R.id.mainUserPageViewPager)
    ViewPager viewPager;
    @BindView(R.id.fabAdd)
    FloatingActionButton fabAdd;

    private FirebaseAuth mAuth;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_page);
        ButterKnife.bind(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mAuth = FirebaseAuth.getInstance();
        List<Fragment> userPageFragments = initializeFragments();
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), userPageFragments);
        viewPager.setAdapter(pagerAdapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newEvent = new Intent(MainUserPage.this,CreateEvent.class);
                startActivity(newEvent);
            }
        });


        //token refresher
        final Handler firebaseTokenHandler = new Handler();
        firebaseTokenHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //refresha il token
                UbiQuoBusinessUtils.refreshCurrentUserToken(getApplication());
            }
        }, 5000);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private List<Fragment> initializeFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(ProposalsFragement.newInstance());


        return fList;
    }

    private void logMeOut() {
        FirebaseAuth.getInstance().signOut();
        finish();
    }
}
