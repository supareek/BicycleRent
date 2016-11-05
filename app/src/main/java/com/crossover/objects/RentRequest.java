package com.crossover.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sumit on 11/5/16.
 */

public class RentRequest {

    @SerializedName("number")
    @Expose
    public String number;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("expiration")
    @Expose
    public String expiration;
    @SerializedName("code")
    @Expose
    public String code;

}
