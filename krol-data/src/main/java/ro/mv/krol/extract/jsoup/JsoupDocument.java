package ro.mv.krol.extract.jsoup;

import ro.mv.krol.extract.Document;
import ro.mv.krol.util.Args;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mihai on 8/22/16.
 */
public class JsoupDocument implements Document {

    private final org.jsoup.nodes.Document document;

    public JsoupDocument(org.jsoup.nodes.Document document) {
        this.document = Args.notNull(document, "org.jsoup.nodes.Document");
    }

    @Override
    public List<String> select(String query, String attr) {
        return document.select(query).stream()
                .map(e -> e.attr(attr))
                .collect(Collectors.toList());
    }
}
