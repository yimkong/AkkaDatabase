package com.akkademo.actor;

import akka.actor.*;
import akka.http.scaladsl.model.HttpResponse;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import com.akkademo.messages.GetRequest;
import com.akkademo.messages.SetRequest;
import com.akkademo.service.ArticleBody;
import com.akkademo.service.ParseArticle;
import com.akkademo.service.ParseHtmlArticle;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

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

    //ask 会返回future(CompletionStage)
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(ParseArticle.class, msg -> {
            final CompletionStage cacheResult = toJava(ask(cacheActor, new GetRequest(msg.url), timeout));
            final CompletionStage result = cacheResult.handle((x, t) -> {
                return (x != null) ? CompletableFuture.completedFuture(x) : toJava(ask(httpClienActor, msg.url, timeout)).thenCompose(rawArticle -> toJava(ask(articleParseActor, new ParseHtmlArticle(msg.url, ((HttpResponse) rawArticle).entity()), timeout)));
            }).thenCompose(x -> x);
            final ActorRef senderRef = sender();
            result.handle((x, t) -> {
                if (x != null) {
                    if (x instanceof ArticleBody) {
                        String body = ((ArticleBody) x).body;
                        cacheActor.tell(body, self());//cache it
                        senderRef.tell(body, self());//reply
                    } else if (x == null) {
                        senderRef.tell(new akka.actor.Status.Failure((Throwable) t), self());
                    }
                }
                return null;
            });
        }).build();
    }

    //tell(更简单、高效)优先于ask
   /* @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(ParseArticle.class, msg -> {
            ActorRef extraActor = buildExtraActor(sender(), msg.url);
            cacheActor.tell(new GetRequest(msg.url), extraActor);
            httpClienActor.tell(msg.url, extraActor);
            context().system().scheduler().scheduleOnce(timeout.duration(), extraActor, "timeout", context().system().dispatcher(), ActorRef.noSender());//超时检查自身
        }).build();
    }*/

    private ActorRef buildExtraActor(ActorRef senderRef, String url) {
        class MyActor extends AbstractActor {
            public MyActor() {
                receive(
                        ReceiveBuilder.matchEquals(
                                String.class, x -> x.equals("timeout"), x -> {
                                    senderRef.tell(new Status.Failure(new TimeoutException("timeout!")), self());
                                    context().stop(self());
                                }).match(HttpResponse.class, httpResponse -> articleParseActor.tell(new ParseHtmlArticle(url, httpResponse.entity()), self())
                        ).match(String.class, body -> {
                            senderRef.tell(body, self());
                            context().stop(self());
                        }).match(ArticleBody.class, articleBody -> {
                            cacheActor.tell(new SetRequest(articleBody.body, self()), self());
                            context().stop(self());
                        }).matchAny(t -> System.err.println("ignoring msg: " + t.getClass())
                        ).build()
                );
            }
        }
        return context().actorOf(Props.create(MyActor::new));
    }
}
