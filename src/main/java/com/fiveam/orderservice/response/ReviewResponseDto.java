package com.fiveam.orderservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    private long reviewId;
    private long itemId;
    private long userId;
    private String displayName;
    private String content;
    private int star;
    private String createdAt;
    private String updatedAt;
}
