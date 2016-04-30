package com.codetank.weatherapp;

import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.codetank.weatherapp.data.Forecast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    public static final String BASE_API = "http://api.openweathermap.org/";
    public static final String API_KEY = "2cf0967f5d444acf71bf234374c3885c";
    public static final String UNIT_FAHRENHEIT = "imperial";
    public static final String UNIT_CELSIUS = "metric";

    private TextView temp;
    private TextView locale;
    private ImageView weatherIcon;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temp = (TextView) findViewById(R.id.temp);
        locale = (TextView) findViewById(R.id.locale);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
    }

    private void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void getCurrentForecastByZip() {
        OpenWeatherMapService openWeatherMapService = retrofit.create(OpenWeatherMapService.class);

        Call<Forecast> forecastCall = openWeatherMapService.currentForecastByZip("11432,us",UNIT_FAHRENHEIT, API_KEY);

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

        getCurrentForecastByZip();
    }
}
