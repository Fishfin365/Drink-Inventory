package com.example.drinkinventoryapp;

public class Ingredient {
    private String name;
    private int count;
    private String volume;

    public Ingredient(String name, int count, String volume) {
        this.name = name;
        this.count = count;
        this.volume = volume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }
}
