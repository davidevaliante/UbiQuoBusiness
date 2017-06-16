package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


public class NewEventFirstPage extends Fragment {


    Unbinder unbinder;
    @BindView(R.id.cameraPicker)
    ImageButton cameraPicker;
    @BindView(R.id.eventName)
    EditText eventName;
    @BindView(R.id.eventDescription)
    EditText eventDescription;
    private static final Integer GALLERY_REQUEST_CODE = 1;
    @BindView(R.id.imagePicker)
    CircularImageView imagePicker;
    @BindView(R.id.freeRadioButton)
    RadioRealButton freeRadioButton;
    @BindView(R.id.payRadioButton)
    RadioRealButton payRadioButton;
    @BindView(R.id.radioRealButtonGroup)
    RadioRealButtonGroup radioRealButtonGroup;
    @BindView(R.id.eventPrice)
    EditText eventPrice;
    @BindView(R.id.eventNextFirst)
    RelativeLayout eventNextFirst;

    private Boolean isFree = true;

    public NewEventFirstPage() {
        // Required empty public constructor
    }

    public static NewEventFirstPage newInstance() {
        NewEventFirstPage newFragement = new NewEventFirstPage();
        return newFragement;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_new_event_first_page, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        imagePicker.setVisibility(View.GONE);
        eventPrice.setVisibility(View.GONE);

        //imagePicker.setVisibility(View.GONE);
        cameraPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        radioRealButtonGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if (position == 0) {
                    eventPrice.setVisibility(View.GONE);
                    isFree = true;
                } else {
                    eventPrice.setVisibility(View.VISIBLE);
                    isFree=false;
                }
            }
        });

        eventNextFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canGoNext()){
                    saveData();
                    ((CreateEvent)getActivity()).newEvenViewPager.setCurrentItem(1,true);
                }
            }
        });

        loadLastData();

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //handle galleria normale
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setBorderLineColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }

        //handle cropper
        //Handle del risultato di CropImage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imagePicker.setVisibility(View.VISIBLE);
                imagePicker.setImageURI(result.getUri());
                imagePicker.setBorderColor(R.color.colorAccent);
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("EVENT_IMAGE", result.getUri().toString()).commit();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private Boolean canGoNext(){
        Boolean canGoNext = true;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
        String eventImage = sharedPreferences.getString("EVENT_IMAGE","NA");
        String eventTitle = eventName.getText().toString().trim();
        String description = eventDescription.getText().toString().trim();
        String price = eventPrice.getText().toString().trim();
        if(eventImage.isEmpty() || eventImage.equalsIgnoreCase("NA")){
            Toasty.error(getActivity(),"Scegli un immagine per l'evento", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(eventTitle.isEmpty()){
            Toasty.error(getActivity(),"Inserisci un titolo", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(description.isEmpty()){
            Toasty.error(getActivity(),"Inserisci una descrizione", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(description.length() < 30){
            Toasty.error(getActivity(),"La descrizione deve contenere almeno 30 caratteri", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        if(!isFree && price.isEmpty()){
            Toasty.error(getActivity(),"Hai dimenticato il prezzo di ingresso", Toast.LENGTH_SHORT,true).show();
            canGoNext = false;
            return false;
        }

        return canGoNext;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void loadLastData(){
        Bundle bundle = ((CreateEvent)getActivity()).proposal;

        if(bundle == null) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
            String imageUri = sharedPreferences.getString("EVENT_IMAGE", "NA");
            String title = sharedPreferences.getString("EVENT_TITLE", "NA");
            String description = sharedPreferences.getString("EVENT_DESCRIPTION", "NA");
            Boolean free = sharedPreferences.getBoolean("EVENT_IS_FREE", true);

            if (!free) {
                Integer price = sharedPreferences.getInt("EVENT_PRICE", 0);
                radioRealButtonGroup.setPosition(1);
                eventPrice.setVisibility(View.VISIBLE);
                eventPrice.setText("" + price);
                isFree = false;
            }

            if (!title.equalsIgnoreCase("NA") && !title.isEmpty()) {
                eventName.setText(title);
            }

            if (!description.equalsIgnoreCase("NA") && !description.isEmpty()) {
                eventDescription.setText(description);
            }

            if (!imageUri.equalsIgnoreCase("NA")) {
                imagePicker.setVisibility(View.VISIBLE);
                imagePicker.setBorderColor(R.color.colorAccent);
                Uri uri = Uri.parse(imageUri);
                imagePicker.setImageURI(uri);
            }
        }else{
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
            String title = bundle.getString("title","NA");
            String description = bundle.getString("description","NA");
            String proposalId = bundle.getString("id","NA");
            Boolean free = sharedPreferences.getBoolean("EVENT_IS_FREE", true);



            if(!proposalId.isEmpty() && !proposalId.equalsIgnoreCase("NA")){
                sharedPreferences.edit().putString("PROPOSAL_ID",proposalId).commit();
            }

            if (!free) {
                Integer price = sharedPreferences.getInt("EVENT_PRICE", 0);
                radioRealButtonGroup.setPosition(1);
                eventPrice.setVisibility(View.VISIBLE);
                eventPrice.setText("" + price);
                isFree = false;
            }

            if (!title.equalsIgnoreCase("NA") && !title.isEmpty()) {
                eventName.setText(title);
            }

            if (!description.equalsIgnoreCase("NA") && !description.isEmpty()) {
                eventDescription.setText(description);
            }




        }
    }

    protected void saveData(){
        Bundle bundle = ((CreateEvent)getActivity()).proposal;
        //salva solo se non derivato da proposta
        if(bundle == null) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LAST_EVENT_DATA", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("EVENT_TITLE", UbiQuoBusinessUtils.capitalize(eventName.getText().toString().trim())).commit();
            sharedPreferences.edit().putString("EVENT_DESCRIPTION", UbiQuoBusinessUtils.capitalize(eventDescription.getText().toString().trim())).commit();
            sharedPreferences.edit().putBoolean("EVENT_IS_FREE", isFree).commit();

            if (!isFree) {
                if (!eventPrice.getText().toString().trim().isEmpty()) {
                    sharedPreferences.edit().putInt("EVENT_PRICE", Integer.valueOf(eventPrice.getText().toString().trim())).commit();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


}
