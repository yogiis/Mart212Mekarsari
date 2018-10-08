package com.kkeu212.mart212mekarsari.Model;

public class Rating {
    private String userPhone;
    private String foodId;
    private String rateValue;
    private String comment;

    public Rating(){
    }

    public Rating(String userPhone, String foodId, String rateValue, String comment){
        this.userPhone = userPhone;
        this.foodId = foodId;
        this.rateValue = rateValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getFoodId() {
        return foodId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public String getComment() {
        return comment;
    }
}
