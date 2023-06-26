package com.fiveam.orderservice.order.mapper;

import com.fiveam.orderservice.client.ItemServiceClient;
import com.fiveam.orderservice.order.dto.ItemOrderDto;
import com.fiveam.orderservice.order.entity.ItemOrder;
import com.fiveam.orderservice.response.ItemCartResponseDto;
import com.fiveam.orderservice.response.ItemInfoResponseDto;
import com.fiveam.orderservice.response.ItemSimpleResponseDto;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemOrderMapper {

    default ItemOrder itemOrderPostDtoToItemOrder(ItemOrderDto.Post itemOrderPostDto,
                                                  ItemServiceClient itemService) {

        ItemOrder itemOrder = new ItemOrder();
        itemOrder.setQuantity(itemOrderPostDto.getQuantity());
        itemOrder.setPeriod(itemOrderPostDto.getPeriod());
        itemOrder.setSubscription(itemOrderPostDto.isSubscription());

        ItemInfoResponseDto item = itemService.findVerifiedItem(itemOrderPostDto.getItemId());
        itemOrder.setItemId(item.getItemId());

        return itemOrder;
    }

    default ItemOrder itemCartToItemOrder(ItemCartResponseDto itemCart) { // 장바구니를 불러와서 주문으로 변환
        ItemOrder itemOrder = new ItemOrder();
        itemOrder.setQuantity(itemCart.getQuantity());
        itemOrder.setPeriod(itemCart.getPeriod());
        itemOrder.setSubscription(itemCart.isSubscription());
        itemOrder.setItemId(itemCart.getItemId());

        return itemOrder;
    }

    default List<ItemOrder> itemCartsToItemOrders(List<ItemCartResponseDto> itemCarts, ItemServiceClient itemService) {

        if(itemCarts == null) return null;

        List<ItemOrder> itemOrders = new ArrayList<>();

        for(ItemCartResponseDto itemCart : itemCarts) {
            itemOrders.add(itemCartToItemOrder(itemCart));
            itemService.deleteItemCart(itemCart.getItemCartId());
        }

        return itemOrders;
    }

    default ItemOrderDto.SimpleResponse itemOrderToItemOrderSimpleResponseDto(
            ItemServiceClient itemService,
            ItemOrder itemOrder){

        ItemOrderDto.SimpleResponse itemOrderSimpleResponseDto = new ItemOrderDto.SimpleResponse();
        itemOrderSimpleResponseDto.setItemOrderId(itemOrder.getItemOrderId());
        itemOrderSimpleResponseDto.setQuantity(itemOrder.getQuantity());
        itemOrderSimpleResponseDto.setPeriod(itemOrder.getPeriod());
        itemOrderSimpleResponseDto.setSubscription(itemOrder.isSubscription());

        Long itemId = itemOrder.getItemId();
        ItemInfoResponseDto item = itemService.getItem(itemId);

        itemOrderSimpleResponseDto.setItem(ItemSimpleResponseDto.fromItemInfoResponse(item));

        itemOrderSimpleResponseDto.setCreatedAt(itemOrder.getCreatedAt().toLocalDate().toString());
        itemOrderSimpleResponseDto.setUpdatedAt(itemOrder.getUpdatedAt().toLocalDate().toString());

        return itemOrderSimpleResponseDto;
    }

    default List<ItemOrderDto.SimpleResponse> itemOrdersToItemOrderSimpleResponseDtos(
            ItemServiceClient itemService,
            List<ItemOrder> itemOrders) {
        if(itemOrders == null) return null;

        List<ItemOrderDto.SimpleResponse> itemOrderSimpleResponseDtos = new ArrayList<>();

        for(ItemOrder itemOrder : itemOrders) {
            itemOrderSimpleResponseDtos.add(itemOrderToItemOrderSimpleResponseDto(itemService, itemOrder));
        }

        return itemOrderSimpleResponseDtos;
    }

    default ItemOrderDto.SubResponse itemOrderToSubResponse(ItemServiceClient itemService, ItemOrder itemOrder) {
        ItemInfoResponseDto item = itemService.getItem(itemOrder.getItemId());

        ItemOrderDto.SubResponse subResponse = new ItemOrderDto.SubResponse();
        subResponse.setOrderId(itemOrder.getOrder().getOrderId());
        subResponse.setItemOrderId(itemOrder.getItemOrderId());
        subResponse.setQuantity(itemOrder.getQuantity());
        subResponse.setPeriod(itemOrder.getPeriod());
        subResponse.setItem(ItemSimpleResponseDto.fromItemInfoResponse(item));

        int totalPrice = subResponse.getQuantity() * (item.getDiscountPrice());

        subResponse.setTotalPrice(totalPrice);
        subResponse.setNextDelivery(itemOrder.getNextDelivery());

        return subResponse;
    }

    default List<ItemOrderDto.SubResponse> itemOrdersToSubResponses(
            ItemServiceClient itemService,
            List<ItemOrder> itemOrders
    ) {
        if(itemOrders == null) return null;

        List<ItemOrderDto.SubResponse> subResponses = new ArrayList<>();

        for(ItemOrder itemOrder : itemOrders) {
            subResponses.add(itemOrderToSubResponse(itemService, itemOrder));
        }

        return subResponses;
    }
}
