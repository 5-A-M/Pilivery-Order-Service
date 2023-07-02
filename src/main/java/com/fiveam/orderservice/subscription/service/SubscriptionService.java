package com.fiveam.orderservice.subscription.service;

import com.fiveam.orderservice.client.ItemServiceClient;
import com.fiveam.orderservice.client.UserServiceClient;
import com.fiveam.orderservice.order.entity.ItemOrder;
import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.order.service.ItemOrderService;
import com.fiveam.orderservice.order.service.OrderService;
import com.fiveam.orderservice.response.ItemDetailResponseDto;
import com.fiveam.orderservice.response.UserInfoResponseDto;
import com.fiveam.orderservice.subscription.job.JobDetailService;
import com.fiveam.orderservice.subscription.trigger.TriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static org.quartz.JobKey.jobKey;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final Scheduler scheduler;
    private final TriggerService trigger;
    private final JobDetailService jobDetail;
    private final OrderService orderService;
    private final ItemOrderService itemOrderService;

    /* Service OpenFeign Client For MSA */
    private final UserServiceClient userService;
    private final ItemServiceClient itemService;


    public void startSchedule( Long orderId, ItemOrder itemOrder ) throws SchedulerException{
        UserInfoResponseDto user = getUser(orderId);
        ItemDetailResponseDto item = itemService.findVerifiedItem(itemOrder.getItemId());
        JobKey jobkey = jobKey(user.getId() + item.getTitle(), String.valueOf(user.getId()));
        JobDetail payDay = jobDetail.buildJobDetail(jobkey, orderId, itemOrder);
        Trigger lastTrigger = trigger.buildTrigger(jobkey, orderId, itemOrder);
        Date date = scheduler.scheduleJob(payDay, lastTrigger);
        log.warn("new scheduler = {}", date);
    }


    public ItemOrder changePeriod( Long orderId, Integer period, Long itemOrderId ) throws SchedulerException, InterruptedException{

        ItemOrder itemOrder = itemOrderService.setItemPeriod(orderId, period, findItemOrderInOrder(orderId, itemOrderId));
        log.info("changed period = {}", itemOrder.getPeriod());

        if(payDirectly(orderId, period, itemOrder)){
            itemOrder.getPaymentDay().plusDays(itemOrder.getPeriod());
            return itemOrder;
        }

        ZonedDateTime paymentDay = itemOrder.getPaymentDay();
        String nextDelivery = String.valueOf(paymentDay.plusDays(itemOrder.getPeriod()));
        log.info("extend nextDelivery = {}", nextDelivery);

        ItemOrder updatedItemOrder = itemOrderService.setDeliveryInfo(orderId, paymentDay, nextDelivery, itemOrder);

        extendPeriod(orderId, updatedItemOrder);

        paymentDay.plusDays(itemOrder.getPeriod());
        return updatedItemOrder;
    }


    private boolean payDirectly( Long orderId, Integer period, ItemOrder itemOrder ) throws SchedulerException{
        boolean noMargin = itemOrder.getPaymentDay().plusDays(period).isBefore(ZonedDateTime.now(ZoneId.of("Asia/Seoul"))); //바궈야진
        log.info("margin = {}", noMargin);

        if(noMargin){
            log.info("directly pay");
            resetSchedule(orderId, itemOrder);
            return true;
        }
        return false;
    }

    private void deleteSchedule( Long orderId, ItemOrder itemOrder ) throws SchedulerException{
        log.info("delete schedule");
        UserInfoResponseDto user = getUser(orderId);
        ItemDetailResponseDto item = itemService.findVerifiedItem(itemOrder.getItemId());
        scheduler.deleteJob(jobKey(user.getId() + item.getTitle(), String.valueOf(user.getId())));
    }

    private void extendPeriod( Long orderId, ItemOrder itemOrder ) throws SchedulerException, InterruptedException{
        log.warn("extendPeriod = {}", itemOrder.getPeriod());
        resetSchedule(orderId, itemOrder);
    }


    public ItemOrder delayDelivery( Long orderId, Integer delay, Long itemOrderId ) throws SchedulerException{
        log.info("delay Delivery");
        ItemOrder itemOrder = itemOrderService.delayDelivery(orderId, delay, findItemOrderInOrder(orderId, itemOrderId));
        resetSchedule(orderId, itemOrder);
        //        itemOrder.getNextDelivery();
        return itemOrder;
    }

    public void cancelScheduler( Long orderId, Long itemOrderId ) throws SchedulerException{
        log.info("cancelScheduler");
        ItemOrder itemOrder = getItemOrder(itemOrderId);
        deleteSchedule(orderId, itemOrder);
        itemOrderService.cancelItemOrder(orderId, itemOrder);
        log.warn("canceled Item Id = {}", itemOrder.getItemId());
    }

    private void resetSchedule( Long orderId, ItemOrder itemOrder ) throws SchedulerException{
        deleteSchedule(orderId, itemOrder);
        startSchedule(orderId, itemOrder);
    }

    public List<ItemOrder> getItemOrders( Long orderId ){
        Order order = orderService.findOrder(orderId);
        return order.getItemOrders();
    }

    public ItemOrder getItemOrder( Long itemOrderId ){
        return itemOrderService.findItemOrder(itemOrderId);
    }

    public UserInfoResponseDto getUser( Long orderId ){
        return userService.findUserById(orderService.findOrder(orderId).getUserId()).getBody();
    }

    private ItemOrder findItemOrderInOrder( Long orderId, Long itemOrderId ){
        Order order = orderService.findOrder(orderId);
        ItemOrder itemOrder = getItemOrder(itemOrderId);
        int i = order.getItemOrders().indexOf(itemOrder);
        return order.getItemOrders().get(i);
    }

}
