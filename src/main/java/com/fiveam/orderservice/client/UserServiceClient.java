package com.fiveam.orderservice.client;

import com.fiveam.orderservice.response.UserInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("user-service")
public interface UserServiceClient {
    @GetMapping("/users")
    UserInfoResponseDto getLoginUser();

    @GetMapping("/users/{userId}")
    UserInfoResponseDto findUserById(@PathVariable Long userId);
}
