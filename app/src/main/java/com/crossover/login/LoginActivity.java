package com.crossover.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.crossover.MapsActivity;
import com.crossover.R;
import com.crossover.network.ClientModel;
import com.crossover.network.NetworkInterface;
import com.crossover.objects.AuthParams;
import com.crossover.objects.Credentials;
import com.crossover.settings.SharedPrefData;
import com.crossover.utils.Utils;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    public UserLoginTask mAuthTask = null;

    // UI references.
    @Bind(R.id.email)
    AutoCompleteTextView mEmailView;

    @Bind(R.id.password)
    EditText mPasswordView;

    @Bind(R.id.login_progress)
    View mProgressView;

    @Bind(R.id.login_form)
    View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(true);
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Show a Snack Bar if connection is offline
        Utils.handleOfflineIssue(this);
    }

    @OnClick(R.id.email_sign_in_button)
    public void signInClicked(){
        attemptLogin(true);
    }

    @OnClick(R.id.email_sign_up_button)
    public void signUpClicked(){
        attemptLogin(false);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(boolean isSignIn) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password,isSignIn);
            mAuthTask.execute();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, AuthParams> {

        private final String mEmail;
        private final String mPassword;
        private final boolean mLogin;
        private ProgressDialog pdia;

        UserLoginTask(String email, String password, boolean isLogin) {
            mEmail = email;
            mPassword = password;
            mLogin = isLogin;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(LoginActivity.this);
            pdia.setMessage("Signing In...");
            pdia.show();
        }

        @Override
        protected AuthParams doInBackground(Void... params) {

            try {
                Credentials creds = new Credentials(mEmail,mPassword);

                Call<AuthParams> call;
                NetworkInterface networkInterface = ClientModel.getUnauthenticatedClient(getApplicationContext());
                if(mLogin) {
                    call = networkInterface.login(creds);
                } else {
                    call = networkInterface.register(creds);
                }
                AuthParams result = call.execute().body();
                return result;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(),e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final AuthParams params) {

            if(pdia != null && pdia.isShowing()) {
                pdia.dismiss();
            }

            mAuthTask = null;
            showProgress(false);

            if (params != null) {
                SharedPrefData.setTextData(getApplicationContext(),SharedPrefData.ACCESS_TOKEN,params.accessToken);
                // Open Activity
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

