package com.example.drinkinventoryapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.drinkinventoryapp.model.Drink;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Drink> drinks = new ArrayList<>();

    public void setDrinks(List<Drink> drinks) {
        this.drinks = drinks != null ? drinks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Drink drink = drinks.get(position);
        holder.title.setText(drink.strDrink);
        holder.category.setText(drink.strCategory);

        Glide.with(holder.itemView.getContext())
                .load(drink.strDrinkThumb)
                .into(holder.image);

        // Handle click event
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RecipeDetailActivity.class);
            // Pass the drink object as JSON to the detail activity
            intent.putExtra("drink_json", new Gson().toJson(drink));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return drinks.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView category;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recipeImage);
            title = itemView.findViewById(R.id.recipeTitle);
            category = itemView.findViewById(R.id.recipeCategory);
        }
    }
}
