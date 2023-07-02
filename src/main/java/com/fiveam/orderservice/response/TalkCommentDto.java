package com.fiveam.orderservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TalkCommentDto { // 상세페이지

    private long talkCommentId;
    private long userId;
    private String displayName;
    private String content;
    private boolean shopper;
    private String createdAt;
    private String updatedAt;
}
