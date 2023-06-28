package com.fiveam.orderservice.order.service;

import com.fiveam.orderservice.client.UserServiceClient;
import com.fiveam.orderservice.exception.bussiness.BusinessLogicException;
import com.fiveam.orderservice.exception.bussiness.ExceptionCode;
import com.fiveam.orderservice.order.entity.ItemOrder;
import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.order.entity.OrderStatus;
import com.fiveam.orderservice.order.reposiroty.ItemOrderRepository;
import com.fiveam.orderservice.order.reposiroty.OrderRepository;
import com.fiveam.orderservice.response.UserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemOrderRepository itemOrderRepository;
    private final ItemOrderService itemOrderService;
    private final UserServiceClient userService;

    public Order callOrder(List<ItemOrder> itemOrders, UserInfoResponseDto user) {
        Order order = new Order();
        order.setItemOrders(itemOrders);
        order.setName(user.getRealName());
        order.setAddress(user.getAddress());
        order.setDetailAddress(user.getDetailAddress());
        order.setPhone(user.getPhone());
        order.setSubscription(itemOrders.get(0).isSubscription());
        order.setTotalItems(itemOrders.size());
        order.setTotalPrice(itemOrderService.countTotalPrice(itemOrders));
        order.setTotalDiscountPrice(itemOrderService.countDiscountTotalPrice(itemOrders));
        order.setExpectPrice(order.getTotalPrice() - order.getTotalDiscountPrice());
        order.setUserId(user.getId());
        order.setOrderStatus(OrderStatus.ORDER_REQUEST);
        order.setTotalQuantity(itemOrderService.countQuantity(itemOrders));

        for(ItemOrder itemOrder : itemOrders) {
            itemOrder.setOrder(order);
            itemOrderService.plusSales(itemOrder); // 판매량 누적
            itemOrderRepository.save(itemOrder);
        }

        orderRepository.save(order);
        return order;
    }


    //    public Order createOrder(Order order) {
//        return orderRepository.save(order);
//    }

    public void cancelOrder(long orderId) {
        Order findOrder = findOrder(orderId);
        findOrder.setOrderStatus(OrderStatus.ORDER_CANCEL);
        itemOrderService.minusSales(findOrder.getItemOrders()); // 주문 취소 -> 판매량 집계에서 제외
        orderRepository.save(findOrder);
    }

    public Order findOrder(long orderId) {
        Order findOrder = findVerifiedOrder(orderId);
        return findOrder;
    }

    public Page<Order> findOrders(Long userId, int page, boolean subscription) {
        UserInfoResponseDto user = userService.findUserById(userId);

        if(subscription) {
            Page<Order> findAllOrder = orderRepository.findAllByUserIdAndSubscriptionAndOrderStatusNot(
                    PageRequest.of(page, 7, Sort.by("orderId").descending()),
                    user.getId(), true, OrderStatus.ORDER_REQUEST);

            return findAllOrder;
        }
        Page<Order> findAllOrder = orderRepository.findAllByUserIdAndSubscriptionAndOrderStatusNotAndOrderStatusNot(
                PageRequest.of(page, 7, Sort.by("orderId").descending()),
                user.getId(), false, OrderStatus.ORDER_REQUEST, OrderStatus.ORDER_SUBSCRIBE);

        return findAllOrder;
    }

    public Page<Order> findSubs(Long userId, int page) {
        Page<Order> findAllSubs = orderRepository.findAllByUserIdAndOrderStatus(
                PageRequest.of(page, 6, Sort.by("orderId").descending()), userId, OrderStatus.ORDER_SUBSCRIBE);

        return findAllSubs;
    }

    public Page<ItemOrder> findAllSubs(Long userId, int page) {
        Page<ItemOrder> findAllSubs = itemOrderRepository.findAllSubs(
                PageRequest.of(page, 6, Sort.by("itemOrderId").descending()), OrderStatus.ORDER_SUBSCRIBE, userId);

        return findAllSubs;
    }

    public Order findVerifiedOrder(long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        Order findOrder = optionalOrder.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND));
        return findOrder;
    }
    public boolean isShopper(long itemId, long userId) { // 유저의 특정 아이템 구매여부 확인
        List<Order> order = orderRepository.findByItemAndUser(itemId, userId, OrderStatus.ORDER_REQUEST);
        if(order.size() == 0) return false;
        else return true;
    }
    public void completeOrder( Long orderId ){
        Order order = findOrder(orderId);
        order.setOrderStatus(OrderStatus.ORDER_COMPLETE);
    }

    public void subsOrder( Long orderId ){
        Order order = findOrder(orderId);
        order.setOrderStatus(OrderStatus.ORDER_SUBSCRIBE);
    }
    public Order deepCopy(Order order){
        Order newOrder = new Order(order);
        orderRepository.save(newOrder);
        return newOrder;
    }

}
