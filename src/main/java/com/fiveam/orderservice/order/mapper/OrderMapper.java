package com.fiveam.orderservice.order.mapper;

import com.fiveam.orderservice.client.ItemServiceClient;
import com.fiveam.orderservice.order.dto.OrderDto;
import com.fiveam.orderservice.order.entity.ItemOrder;
import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.response.ItemInfoResponseDto;
import com.fiveam.orderservice.response.ItemSimpleResponseDto;
import com.fiveam.orderservice.response.MultiResponseDto;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

//    default Order orderPostDtoToOrder(UserService userService, OrderDto.Post orderPostDto) {
//
//        Order order = new Order();
//
//        User user = userService.getLoginUser(); // 이름, 주소, 번호는 유저 정보에서 기본값을 불러옴
//        order.setName(user.getName());
//        order.setAddress(user.getAddress());
//        order.setPhone(user.getPhone());
//
//        order.setSubscription(orderPostDto.isSubscription());
//
//        return order;
//    }

    default OrderDto.SimpleResponse orderToOrderSimpleResponseDto(ItemServiceClient itemService, Order order) {

        OrderDto.SimpleResponse orderSimpleResponseDto = new OrderDto.SimpleResponse();
        orderSimpleResponseDto.setOrderId(order.getOrderId());
        orderSimpleResponseDto.setOrderStatus(order.getOrderStatus());
        orderSimpleResponseDto.setTotalItems(order.getTotalItems());
        orderSimpleResponseDto.setExpectPrice(order.getExpectPrice());
        orderSimpleResponseDto.setSubscription(order.isSubscription());

        // 한 건의 주문에 여러 건의 아이템의 포함되어 있어도, 첫번째 제품의 정보를 사용함.
        Long itemId = order.getItemOrders().get(0).getItemId();
        ItemInfoResponseDto item = itemService.getItem(itemId);

        orderSimpleResponseDto.setItem(ItemSimpleResponseDto.fromItemInfoResponse(item));

        orderSimpleResponseDto.setCreatedAt(order.getCreatedAt().toLocalDate().toString());
        orderSimpleResponseDto.setUpdatedAt(order.getUpdatedAt().toLocalDate().toString());

        return orderSimpleResponseDto;
    }

    default List<OrderDto.SimpleResponse> ordersToOrderSimpleResponseDtos(ItemServiceClient itemService, List<Order> orders) {
        if(orders == null) return null;

        List<OrderDto.SimpleResponse> orderSimpleResponseDtos = new ArrayList<>(orders.size());

        for(Order order : orders) {
            orderSimpleResponseDtos.add(orderToOrderSimpleResponseDto(itemService, order));
        }

        return orderSimpleResponseDtos;
    }

    default OrderDto.DetailResponse orderToOrderDetailResponseDto(
            ItemServiceClient itemService,
            Order order,
            ItemOrderMapper itemOrderMapper
    ) {

        OrderDto.DetailResponse orderDetailResponseDto = new OrderDto.DetailResponse();
        orderDetailResponseDto.setOrderId(order.getOrderId());
        orderDetailResponseDto.setName(order.getName());
        orderDetailResponseDto.setAddress(order.getAddress());
        orderDetailResponseDto.setDetailAddress(order.getDetailAddress());
        orderDetailResponseDto.setPhone(order.getPhone());
        orderDetailResponseDto.setTotalItems(order.getTotalItems());
        orderDetailResponseDto.setTotalPrice(order.getTotalPrice());
        orderDetailResponseDto.setTotalDiscountPrice(order.getTotalDiscountPrice());
        orderDetailResponseDto.setExpectPrice(order.getExpectPrice());
        orderDetailResponseDto.setExpectPrice(order.getExpectPrice());
        orderDetailResponseDto.setSubscription(order.isSubscription());

        List<ItemOrder> itemOrders = order.getItemOrders();

        orderDetailResponseDto.setItemOrders(new MultiResponseDto<>(
                itemOrderMapper.itemOrdersToItemOrderSimpleResponseDtos(itemService, itemOrders)));

        orderDetailResponseDto.setOrderStatus(order.getOrderStatus());
        orderDetailResponseDto.setCreatedAt(order.getCreatedAt().toLocalDate().toString());
        orderDetailResponseDto.setUpdatedAt(order.getUpdatedAt().toLocalDate().toString());

        orderDetailResponseDto.setTotalQuantity(order.getTotalQuantity());

        return orderDetailResponseDto;
    }
}
