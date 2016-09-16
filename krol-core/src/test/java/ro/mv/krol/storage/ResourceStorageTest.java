package ro.mv.krol.storage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ro.mv.krol.model.Link;
import ro.mv.krol.model.Resource;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;

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

    @Before
    public void setUp() throws Exception {
        resourceStorage = new ResourceStorage(storage);
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
    }

    @Test
    public void storeShouldHandleDataEncodedLink() throws Exception {
        String base64Data = "R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKToQGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7";
        Link encodedLink = Link.builder()
                .withUrl("data:image/gif;base64," + base64Data)
                .putMetadata("key", "value")
                .build();
        final String expectedLocator = "file:/image.gif";
        ByteArrayOutputStream storeBuffer = new ByteArrayOutputStream();
        when(storage.write(any(StorageKey.class), any(InputStream.class)))
                .thenAnswer(inv -> {
                    InputStream stream = (InputStream) inv.getArguments()[1];
                    IOUtils.copy(stream, storeBuffer);
                    return expectedLocator;
                });

        Resource resource = resourceStorage.store(encodedLink);

        assertThat(resource.getLocator(), is(equalTo(expectedLocator)));
        assertThat(resource.getTimestamp(), is(notNullValue()));
        assertThat(resource.getUrl(), is(equalTo(encodedLink.getUrl())));
        assertThat(resource.getMetadata().entrySet(), is(equalTo(encodedLink.getMetadata().entrySet())));
        assertThat(resource.getContentType(), is(equalTo("image/gif")));
        assertThat(resource.getCharset(), is(equalTo(Charset.forName("us-ascii"))));

        byte[] storedData = storeBuffer.toByteArray();
        assertThat(base64Data, is(equalTo(Base64.getEncoder().encodeToString(storedData))));
    }
}
