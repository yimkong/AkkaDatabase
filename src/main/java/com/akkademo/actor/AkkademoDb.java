package com.akkademo.actor;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.akkademo.commonMessages.*;

import java.util.HashMap;
import java.util.List;
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
                    sender().tell(new Status.Success(message.key), self());
                })
                .match(List.class, message -> {
                            message.forEach(x -> {
                                if (x instanceof SetRequest) {
                                    SetRequest setRequest = (SetRequest) x;
                                    handleSetRequest(setRequest);
                                }
                                if (x instanceof GetRequest) {
                                    GetRequest getRequest = (GetRequest) x;
                                    handleGetRequest(getRequest);
                                }
                            });
                        }
                )
                .match(GetRequest.class, message -> {
                    log.info("Received Get request:{}", message);
                    handleGetRequest(message);
                })
                .match(Ping.class, msg -> {
                    log.info("Received Ping");
                    sender().tell(new Connected(), self());
                })
                .matchAny(o -> sender().tell(new Status.Failure(new ClassNotFoundException()), self())).build());
    }

    private void handleSetRequest(SetRequest message) {
        log.info("Received Set request: {}", message);
        map.put(message.key, message.value);
        message.sender.tell(new Status.Success(message.key), self());
    }

    private void handleGetRequest(GetRequest getRequest) {
        log.info("Received Get request: {}", getRequest);
        Object value = map.get(getRequest.key);
        Object response = (value != null)
                ? value
                : new Status.Failure(new KeyNotFoundException(getRequest.key));
        sender().tell(response, self());
    }
}
