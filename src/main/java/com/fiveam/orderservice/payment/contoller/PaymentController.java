package com.fiveam.orderservice.payment.contoller;

import com.fiveam.orderservice.client.UserServiceClient;
import com.fiveam.orderservice.response.UserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fiveam.orderservice.exception.bussiness.BusinessLogicException;
import com.fiveam.orderservice.exception.bussiness.ExceptionCode;
import com.fiveam.orderservice.order.entity.Order;
import com.fiveam.orderservice.order.service.OrderService;
import com.fiveam.orderservice.payment.dto.KakaoPayApproveDto;
import com.fiveam.orderservice.payment.dto.KakaoPayRequestDto;
import com.fiveam.orderservice.payment.service.PayService;
import com.fiveam.orderservice.payment.service.SubsPayService;
import com.fiveam.orderservice.redis.RedisConfig;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PayService payService;
    private final OrderService orderService;
    private final UserServiceClient userService;
    private final RedisConfig redis;
    private final SubsPayService subsPayService;
    private Long userId;
    private String clientAuth;

    @Value(("${back.url}"))
    private String backUrl;

    @Value("${back.scheme}")
    private String scheme;

    @Value("${back.host}")
    private String host;

    @Value("${back.port}")
    private String port;

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/kakao-pay")
    public KakaoPayRequestDto payRequest(@RequestHeader("Authorization") String authorization, @RequestParam(name = "orderId") Long orderId ){
        UserInfoResponseDto user = userService.getLoginUser(authorization);
        userId = user.getId();
        clientAuth = authorization;

        Order order = orderService.findOrder(orderId);
        KakaoPayRequestDto requestResponse =
                payService.kakaoPayRequest(order.getExpectPrice(), order.getTotalQuantity(), orderId);

        redis.redisTemplate().opsForValue().set(
                String.valueOf(userId),
                requestResponse.getTid(), 1000 * 60 * 15, TimeUnit.MILLISECONDS
        );

        return requestResponse;
    }


    @GetMapping("/kakao/success")
    public ResponseEntity payApprove(
            @RequestParam("pg_token") String pgToken
    ){
        String tid = (String) redis.redisTemplate().opsForValue().get(String.valueOf(userId));
        if(tid == null) throw new BusinessLogicException(ExceptionCode.EXPIRED_TID);

        KakaoPayApproveDto kakaoPayApproveDto = payService.kakaoPayApprove(tid, pgToken);

        Long orderId = Long.valueOf(kakaoPayApproveDto.getPartner_order_id());
        log.info("orderId = {}", orderId);
        orderService.completeOrder(orderId);

        return new ResponseEntity<>(kakaoPayApproveDto, HttpStatus.CREATED);
    }


    @GetMapping("/kakao/subs/success")
    public void paySubsApprove(
            @RequestParam("pg_token") String pgToken ){
        String tid = (String) redis.redisTemplate().opsForValue().get(String.valueOf(userId));
        if(tid == null) throw new BusinessLogicException(ExceptionCode.EXPIRED_TID);

        KakaoPayApproveDto kakaoPayApproveDto = payService.kakaoSubsPayApprove(tid, pgToken);

        log.warn("sid 진짜로 = {}", kakaoPayApproveDto);

        Long orderId = Long.valueOf(kakaoPayApproveDto.getPartner_order_id());
        log.info("orderId = {}", orderId);

        hasSid(kakaoPayApproveDto, orderId);

        orderService.subsOrder(orderId);
        doKakaoScheduling(orderId);
    }


    @GetMapping("/general/success")
    public ResponseEntity<Map<String, String>> home(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("amount") String amount,
            @RequestParam("orderId") String orderId
    ) throws IOException{
        String result = payService.generalPay(paymentKey, orderId, Integer.parseInt(amount));
        orderId = orderId.replace("abcdef", "");

        orderService.completeOrder(Long.parseLong(orderId));

//        String url = "http://pillivery.s3-website.ap-northeast-2.amazonaws.com/mypage/order/normal";
//        response.sendRedirect(url); // 302

        HashMap<String, String> body = new HashMap<>();
        body.put("redirectUrl", backUrl + "/mypage/order/normal");
        body.put("status", String.valueOf(HttpStatus.OK.value()));
        return ResponseEntity.ok(body);
    }

//    @GetMapping("/general/subs/success")
//    public void home(
//            @RequestParam("customerKey") String customerKey, @RequestParam("authKey") String authKey ) throws IOException, InterruptedException{
//
//        long orderId = getOrderIdAndSetBillingKey(customerKey, authKey);
//
//        orderService.subsOrder(orderId);
//        doGeneralScheduling(orderId);
//    }


//    @GetMapping("/general/subscription")
//    public void subsGeneral(
//            @RequestParam(name = "orderId") String orderId ) throws IOException{
//        long parseOrderId = Long.parseLong(orderId);
//        subsPayService.subsApprove(parseOrderId);
//        orderService.subsOrder(parseOrderId);
//    }

    @GetMapping("/kakao/subscription")
    public ResponseEntity subsKakao(
            @RequestParam(name = "orderId") String orderId ) throws IOException{

        Order order = orderService.findOrder(Long.parseLong(orderId));
        String sid = userService.findUserById(order.getUserId()).getBody().getSid();
        log.warn("sid = {}", sid);

        KakaoPayApproveDto kakaoPayApproveDto = subsPayService.kakaoSubsPayRequest(order.getExpectPrice(), order.getTotalQuantity(), Long.parseLong(orderId), sid);

        orderService.subsOrder(Long.valueOf(orderId));

        return new ResponseEntity<>(kakaoPayApproveDto, HttpStatus.CREATED);
    }

    @GetMapping("/cancel")//TODO 일반결제 카카오페이결제 실패시 url결정해야
    public ResponseEntity cancel(){
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/fail")
    public ResponseEntity fail(){
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

//    private long getOrderIdAndSetBillingKey( String customerKey, String authKey ){
//        String parsedOrderId = customerKey.replace("a", "");
//        long orderId = Long.parseLong(parsedOrderId);
//        Order order = orderService.findOrder(orderId);
//        User user = order.getUser();
//        BillingKeyDto.Response response = subsPayService.getBillingKey(authKey, customerKey);
//        String billingKey = response.getBillingKey();
//        user.setBillingKey(billingKey);
//        return orderId;
//    }

    private void doKakaoScheduling( Long orderId ){

        log.info("scheduler orderId = {}", orderId);
        MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();

        queryParam.add("orderId", String.valueOf(orderId));
        log.info("query = {}", queryParam);

        URI uri = UriComponentsBuilder.newInstance().scheme(scheme).host(host).port(port).path("/schedule/kakao")//TODO: 나중에 URL 전체변경
                .queryParams(queryParam).build().toUri();
        log.info("uri = {}", uri);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(uri, String.class);
    }

    private void hasSid( KakaoPayApproveDto kakaoPayApproveDto, Long orderId){
        Order order = orderService.findOrder(orderId);
        UserInfoResponseDto user = userService.findUserById(clientAuth, order.getUserId()).getBody();

        if(user.getSid() != null) return;
        user.setSid(kakaoPayApproveDto.getSid());
    }
//    private void doGeneralScheduling( Long orderId ){
//
//        log.info("scheduler orderId = {}", orderId);
//        MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
//
//        queryParam.add("orderId", String.valueOf(orderId));
//        log.info("query = {}", queryParam);
//
//        URI uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(9090).path("/schedule/general")//TODO: 나중에 URL 전체변경
//                .queryParams(queryParam).build().toUri();
//        log.info("uri = {}", uri);
//
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.getForObject(uri, String.class);
//    }
}
