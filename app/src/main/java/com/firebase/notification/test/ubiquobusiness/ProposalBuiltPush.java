package com.firebase.notification.test.ubiquobusiness;

/**
 * Created by akain on 16/06/2017.
 */

public class ProposalBuiltPush {
    String proposal_id,organizer,organizerName;

    ProposalBuiltPush(){

    }

    public ProposalBuiltPush(String proposal_id, String organizer,String organizerName) {
        this.proposal_id = proposal_id;
        this.organizer = organizer;
        this.organizerName = organizerName;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getProposal_id() {
        return proposal_id;
    }

    public void setProposal_id(String proposal_id) {
        this.proposal_id = proposal_id;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }
}
