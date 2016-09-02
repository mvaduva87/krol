package ro.mv.krol.storage.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ro.mv.krol.model.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by mihai on 9/2/16.
 */
@Singleton
public class MemoryResourceCache implements ResourceCache {

    private final Cache<String, Resource> cache;

    @Inject
    public MemoryResourceCache(@Named("resource.cache.size") long size) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(size)
                .build();
    }

    @Override
    public Resource get(String url) {
        return cache.getIfPresent(url);
    }

    @Override
    public void put(String url, Resource resource) {
        cache.put(url, resource);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}
