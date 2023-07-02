package com.fiveam.orderservice.response;

import lombok.*;

@Data
@Builder
@ToString
public class ItemCartResponseDto {
    private Long itemCartId;
    private Integer quantity;
    private Integer period;
    private boolean subscription;
    private Long itemId;
}
