package com.fiveam.orderservice.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
public class CartInfoResponseDto {

    @Positive
    private Long cartId;
    private boolean subscription;
    private MultiResponseDto<ItemCartInfoResponseDto> itemCarts;
    private int totalItems;
    private int totalPrice;
    private int totalDiscountPrice;
    private int expectPrice; //
    // 결제 예상 금액 (totalPrice - totalDiscountPrice)
}
