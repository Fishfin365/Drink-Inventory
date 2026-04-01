package com.example.drinkinventoryapp.network;

import com.example.drinkinventoryapp.model.DrinkResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CocktailAPI {

    // Search drinks by name
    @GET("search.php")
    Call<DrinkResponse> searchByName(@Query("s") String name);
}