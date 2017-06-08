package com.firebase.notification.test.ubiquobusiness;

import android.content.Intent;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindFloat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class Registration extends FragmentActivity {
    protected FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private PagerAdapter pagerAdapter;
    @BindView(R.id.registrationViewPager) ViewPager registrationViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        List<Fragment> registrationFragements = initializeFragments();
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),registrationFragements);
        registrationViewPager.setAdapter(pagerAdapter);

        mAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    Intent toUserPage = new Intent(Registration.this, MainUserPage.class);
                    startActivity(toUserPage);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);


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
        mAuth.removeAuthStateListener(authStateListener);
    }


    private List<Fragment> initializeFragments(){
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(NamePlaceFragment.newInstance());
        fList.add(OpeningClosing.newInstance());
        fList.add(ArgumentsImageFragment.newInstance());

        return fList;
    }
}
