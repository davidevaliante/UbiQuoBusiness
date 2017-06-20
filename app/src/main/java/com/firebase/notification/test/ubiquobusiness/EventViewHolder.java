package com.firebase.notification.test.ubiquobusiness;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by akain on 19/06/2017.
 */

public class EventViewHolder extends RecyclerView.ViewHolder {
    View mView;
    CircularImageView image;
    TextView title, likes, date;
    public RelativeLayout edit;

    public EventViewHolder(View itemView) {
        super(itemView);
        mView = itemView;

        image = (CircularImageView)itemView.findViewById(R.id.card_image);
        title = (TextView)itemView.findViewById(R.id.card_title);
        likes = (TextView)itemView.findViewById(R.id.card_likes);
        date = (TextView)itemView.findViewById(R.id.card_date);
        edit = (RelativeLayout)itemView.findViewById(R.id.editButton);


    }

    public void setImage(String path, Activity activity){
        Glide.with(activity).load(path).asBitmap().into(image);

    }

    public void setTitle(String mTitle){
        title.setText(mTitle);
    }

    public void setLikes(Integer mLikes){
        if(mLikes==0) {
            likes.setText("Ancora nessuno interessao");
        }

        if(mLikes==1) {

            likes.setText("1 Interessato");
        }

         if(mLikes>1){
             likes.setText(mLikes+" Interessati");
         }
    }

    public void setDate(Long mDate){
        date.setText(UbiQuoBusinessUtils.fromMillisToStringDate(mDate)+ "          "+UbiQuoBusinessUtils.fromMillisToStringTime(mDate) );
    }
}
