package com.crossover.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sumit on 11/5/16.
 */

public class Result {

    @SerializedName("location")
    @Expose
    public Location location;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;

}
