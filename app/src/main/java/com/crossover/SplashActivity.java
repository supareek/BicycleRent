package com.crossover;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.crossover.login.LoginActivity;
import com.crossover.settings.SharedPrefData;

/**
 * Launcher Activity.
 * Checks whether the token is available in Shared Preferences.
 * If yes Map screen is shown
 * If No user will be taken ro Login/Register Screen
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Access Token
        String accessToken = SharedPrefData.getTextData(getApplicationContext(),
                SharedPrefData.ACCESS_TOKEN);

        // User not logged In
        if(TextUtils.isEmpty(accessToken)) {
            Intent intent = new Intent(this,LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else { // User Logged In
            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
