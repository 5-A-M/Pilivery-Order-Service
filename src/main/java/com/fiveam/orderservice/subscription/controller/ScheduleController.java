package com.fiveam.orderservice.subscription.controller;

import com.fiveam.orderservice.client.ItemServiceClient;
import com.fiveam.orderservice.order.entity.ItemOrder;
import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.order.mapper.ItemOrderMapper;
import com.fiveam.orderservice.order.service.ItemOrderService;
import com.fiveam.orderservice.order.service.OrderService;
import com.fiveam.orderservice.response.SingleResponseDto;
import com.fiveam.orderservice.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@Slf4j

@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final SubscriptionService subscriptionService;
    private final OrderService orderService;
    private final ItemOrderService itemOrderService;
    private final ItemServiceClient itemService;

    private final ItemOrderMapper itemOrderMapper;

    @GetMapping("/kakao")
    public ResponseEntity startsKakaoSchedule( @RequestParam(name = "orderId") Long orderId ) throws SchedulerException{

        List<ItemOrder> itemOrders = subscriptionService.getItemOrders(orderId);

        for(ItemOrder io : itemOrders){
            log.warn("start kakao scheduling...item-id: " + io.getItemId());
            Order order = orderService.findOrder(orderId);
            String nextDelivery = String.valueOf(order.getCreatedAt().plusDays(io.getPeriod()));
            ItemOrder itemOrder = itemOrderService.setDeliveryInfo(orderId, order.getCreatedAt(), nextDelivery, io);
            subscriptionService.startSchedule(orderId, itemOrder);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/change")
    public ResponseEntity changePeriod(
            @RequestParam(name = "orderId") Long orderId, @RequestParam(name = "period") Integer period, @RequestParam(name = "itemOrderId") Long itemOrderId ) throws SchedulerException, InterruptedException{
        ItemOrder itemOrder = subscriptionService.changePeriod(orderId, period, itemOrderId);
        return new ResponseEntity<>(new SingleResponseDto<>(itemOrderMapper.itemOrderToSubResponse(itemService, itemOrder)), HttpStatus.OK);
    }

    @PatchMapping("/delay")
    public ResponseEntity delay( @RequestParam(name = "orderId") Long orderId, @RequestParam(name = "delay") Integer delay, @RequestParam(name = "itemOrderId") Long itemOrderId ) throws SchedulerException{
        ItemOrder itemOrder = subscriptionService.delayDelivery(orderId, delay, itemOrderId);
        return new ResponseEntity<>(new SingleResponseDto<>(itemOrderMapper.itemOrderToSubResponse(itemService, itemOrder)), HttpStatus.OK);
    }

    @DeleteMapping("/cancel")
    public ZonedDateTime delete( @RequestParam(name = "orderId") Long orderId, @RequestParam(name = "itemOrderId") Long itemOrderId ) throws SchedulerException{
        subscriptionService.cancelScheduler(orderId, itemOrderId);
        return ZonedDateTime.now();
    }

}



