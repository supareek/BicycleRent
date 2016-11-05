package com.crossover.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sumit on 11/5/16.
 */

public class Locations {

    @SerializedName("results")
    @Expose
    public List<Result> results = new ArrayList<Result>();

}
