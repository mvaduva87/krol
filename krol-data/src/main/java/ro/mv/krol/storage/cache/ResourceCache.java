package ro.mv.krol.storage.cache;

import ro.mv.krol.model.Resource;
import ro.mv.krol.util.Cache;

/**
 * Created by mihai on 9/2/16.
 */
public interface ResourceCache extends Cache<String, Resource> {

    @Override
    Resource get(String url);

    @Override
    void put(String url, Resource resource);
}
