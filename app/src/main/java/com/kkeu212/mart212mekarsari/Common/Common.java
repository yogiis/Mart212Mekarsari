package com.kkeu212.mart212mekarsari.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.kkeu212.mart212mekarsari.Model.Request;
import com.kkeu212.mart212mekarsari.Model.User;
import com.kkeu212.mart212mekarsari.Remote.APIService;
import com.kkeu212.mart212mekarsari.Remote.GoogleRetrofitClient;
import com.kkeu212.mart212mekarsari.Remote.IGoogleAPI;
import com.kkeu212.mart212mekarsari.Remote.IGoogleService;
import com.kkeu212.mart212mekarsari.Remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Common {

    public static String topicName = "News";

    public static User currentUser;
    public static Request currentRequest;
    public static String currentKey;
    private static double base_fare = 2.55;
    private static double time_rate = 0.35;
    private static double distance_rate = 1.75;

    public static double getPrice(double km,int min){
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }

    public static String PHONE_TEXT = "userPhone";

    public static final String INTENT_FOOD_ID = "FoodId";

    private static final String BASE_URL = "https://fcm.googleapis.com/";

    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";


    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI()
    {
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static IGoogleAPI getGoogleService()
    {
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleAPI.class);
    }

    public static String convertCodeToStatus(String code)
    {
        if(code.equals("0"))
            return "Dalam Antrian";
        else if (code.equals("1"))
            return "Dibatalkan";
        else if (code.equals("2"))
            return "Proses Pengiriman";
        else
            return "Terkirim";
    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";


    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null)
            {
                for(int i=0;i<info.length;i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }

        }
        return false;
    }

    public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException
    {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if(format instanceof DecimalFormat)
            ((DecimalFormat) format).setParseBigDecimal(true);
            return (BigDecimal)format.parse(amount.replace("[^\\d.,]",""));
    }

}
