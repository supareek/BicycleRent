package com.crossover;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.crossover.network.ClientModel;
import com.crossover.network.NetworkInterface;
import com.crossover.objects.Locations;
import com.crossover.objects.Result;
import com.crossover.settings.SharedPrefData;
import com.crossover.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

import static com.crossover.R.id.map;

/**
 * Activity that displays map on the screen
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String MARKER_ID = "markerId";
    private GoogleMap mMap;

    // Map to keep track of markers on screen along with their unique id's
    private Map<Marker, String> mMarkersMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Show a Snack Bar if connection is offline
        Utils.handleOfflineIssue(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        new GetNearbyPlaces().execute();
    }

    /**
     * Mark all the location on Map.
     * @param list
     */
    private void markPlacesonMap(List<Result> list) {

        mMarkersMap = new HashMap<>();
        for(Result result : list){
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(result.location.lat,result.location.lng))
                    .title(result.name).visible(true));

            marker.showInfoWindow();
            mMarkersMap.put(marker, result.id);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkersMap.keySet()) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);

        initListener();

    }

    private void initListener() {

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showDialog(marker);
                return true;
            }
        });
    }

    /**
     * Show Confirmation dialog when user selects a location to rent
     * @param marker
     */
    private void showDialog(final Marker marker){

        new AlertDialog.Builder(MapsActivity.this)
                .setTitle(marker.getTitle())
                .setMessage("Are you sure you want to rent bike from " + marker.getTitle() + "?")
                .setPositiveButton(R.string.rent, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Call payment activity
                        Intent intent = new Intent(MapsActivity.this, PaymentActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        // Populating the intent with Marker ID for future use
                        intent.putExtra(MARKER_ID, mMarkersMap.get(marker));

                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_map)
                .show();
    }

    /**
     * Async task to get all the places from backend.
     */
    public class GetNearbyPlaces extends AsyncTask<Void, Void, Locations> {

        private ProgressDialog pdia;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(MapsActivity.this);
            pdia.setMessage("Loading...");
            pdia.show();
        }

        @Override
        protected Locations doInBackground(Void... params) {
            try {

                final Context context = getApplicationContext();
                NetworkInterface networkInterface = ClientModel.getAuthenticatedClient(context,
                        SharedPrefData.getTextData(context, SharedPrefData.ACCESS_TOKEN));

                Call<Locations> locationsCall = networkInterface.getPlaces();
                Locations locations = locationsCall.execute().body();
                return locations;
            } catch (Exception e){
                Log.e(TAG, e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Locations locations) {
            if(pdia != null && pdia.isShowing()) {
                pdia.dismiss();
            }

            if(locations != null){
                List<Result> list = locations.results;
                markPlacesonMap(list);
            }
        }
    }
}
