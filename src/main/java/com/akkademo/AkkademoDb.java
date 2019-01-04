package com.akkademo;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.akkademo.messages.GetRequest;
import com.akkademo.messages.KeyNotFoundException;
import com.akkademo.messages.SetRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * author yg
 * description
 * date 2018/12/2
 */
public class AkkademoDb extends AbstractActor {
    public final LoggingAdapter log = Logging.getLogger(context().system(), this);
    public final Map<String, Object> map = new HashMap<>();

    private AkkademoDb() {
        receive(ReceiveBuilder
                .match(SetRequest.class, message -> {
                    log.info("received Set requeset:{}", message);
                    map.put(message.key, message.value);
                })
                .match(GetRequest.class, message -> {
                    log.info("Received Get request:{}", message);
                    Object value = map.get(message.key);
                    Object response = value != null ? value : new Status.Failure(new KeyNotFoundException(message.key));
                    sender().tell(response, sender());
                })
                .matchAny(o -> sender().tell(new Status.Failure(new ClassNotFoundException()), self())).build());
    }
}
