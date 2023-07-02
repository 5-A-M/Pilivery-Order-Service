package com.fiveam.orderservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TalkAndCommentDto { // 상세페이지 - 토크

    private long talkId;
    private long userId;
    private String displayName;
    private long itemId;
    private String content;
    private boolean shopper;
    private String createdAt;
    private String updatedAt;
    private List<TalkCommentDto> talkComments;
}
