package com.firebase.notification.test.ubiquobusiness;


import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;
import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeeFirstPageRegistration extends Fragment {


    @BindView(R.id.employeeName)
    EditText employeeName;
    @BindView(R.id.employeeSurname)
    EditText employeeSurname;

    @BindView(R.id.employeeBirthday)
    EditText employeeBirthday;
    @BindView(R.id.employeeImagePicker)
    ImageView employeeImagePicker;
    @BindView(R.id.employeeImage)
    CircularImageView employeeImage;
    @BindView(R.id.hasCarButton)
    RadioRealButton hasCarButton;
    @BindView(R.id.noCarButton)
    RadioRealButton noCarButton;
    @BindView(R.id.hasCarGroup)
    RadioRealButtonGroup hasCarGroup;
    @BindView(R.id.employeeFirstNextButton)
    RelativeLayout employeeFirstNextButton;
    Unbinder unbinder;

    private Geocoder mGeocoder;
    protected SupportPlaceAutocompleteFragment employeeAutoCity;
    private CardView cityCard;
    private String city;
    private Boolean hasCar = true;
    private final static Integer GALLERY_REQUEST_CODE = 0;
    private final static String BIRTHDAY_TAG = "e_birthday";

    public EmployeeFirstPageRegistration() {
        // Required empty public constructor
    }

    public static EmployeeFirstPageRegistration newInstance() {
        EmployeeFirstPageRegistration newFrag = new EmployeeFirstPageRegistration();
        return newFrag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_employee_first_page_registration, container, false);
        unbinder = ButterKnife.bind(this, viewGroup);

        mGeocoder = new Geocoder(getActivity(), Locale.getDefault());

        employeeImage.setVisibility(View.GONE);

        AutocompleteFilter filter =
                new AutocompleteFilter.Builder().setCountry("IT").build();

        employeeAutoCity = (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.employeeAutoCity);
        cityCard = (CardView)viewGroup.findViewById(R.id.cemployeeCity);

        employeeBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog
                        = DatePickerDialog.newInstance(
                        ((Registration)getActivity()),
                        2017,
                        0,
                        1
                );
                datePickerDialog.setAccentColor(Color.parseColor("#673AB7"));
                datePickerDialog.setCancelColor(Color.parseColor("#18FFFF"));
                datePickerDialog.vibrate(false);
                datePickerDialog.showYearPickerFirst(true);
                datePickerDialog.show(((Registration)getActivity()).getFragmentManager(),"DatepickerDialog");
            }
        });


        employeeAutoCity.setHint("Scegli la tua zona lavorativa");
        cityCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasty.info(getActivity(),"E' preferibile inserire la provincia per avere maggiore visibilità", Toast.LENGTH_SHORT,true).show();
            }
        });
        employeeAutoCity.setFilter(filter);
        employeeAutoCity.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                cityMapHandler(place);
            }

            @Override
            public void onError(Status status) {

            }
        });

        hasCarGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if(position==0){
                    hasCar = true;
                }

                if(position==1){
                    hasCar = false;
                }
            }
        });

        employeeImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        employeeFirstNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canGoNext()){
                    Toast.makeText(getActivity(), "Puoi andare avanti", Toast.LENGTH_SHORT).show();
                }else{
                    Toasty.error(getActivity(),"Riempi tutti i campi",Toast.LENGTH_SHORT,true).show();
                }
            }
        });





        return viewGroup;
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
                employeeImage.setVisibility(View.VISIBLE);
                employeeImage.setImageURI(result.getUri());
                employeeImage.setBorderColor(R.color.colorAccent);

                //se viene cambiata l'immagine
                ((Registration)getActivity()).employeeImage = result.getUri().toString();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void cityMapHandler(Place target_place) {
        Place place = target_place;
        Double latitude = place.getLatLng().latitude;
        Double longitude = place.getLatLng().longitude;

        try {
            String cityName = getCityNameByCoordinates(latitude, longitude);
            employeeAutoCity.setText(cityName);
            city = cityName;
            ((Registration)getActivity()).employeeCity = city;
        } catch (IOException e) {
            e.printStackTrace();
        }


        LatLng upper = new LatLng(latitude, longitude);
        LatLng lower = new LatLng(latitude, longitude);

    }

    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }

    private Boolean canGoNext(){
        Boolean cangoNext = true;

        String name = employeeName.getText().toString().trim();
        String surname = employeeSurname.getText().toString().trim();
        String birthday = ((Registration)getActivity()).employeeBirthday;
        String city = ((Registration)getActivity()).employeeCity;

        if(birthday == null || city == null){
            return false;
        }

        if(name.isEmpty() || surname.isEmpty() || birthday.isEmpty() || city.isEmpty()){
            return false;
        }

        return cangoNext;
    }

}
