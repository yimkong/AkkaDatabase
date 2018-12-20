package pong;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * author yg
 * description
 * date 2018/12/19
 */
public class JavaPongActor extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.matchEquals("Ping", s -> sender().
                tell("Pong", ActorRef.noSender())).matchAny(x -> sender().
                tell(new Status.Failure(new Exception("unkown message")), self())).build();
    }
}
