package ro.mv.krol.engine;

import ro.mv.krol.browser.HtmlPage;
import ro.mv.krol.extract.Document;
import ro.mv.krol.extract.DocumentFactory;
import ro.mv.krol.extract.LinkExtractor;
import ro.mv.krol.model.CompiledSeed;
import ro.mv.krol.model.CompiledSelector;
import ro.mv.krol.model.Link;
import ro.mv.krol.model.Selector;
import ro.mv.krol.util.Args;
import ro.mv.krol.util.URLUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<Selector.Target, List<Link>> extract(HtmlPage htmlPage, CompiledSeed seed) throws IOException {
        URL baseURL = URLUtils.getBaseURLOf(htmlPage.getUrl());
        Document document = documentFactory.createFrom(baseURL, htmlPage.getSource());
        Map<Selector.Target, List<Link>> linkMap = new HashMap<>();
        seed.getCompiledSelectors().forEach((target, compiledSelectors) -> {
            if (compiledSelectors == null || compiledSelectors.isEmpty()) {
                return;
            }
            linkMap.put(target, extract(document, htmlPage, compiledSelectors));
        });
        return linkMap;
    }

    public List<Link> extract(Document document, HtmlPage htmlPage, List<CompiledSelector> selectors) {
        return selectors.stream()
                .filter(s -> s.pass(htmlPage.getMetadata()))
                .map(s -> linkExtractor.extract(document, s))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
