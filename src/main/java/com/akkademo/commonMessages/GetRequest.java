package com.akkademo.commonMessages;

import scala.Serializable;

/**
 * author yg
 * description
 * date 2018/12/29
 */
public class GetRequest implements Serializable {
    public final String key;

    public GetRequest(String key) {
        this.key = key;
    }
}

