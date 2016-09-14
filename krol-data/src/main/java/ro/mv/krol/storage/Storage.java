package ro.mv.krol.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
public interface Storage {

    String write(StorageKey key, InputStream source) throws IOException;

    default String write(StorageKey key, String data, Charset charset) throws IOException {
        return write(key, new ByteArrayInputStream(data.getBytes(charset)));
    }

}
