package com.fiveam.orderservice.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Builder
public class ItemOrderInfoResponseDto implements Serializable {

    private Long itemOrderId;

    private Integer quantity;

    private Integer period;

    private boolean subscription;

    private ZonedDateTime nextDelivery;

    private ZonedDateTime paymentDay;

    private Long itemId;

    private Long orderId;
}
