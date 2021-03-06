package akkademy;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.testkit.TestProbe;
import akka.util.Timeout;
import com.akkademo.articleMessages.HttpResponse;
import com.akkademo.articleMessages.ParseArticle;
import com.akkademo.commonMessages.GetRequest;
import com.akkademo.commonMessages.SetRequest;
import scala.concurrent.Await;
import scala.concurrent.Future;

import static akka.pattern.Patterns.ask;

/**
 * author yg
 * description
 * date 2019/1/9
 */
public class Common {

    //无缓存测试
    public static void testWithNoCache(ActorRef askDemoActor, TestProbe cacheProbe, TestProbe httpClientProbe, Timeout timeout) throws Exception {
        Future f = ask(askDemoActor, new ParseArticle(("http://www.baidu.com")), timeout);

        cacheProbe.expectMsgClass(GetRequest.class);
        cacheProbe.reply(new Status.Failure(new Exception("no cache")));//返回抛出没有缓存的异常

        httpClientProbe.expectMsgClass(String.class);
        httpClientProbe.reply(new HttpResponse(Articles.article1));

        String result = (String) Await.result(f, timeout.duration());
        assert (result.contains("I’ve been writing a lot in emacs lately"));
        assert (!result.contains("<body>"));
    }

    //有缓存测试
    public static void testWithCache(ActorRef askDemoActor, TestProbe cacheProbe, TestProbe httpClientProbe, Timeout timeout) throws Exception {
        Future f = ask(askDemoActor, new ParseArticle(("http://www.baidu.com")), timeout);

        cacheProbe.expectMsgClass(GetRequest.class);
        cacheProbe.reply(Articles.article1);//返回有缓存

        String result = (String) Await.result(f, timeout.duration());
        assert result.equals(Articles.article1);
    }

    //无缓存结果返回测试
    public static void testWithNoCacheReturn(ActorRef askDemoActor, TestProbe cacheProbe, TestProbe httpClientProbe, Timeout timeout) throws Exception {
        Future f = ask(askDemoActor, new ParseArticle(("http://www.baidu.com")), timeout);

        httpClientProbe.expectMsgClass(String.class);
        httpClientProbe.reply(new HttpResponse(Articles.article1));

        String result = (String) Await.result(f, timeout.duration());
        assert (result.contains("I’ve been writing a lot in emacs lately"));
        assert (!result.contains("<body>"));
    }

    /**
     * 无缓存结果请求并请求缓存测试
     * @see com.akkademo.actor.TellDemoArticleParser#buildExtraActorOnlyNoCache
     */
    public static void testMethod(ActorRef askDemoActor, TestProbe cacheProbe, TestProbe httpClientProbe, Timeout timeout) throws Exception {
        Future f = ask(askDemoActor, new ParseArticle(("http://www.baidu.com")), timeout);

        cacheProbe.expectMsgClass(GetRequest.class);
        cacheProbe.reply(new Status.Failure(new Exception("no cache")));//返回抛出没有缓存的异常

        httpClientProbe.expectMsgClass(String.class);
        httpClientProbe.reply(new HttpResponse(Articles.article1));

        cacheProbe.expectMsgClass(SetRequest.class);

        String result = (String) Await.result(f, timeout.duration());
        assert (result.contains("I’ve been writing a lot in emacs lately"));
        assert (!result.contains("<body>"));
        //有缓存了再次请求应该不请求原始文章解析
        Future f1 = ask(askDemoActor, new ParseArticle(("http://www.baidu.com")), timeout);

        cacheProbe.expectMsgClass(GetRequest.class);
        cacheProbe.reply(Articles.article1);//返回有缓存

        String result1 = (String) Await.result(f1, timeout.duration());
        assert result1.equals(Articles.article1);
    }
}
