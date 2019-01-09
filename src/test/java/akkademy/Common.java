package akkademy;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.testkit.TestProbe;
import akka.util.Timeout;
import com.akkademo.messages.GetRequest;
import com.akkademo.service.HttpResponse;
import com.akkademo.service.ParseArticle;
import scala.concurrent.Await;
import scala.concurrent.Future;

import static akka.pattern.Patterns.ask;

/**
 * author yg
 * description
 * date 2019/1/9
 */
public class Common {

    public static void test(ActorRef askDemoActor, TestProbe cacheProbe,TestProbe httpClientProbe, Timeout timeout) throws Exception {
        Future f = ask(askDemoActor, new ParseArticle(("http://www.baidu.com")), timeout);
        cacheProbe.expectMsgClass(GetRequest.class);
        cacheProbe.reply(new Status.Failure(new Exception("no cache")));

        httpClientProbe.expectMsgClass(String.class);
        httpClientProbe.reply(new HttpResponse(Articles.article1));

        String result = (String) Await.result(f, timeout.duration());
        assert (result.contains("I’ve been writing a lot in emacs lately"));
        assert (!result.contains("<body>"));
    }
}
