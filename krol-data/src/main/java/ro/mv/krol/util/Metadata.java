package ro.mv.krol.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mihai.vaduva on 16/04/2016.
 */
public class Metadata {

    public static final String FIELD_SCRIPT = "x-script";

    public static String getScriptFrom(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        return metadata.get(FIELD_SCRIPT);
    }

    public static void transfer(Map<String, String> from, Map<String, String> to, boolean overwriteExisting) {
        if (from == null || to == null) {
            return;
        }
        for (Map.Entry<String, String> entry : from.entrySet()) {
            String key = entry.getKey();
            if (!overwriteExisting && to.containsKey(key)) {
                continue;
            }
            to.put(key, entry.getValue());
        }
    }

    public static Map<String, String> createWith(String... pairs) {
        if (pairs == null || pairs.length < 2) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i] != null) {
                map.put(pairs[i], pairs[i + 1]);
            }
        }
        return map;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> map = new HashMap<>();

        public Builder with(String key, String value) {
            map.put(key, value);
            return this;
        }

        public Map<String, String> build() {
            return map;
        }
    }

}
