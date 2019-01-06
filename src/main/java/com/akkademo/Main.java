package com.akkademo;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.akkademo.actor.AkkademoDb;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * author yg
 * description
 * date 2018/12/29
 */
public class Main {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create("akkademo", ConfigFactory.load());
        Config config = actorSystem.settings().config();
        actorSystem.actorOf(Props.create(AkkademoDb.class), "akkademo-db");
    }
}
