package com.kkeu212.mart212mekarsari.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import com.kkeu212.mart212mekarsari.Interface.ItemClickListener;
import com.kkeu212.mart212mekarsari.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress,txtOrderTotal,txtOrderCustomer,txtOrderOngkir,txtOrderNoted;

    private ItemClickListener itemClickListener;

    public ImageView btn_delete;

    public Button btnShipper,btnDetail;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderAddress = (TextView)itemView.findViewById(R.id.order_address);
        txtOrderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus = (TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone = (TextView)itemView.findViewById(R.id.order_phone);
        txtOrderCustomer = (TextView)itemView.findViewById(R.id.order_customer);
        txtOrderTotal = (TextView)itemView.findViewById(R.id.order_total);
        txtOrderOngkir = (TextView)itemView.findViewById(R.id.order_ongkir);
        txtOrderNoted = (TextView)itemView.findViewById(R.id.order_comment);
        btn_delete = (ImageView)itemView.findViewById(R.id.btn_delete);
        btnShipper  = (Button)itemView.findViewById(R.id.btnShipper);
        btnDetail = (Button)itemView.findViewById(R.id.btnDetail);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
