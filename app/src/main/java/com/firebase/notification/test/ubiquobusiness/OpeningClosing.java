package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class OpeningClosing extends Fragment {


    @BindView(R.id.openingTime)
    TextView openingTime;
    @BindView(R.id.closingTime)
    TextView closingTime;
    @BindView(R.id.cameraButton)
    ImageButton cameraButton;
    @BindView(R.id.buttonLayout2)
    RelativeLayout buttonNext;
    @BindView(R.id.avatar)
    CircularImageView avatar;
    Unbinder unbinder;
    private final static int GALLERY_REQUEST_CODE = 0;

    public OpeningClosing() {
        // Required empty public constructor
    }

    public static OpeningClosing newInstance() {
        OpeningClosing openingClosing = new OpeningClosing();
        return openingClosing;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_opening_closing, container, false);

        // Inflate the layout for this fragment
        unbinder = ButterKnife.bind(this, rootView);



        //rimuove avatar fino a quando non viene scelto dalla galleria
        avatar.setVisibility(View.GONE);

        openingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog().newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        String opening = UbiQuoBusinessUtils.hourFormatter(hourOfDay,minute);
                        openingTime.setText("Apertura\n"+opening);
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("PLACE_OPENING_TIME",opening).commit();


                    }
                },true);
                timePickerDialog.vibrate(false);
                timePickerDialog.show(getActivity().getFragmentManager(),"hourpicker");
            }
        });

        closingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog().newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        String closing = UbiQuoBusinessUtils.hourFormatter(hourOfDay,minute);
                        closingTime.setText("Chiusura\n"+closing);
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("PLACE_CLOSING_TIME",closing).commit();

                    }
                },true);
                timePickerDialog.vibrate(false);
                timePickerDialog.show(getActivity().getFragmentManager(),"minutepicker");
            }
        });

        loadOldData();

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,GALLERY_REQUEST_CODE);
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canGoNext()){
                    ((Registration) getActivity()).registrationViewPager.setCurrentItem(2, true);
                }
            }
        });



        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            UbiQuoBusinessUtils.changeStatusBarColor(R.color.colorPrimary, getActivity());

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //handle galleria normale
        if(requestCode==GALLERY_REQUEST_CODE && resultCode==RESULT_OK){
            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setBorderLineColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary))
                    .setAspectRatio(1,1)
                    .start(getContext(),this);
        }

        //handle cropper
        //Handle del risultato di CropImage
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                avatar.setVisibility(View.VISIBLE);
                avatar.setImageURI(result.getUri());
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("PLACE_AVATAR",result.getUri().toString()).commit();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
        String startingTime = sharedPreferences.getString("PLACE_OPENING_TIME","NA");
        String endingTime = sharedPreferences.getString("PLACE_CLOSING_TIME","NA");
        String imageUri = sharedPreferences.getString("PLACE_AVATAR","NA");

        if(startingTime.isEmpty() || endingTime.isEmpty()){
            Toasty.error(getActivity(),"Imposta gli orari di apertura e chiusura",Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
        }

        return canGoNext;
    }

    private void loadOldData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS",Context.MODE_PRIVATE);
        String start = sharedPreferences.getString("PLACE_OPENING_TIME","NA");
        String end = sharedPreferences.getString("PLACE_CLOSING_TIME","NA");
        if(!start.isEmpty() && !start.equalsIgnoreCase("NA")){
            openingTime.setText("Apertura\n"+start);
        }


        if(!end.isEmpty() && !end.equalsIgnoreCase("NA")){
            closingTime.setText("Chiusura\n"+end);
        }
    }

}
