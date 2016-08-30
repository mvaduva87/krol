package ro.mv.krol.extract.jsoup;

import ro.mv.krol.extract.DocumentFactory;
import org.jsoup.Jsoup;
import ro.mv.krol.extract.Document;

import java.io.IOException;
import java.net.URL;

/**
 * Created by mihai on 8/22/16.
 */
public class JsoupDocumentFactory implements DocumentFactory {

    @Override
    public Document createFrom(URL baseURL, String source) throws IOException {
        try {
            return new JsoupDocument(Jsoup.parse(source, baseURL.toExternalForm()));
        } catch (RuntimeException e) {
            throw new IOException(e);
        }
    }
}
