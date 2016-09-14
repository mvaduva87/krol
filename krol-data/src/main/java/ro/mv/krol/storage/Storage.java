package ro.mv.krol.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Created by mihai.vaduva on 08/08/2016.
 */
public interface Storage {

    String write(StorageKey key, Reader source) throws IOException;

    String write(StorageKey key, InputStream source) throws IOException;

}
