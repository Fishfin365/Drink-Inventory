package com.example.drinkinventoryapp.model;

import com.google.firebase.firestore.PropertyName;
import java.util.HashMap;
import java.util.Map;

public class CustomDrink {
    private String id;
    private String name;
    private String category;
    private String instructions;
    private String imageUrl;
    private String creatorId;
    private Map<String, String> ingredients;

    public CustomDrink() {
        ingredients = new HashMap<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("creatorID")
    public String getCreatorId() { return creatorId; }
    
    @PropertyName("creatorID")
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public Map<String, String> getIngredients() { return ingredients; }
    public void setIngredients(Map<String, String> ingredients) { this.ingredients = ingredients; }

    public Drink toDrink() {
        Drink drink = new Drink();
        drink.idDrink = id;
        drink.strDrink = name;
        drink.strCategory = category;
        drink.strInstructions = instructions;
        drink.strDrinkThumb = imageUrl;
        
        if (ingredients != null) {
            int i = 1;
            for (Map.Entry<String, String> entry : ingredients.entrySet()) {
                if (i > 15) break;
                setIngredientFields(drink, i, entry.getKey(), entry.getValue());
                i++;
            }
        }
        return drink;
    }

    private void setIngredientFields(Drink drink, int index, String name, String measure) {
        try {
            drink.getClass().getField("strIngredient" + index).set(drink, name);
            drink.getClass().getField("strMeasure" + index).set(drink, measure);
        } catch (Exception ignored) {}
    }
}
