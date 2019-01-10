package clientactor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import com.akkademo.actor.AkkademoDb;
import com.akkademo.actor.clientactor.HotswapClientActor;
import com.akkademo.commonMessages.GetRequest;
import com.akkademo.commonMessages.SetRequest;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

public class HotswapClientActorTest {
    ActorSystem system = ActorSystem.create("testSystem", ConfigFactory.defaultReference());

    @Test
    public void itShouldSet() throws Exception {
        TestActorRef<AkkademoDb> dbRef = TestActorRef.create(system, Props.create(AkkademoDb.class));
        AkkademoDb db = dbRef.underlyingActor(); //通信actor

        TestProbe probe = TestProbe.apply(system);
        TestActorRef<HotswapClientActor> clientRef =
                TestActorRef.create(system, Props.create(HotswapClientActor.class, dbRef.path().toString()));

        clientRef.tell(new SetRequest("testkey", "testvalue", probe.ref()), probe.ref());

        probe.expectMsg(new Status.Success("testkey"));
        assert (db.map.get("testkey") == "testvalue");
    }

    @Test
    public void itShouldGet() throws Exception {
        TestActorRef<AkkademoDb> dbRef = TestActorRef.create(system, Props.create(AkkademoDb.class));
        AkkademoDb db = dbRef.underlyingActor();
        db.map.put("testkey", "testvalue");

        TestProbe probe = TestProbe.apply(system);
        TestActorRef<HotswapClientActor> clientRef =
                TestActorRef.create(system, Props.create(HotswapClientActor.class, dbRef.path().toString()));

        clientRef.tell(new GetRequest("testkey"), probe.ref());
        probe.expectMsg("testvalue");
    }
}
