package ro.mv.krol.queue;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by mihai on 8/25/16.
 */
public interface Channel<M> {

    void consumeWith(Consumer<M> consumer) throws IOException;

    void publish(M message) throws IOException;

}
