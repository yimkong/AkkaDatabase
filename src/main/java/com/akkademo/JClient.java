package com.akkademo;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.akkademo.messages.GetRequest;
import com.akkademo.messages.SetRequest;

import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * author yg
 * description
 * date 2018/12/29
 */
public class JClient {
    private final ActorSystem system = ActorSystem.create("LocalSystem");
    private final ActorSelection remoteDb;

    public JClient(String remoteAddr) {
        remoteDb = system.actorSelection("akka.tcp://akkademo@" + remoteAddr + "/user/akkademo-db");
    }

    public CompletionStage set(String key, int value) {
        return toJava(ask(remoteDb, new SetRequest(key, value), 2000));
    }

    public CompletionStage<Object> get(String key) {
        return toJava(ask(remoteDb, new GetRequest(key), 2000));
    }
}
