package ro.mv.krol.extract;

import ro.mv.krol.model.Link;
import ro.mv.krol.model.Selector;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mihai.vaduva on 21/08/2016.
 */
public class LinkExtractor {

    public List<Link> extract(Document document, Selector selector) {
        return document.select(selector.getQuery(), selector.getAttribute()).stream()
                .filter(value -> value != null && !value.isEmpty())
                .map(value -> Link.builder()
                        .withUrl(value)
                        .withMetadata(selector.getMetadata())
                        .putMetadata("selector", selector.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
