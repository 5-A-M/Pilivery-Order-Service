package com.fiveam.orderservice.order.dto;


import com.fiveam.orderservice.order.entity.OrderStatus;
import com.fiveam.orderservice.response.ItemSimpleResponseDto;
import com.fiveam.orderservice.response.MultiResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class OrderDto {

    @Setter
    public static class Patch { // 이름, 주소, 번호를 변경하는 경우

        private long orderId;
        private String name;
        private String address;
        private String detailAddress;
        private String phone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponse { // 주문 목록 조회

        private long orderId;
        private OrderStatus orderStatus;
        private int totalItems;
        private int expectPrice;
        private boolean subscription;
        private ItemSimpleResponseDto item;
        private String createdAt;
        private String updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse { // 주문 상세 내역 조회

        private long orderId;
        private String name;
        private String address;
        private String detailAddress;
        private String phone;
        private int totalItems;
        private int totalPrice;
        private int totalDiscountPrice;
        private int expectPrice;
        private boolean subscription;
        private MultiResponseDto<ItemOrderDto.SimpleResponse> itemOrders; // 페이지네이션 X
        private OrderStatus orderStatus;
        private String createdAt;
        private String updatedAt;

        private int totalQuantity;
    }

}
