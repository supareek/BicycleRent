package com.crossover;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.crossover.network.ClientModel;
import com.crossover.network.NetworkInterface;
import com.crossover.objects.RentRequest;
import com.crossover.objects.RentResponse;
import com.crossover.settings.SharedPrefData;
import com.crossover.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit2.Call;

/**
 * Activity responsible for taking credit card details.
 * It performs a series of checks on the input.
 */
public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = PaymentActivity.class.getSimpleName();

    private static final int CARD_NUMBER_TOTAL_SYMBOLS = 19; // size of pattern 0000-0000-0000-0000
    private static final int CARD_NUMBER_TOTAL_DIGITS = 16; // max numbers of digits in pattern: 0000 x 4
    private static final int CARD_NUMBER_DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
    private static final int CARD_NUMBER_DIVIDER_POSITION = CARD_NUMBER_DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
    private static final char CARD_NUMBER_DIVIDER = '-';

    private static final int CARD_DATE_TOTAL_SYMBOLS = 5; // size of pattern MM/YY
    private static final int CARD_DATE_TOTAL_DIGITS = 4; // max numbers of digits in pattern: MM + YY
    private static final int CARD_DATE_DIVIDER_MODULO = 3; // means divider position is every 3rd symbol beginning with 1
    private static final int CARD_DATE_DIVIDER_POSITION = CARD_DATE_DIVIDER_MODULO - 1; // means divider position is every 2nd symbol beginning with 0
    private static final char CARD_DATE_DIVIDER = '/';

    private static final int CARD_CVC_TOTAL_SYMBOLS = 3;

    @Bind(R.id.cardNumberEditText)
    EditText mCardNumber;

    @Bind(R.id.cardHolderName)
    EditText mCardHolderName;

    @Bind(R.id.cardCVCEditText)
    EditText mCVVText;

    @Bind(R.id.cardDateEditText)
    EditText mExpiryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Show a Snack Bar if connection is offline
        Utils.handleOfflineIssue(this);
    }

    @OnClick(R.id.paymentBtn)
    public void makePayment(){

        RentRequest rentRequest = new RentRequest();
        rentRequest.name = mCardHolderName.getText().toString();
        rentRequest.code = mCVVText.getText().toString();
        rentRequest.expiration = mExpiryDate.getText().toString();
        rentRequest.number = mCardNumber.getText().toString().replace("-","");

        new MakePaymentAsyncTask(rentRequest).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Text Change listener to format the Credit card number
    @OnTextChanged(value = R.id.cardNumberEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onCardNumberTextChanged(Editable s) {
        if (!isInputCorrect(s, CARD_NUMBER_TOTAL_SYMBOLS, CARD_NUMBER_DIVIDER_MODULO, CARD_NUMBER_DIVIDER)) {
            s.replace(0, s.length(), concatString(getDigitArray(s, CARD_NUMBER_TOTAL_DIGITS), CARD_NUMBER_DIVIDER_POSITION, CARD_NUMBER_DIVIDER));
        }
    }

    // Text Change listener to format the expiry date
    @OnTextChanged(value = R.id.cardDateEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onCardDateTextChanged(Editable s) {
        if (!isInputCorrect(s, CARD_DATE_TOTAL_SYMBOLS, CARD_DATE_DIVIDER_MODULO, CARD_DATE_DIVIDER)) {
            s.replace(0, s.length(), concatString(getDigitArray(s, CARD_DATE_TOTAL_DIGITS), CARD_DATE_DIVIDER_POSITION, CARD_DATE_DIVIDER));
        }
    }

    @OnTextChanged(value = R.id.cardCVCEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onCardCVCTextChanged(Editable s) {
        if (s.length() > CARD_CVC_TOTAL_SYMBOLS) {
            s.delete(CARD_CVC_TOTAL_SYMBOLS, s.length());
        }
    }

    private boolean isInputCorrect(Editable s, int size, int dividerPosition, char divider) {
        boolean isCorrect = s.length() <= size;
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (i + 1) % dividerPosition == 0) {
                isCorrect &= divider == s.charAt(i);
            } else {
                isCorrect &= Character.isDigit(s.charAt(i));
            }
        }
        return isCorrect;
    }

    private String concatString(char[] digits, int dividerPosition, char divider) {
        final StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < digits.length; i++) {
            if (digits[i] != 0) {
                formatted.append(digits[i]);
                if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                    formatted.append(divider);
                }
            }
        }

        return formatted.toString();
    }

    private char[] getDigitArray(final Editable s, final int size) {
        char[] digits = new char[size];
        int index = 0;
        for (int i = 0; i < s.length() && index < size; i++) {
            char current = s.charAt(i);
            if (Character.isDigit(current)) {
                digits[index] = current;
                index++;
            }
        }
        return digits;
    }

    /**
     * This task will run in background thread to make the payment for the rented bicycle
     */
    public class MakePaymentAsyncTask extends AsyncTask<Void, Void, RentResponse>{

        private final RentRequest mRentRequest;
        private ProgressDialog pdia;

        public MakePaymentAsyncTask(RentRequest rentRequest){
            mRentRequest = rentRequest;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(PaymentActivity.this);
            pdia.setMessage("Processing...");
            pdia.show();
        }

        @Override
        protected RentResponse doInBackground(Void... params) {

            try {
                final Context context = getApplicationContext();
                NetworkInterface networkInterface = ClientModel.getAuthenticatedClient(context,
                        SharedPrefData.getTextData(context, SharedPrefData.ACCESS_TOKEN));

                Call<RentResponse> call = networkInterface.bookonRent(mRentRequest);
                RentResponse msg = call.execute().body();
                return msg;

            } catch (Exception e){
                Log.e(TAG, e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(RentResponse s) {

            if(pdia != null && pdia.isShowing()) {
                pdia.dismiss();
            }

            if(s != null){
                Utils.showGlobalSnackBar(getApplicationContext(), getString(R.string.paymentSuccess));
                finish();
            } else {
                Utils.showGlobalSnackBar(getApplicationContext(), getString(R.string.paymentFail));
            }
        }
    }

}
