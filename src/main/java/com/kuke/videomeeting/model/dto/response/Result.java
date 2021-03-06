package com.kuke.videomeeting.model.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Result {
    private boolean success;
    private int code;
    private String msg;
}
