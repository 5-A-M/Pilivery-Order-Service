package com.fiveam.orderservice.client;

import com.fiveam.orderservice.order.dto.SalesQuantityDto;
import com.fiveam.orderservice.response.ItemCartResponseDto;
import com.fiveam.orderservice.response.ItemInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("item-service")
public interface ItemServiceClient {
    @GetMapping("/items/{itemId}")
    ItemInfoResponseDto getItem(@PathVariable Long itemId);

    @GetMapping("/items/{itemId}/check")
    ItemInfoResponseDto findVerifiedItem(@PathVariable Long itemId);

    @GetMapping("/carts/itemcarts/{itemCartId}")
    void getItemCart(@PathVariable Long itemCartId);

    @DeleteMapping("/carts/itemcarts/{itemCartId}")
    void deleteItemCart(@PathVariable Long itemCartId);

    @GetMapping("/carts/{cartId}/itemcarts/{subscription}")
    List<ItemCartResponseDto> findItemCarts(@PathVariable Long cartId, @PathVariable Boolean subscription, Boolean buyNow);

    @GetMapping("/carts/itemcarts/{cartId}/{subscription}")
    void refreshCart(@PathVariable Long cartId, @PathVariable Boolean subscription);

    @PostMapping("/items/list")
    List<ItemInfoResponseDto> getItems(List<Long> itemIds);

    @PostMapping("/items/{itemId}/sales")
    void plusSales(@PathVariable Long itemId, @RequestBody SalesQuantityDto quantity);

    @DeleteMapping("/items/{itemId}/sales")
    void minusSales(@PathVariable Long itemId, @RequestBody SalesQuantityDto quantity);
}
