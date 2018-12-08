package com.akkademo;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.akkademo.messages.SetRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * author yg
 * description
 * date 2018/12/2
 */
public class AkkademoDb extends AbstractActor {
    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
    protected final Map<String, Object> map = new HashMap<>();

    private AkkademoDb() {
        receive(ReceiveBuilder.match(SetRequest.class, message -> {
            log.info("received Set requeset:{}", message);
            map.put(message.getKey(), message.getValue());
        }).matchAny(o -> log.info("received unknown message:{}", o)).build());
    }
}
