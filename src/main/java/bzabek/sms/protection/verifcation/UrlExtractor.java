package bzabek.sms.protection.verifcation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts a list of urls found inside a text. Supported protocols: http, https, ftp and file
 */
@Component
class UrlExtractor {

    private static final String URL_REGEX =
            "\\b(https?|ftp|file):\\/\\/[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Extracts all URLs from the given text.
     *
     * @param text The input text
     * @return A list of URLs found in the text
     */
    List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }
}