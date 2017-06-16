package com.firebase.notification.test.ubiquobusiness;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.hlab.fabrevealmenu.enums.Direction;
import com.hlab.fabrevealmenu.listeners.OnFABMenuSelectedListener;
import com.hlab.fabrevealmenu.model.FABMenuItem;
import com.hlab.fabrevealmenu.view.FABRevealMenu;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ProposalsFragement extends Fragment {


    protected RecyclerView pRecyclerView;
    protected FloatingActionButton addProposal;
    protected FirebaseRecyclerAdapter proposalAdapter;
    protected @BindView(R.id.mainFab)
    FloatingActionButton mainFab;
    protected @BindView(R.id.fabAdd)
    FABRevealMenu fabAdd;
    Unbinder unbinder;
    private DatabaseReference proposalRef, proposalUserLikeRef, userProposalLikeRef;
    protected String current_city;
    protected LinearLayoutManager proposalLayoutManager;
    private final String PROPOSAL_RECYCLER_LAYOUT = "RECYCLERVIEW_PROPOSAL_STATE";
    private Parcelable rcPropState;
    private Boolean processClick = true;
    private ArrayList<FABMenuItem> items;



    public ProposalsFragement() {
        // Required empty public constructor
    }

    public static ProposalsFragement newInstance() {
        ProposalsFragement newFrag = new ProposalsFragement();
        return newFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_proposals_fragement, container, false);

        pRecyclerView = (RecyclerView) rootView.findViewById(R.id.proposalRecycler);

        SharedPreferences userPrefs = getActivity().getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);

        current_city = userPrefs.getString("USER_CITY", "Isernia");
        proposalRef = FirebaseDatabase.getInstance().getReference().child("Proposals").child(current_city);
        proposalRef.keepSynced(true);
        proposalUserLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("Proposals");
        proposalUserLikeRef.keepSynced(true);
        userProposalLikeRef = FirebaseDatabase.getInstance().getReference().child("Likes").child("ProposalsUserLike");
        userProposalLikeRef.keepSynced(true);
        pRecyclerView.setHasFixedSize(true);
        proposalLayoutManager = new LinearLayoutManager(getActivity());

        proposalLayoutManager.setReverseLayout(true);
        proposalLayoutManager.setStackFromEnd(true);

        pRecyclerView.setLayoutManager(proposalLayoutManager);
        pRecyclerView.getItemAnimator().setChangeDuration(0);


        unbinder = ButterKnife.bind(this, rootView);

        initItems(false);
        if(fabAdd!=null && mainFab!=null){
            //View fab_menu = inflater.inflate(R.layout.fab_menu,container,false);
            //fabAdd.setCustomView(fab_menu);
            fabAdd.setMenuItems(items);
            fabAdd.bindAncherView(mainFab);
            fabAdd.setShowOverlay(true);

            fabAdd.setOnFABMenuSelectedListener(new OnFABMenuSelectedListener() {
                @Override
                public void onMenuItemSelected(View view) {
                    int position = (int) view.getTag();
                    if(position==0){
                        Intent createEvent = new Intent(getActivity(),CreateEvent.class);
                        startActivity(createEvent);
                    }
                    if(position==1){
                        Intent createProposal = new Intent(getActivity(),CreateProposal.class);
                        startActivity(createProposal);
                    }
                }
            });

        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (pRecyclerView.getAdapter() == null) {

            proposalAdapter = new FirebaseRecyclerAdapter<Proposal, ProposalViewholder>(

                    Proposal.class,
                    R.layout.proposal_card_layout,
                    ProposalViewholder.class,
                    proposalRef.orderByChild("creationTime")) {

                @Override
                protected void populateViewHolder(final ProposalViewholder viewHolder, final Proposal model, final int position) {
                    Long currentTime = System.currentTimeMillis();
                    final String post_key = getRef(position).getKey();
                    final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    viewHolder.setTheme(model.getArgument(), getActivity());
                    viewHolder.setDescription(model.getDescription());
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setPeopleInterested(model.getLikes());
                    viewHolder.setPlacesNotified(model.getPlaces());
                    viewHolder.setElapsedTime(currentTime, model.getCreationTime());
                    viewHolder.interestButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           /* if (processClick) {
                                processClick = false;
                                //metodo che legge se il like è presente oppure no e chiama un metodo agiuntivo
                                //di conseguenza
                                likeCheckerForLikeProcess(user_id, post_key, viewHolder.interestButton, model.getArgument());
                            }*/
                            final FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(getActivity())
                                    .setImageRecourse(R.drawable.ic_balloons_24)
                                    .setTextTitle("Ricorda")
                                    .setBody("Creando un nuovo evento partendo da una proposta, gli utenti interessati riceveranno una notifica di avvenuta creazione da parte tua")
                                    .setNegativeColor(R.color.colorPrimary)
                                    .setNegativeButtonText("Annulla")
                                    .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
                                        @Override
                                        public void OnClick(View view, Dialog dialog) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButtonText("Continua")
                                    .setPositiveColor(R.color.matte_blue)
                                    .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                                        @Override
                                        public void OnClick(View view, Dialog dialog) {
                                           Intent createProposal = new Intent(getActivity(),CreateEvent.class);
                                           createProposal.putExtra("proposal_bundle",fecthDataToCreateEvent(model,post_key));
                                           startActivity(createProposal);
                                            dialog.dismiss();
                                        }
                                    })
                                 /* .setAutoHide(true)*/
                                    .build();
                            alert.show();

                        }
                    });


                    //listener per capire se la proposta ha già il like oppure no
                    ValueEventListener likeProposalListener = new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(user_id)) {
                                viewHolder.likingTransition(model.argument, getActivity());
                                proposalRef.removeEventListener(this);
                            } else {
                                viewHolder.dislikeTransition(model.argument, getActivity());
                                proposalRef.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    proposalUserLikeRef.child(post_key).addListenerForSingleValueEvent(likeProposalListener);


                }


            };

        }
        pRecyclerView.setAdapter(proposalAdapter);
        if (rcPropState != null) {
            pRecyclerView.getLayoutManager().onRestoreInstanceState(rcPropState);
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        pRecyclerView.setAdapter(null);
        proposalAdapter.cleanup();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pRecyclerView.setAdapter(null);
        proposalAdapter.cleanup();
        unbinder.unbind();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pRecyclerView.setAdapter(null);
        proposalAdapter.cleanup();
    }

    //aggiunge il like e rende il pulsante nuovamente cliccabile
    private void addLikeToProposal(String userId, final String proposalId) {
        proposalUserLikeRef.child(proposalId).child(userId).setValue(true);
        userProposalLikeRef.child(userId).child(proposalId).setValue(true);
        //transaction
        proposalRef.child(proposalId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Proposal proposal = mutableData.getValue(Proposal.class);
                if (proposal == null) {
                    return Transaction.success(mutableData);
                }
                proposal.likes++;
                mutableData.setValue(proposal);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                processClick = true;
            }
        });

    }

    //rimuove il like e rende il pulsante nuovamente cliccabile
    private void removeLikeToProposal(String userId, final String proposalId) {
        proposalUserLikeRef.child(proposalId).child(userId).removeValue();
        userProposalLikeRef.child(userId).child(proposalId).removeValue();
        //transaction
        proposalRef.child(proposalId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Proposal proposal = mutableData.getValue(Proposal.class);
                if (proposal == null) {
                    return Transaction.success(mutableData);
                }
                proposal.likes--;
                mutableData.setValue(proposal);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                processClick = true;
            }
        });
    }

    private void likeCheckerForLikeProcess(final String userId, final String proposalId, final View target, final String argument) {
        //listener per capire se la proposta ha già il like oppure no
        ValueEventListener likeProposalListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    //like già presente
                    removeLikeToProposal(userId, proposalId);
                    proposalRef.removeEventListener(this);
                } else {
                    //like non presente
                    addLikeToProposal(userId, proposalId);
                    proposalRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        proposalUserLikeRef.child(proposalId).addListenerForSingleValueEvent(likeProposalListener);
    }

    //restituisce int color in base all'argomento della proposta
    private Integer colorArgumentSwitcher(String argument) {
        switch (argument) {
            case "cocktail":
                return ContextCompat.getColor(getActivity(), R.color.cocktail_green);

            case "dance":
                return ContextCompat.getColor(getActivity(), R.color.dance_red);

            case "music":
                return ContextCompat.getColor(getActivity(), R.color.music_blue);

            case "party":
                return ContextCompat.getColor(getActivity(), R.color.party_orange);

            case "themed":
                return ContextCompat.getColor(getActivity(), R.color.themed_purple);

            default:
                return ContextCompat.getColor(getActivity(), R.color.colorPrimary);

        }

    }

    private void initItems(boolean toShowDoubleItems) {
        items = new ArrayList<>();
            items.add(new FABMenuItem("Crea nuovo evento", ContextCompat.getDrawable(getActivity(),R.drawable.party_62)));
            items.add(new FABMenuItem("Aggiungi nuova proposta", ContextCompat.getDrawable(getActivity(),R.drawable.vector_right_arrow_18)));


    }

    private Bundle fecthDataToCreateEvent(Proposal model, String postKey){
        Bundle basic_data = new Bundle();
        basic_data.putString("title",model.getTitle());
        basic_data.putString("description",model.getDescription());
        basic_data.putString("argument",model.getArgument());
        basic_data.putString("city",model.getCity());
        basic_data.putString("id",postKey);
        return basic_data;
    }


}
