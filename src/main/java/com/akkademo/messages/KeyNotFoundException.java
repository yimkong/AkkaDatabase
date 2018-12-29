package com.akkademo.messages;

import scala.Serializable;

/**
 * author yg
 * description
 * date 2018/12/29
 */
public class KeyNotFoundException extends Exception implements Serializable {
    public final String key;

    public KeyNotFoundException(String key) {
        this.key = key;
    }
}
