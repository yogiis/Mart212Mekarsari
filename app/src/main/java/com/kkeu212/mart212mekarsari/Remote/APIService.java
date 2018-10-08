package com.kkeu212.mart212mekarsari.Remote;

import com.kkeu212.mart212mekarsari.Model.DataMessage;
import com.kkeu212.mart212mekarsari.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAARzywB4U:APA91bEziSKywNl_8s25Bxgs0uJmkE5o5s7YTodCHo9w50HyU-S3W18PGNb4KptWgnskOaXsxS-MFfdmFH0xVj3BJCX6kBhEtm7IDYagIXcY33ii_-Ul_4a1JMr71h8TuAiftYSwu3y-"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}
