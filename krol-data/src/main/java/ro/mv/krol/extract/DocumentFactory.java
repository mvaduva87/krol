package ro.mv.krol.extract;

import java.io.IOException;
import java.net.URL;

/**
 * Created by mihai on 8/19/16.
 */
public interface DocumentFactory {

    Document createFrom(URL baseURL, String source) throws IOException;
}
