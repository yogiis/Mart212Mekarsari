package com.kkeu212.mart212mekarsari;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.kkeu212.mart212mekarsari.Common.Common;
import com.kkeu212.mart212mekarsari.ViewHolder.OrderDetailAdapter;




public class OrderDetail extends AppCompatActivity {

    TextView order_id,order_phone,order_address,order_total,order_comment,order_customer,order_ongkir;
    String order_id_value="";
    RecyclerView listFoods;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        order_id = (TextView)findViewById(R.id.order_id);
        order_customer = (TextView)findViewById(R.id.order_customer);
        order_phone = (TextView)findViewById(R.id.order_phone);
        order_total = (TextView)findViewById(R.id.order_total);
        order_ongkir = (TextView)findViewById(R.id.order_ongkir);
        order_address = (TextView)findViewById(R.id.order_address);
        order_comment = (TextView)findViewById(R.id.order_comment);

        listFoods = (RecyclerView)findViewById(R.id.listFoods);
        listFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listFoods.setLayoutManager(layoutManager);

        if(getIntent()!= null)
            order_id_value = getIntent().getStringExtra("OrderId");

        //set value
        order_id.setText(order_id_value);
        order_customer.setText(Common.currentRequest.getCustomer());
        order_phone.setText(Common.currentRequest.getPhone());
        order_total.setText(Common.currentRequest.getTotal());
        order_ongkir.setText(Common.currentRequest.getSumongkir());
        order_address.setText(Common.currentRequest.getAddress());
        order_comment.setText(Common.currentRequest.getComment());


        OrderDetailAdapter adapter = new OrderDetailAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        listFoods.setAdapter(adapter);

    }
}