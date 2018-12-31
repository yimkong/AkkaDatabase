package com.akkademo.messages;

import scala.Serializable;

/**
 * author yg
 * description
 * date 2018/12/2
 */
public class SetRequest implements Serializable {
    public final String key;
    public final Object value;

    public SetRequest(String key, Object value) {
        this.key = key;
        this.value = value;
    }

}
