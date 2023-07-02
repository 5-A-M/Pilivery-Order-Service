package com.fiveam.orderservice.subscription.job;


import com.fiveam.orderservice.client.ItemServiceClient;
import com.fiveam.orderservice.exception.bussiness.BusinessLogicException;
import com.fiveam.orderservice.exception.bussiness.ExceptionCode;
import com.fiveam.orderservice.order.entity.ItemOrder;
import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.order.service.ItemOrderService;
import com.fiveam.orderservice.order.service.OrderService;
import com.fiveam.orderservice.response.ItemDetailResponseDto;
import com.fiveam.orderservice.response.UserInfoResponseDto;
import com.fiveam.orderservice.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.quartz.JobKey.jobKey;

@Slf4j
@RequiredArgsConstructor
@Component

public class SubscriptionJob implements Job {
    private final ItemOrderService itemOrderService;
    private final OrderService orderService;
    private final Scheduler scheduler;
    private final JobDetailService jobDetail;
    private final SubscriptionService subscriptionService;
    private final ItemServiceClient itemService;

    @Override
    public void execute( JobExecutionContext context ) throws JobExecutionException{

        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();

        ItemOrder itemOrder = (ItemOrder) mergedJobDataMap.get("itemOrder");
        log.info("start itemOrderId = {}", itemOrder.getItemOrderId());
        log.info("itemOrder's Item Id = {}", itemOrder.getItemId());

        ZonedDateTime paymentDay = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("payment = {}", paymentDay);

        Long orderId = (Long) mergedJobDataMap.get("orderId");
        log.info("start orderId = {}", orderId);
        String nextDelivery = String.valueOf(paymentDay.plusDays(itemOrder.getPeriod()));
        itemOrderService.setDeliveryInfo(orderId, paymentDay, nextDelivery, itemOrder);

        Order newOrder = getNewOrder(orderId);
        log.info("newOrder Id = {}", newOrder.getOrderId());
        ItemOrder newItemOrder = itemOrderService.itemOrderCopy(orderId, newOrder, itemOrder);

        JobDetail newJob = updateJob(itemOrder, orderId, newOrder, newItemOrder);
        try{
            scheduler.addJob(newJob, true);
        } catch(SchedulerException e){
            throw new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND);
        }

        connectAutoPay(newOrder.getOrderId());
    }

    private Order getNewOrder( Long orderId ){
        orderService.completeOrder(orderId);
        Order order = orderService.findOrder(orderId);
        return orderService.deepCopy(order);
    }

    private JobDetail updateJob( ItemOrder itemOrder, Long orderId, Order newOrder, ItemOrder newItemOrder ){
        UserInfoResponseDto user = subscriptionService.getUser(orderId);
        ItemDetailResponseDto item = itemService.findVerifiedItem(itemOrder.getItemId());
        JobKey jobkey = jobKey(user.getId() + item.getTitle(), String.valueOf(user.getId()));
        return jobDetail.buildJobDetail(jobkey, newOrder.getOrderId(), newItemOrder);
    }

    private void connectAutoPay( Long orderId ){

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

        parameters.add("orderId", String.valueOf(orderId));

        URI uri = UriComponentsBuilder.newInstance().scheme("http").host("ec2-43-201-37-71.ap-northeast-2.compute.amazonaws.com").port(8080) // 호스트랑 포트는 나중에 변경해야한다.
                .path("/payments/subscription").queryParams(parameters).build().toUri();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(uri, String.class);
    }


}
