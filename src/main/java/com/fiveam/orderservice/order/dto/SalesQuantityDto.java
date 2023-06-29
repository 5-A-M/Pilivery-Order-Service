package com.fiveam.orderservice.order.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalesQuantityDto {
    private Integer quantity;
}
