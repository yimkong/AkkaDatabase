package clientactor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import com.akkademo.Constant.State;
import com.akkademo.actor.AkkademoDb;
import com.akkademo.actor.clientactor.FSMClientActor;
import com.akkademo.commonMessages.FlushMsg;
import com.akkademo.commonMessages.GetRequest;
import com.akkademo.commonMessages.SetRequest;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

public class FSMClientActorTest {
    ActorSystem system = ActorSystem.create("testSystem", ConfigFactory.defaultReference());

    TestActorRef<AkkademoDb> dbRef = TestActorRef.create(system, Props.create(AkkademoDb.class));

    TestProbe probe = TestProbe.apply(system);

    @Test
    public void itShouldTransitionToConnectedAndPending() throws Exception {
        TestActorRef<FSMClientActor> fsmClientRef =
                TestActorRef.create(system, Props.create(FSMClientActor.class, dbRef.path().toString()));

        assert (fsmClientRef.underlyingActor().stateName() == State.DISCONNECTED);
        fsmClientRef.tell(new GetRequest("testkey"), probe.ref());

        assert (fsmClientRef.underlyingActor().stateName() == State.CONNECTED_AND_PENDING);
    }

    @Test
    public void itShouldFlushMessagesInConnectedAndPending() throws Exception {
        TestActorRef<FSMClientActor> fsmClientRef =
                TestActorRef.create(system, Props.create(FSMClientActor.class, dbRef.path().toString()));

        fsmClientRef.tell(new SetRequest("testkey", "testvalue", probe.ref()), probe.ref());

        assert (fsmClientRef.underlyingActor().stateName() == State.CONNECTED_AND_PENDING);

        fsmClientRef.tell(new FlushMsg(), probe.ref());

        probe.expectMsgClass(Status.Success.class);
        assert (fsmClientRef.underlyingActor().stateName() == State.CONNECTED);
    }

}
