package com.codetank.weatherapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetank.weatherapp.data.Forecast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static final String BASE_API = "http://api.openweathermap.org/";
    public static final String API_KEY = "2cf0967f5d444acf71bf234374c3885c";
    public static final String UNIT_FAHRENHEIT = "imperial";
    public static final String UNIT_CELSIUS = "metric";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private RelativeLayout relativeLayout;
    private TextView temp;
    private TextView locale;
    private ImageView weatherIcon;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        relativeLayout = (RelativeLayout) findViewById(R.id.activity_layout);
        temp = (TextView) findViewById(R.id.temp);
        locale = (TextView) findViewById(R.id.locale);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);

        weatherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void displayLocation() {

        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            Log.d("flow","security excepetion: " + e.getMessage());
        }

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.d("flow", latitude + ", " + longitude);
            getAddress(longitude, latitude);

        } else {

            Snackbar.make(relativeLayout, "(Couldn't get the location. Make sure location is enabled on the device)", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void getCurrentForecastByZip(String zipCode) {
        OpenWeatherMapService openWeatherMapService = retrofit.create(OpenWeatherMapService.class);

        Call<Forecast> forecastCall = openWeatherMapService.currentForecastByZip(zipCode,UNIT_FAHRENHEIT, API_KEY);

        forecastCall.enqueue(new Callback<Forecast>() {
            @Override
            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                populateScreen(response.body());
            }

            @Override
            public void onFailure(Call<Forecast> call, Throwable t) {
                Log.d("flow", "failure: " + t.getMessage());
            }
        });
    }

    private void getAddress(double longitude, double latitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            Log.d("flow", e.getMessage());
        } catch (IllegalArgumentException e){
            Log.d("flow", e.getMessage());
        }

        if(addresses != null) {
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            Log.d("flow", "this is the zip code: " + postalCode);

            getCurrentForecastByZip(postalCode);
        } else {

            Log.d("flow", "Geo Location Addresses was null");
        }
    }

    private void populateScreen(Forecast forecast) {
        
        String temperature = Integer.toString((int)forecast.getMain().getTemp());
        temp.setText(getString(R.string.degrees_fahr, temperature));
        locale.setText(getString(R.string.hello_city, forecast.getName()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(retrofit == null) {
            initRetrofit();
        }

        if(checkPlayServices()) {
            displayLocation();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("flow", "Connection SUCCESS");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("flow", "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }
}
