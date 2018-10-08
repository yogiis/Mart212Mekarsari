package com.kkeu212.mart212mekarsari;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.kkeu212.mart212mekarsari.Common.Common;
import com.kkeu212.mart212mekarsari.Common.Config;
import com.kkeu212.mart212mekarsari.Database.Database;
import com.kkeu212.mart212mekarsari.Helper.RecyclerItemTouchHelper;
import com.kkeu212.mart212mekarsari.Interface.RecyclerItemTouchHelperListener;
import com.kkeu212.mart212mekarsari.Model.DataMessage;
import com.kkeu212.mart212mekarsari.Model.MyResponse;
import com.kkeu212.mart212mekarsari.Model.Order;
import com.kkeu212.mart212mekarsari.Model.Request;
import com.kkeu212.mart212mekarsari.Model.Token;
import com.kkeu212.mart212mekarsari.Model.User;
import com.kkeu212.mart212mekarsari.Remote.APIService;
import com.kkeu212.mart212mekarsari.Remote.IGoogleAPI;
import com.kkeu212.mart212mekarsari.Remote.IGoogleService;
import com.kkeu212.mart212mekarsari.ViewHolder.CartAdapter;
import com.kkeu212.mart212mekarsari.ViewHolder.CartViewHoder;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerItemTouchHelperListener {

    private static final int PAYPAL_REQUEST_CODE = 9999;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice, sumOngkir;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    String position1, tvDistance;

    //Paypal Payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    String address, comment, customer;


    Place shippingAddress;

    AutocompleteFilter typeFilter;

    TextView txtCalculate;


    //location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FATEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;

    //Deklarasi Google Map Api Retrofit
    IGoogleService mGoogleMapService;
    APIService mService;
    IGoogleAPI mapsService;

    RelativeLayout rootLayout;


    //Font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        //GPS Setting
        isGPSEnable();

        //init Paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/gojek.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        //init
        mGoogleMapService = Common.getGoogleMapAPI();

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

/////////////////RUNTIME PERMISSION FIX/////////////////////////
        //checkPlayServicesme permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)

        {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }
        /////////////////RUNTIME PERMISSION FIX/////////////////////////


        mService = Common.getFCMService();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Inint
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice = findViewById(R.id.total_price);
        btnPlace = findViewById(R.id.btnPlaceOrder);


        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cart.size() > 0)
                    if (mLastLocation != null) {
                        try {
                            showAlertDialog();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(Cart.this, "Harap Aktifkan GPS", Toast.LENGTH_SHORT).show();
                        isGPSEnable();
                    }
                else
                    Toast.makeText(Cart.this, "Keranjang belanja anda kosong !!!", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();
    }


    //GPS Setting
    public void isGPSEnable() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }
    //GPS Setting


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }
/////////////////RUNTIME PERMISSION FIX/////////////////////////

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            else {
                Toast.makeText(this, "Device tidak mendukung", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Satu langkah ,Oke!");
        alertDialog.setMessage("Masukan Alamat Anda : ");

        final LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

        sumOngkir = order_address_comment.findViewById(R.id.txtCalculate);


        //final MaterialEditText edtAddress = (MaterialEditText)order_address_comment.findViewById(R.id.edtAddress);
        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        //Filter Address
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        /////FILTER LOCATION/////
        LatLng center = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        LatLng northside = SphericalUtil.computeOffset(center, 1000, 0);
        LatLng southSide = SphericalUtil.computeOffset(center, 1000, 180);

        final LatLngBounds bounds = LatLngBounds.builder()
                .include(northside)
                .include(southSide)
                .build();

        edtAddress.setBoundsBias(bounds);
        edtAddress.setFilter(typeFilter);

        /////FILTER LOCATION/////

        //ESTIMATE

        double lati = Double.parseDouble(String.valueOf(-6.361177));
        double lngi = Double.parseDouble(String.valueOf(106.870704));
        LatLng position1 = new LatLng(lati, lngi);


        mapsService = Common.getGoogleService();
        // getPrice(String.valueOf(position1),String.valueOf(edtAddress));

        //ESTIMATE


        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Tuliskan Alamat Jalan Anda");
        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(16);


        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;

                double lati = Double.parseDouble(String.valueOf(-6.361177));
                double lngi = Double.parseDouble(String.valueOf(106.870704));
                LatLng position1 = new LatLng(lati, lngi);

                Location locationA = new Location("point A");

                locationA.setLatitude(lati);
                locationA.setLongitude(lngi);

                Location locationB = new Location("point B");

                locationB.setLatitude(shippingAddress.getLatLng().latitude);
                locationB.setLongitude(shippingAddress.getLatLng().longitude);


                DecimalFormat format = new DecimalFormat("#.##");
                double distance = locationA.distanceTo(locationB) / 1000;
                //txtCalculate.setText(format.format(distance) + " Km");
                Locale locale = new Locale("in", "ID");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                double ongkir = distance * 4000;
                sumOngkir.setText(format.format(distance) + " Km " + " Ongkir Anda " + fmt.format(ongkir));

            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR", status.getStatusMessage());
            }
        });

        final MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);
        final MaterialEditText edtCustomer = (MaterialEditText) order_address_comment.findViewById(R.id.edtCustomer);

        //Radio
        final RadioButton rdiShipToAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiCOD = (RadioButton) order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal = (RadioButton) order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiBalance = (RadioButton) order_address_comment.findViewById(R.id.rdi212MartBalance);


        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (Common.currentUser.getHomeAddress() != null ||
                            !TextUtils.isEmpty(Common.currentUser.getHomeAddress())) {
                        address = Common.currentUser.getHomeAddress();
                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);

                        double lati = Double.parseDouble(String.valueOf(-6.361177));
                        double lngi = Double.parseDouble(String.valueOf(106.870704));
                        LatLng position1 = new LatLng(lati, lngi);

                        Location locationA = new Location("point A");

                        locationA.setLatitude(lati);
                        locationA.setLongitude(lngi);

                        Location locationB = new Location("point B");

                        locationB.setLatitude(mLastLocation.getLatitude());
                        locationB.setLongitude(mLastLocation.getLongitude());

                        DecimalFormat format = new DecimalFormat("#.##");
                        double distance = locationA.distanceTo(locationB) / 1000;
                        //txtCalculate.setText(format.format(distance) + " Km");
                        Locale locale = new Locale("in", "ID");
                        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                        double ongkir = distance * 4000;
                        sumOngkir.setText(format.format(distance) + " Km " + " Ongkir Anda " + fmt.format(ongkir));

                    } else {
                        Toast.makeText(Cart.this, "Harap update alamat rumah kamu", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //event radio
        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //ship to this address feature
                if (b) //b==true
                {
                    //  if(mLastLocation != null) {
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //if fetch API ok
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());

                                        JSONArray resultsArray = jsonObject.getJSONArray("results");

                                        JSONObject firstObject = resultsArray.getJSONObject(0);

                                        address = firstObject.getString("formatted_address");

                                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    // }
                    //else
                    // {
                    //    Toast.makeText(Cart.this,"Harap aktifkan GPS anda!", Toast.LENGTH_SHORT).show();


                    //}

                }
                double lati = Double.parseDouble(String.valueOf(-6.361177));
                double lngi = Double.parseDouble(String.valueOf(106.870704));
                LatLng position1 = new LatLng(lati, lngi);

                Location locationA = new Location("point A");

                locationA.setLatitude(lati);
                locationA.setLongitude(lngi);

                Location locationB = new Location("point B");

                locationB.setLatitude(mLastLocation.getLatitude());
                locationB.setLongitude(mLastLocation.getLongitude());

                DecimalFormat format = new DecimalFormat("#.##");
                double distance = locationA.distanceTo(locationB) / 1000;
                //txtCalculate.setText(format.format(distance) + " Km");
                Locale locale = new Locale("in", "ID");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                double ongkir = distance * 4000;
                sumOngkir.setText(format.format(distance) + " Km " + " Ongkir Anda " + fmt.format(ongkir));
            }
        });


        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Oke", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                double dLAT = mLastLocation.getLatitude();
                double dLON = mLastLocation.getLongitude();

                //Show paypal to payment
                if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {
                    if (shippingAddress != null) {
                        address = shippingAddress.getAddress().toString();
                        double lati = Double.parseDouble(String.valueOf(-6.361177));
                        double lngi = Double.parseDouble(String.valueOf(106.870704));
                        LatLng position1 = new LatLng(lati, lngi);

                        Location locationA = new Location("point A");

                        locationA.setLatitude(lati);
                        locationA.setLongitude(lngi);

                        Location locationB = new Location("point B");

                        locationB.setLatitude(shippingAddress.getLatLng().latitude);
                        locationB.setLongitude(shippingAddress.getLatLng().longitude);

                        DecimalFormat format = new DecimalFormat("#.##");
                        double distance = locationA.distanceTo(locationB) / 1000;
                        //txtCalculate.setText(format.format(distance) + " Km");
                        Locale locale = new Locale("in", "ID");
                        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                        double ongkir = distance * 4000;
                        sumOngkir.setText(format.format(distance) + " Km " + " Ongkir Anda " + fmt.format(ongkir));
                    } else {
                        Toast.makeText(Cart.this, "Harap tuliskan alamat atau pilih opsi alamat kamu", Toast.LENGTH_SHORT).show();

                        //fix crash fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();

                        return;
                    }

                }

                if (TextUtils.isEmpty(address)) {

                    Toast.makeText(Cart.this, "Harap tuliskan alamat atau pilih opsi alamat kamu", Toast.LENGTH_SHORT).show();

                    //fix crash fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;
                }

                comment = edtComment.getText().toString();
                customer = edtCustomer.getText().toString();

                if (!rdiCOD.isChecked() && !rdiPaypal.isChecked() && !rdiBalance.isChecked()) {
                    Toast.makeText(Cart.this, "Harap pilih metode pembayaran atau isi kolom yang kosong", Toast.LENGTH_SHORT).show();

                    //fix crash fragment k
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;
                } else if (rdiPaypal.isChecked()) {

                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace("Rp", "")
                            .replace(",", "");

                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                            "ID",
                            "212 Mart Mekarsari Order",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                } else if (rdiCOD.isChecked()) {
                    //Create new request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",//status
                            customer,
                            comment,
                            sumOngkir.getText().toString(),
                            "COD",
                            "Unpaid",
                            String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            cart
                    );
                    //Submit to firebase
                    String order_number = String.valueOf(System.currentTimeMillis());
                    requests.child(order_number)
                            .setValue(request);
                    //Delete cart
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                    sendNotificationOrder(order_number);
                    finish();

                } else if (rdiBalance.isChecked()) {
                    double amount = 0;
                    try {
                        amount = Common.formatCurrency(txtTotalPrice.getText().toString(), Locale.US).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();

                        if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount) {
                            //Create new request
                            Request request = new Request(
                                    Common.currentUser.getPhone(),
                                    Common.currentUser.getName(),
                                    address,
                                    txtTotalPrice.getText().toString(),
                                    "0",//status
                                    customer,
                                    comment,
                                    sumOngkir.getText().toString(),
                                    "212Pay",
                                    "Paid",
                                    String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                    cart
                            );
                            //Submit to firebase
                            final String order_number = String.valueOf(System.currentTimeMillis());
                            requests.child(order_number)
                                    .setValue(request);
                            //Delete cart
                            new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                            //update balance
                            double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                            Map<String, Object> update_balance = new HashMap<>();
                            update_balance.put("balance", balance);

                            FirebaseDatabase.getInstance()
                                    .getReference("User")
                                    .child(Common.currentUser.getPhone())
                                    .updateChildren(update_balance)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseDatabase.getInstance()
                                                        .getReference("User")
                                                        .child(Common.currentUser.getPhone())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                                                sendNotificationOrder(order_number);

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }

                                        }
                                    });

                        } else {
                            Toast.makeText(Cart.this, "212Pay kamu tidak cukup, harap pilih opsi pembayaran lain", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                /*
                //Create new request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        shippingAddress.getAddress().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",//status
                        edtComment.getText().toString(),
                        String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                        cart
                );
                //Submit to firebase
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart();

                sendNotificationOrder(order_number);
                  //  Toast.makeText(Cart.this, "Terimakasih , Pembelian ", Toast.LENGTH_SHORT).show();
                  //  finish();
                  */

                //remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDialog.show();

    }


    /*private void getPrice(String position1, String edtAddress) {
        String requestUrl=null;
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+"origin="+position1+"&"+
                    "destination="+edtAddress+"&"+"keys="+getResources().getString(R.string.google_browser_key);
            Log.e("LINK",requestUrl);
            mapsService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        //get distance
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");

                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                        //get Time
                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+",""));

                        String final_calculate = String.format("%s + %s = Rp%.2f",distance_text,time_text,
                                Common.getPrice(distance_value,time_value));

                        txtCalculate.setText(final_calculate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                                    }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR",t.getMessage());
                }
            });
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    */


    //PAYPAL CONFIRM


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);


                        //Create new request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",//status
                                customer,
                                comment,
                                sumOngkir.getText().toString(),
                                "Paypal",
                                jsonObject.getJSONObject("response").getString("state"),
                                String.format("%s,%s", shippingAddress.getLatLng().latitude, shippingAddress.getLatLng().longitude),
                                cart
                        );
                        //Submit to firebase
                        String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number)
                                .setValue(request);
                        //Delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                        sendNotificationOrder(order_number);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(this, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show();
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Pembayaran Gagal", Toast.LENGTH_SHORT).show();
        }
    }

    /////////////////RUNTIME PERMISSION FIX/////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }
    /////////////////RUNTIME PERMISSION FIX/////////////////////////


    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Token serverToken = postSnapShot.getValue(Token.class);

                    // Notification notification = new Notification("212Mart Mekarsari","Anda Mendapatkan Pesanan Baru"+order_number);
                    // Sender content = new Sender(serverToken.getToken(),notification);

                    Map<String, String> dataSend = new HashMap<>();
                    dataSend.put("title", "212Mart Mekarsari");
                    dataSend.put("message", "Anda Mendapatkan Orderan Baru " + order_number);
                    DataMessage dataMessage = new DataMessage(serverToken.getToken(), dataSend);

                    String test = new Gson().toJson(dataMessage);
                    Log.d("Content", test);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "Terimakasih telah memesan ", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Gagal Memesan", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());

                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {

        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        Integer total = 0;
        for (Order order : cart)

            //try { } catch(NumberFormatException nfe) {
            //   System.out.println("NumberFormatException: " + nfe.getMessage());
            //  }
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));


        Locale locale = new Locale("in", "ID");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        new Database(this).cleanCart(Common.currentUser.getPhone());
        for (Order item : cart)
            new Database(this).addToCart(item);
        //Refresh
        loadListFood();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }


    /////////////////RUNTIME PERMISSION FIX/////////////////////////
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    /////////////////RUNTIME PERMISSION FIX/////////////////////////

/////////////////RUNTIME PERMISSION FIX/////////////////////////

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {


            Log.d("LOCATION", "Lokasi anda : " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        } else {
            Log.d("LOCATION", "Tidak bisa mendapatkan lokasi anda ");

        }
    }
/////////////////RUNTIME PERMISSION FIX/////////////////////////

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /////////////////RUNTIME PERMISSION FIX////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHoder) {
            String name = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            Integer total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("in", "ID");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));

            //make snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " Hapus dari keranjang belanja", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    Integer total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("in", "ID");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText(fmt.format(total));

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
/////////////////RUNTIME PERMISSION FIX/////////////////////////

