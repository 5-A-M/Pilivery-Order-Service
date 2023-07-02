package com.fiveam.orderservice.payment.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@ToString
public class KakaoPayRequestDto {

    private String tid;
    private String next_redirect_pc_url;
    private ZonedDateTime create_at = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

}
