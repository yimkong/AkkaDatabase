package com.akkademo.actor;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import com.akkademo.articleMessages.ArticleBody;
import com.akkademo.articleMessages.HttpResponse;
import com.akkademo.articleMessages.ParseArticle;
import com.akkademo.articleMessages.ParseHtmlArticle;
import com.akkademo.commonMessages.GetRequest;
import com.akkademo.commonMessages.SetRequest;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.concurrent.TimeoutException;

/**
 * author yg
 * description 思考每个actor会接收到什么消息，并且返回什么消息
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
                receive(ReceiveBuilder
                        .matchEquals(String.class, x -> x.equals("timeout"), x -> {
                            senderRef.tell(new Status.Failure(new TimeoutException("timeout!")), self());
                            context().stop(self());
                        })
                        .match(HttpResponse.class, httpResponse -> articleParseActor.tell(new ParseHtmlArticle(url, httpResponse.body), self()))//请求ParsingActor解析并且返回目标是自己
                        .match(String.class, body -> {
                            senderRef.tell(body, self());
                            context().stop(self());
                        })
                        .match(ArticleBody.class, articleBody -> {//经过自己再请求别的actor并二次返回给自己的消息
                            cacheActor.tell(new SetRequest(articleBody.body, self()), self());
                            senderRef.tell(articleBody.body, self());
                            context().stop(self());
                        })
                        .matchAny(t -> System.err.println("ignoring msg: " + t.getClass()))
                        .build()
                );
            }
        }
        return context().actorOf(Props.create(MyActor.class, () -> new MyActor()));
    }

    //按书上要求只有在缓存请求无法返回结果的时候才请求原始文章并解析 TODO 待修改
    private ActorRef buildExtraActorOnlyNoCache(ActorRef senderRef, String url) {
        class MyActor extends AbstractActor {
            //是否有缓存
            private boolean ifHasCache = false;

            MyActor() {
                if (ifHasCache) {
                    System.err.println("有缓存经过");
                    receive(ReceiveBuilder
                            .match(String.class, body -> {//cache
                                senderRef.tell(body, self());
                                ifHasCache = true;
                                context().stop(self());
                            })
                            .matchAny(t -> {
                                ifHasCache = false;
                                System.err.println("ignoring msg: " + t.getClass());
                            })
                            .build()
                    );
                }else {
                    System.err.println("没有缓存经过");
                    receive(ReceiveBuilder
                            .matchEquals(String.class, x -> x.equals("timeout"), x -> {
                                senderRef.tell(new Status.Failure(new TimeoutException("timeout!")), self());
                                ifHasCache = false;
                                context().stop(self());
                            })
                            .match(HttpResponse.class, httpResponse -> {
                                if (!ifHasCache) {
                                    articleParseActor.tell(new ParseHtmlArticle(url, httpResponse.body), self());//请求ParsingActor解析并且返回目标是自己
                                }
                            })
                            .match(ArticleBody.class, articleBody -> {//经过自己再请求别的actor并二次返回给自己的消息 或者是别的方法调用发送的消息
                                cacheActor.tell(new SetRequest(articleBody.body, self()), self());
                                senderRef.tell(articleBody.body, self());
                                context().stop(self());
                            })
                            .matchAny(t -> {
                                ifHasCache = false;
                                System.err.println("ignoring msg: " + t.getClass());
                            })
                            .build()
                    );
                }
            }
        }
        return context().actorOf(Props.create(MyActor.class, () -> new MyActor()));
    }
}
