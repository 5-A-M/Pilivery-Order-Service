package com.fiveam.orderservice.order.service;

import com.fiveam.orderservice.client.ItemServiceClient;
import com.fiveam.orderservice.exception.bussiness.BusinessLogicException;
import com.fiveam.orderservice.exception.bussiness.ExceptionCode;
import com.fiveam.orderservice.order.dto.SalesQuantityDto;
import com.fiveam.orderservice.order.entity.ItemOrder;
import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.order.reposiroty.ItemOrderRepository;
import com.fiveam.orderservice.order.reposiroty.OrderRepository;
import com.fiveam.orderservice.response.ItemInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemOrderService {

    private final ItemServiceClient itemService;
    private final ItemOrderRepository itemOrderRepository;
    private final OrderRepository orderRepository;

    public List<ItemOrder> createItemOrder( ItemOrder itemOrder ){
        itemOrderRepository.save(itemOrder);
        List<ItemOrder> itemOrders = new ArrayList<>();
        itemOrders.add(itemOrder);

        return itemOrders;
    }

    public ItemOrder findItemOrder( long itemOrderId ){
        Optional<ItemOrder> optionalItemOrder = itemOrderRepository.findById(itemOrderId);
        ItemOrder itemOrder = optionalItemOrder.orElseThrow(() -> new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND));

        return itemOrder;
    }

    public ItemOrder changeSubQuantity( long itemOrderId, int upDown ){
        ItemOrder itemOrder = findItemOrder(itemOrderId);

        itemOrder.setQuantity(itemOrder.getQuantity() + upDown);
        itemOrderRepository.save(itemOrder);

        return itemOrder;
    }

    public int countTotalPrice( List<ItemOrder> itemOrders ){

        if(itemOrders == null) return 0;

        int totalPrice = 0;

//        TODO: 효율성 문제(itemId 하나당 통신을 보내버림)
        for(ItemOrder itemOrder : itemOrders){
            log.info("상품 아이디: " + itemOrder.getItemId());
            int quantity = itemOrder.getQuantity();
            int price = itemService.getItem(itemOrder.getItemId()).getPrice();
            totalPrice += ( quantity * price );
        }

        return totalPrice;
    }

    public int countDiscountTotalPrice( List<ItemOrder> itemOrders ){

        if(itemOrders == null) return 0;

        int totalDiscountPrice = 0;

//        TODO: 효율성 문제(itemId 하나당 통신을 보내버림)
        for(ItemOrder itemOrder : itemOrders){
            int quantity = itemOrder.getQuantity();
            ItemInfoResponseDto item = itemService.getItem(itemOrder.getItemOrderId());
            int price = item.getPrice();
            int discountRate = item.getDiscountRate();
            totalDiscountPrice += ( quantity * price * discountRate / 100 );
        }

        return totalDiscountPrice;
    }

    public int countQuantity( List<ItemOrder> itemOrders ){ // 주문의 담긴 상품의 총량을 구하는 메서드

        if(itemOrders == null) return 0;

        int totalquantity = 0;

        for(ItemOrder itemOrder : itemOrders){
            int quantity = itemOrder.getQuantity();
            totalquantity += quantity;
        }

        return totalquantity;
    }

    public void minusSales( List<ItemOrder> itemOrders ){ // 주문 취소할 경우 아이템 판매량에서 제외

        for(ItemOrder itemOrder : itemOrders){
            itemService.minusSales(itemOrder.getItemId(),
                    SalesQuantityDto.builder().quantity(itemOrder.getQuantity()).build());
        }
    }

    public void plusSales( ItemOrder itemOrder ){ // 주문 요청할 경우 아이템 판매량 증가
        log.info("Plus Sales Quantity" + itemOrder);
        itemService.plusSales(itemOrder.getItemId(),
                SalesQuantityDto.builder().quantity(itemOrder.getQuantity()).build());
    }

    public ItemOrder setItemPeriod( Long orderId, Integer period, ItemOrder io ){

        Optional<Order> order = orderRepository.findById(orderId);

        if(order.isPresent()){
            ItemOrder itemOrder = getItemOrder(io, order);
            itemOrder.setPeriod(period);
            log.error("주기변경 = {}", itemOrder.getPeriod());
            return itemOrder;
        }
        throw new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND);
    }

    public ItemOrder delayDelivery( Long orderId, Integer delay, ItemOrder io ){

        Optional<Order> order = orderRepository.findById(orderId);

        if(order.isPresent()){
            ItemOrder itemOrder = getItemOrder(io, order);
            ZonedDateTime nextDelivery = itemOrder.getNextDelivery().plusDays(delay);
            itemOrder.setNextDelivery(nextDelivery);
            return itemOrder;
        }
        throw new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND);
    }


    public ItemOrder setDeliveryInfo( Long orderId, ZonedDateTime paymentDay, String nextDelivery, ItemOrder io ){

        Optional<Order> order = orderRepository.findById(orderId);

        if(order.isPresent()){
            ItemOrder itemOrder = getItemOrder(io, order);
            itemOrder.setPaymentDay(paymentDay);
            itemOrder.setNextDelivery(ZonedDateTime.parse(nextDelivery));
            return itemOrder;
        }

        throw new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND);
    }

    public ItemOrder itemOrderCopy( Long lastOrderId, Order newOrder, ItemOrder io ){
        Optional<Order> orderEntity = orderRepository.findById(lastOrderId);
        log.warn("여기서 오더 아이디 = {}", newOrder.getOrderId());
        if(orderEntity.isPresent()){
            ItemOrder itemOrder = new ItemOrder(getItemOrder(io, orderEntity));
            itemOrder.setOrder(newOrder);
            plusSales(itemOrder);
            itemOrderRepository.save(itemOrder);
            return itemOrder;
        } throw new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND);
    }

    public void cancelItemOrder( Long orderId, ItemOrder itemOrder ){
        Optional<Order> order = orderRepository.findById(orderId);
        if(order.isPresent()){
            ItemOrder itemOrderInOrder = getItemOrder(itemOrder, order);
            itemOrderInOrder.setSubscription(false);
            log.warn("is subsucription = {}", itemOrderInOrder.isSubscription());
        }

    }

    private ItemOrder getItemOrder( ItemOrder io, Optional<Order> order ){
        int i = order.get().getItemOrders().indexOf(io);
        return order.get().getItemOrders().get(i);
    }
}
