package com.fiveam.orderservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCartInfoResponseDto {

    private Long itemCartId;
    private Integer quantity;
    private Integer period;
    private boolean buyNow;
    private boolean subscription;
    private ItemSimpleResponseDto item;
    private String createdAt;
    private String updatedAt;
}