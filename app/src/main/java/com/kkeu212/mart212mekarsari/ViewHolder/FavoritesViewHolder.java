package com.kkeu212.mart212mekarsari.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kkeu212.mart212mekarsari.Interface.ItemClickListener;
import com.kkeu212.mart212mekarsari.R;

public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name,food_price;
    public ImageView food_image,fav_image,share_image,quick_cart;

    private ItemClickListener itemClickListener;

    public RelativeLayout view_background;
    public LinearLayout view_foreground;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;

    }

    public FavoritesViewHolder(View itemView) {
        super(itemView);

        food_name = (TextView)itemView.findViewById(R.id.food_name);
        food_image = (ImageView)itemView.findViewById(R.id.food_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);
        //Facebook Share
        share_image = (ImageView)itemView.findViewById(R.id.btnShare);
        //Facebook Share
        food_price = (TextView)itemView.findViewById(R.id.food_price);
        quick_cart = (ImageView)itemView.findViewById(R.id.btn_quck_cart);

        view_background = (RelativeLayout)itemView.findViewById(R.id.view_background);
        view_foreground = (LinearLayout)itemView.findViewById(R.id.view_foreground);


        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
