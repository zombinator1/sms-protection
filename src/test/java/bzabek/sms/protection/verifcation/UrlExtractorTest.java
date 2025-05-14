package bzabek.sms.protection.verifcation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlExtractorTest {

    UrlExtractor urlExtractor = new UrlExtractor();

    @Test
    public void testExtractMultipleUrls() {
        String input = "Check https://openai.com and ftp://files.example.com/file.txt.";
        List<String> urls = urlExtractor.extractUrls(input);
        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://openai.com"));
        assertTrue(urls.contains("ftp://files.example.com/file.txt"));
    }

    @Test
    public void testNoUrls() {
        String input = "There are no links here!";
        List<String> urls = urlExtractor.extractUrls(input);
        assertTrue(urls.isEmpty());
    }

    @Test
    public void testUrlsWithQueryAndFragment() {
        String input = "Go to https://example.com/page?query=test#anchor.";
        List<String> urls = urlExtractor.extractUrls(input);
        assertEquals(1, urls.size());
        assertEquals("https://example.com/page?query=test#anchor", urls.get(0));
    }

    @Test
    public void testUrlsWithTrailingPunctuation() {
        String input = "Here’s a link: https://example.com/page?test=1.";
        List<String> urls = urlExtractor.extractUrls(input);
        assertEquals(1, urls.size());
        assertEquals("https://example.com/page?test=1", urls.get(0));
    }

    /* verify each supported url protocol */
    @Nested
    class singleUrlTests {
        @Test
        public void http() {
            String input = "Visit http://example.com for more info.";
            List<String> urls = urlExtractor.extractUrls(input);
            assertEquals(1, urls.size());
            assertEquals("http://example.com", urls.get(0));
        }

        @Test
        public void https() {
            String input = "Visit https://example.com for more info.";
            List<String> urls = urlExtractor.extractUrls(input);
            assertEquals(1, urls.size());
            assertEquals("https://example.com", urls.get(0));
        }

        @Test
        public void ftp() {
            String input = "Visit ftp://example.com for more info.";
            List<String> urls = urlExtractor.extractUrls(input);
            assertEquals(1, urls.size());
            assertEquals("ftp://example.com", urls.get(0));
        }

        @Test
        public void file() {
            String input = "Visit file:///C:/Temp/Fake_Invoice.pdf for more info.";
            List<String> urls = urlExtractor.extractUrls(input);
            assertEquals(1, urls.size());
            assertEquals("file:///C:/Temp/Fake_Invoice.pdf", urls.get(0));
        }
    }
}
