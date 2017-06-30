package com.firebase.notification.test.ubiquobusiness;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment {

    protected RecyclerView eventRecycler;
    protected FirebaseRecyclerAdapter recyclerAdapter;
    protected DatabaseReference eventReference;
    protected String placeName,place_city,place_id;


    public EventFragment() {
        // Required empty public constructor
    }

    public static EventFragment newInstance(){
        EventFragment newFrag = new EventFragment();
        return  newFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_event,container,false);

        eventRecycler = (RecyclerView)rootView.findViewById(R.id.eventRecycler);
        eventRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        eventRecycler.setHasFixedSize(true);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UBIQUO_BUSINESS", Context.MODE_PRIVATE);
        place_city = sharedPreferences.getString("PLACE_CITY","NA");
        place_id = sharedPreferences.getString("PLACE_ID","NA");
        placeName = sharedPreferences.getString("PLACE_NAME","NA");
        eventReference = FirebaseDatabase.getInstance().getReference().child("BusinessesEvents").child(place_city).child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerAdapter = new FirebaseRecyclerAdapter<Boolean,EventViewHolder>(Boolean.class,R.layout.event_card,EventViewHolder.class,eventReference) {
            @Override
            protected void populateViewHolder(final EventViewHolder viewHolder, final Boolean model, int position) {
                final String eventId = getRef(position).getKey();

                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final DynamicData eventData = dataSnapshot.getValue(DynamicData.class);
                        viewHolder.setTitle(eventData.geteName());
                        viewHolder.setDate(eventData.getDate());
                        viewHolder.setImage(eventData.getiPath(),getActivity());
                        viewHolder.setLikes(eventData.getLike());
                        FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(place_city).child(eventId).removeEventListener(this);
                        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent editEvent = new Intent(getActivity(),CreateEvent.class);
                                String id = eventId;

                                editEvent.putExtra("edit_string_id", id);
                                editEvent.putExtra("edit_string_city",place_city);
                                startActivity(editEvent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(place_city).child(eventId).addValueEventListener(eventListener);




            }


        };
        eventRecycler.setAdapter(recyclerAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        eventRecycler.setAdapter(null);
        recyclerAdapter.cleanup();
    }
}
