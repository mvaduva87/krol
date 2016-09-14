package ro.mv.krol.storage.path;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import ro.mv.krol.storage.StorageKey;
import ro.mv.krol.storage.StoredType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by mihai.vaduva on 13/08/2016.
 */
public class PathTemplateTest {

    @Test
    public void shouldUseTemplateELtoEvaluatePathForAStorageKey() throws Exception {
        // given
        Map<StoredType, String> templateMap = new HashMap<>();
        templateMap.put(StoredType.SOURCE, "pages/{date:format(timestamp,'yyyy-MM-dd')}/{metadata.job}/{hash:md5(url)}.html");
        PathTemplate pathTemplate = new PathTemplate(templateMap);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("job", "testJob");
        StorageKey key = mock(StorageKey.class);
        when(key.getType()).thenReturn(StoredType.SOURCE);
        when(key.getTimestamp()).thenReturn(new Date());
        when(key.getUrl()).thenReturn("test.html");
        when(key.getMetadata()).thenReturn(metadata);

        // when
        String path = pathTemplate.getPathFor(key);

        // then
        String tsStr = new SimpleDateFormat("yyyy-MM-dd").format(key.getTimestamp());
        String fileStr = DigestUtils.md5Hex(key.getUrl()) + ".html";
        String expectedPath = "pages/" + tsStr + "/testJob/" + fileStr;
        assertThat(path, is(equalTo(expectedPath)));
    }
}
