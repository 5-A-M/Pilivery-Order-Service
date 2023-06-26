package com.fiveam.orderservice.payment.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneralPayDto {

    private String paymentKey;
    private String orderId;
    private int amount;
}
