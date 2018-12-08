package com.akkademo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.akkademo.messages.SetRequest;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * author yg
 * description
 * date 2018/12/2
 */
public class AkkademoTest {
    ActorSystem system = ActorSystem.create();

    @Test
    public void itShouldPlaceKyeValueFromSetMessageIntoMap() {
        TestActorRef<AkkademoDb> actorRef = TestActorRef.create(system, Props.create(AkkademoDb.class));
        actorRef.tell(new SetRequest("key", "value"), ActorRef.noSender());
        AkkademoDb akkademoDb = actorRef.underlyingActor();
        assertEquals(akkademoDb.map.get("key"), "value");
    }
}
