package com.akkademo.actor;


import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.akkademo.articleMessages.ArticleBody;
import com.akkademo.articleMessages.ParseHtmlArticle;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import scala.PartialFunction;

public class ParsingActor extends AbstractActor {
    public PartialFunction receive() {
        return ReceiveBuilder.
                match(ParseHtmlArticle.class, msg -> {
                    String body = ArticleExtractor.INSTANCE.getText(msg.htmlString);
                    sender().tell(new ArticleBody(msg.uri, body), self());
                }).build();
    }
}
