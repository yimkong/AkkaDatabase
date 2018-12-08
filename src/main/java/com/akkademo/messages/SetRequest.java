package com.akkademo.messages;

import lombok.*;

/**
 * author yg
 * description
 * date 2018/12/2
 */
@Getter
public class SetRequest {
    private final String key;
    private final String value;

    public SetRequest(String key, String value) {
        this.key = key;
        this.value = value;
    }

}
