package ro.mv.krol.queue;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by mihai on 8/25/16.
 */
public interface ChannelFactory extends Closeable {

    <M> Channel<M> openChannelFor(Class<M> messageClass, String queueName) throws IOException;

}
