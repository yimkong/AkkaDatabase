package akkademy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestProbe;
import akka.util.Timeout;
import com.akkademo.actor.AskDemoArticleParser;
import com.akkademo.actor.ParsingActor;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class AskTest {
    ActorSystem system = ActorSystem.create("testSystem");
    Timeout timeout = Timeout.apply(10000, TimeUnit.MILLISECONDS);

    TestProbe cacheProbe = new TestProbe(system);
    TestProbe httpClientProbe = new TestProbe(system);
    ActorRef articleParseActor = system.actorOf(Props.create(ParsingActor.class));

    ActorRef askDemoActor = system.actorOf(
            Props.create(AskDemoArticleParser.class,
                    cacheProbe.ref().path().toString(),
                    httpClientProbe.ref().path().toString(),
                    articleParseActor.path().toString(),
                    timeout)
    );


    @Test
    public void itShouldParseArticleTest() throws Exception {
        Common.test(askDemoActor, cacheProbe, httpClientProbe, timeout);
    }
}
