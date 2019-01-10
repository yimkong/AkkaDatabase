package akkademy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestProbe;
import akka.util.Timeout;
import com.akkademo.actor.ParsingActor;
import com.akkademo.actor.TellDemoArticleParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TellTest {
    private ActorSystem system;
    private Timeout timeout;
    private TestProbe cacheProbe;
    private TestProbe httpClientProbe;
    private ActorRef tellDemoActor;

    @Before
    public void before() {
        system = ActorSystem.create("testSystem");
        timeout = Timeout.apply(10000, TimeUnit.MILLISECONDS);

        cacheProbe = new TestProbe(system);
        httpClientProbe = new TestProbe(system);
        ActorRef articleParseActor = system.actorOf(Props.create(ParsingActor.class));

        tellDemoActor = system.actorOf(
                Props.create(TellDemoArticleParser.class,
                        cacheProbe.ref().path().toString(),
                        httpClientProbe.ref().path().toString(),
                        articleParseActor.path().toString(),
                        timeout)
        );
    }

    @After
    public void after() {
        if (system != null) {
            system.terminate();
        }
    }


    @Test
    public void itShouldParseArticleTest0() throws Exception {
        Common.testWithNoCache(tellDemoActor, cacheProbe, httpClientProbe, timeout);
    }

    @Test
    public void itShouldParseArticleTest1() throws Exception {
        Common.testWithCache(tellDemoActor, cacheProbe, httpClientProbe, timeout);
    }

    @Test
    public void itShouldParseArticleTest2() throws Exception {
        Common.testWithNoCacheReturn(tellDemoActor, cacheProbe, httpClientProbe, timeout);
    }

    /**
     * 该测试应将 {@link com.akkademo.actor.TellDemoArticleParser#receive()}里的 "buildExtraActor(sender(), msg.url);"改为 "buildExtraActorOnlyNoCache(sender(), msg.url);"
     */
    @Test
    public void itShouldParseArticleTest3() throws Exception {
        Common.testMethod(tellDemoActor, cacheProbe, httpClientProbe, timeout);
    }
}
