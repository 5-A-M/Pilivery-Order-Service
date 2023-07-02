package com.fiveam.orderservice.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemDetailResponseDto {
    private Long itemId;
    private String thumbnail;
    private String descriptionImage;
    private String title;
    private String content;
    private String expiration;
    private Integer sales;
    private Integer price;
    private String brand;
    private Integer capacity;
    private Integer servingSize;
    private Integer discountRate;
    private Integer discountPrice;
    private List<String> categories;
    private List<NutritionFactDto.Response> nutritionFacts;
    private Double starAvg;
    private MultiResponseDto<ReviewResponseDto> reviews;
    private MultiResponseDto<TalkAndCommentDto> talks;
}
