package com.akkademo.actor;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import com.akkademo.messages.GetRequest;
import com.akkademo.messages.SetRequest;
import com.akkademo.service.ArticleBody;
import com.akkademo.service.HttpResponse;
import com.akkademo.service.ParseArticle;
import com.akkademo.service.ParseHtmlArticle;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeoutException;

/**
 * author yg
 * description
 * date 2019/1/9
 */
public class TellDemoArticleParser extends AbstractActor {
    private final ActorSelection cacheActor;
    private final ActorSelection httpClienActor;
    private final ActorSelection articleParseActor;
    private final Timeout timeout;

    public TellDemoArticleParser(String cacheActor, String httpClienActor, String articleParseActor, Timeout timeout) {
        this.cacheActor = context().actorSelection(cacheActor);
        this.httpClienActor = context().actorSelection(httpClienActor);
        this.articleParseActor = context().actorSelection(articleParseActor);
        this.timeout = timeout;
    }

    //tell(更简单、高效)优先于ask
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(ParseArticle.class, msg -> {
            ActorRef extraActor = buildExtraActor(sender(), msg.url);
            cacheActor.tell(new GetRequest(msg.url), extraActor);
            httpClienActor.tell(msg.url, extraActor);
            context().system().scheduler().scheduleOnce(timeout.duration(), extraActor, "timeout", context().system().dispatcher(), ActorRef.noSender());//超时检查自身
        }).build();
    }

    private ActorRef buildExtraActor(ActorRef senderRef, String url) {
        class MyActor extends AbstractActor {
            public MyActor() {
                receive(
                        ReceiveBuilder.matchEquals(
                                String.class, x -> x.equals("timeout"), x -> {
                                    senderRef.tell(new Status.Failure(new TimeoutException("timeout!")), self());
                                    context().stop(self());
                                }
                        ).match(HttpResponse.class, httpResponse -> articleParseActor.tell(new ParseHtmlArticle(url, httpResponse.body), self())
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
        return context().actorOf(Props.create(MyActor.class, () -> new MyActor()));
    }

}
