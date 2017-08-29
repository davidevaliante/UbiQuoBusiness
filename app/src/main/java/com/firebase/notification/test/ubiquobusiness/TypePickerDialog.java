package com.firebase.notification.test.ubiquobusiness;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class TypePickerDialog extends DialogFragment {
    private RelativeLayout organizerButton, employeeButton, performerButton;

    public TypePickerDialog() {
        // Required empty public constructor
    }

    public static TypePickerDialog newInstance(){
        TypePickerDialog pickerDialog = new TypePickerDialog();
        return pickerDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_type_picker_dialog, container,false);
        organizerButton = (RelativeLayout)viewGroup.findViewById(R.id.organizerButton);
        employeeButton = (RelativeLayout)viewGroup.findViewById(R.id.employeeButton);
        performerButton = (RelativeLayout)viewGroup.findViewById(R.id.performerButton);

        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegistrationAsOrganizer = new Intent(getActivity(),Registration.class);
                toRegistrationAsOrganizer.putExtra("ACCOUNT_TYPE",0);
                startActivity(toRegistrationAsOrganizer);
                dismiss();
            }
        });

        employeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegistrationAsEmployee = new Intent(getActivity(),Registration.class);
                toRegistrationAsEmployee.putExtra("ACCOUNT_TYPE",1);
                startActivity(toRegistrationAsEmployee);
                dismiss();
            }
        });

        performerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegistrationAsPerformer = new Intent(getActivity(),Registration.class);
                toRegistrationAsPerformer.putExtra("ACCOUNT_TYPE",2);
                startActivity(toRegistrationAsPerformer);
                dismiss();
            }
        });


        return viewGroup;
    }



}
