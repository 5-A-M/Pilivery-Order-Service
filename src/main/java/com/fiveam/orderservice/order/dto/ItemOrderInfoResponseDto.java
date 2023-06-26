package com.fiveam.orderservice.order.dto;

import lombok.Builder;
import lombok.Getter;
import com.fiveam.orderservice.order.entity.ItemOrder;

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

    public static ItemOrderInfoResponseDto fromEntity(ItemOrder itemOrder) {
        return ItemOrderInfoResponseDto.builder()
                .itemOrderId(itemOrder.getItemOrderId())
                .quantity(itemOrder.getQuantity())
                .period(itemOrder.getPeriod())
                .subscription(itemOrder.getOrder().isSubscription())
                .nextDelivery(itemOrder.getNextDelivery())
                .paymentDay(itemOrder.getPaymentDay())
                .itemId(itemOrder.getItemId())
                .orderId(itemOrder.getOrder().getOrderId())
                .build();
    }
}
