package com.fiveam.orderservice.client;

import com.fiveam.orderservice.response.UserInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("user-service")
public interface UserServiceClient {

    @GetMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    UserInfoResponseDto getLoginUser(@RequestHeader("Authorization") String authorization);

    @GetMapping("/users/{userId}")
    ResponseEntity<UserInfoResponseDto> findUserById(@PathVariable Long userId);

    @GetMapping("/users/{userId}")
    ResponseEntity<UserInfoResponseDto> findUserById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long userId
    );
}
