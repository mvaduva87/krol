package ro.mv.krol.util;

import ro.mv.krol.model.DecodedURL;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mihai.vaduva on 07/08/2016.
 */
public class URLUtils {

    private static final String DATA_URL_REGEX = "^data:(?<mediaType>[^;]+)?;?(?<charset>charset=[a-zA-Z0-9-]+)?;?(?<base64>base64)?,(?<data>.*)";
    private static final Pattern DATA_URL_PATTERN = Pattern.compile(DATA_URL_REGEX);

    public static URL getBaseURLOf(URL url) throws MalformedURLException {
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
    }

    public static URL abs(URL baseUrl, String url) throws MalformedURLException, URISyntaxException {
        URI relativeUri = URI.create(url);
        if (relativeUri.isAbsolute()) {
            return relativeUri.toURL();
        }
        return new URL(baseUrl, url);
    }

    public static boolean isDataEncoded(String urlString) {
        return DATA_URL_PATTERN.matcher(urlString).matches();
    }

    public static DecodedURL decode(String urlEncodedData) {
        Matcher matcher = DATA_URL_PATTERN.matcher(urlEncodedData);
        if (matcher.find()) {
            DecodedURL.Builder builder = DecodedURL.builder();
            builder.withMediaType(matcher.group("mediaType"));
            builder.withCharsetName(matcher.group("charset"));
            builder.withBase64Encoded(null != matcher.group("base64"));
            builder.withData(matcher.group("data"));
            return builder.build();
        } else {
            return null;
        }
    }

}
