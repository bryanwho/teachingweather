package com.codetank.weatherapp;

import com.codetank.weatherapp.data.Forecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by bryan on 4/30/16.
 */
public interface OpenWeatherMapService {

    @GET("data/2.5/weather?")
    Call<Forecast> currentForecastByZip(@Query("zip") String zip,
                                        @Query("units") String units,
                                        @Query("apikey") String apiKey);
}
