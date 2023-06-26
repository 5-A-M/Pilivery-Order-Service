package com.fiveam.orderservice.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ItemInfoResponseDto implements Serializable {
    private Long itemId;

    private String title;

    private String content;

    private String thumbnail;

    private String descriptionImage;

    private String expiration;

    private int discountPrice;

    private int price;

    private int discountRate;

    private int view;

    private int sales;

    private int capacity;

    private int servingSize;

    private int totalWishes;

    private String brand;

    private double starAvg;

//    private List<Wish> wishList = new ArrayList<>();
//
//    private List<Category> categories = new ArrayList<>();
//
//    private List<Review> reviews = new ArrayList<>();
//
//    private List<Talk> talks = new ArrayList<>();
//
//    private List<NutritionFact> nutritionFacts = new ArrayList<>();
}
