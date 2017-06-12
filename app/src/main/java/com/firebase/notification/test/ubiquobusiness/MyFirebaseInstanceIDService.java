package com.firebase.notification.test.ubiquobusiness;

/**
 * Created by akain on 11/06/2017.
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private SharedPreferences user_data;
    private SharedPreferences.Editor editor;


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);

    }
    // [END refresh_token]



    //aggiorna/scrive il token nel nodo Token/{userId}/user_token
    private void sendRegistrationToServer(String token) {
        user_data = getSharedPreferences("HARLEE_USER_DATA", Context.MODE_PRIVATE);
        editor = user_data.edit();
        editor.putString("USER_TOKEN", token);
        editor.commit();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
            FirebaseDatabase.getInstance().getReference().child("Token").child(user_id).child(token);
        }



    }




}
