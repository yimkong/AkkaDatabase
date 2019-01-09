package com.akkademo.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import com.akkademo.messages.GetRequest;
import com.akkademo.service.ArticleBody;
import com.akkademo.service.HttpResponse;
import com.akkademo.service.ParseArticle;
import com.akkademo.service.ParseHtmlArticle;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * author yg
 * description
 * date 2019/1/6
 */
public class AskDemoArticleParser extends AbstractActor {
    private final ActorSelection cacheActor;
    private final ActorSelection httpClienActor;
    private final ActorSelection articleParseActor;
    private final Timeout timeout;

    public AskDemoArticleParser(String cacheActor, String httpClienActor, String articleParseActor, Timeout timeout) {
        this.cacheActor = context().actorSelection(cacheActor);
        this.httpClienActor = context().actorSelection(httpClienActor);
        this.articleParseActor = context().actorSelection(articleParseActor);
        this.timeout = timeout;
    }

    //ask 会返回future(CompletionStage) 以及创建临时actor  有额外的性能和内存消耗
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(ParseArticle.class, msg -> {
            final CompletionStage cacheResult = toJava(ask(cacheActor, new GetRequest(msg.url), timeout));
            final CompletionStage result = cacheResult.handle((x, t) -> {
                return (x != null) ? CompletableFuture.completedFuture(x) : toJava(ask(httpClienActor, msg.url, timeout)).thenCompose(rawArticle -> toJava(ask(articleParseActor, new ParseHtmlArticle(msg.url, ((HttpResponse) rawArticle).body), timeout)));
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
                    senderRef.tell(new akka.actor.Status.Failure((Throwable) t), self());
                }
                return null;
            });
        }).build();
    }

}
