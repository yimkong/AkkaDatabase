package com.akkademo.actor.cluster;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import com.akkademo.articleMessages.ParseArticle;

public class ArticleParseActor extends AbstractActor {
    private ArticleParseActor() {
        receive(ReceiveBuilder.
                match(ParseArticle.class, x -> {
                            ArticleParser.apply(x.url).
                                    onSuccess(body -> sender().tell(body, self())).
                                    onFailure(t -> sender().tell(new Status.Failure(t), self()));
                        }
                ).
                build());
    }
}
