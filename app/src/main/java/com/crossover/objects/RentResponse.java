package com.crossover.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sumit on 11/5/16.
 */

public class RentResponse {

    @SerializedName("message")
    @Expose
    public String message;
}
