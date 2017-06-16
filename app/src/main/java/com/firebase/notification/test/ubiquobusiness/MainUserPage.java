package com.firebase.notification.test.ubiquobusiness;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.hlab.fabrevealmenu.enums.Direction;
import com.hlab.fabrevealmenu.model.FABMenuItem;
import com.hlab.fabrevealmenu.view.FABRevealMenu;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainUserPage extends AppCompatActivity {


    @BindView(R.id.mainUserPageViewPager)
    ViewPager viewPager;
    @BindView(R.id.my_tb)
    Toolbar myTb;


    private FirebaseAuth mAuth;
    private PagerAdapter pagerAdapter;

    private ArrayList<FABMenuItem> items;
    private String[] mDirectionStrings = {"Direction - LEFT", "Direction - UP"};
    private Direction currentDirection = Direction.LEFT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user_page);
        ButterKnife.bind(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Toolbar toolbar = (Toolbar)findViewById(R.id.my_tb);

        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.pureWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.vector_burger_menu_24));


        mAuth = FirebaseAuth.getInstance();
        List<Fragment> userPageFragments = initializeFragments();
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), userPageFragments);
        viewPager.setAdapter(pagerAdapter);


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

    private void initItems(boolean toShowDoubleItems) {
        items = new ArrayList<>();
        for (int i = 0; i < (toShowDoubleItems ? 10 : 5); i++) {
            items.add(new FABMenuItem("Item " + i, BitmapFactory.decodeResource(getResources(), R.drawable.accent_clock_14)));
        }
    }

    @Override
    public void onBackPressed() {
        Fragment frag = getSupportFragmentManager().getFragments().get(0);
        FABRevealMenu fabAdd = (FABRevealMenu) frag.getView().findViewById(R.id.fabAdd);
        if (fabAdd.isShowing())
            fabAdd.closeMenu();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fab_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.logout){
            logMeOut();
        }
        return super.onOptionsItemSelected(item);
    }
}
