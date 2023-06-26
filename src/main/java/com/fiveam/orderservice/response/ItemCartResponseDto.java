package com.fiveam.orderservice.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemCartResponseDto {
    private Long itemCartId;
    private Integer quantity;
    private Integer period;
    private boolean subscription;
    private Long itemId;
}
