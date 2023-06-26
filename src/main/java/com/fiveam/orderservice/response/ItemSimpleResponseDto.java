package com.fiveam.orderservice.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSimpleResponseDto {

    private long itemId;
    private String brand;
    private String thumbnail;
    private String title;
    private int capacity;
    private int price;
    private int discountRate;
    private int disCountPrice;

    public static ItemSimpleResponseDto fromItemInfoResponse(ItemInfoResponseDto item) {
        return ItemSimpleResponseDto.builder()
                .itemId(item.getItemId())
                .brand(item.getBrand())
                .thumbnail(item.getThumbnail())
                .title(item.getTitle())
                .capacity(item.getCapacity())
                .price(item.getPrice())
                .discountRate(item.getDiscountRate())
                .disCountPrice(item.getDiscountPrice())
                .build();
                

    }
}
