package com.example.drinkinventoryapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DrinkResponse {

    @SerializedName("drinks")
    public List<Drink> drinks;
}