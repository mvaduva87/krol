package ro.mv.krol.storage;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ro.mv.krol.model.Link;
import ro.mv.krol.model.Resource;
import ro.mv.krol.storage.cache.MemoryResourceCache;
import ro.mv.krol.storage.cache.ResourceCache;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by mihai on 9/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceStorageTest {

    private ResourceStorage resourceStorage;

    @Mock
    private Storage storage;

    @Spy
    private ResourceCache resourceCache = new MemoryResourceCache(10);

    @Before
    public void setUp() throws Exception {
        resourceStorage = new ResourceStorage(storage, resourceCache);
    }

    @Test
    public void storeShouldSaveLinkDataAndReturnResourceObject() throws Exception {
        URL resourceURL = getClass().getResource("/resource.txt");
        String path = resourceURL.toExternalForm();
        String storedPath = path + ".stored";
        Link link = Link.builder()
                .withUrl(path)
                .putMetadata("key", "value")
                .build();
        when(storage.write(any(StorageKey.class), any(InputStream.class)))
                .thenReturn(storedPath);

        Resource resource = resourceStorage.store(link);

        assertThat(resource.getLocator(), is(equalTo(storedPath)));
        assertThat(resource.getTimestamp(), is(notNullValue()));
        assertThat(resource.getUrl(), is(equalTo(link.getUrl())));
        assertThat(resource.getMetadata().entrySet(), is(equalTo(link.getMetadata().entrySet())));
        assertThat(resource.getContentType(), is(equalTo("text/plain")));
    }

    @Test
    public void storeShouldFetchLinkDataAndPersistItUsingStorage()
            throws Exception {
        URL resourceURL = getClass().getResource("/resource.txt");
        String path = resourceURL.toExternalForm();
        String storedPath = path + ".stored";
        Link link = Link.builder()
                .withUrl(path)
                .putMetadata("key", "value")
                .build();
        when(storage.write(any(StorageKey.class), any(InputStream.class)))
                .thenReturn(storedPath);

        Resource resource = resourceStorage.store(link);

        // should use storage to persist data
        ArgumentCaptor<StorageKey> captor = ArgumentCaptor.forClass(StorageKey.class);
        verify(storage, times(1)).write(captor.capture(), any(InputStream.class));
        StorageKey storageKey = captor.getValue();

        // verify that correct data was passed to storage object
        assertThat(storageKey.getTimestamp(), is(equalTo(resource.getTimestamp())));
        assertThat(storageKey.getContentType(), is(equalTo("text/plain")));
        String expectedName = DigestUtils.md5Hex(IOUtils.toByteArray(resourceURL));
        assertThat(storageKey.getName(), is(expectedName));
    }

    @Test
    public void storeShouldHandleDataEncodedLink() throws Exception {
        String base64Data = "R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKToQGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7";
        Link encodedLink = Link.builder()
                .withUrl("data:image/gif;base64," + base64Data)
                .putMetadata("key", "value")
                .build();
        final String locator = "file:/image.gif";
        ByteArrayOutputStream storeBuffer = new ByteArrayOutputStream();
        when(storage.write(any(StorageKey.class), any(InputStream.class)))
                .thenAnswer(inv -> {
                    InputStream stream = (InputStream) inv.getArguments()[1];
                    IOUtils.copy(stream, storeBuffer);
                    return locator;
                });

        Resource resource = resourceStorage.store(encodedLink);

        assertThat(resource.getLocator(), is(equalTo(locator)));
        assertThat(resource.getTimestamp(), is(notNullValue()));
        assertThat(resource.getUrl(), is(equalTo(encodedLink.getUrl())));
        assertThat(resource.getMetadata().entrySet(), is(equalTo(encodedLink.getMetadata().entrySet())));
        assertThat(resource.getContentType(), is(equalTo("image/gif")));

        byte[] storedData = storeBuffer.toByteArray();
        assertThat(base64Data, is(equalTo(Base64.getEncoder().encodeToString(storedData))));

        ArgumentCaptor<StorageKey> captor = ArgumentCaptor.forClass(StorageKey.class);
        verify(storage, times(1)).write(captor.capture(), any(InputStream.class));
        StorageKey storageKey = captor.getValue();

        assertThat(storageKey.getName(), is(equalTo(DigestUtils.md5Hex(storedData))));
        assertThat(storageKey.getContentType(), is(equalTo("image/gif")));
    }

    @Test
    public void storeShouldCacheDirectLinkResources() throws Exception {
        URL resourceURL = getClass().getResource("/resource.txt");
        String path = resourceURL.toExternalForm();
        String storedPath = path + ".stored";
        Link link = Link.builder().withUrl(path).build();
        when(storage.write(any(StorageKey.class), any(InputStream.class)))
                .thenReturn(storedPath);
        InOrder inOrder = inOrder(resourceCache, storage);

        // store link first time
        Resource first = resourceStorage.store(link);

        // store multiple times same link and collect resources
        int restoreCount = new Random().nextInt(100);
        restoreCount = Math.max(1, restoreCount);
        List<Resource> resources = new ArrayList<>();
        for (int i = 0; i < restoreCount; i++) {
            resources.add(resourceStorage.store(link));
        }

        // storing same link multiple times should return cached resource
        for (Resource resource : resources) {
            assertThat(resource, is(first));
        }
        inOrder.verify(resourceCache).get(path);
        inOrder.verify(storage).write(any(StorageKey.class), any(InputStream.class));
        inOrder.verify(resourceCache).put(path, first);
        inOrder.verify(resourceCache, times(restoreCount)).get(path);
        inOrder.verifyNoMoreInteractions();
    }
}
