package com.example.drinkinventoryapp.model;

import com.google.firebase.firestore.Exclude;
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

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("name")
    public String getName() { return name; }
    @PropertyName("name")
    public void setName(String name) { this.name = name; }

    @PropertyName("category")
    public String getCategory() { return category; }
    @PropertyName("category")
    public void setCategory(String category) { this.category = category; }

    @PropertyName("instructions")
    public String getInstructions() { return instructions; }
    @PropertyName("instructions")
    public void setInstructions(String instructions) { this.instructions = instructions; }

    @PropertyName("imageUrl")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("creatorId")
    public String getCreatorId() { return creatorId; }
    @PropertyName("creatorId")
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    @PropertyName("ingredients")
    public Map<String, String> getIngredients() { return ingredients; }
    @PropertyName("ingredients")
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
            for (String ingredientName : ingredients.keySet()) {
                if (i > 15) break;
                String measure = ingredients.get(ingredientName);
                setIngredientFields(drink, i, ingredientName, measure);
                i++;
            }
        }
        return drink;
    }

    private void setIngredientFields(Drink drink, int index, String name, String measure) {
        try {
            drink.getClass().getField("strIngredient" + index).set(drink, name);
            drink.getClass().getField("strMeasure" + index).set(drink, (measure == null || measure.isEmpty()) ? "" : measure);
        } catch (Exception ignored) {}
    }
}
