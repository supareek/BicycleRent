package com.crossover.objects;

import com.google.gson.annotations.Expose;

/**
 * Created by Sumit on 11/5/16.
 */

public class Credentials {

    @Expose
    public String email;
    @Expose
    public String password;

    public Credentials(String mEmail, String mPassword) {
        email = mEmail;
        password = mPassword;
    }
}
