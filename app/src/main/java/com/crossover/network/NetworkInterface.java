package com.crossover.network;

import com.crossover.objects.AuthParams;
import com.crossover.objects.Credentials;
import com.crossover.objects.Locations;
import com.crossover.objects.RentRequest;
import com.crossover.objects.RentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Sumit on 11/5/16.
 * Retrofit interface for all the network calls.
 */

public interface NetworkInterface {

    @POST("/api/v1/auth")
    Call<AuthParams> login(@Body Credentials task);

    @POST("/api/v1/register")
    Call<AuthParams> register(@Body Credentials task);

    @GET("/api/v1/places")
    Call<Locations> getPlaces();

    @POST("/api/v1/rent")
    Call<RentResponse> bookonRent(@Body RentRequest rentRequest);
}
