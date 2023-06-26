package com.fiveam.orderservice.order.dto;

import com.fiveam.orderservice.response.ItemInfoResponseDto;
import com.fiveam.orderservice.response.ItemSimpleResponseDto;
import lombok.*;

import javax.validation.constraints.Min;
import java.time.ZonedDateTime;

public class ItemOrderDto {

    @Getter
    public static class Post {

        private long itemId;

        @Min(value = 1, message = "수량은 1개 이상 선택해주세요.")
        private Integer quantity;
        private int period;
        private boolean subscription;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleResponse { // 주문 목록 조회 용도

        private long itemOrderId;
        private int quantity;
        private int period;
        private boolean subscription;
        private ItemSimpleResponseDto item;
        private String createdAt;
        private String updatedAt;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubResponse { // 정기 구독 목록 조회

        private long orderId;
        private long itemOrderId;
        private int quantity;
        private int period;
        private ItemSimpleResponseDto item;
        private int totalPrice; // quantity * price
        private ZonedDateTime nextDelivery; // 다음 배송일
    }

}
