package com.akkademo.commonMessages;

import akka.actor.ActorRef;
import scala.Serializable;

/**
 * author yg
 * description
 * date 2018/12/2
 */
public class SetRequest implements Serializable, Request {
    public final String key;
    public final Object value;
    public final ActorRef sender;

    public SetRequest(String key, Object value) {
        this.key = key;
        this.value = value;
        this.sender = ActorRef.noSender();
    }

    public SetRequest(String key, Object value, ActorRef sender) {
        this.key = key;
        this.value = value;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "SetRequest{" +
                "key='" + key + '\'' +
                ", value=" + value +
                ", sender=" + sender +
                '}';
    }

}
