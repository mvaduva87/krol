package ro.mv.krol.util;

import org.junit.Test;
import ro.mv.krol.storage.StorageKey;
import ro.mv.krol.storage.StoredType;

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
        templateMap.put(StoredType.SOURCE, "pages/{date:format(timestamp, 'yyyy-MM-dd')}/{metadata.job}/{name}");
        PathTemplate pathTemplate = new PathTemplate(templateMap);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("job", "testJob");
        StorageKey key = mock(StorageKey.class);
        when(key.getType()).thenReturn(StoredType.SOURCE);
        when(key.getTimestamp()).thenReturn(new Date());
        when(key.getName()).thenReturn("test.html");
        when(key.getMetadata()).thenReturn(metadata);

        // when
        String path = pathTemplate.getPathFor(key);

        // then
        String tsStr = new DateHelper().format(key.getTimestamp(), "yyyy-MM-dd");
        assertThat(path, is(equalTo("pages/" + tsStr + "/testJob/test.html")));
    }
}
