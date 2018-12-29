package com.akkademo;

import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * author yg
 * description
 * date 2018/12/29
 */
public class Main {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create("akkademo");
        actorSystem.actorOf(Props.create(AkkademoDb.class), "akkademo-db");
    }
}
