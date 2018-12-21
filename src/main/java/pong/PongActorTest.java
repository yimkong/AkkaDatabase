package pong;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.Test;
import scala.concurrent.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * author yg
 * description 异步测试
 * date 2018/12/19
 */
public class PongActorTest {
    ActorSystem system = ActorSystem.create();
    ActorRef actorRef = system.actorOf(Props.create(JavaPongActor.class));

    @Test
    public void shouldReplyToPingWithPong() throws Exception {
        final CompletionStage<String> cs = askPong("Ping"); //转换成java的future
        final CompletableFuture<String> jFuture = (CompletableFuture<String>) cs;
        assert (jFuture.get(1000, TimeUnit.MILLISECONDS)).equals("Pong");
    }

    private CompletionStage<String> askPong(String message) {
        Future sFuture = ask(actorRef, message, 1000);//scala的future
        final CompletionStage<String> cs = toJava(sFuture); //转换成java的future
        return cs;
    }

    @Test(expected = ExecutionException.class)
    public void shouldReplyToUnknownMessageWithFailure() throws Exception {
        Future sFuture = ask(actorRef, "unknown", 1000);
        final CompletionStage<String> cs = toJava(sFuture);
        final CompletableFuture<String> jFuture = (CompletableFuture<String>) cs;
        jFuture.get(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void printToConsole() throws Exception {
        askPong("Ping").thenAccept(x -> System.out.println("replied with: " + x));
        askPong("Ping").thenApply(x -> x.charAt(1)).thenAccept(x -> System.out.println("replied with: " + x.getClass()));//thenApply进行类型转换
        CompletionStage<String> stringCompletionStage = askPong("Ping").thenCompose(x -> askPong("Ping"));//链式异步(不会多层future嵌套)
        Thread.sleep(1000);
    }
}
