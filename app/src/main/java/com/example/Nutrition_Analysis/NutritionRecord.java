package com.example.Nutrition_Analysis;

public class NutritionRecord {
    private int id;
    private String imagePath;
    private String nutritionDetails;

    // Constructor
    public NutritionRecord(int id, String imagePath, String nutritionDetails) {
        this.id = id;
        this.imagePath = imagePath;
        this.nutritionDetails = nutritionDetails;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getNutritionDetails() {
        return nutritionDetails;
    }
}
