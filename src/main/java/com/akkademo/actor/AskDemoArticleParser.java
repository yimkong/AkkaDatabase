package com.akkademo.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import com.akkademo.commonMessages.GetRequest;
import com.akkademo.articleMessages.ArticleBody;
import com.akkademo.articleMessages.HttpResponse;
import com.akkademo.articleMessages.ParseArticle;
import com.akkademo.articleMessages.ParseHtmlArticle;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * author yg
 * description 解析请求文章的actor
 * date 2019/1/6
 */
public class AskDemoArticleParser extends AbstractActor {
    private final ActorSelection cacheActor;
    private final ActorSelection httpClienActor;
    private final ActorSelection parsingActor;
    private final Timeout timeout;

    public AskDemoArticleParser(String cacheActor, String httpClienActor, String parsingActor, Timeout timeout) {
        this.cacheActor = context().actorSelection(cacheActor);
        this.httpClienActor = context().actorSelection(httpClienActor);
        this.parsingActor = context().actorSelection(parsingActor);
        this.timeout = timeout;
    }

    //ask 会返回future(CompletionStage) 以及创建临时actor  有额外的性能和内存消耗
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(ParseArticle.class, msg -> {
            final CompletionStage cacheResult = toJava(ask(cacheActor, new GetRequest(msg.url), timeout));//请求缓存
            final CompletionStage result = cacheResult.handle((x, t) -> {
                if (x == null) {
                    System.err.println(t);
                }
                return (x != null) ? CompletableFuture.completedFuture(x) : toJava(ask(httpClienActor, msg.url, timeout))//缓存里有的话就返回，没有的话请求httpClienActor得到html，然后请求ParsingActor解析html
                        .thenCompose(rawArticle -> toJava(ask(parsingActor, new ParseHtmlArticle(msg.url, ((HttpResponse) rawArticle).body), timeout)));
            }).thenCompose(x -> x);
            final ActorRef senderRef = sender();
            result.handle((x, t) -> {
                if (x != null) {
                    if (x instanceof ArticleBody) {
                        String body = ((ArticleBody) x).body;//parsed article
                        cacheActor.tell(body, self());//cache it
                        senderRef.tell(body, self());//reply
                    } else if (x instanceof String) //cached article
                        senderRef.tell(x, self());
                } else {
                    senderRef.tell(new Status.Failure((Throwable) t), self());
                }
                return null;
            });
        }).build();
    }

}
