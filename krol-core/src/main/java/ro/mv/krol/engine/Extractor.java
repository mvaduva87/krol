package ro.mv.krol.engine;

import groovy.lang.Script;
import ro.mv.krol.browser.HtmlPage;
import ro.mv.krol.extract.Document;
import ro.mv.krol.extract.DocumentFactory;
import ro.mv.krol.extract.LinkExtractor;
import ro.mv.krol.model.Link;
import ro.mv.krol.model.Seed;
import ro.mv.krol.model.Selector;
import ro.mv.krol.util.Args;
import ro.mv.krol.util.URLUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by mihai.vaduva on 30/08/2016.
 */
@Singleton
public class Extractor {

    private final DocumentFactory documentFactory;
    private final LinkExtractor linkExtractor;

    @Inject
    public Extractor(DocumentFactory documentFactory, LinkExtractor linkExtractor) {
        this.documentFactory = Args.notNull(documentFactory, "documentFactory");
        this.linkExtractor = Args.notNull(linkExtractor, "linkExtractor");
    }

    public Map<Selector.Target, List<Link>> extract(HtmlPage htmlPage, Seed origin) throws IOException {
        URL baseURL = URLUtils.getBaseURLOf(htmlPage.getUrl());
        Document document = documentFactory.createFrom(baseURL, htmlPage.getSource());

    }

    private Stream<Link> extract(Document document, HtmlPage htmlPage, List<Selector> selectors) {
        return selectors.stream()
                .filter(s -> pass(s, htmlPage))
                .map(s -> linkExtractor.extract(document, s))
                .flatMap(Collection::stream);
    }

    private boolean pass(Selector selector, HtmlPage htmlPage) {
        if (selector.getWhen() == null || selector.getWhen().isEmpty()) {
            return true;
        }
        if (htmlPage.getMetadata() == null || htmlPage.getMetadata().isEmpty()) {
            return false;
        }
        try {
            Script script = scriptManager.parse(selector.getWhen());
            htmlPage.getMetadata().forEach(script::setProperty);
            return (boolean) script.run();
        } catch (Exception ignored) {
            return false;
        }
    }

}
