package ro.mv.krol.util;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mihai.vaduva on 07/08/2016.
 */
public class HtmlUtils {

    private static final Pattern CHARSET_META_REGEX = Pattern.compile("<meta [^<>]*charset=(?<charset>[a-zA-Z0-9-]+)[^<]*>", Pattern.CASE_INSENSITIVE);

    public static Charset getCharset(String html) {
        Matcher matcher = CHARSET_META_REGEX.matcher(html);
        if (matcher.find()) {
            String group = matcher.group("charset");
            if (group != null) {
                return Charset.forName(group);
            }
        }
        return null;
    }

}
