/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.spider.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import net.htmlparser.jericho.Source;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.spider.SpiderParam;

/** Unit test for {@link SpiderSitemapXmlParser}. */
class SpiderSitemapXmlParserUnitTest extends SpiderParserTestUtils {

    private static final String ROOT_PATH = "/";
    private static final int BASE_DEPTH = 0;

    private static final Path BASE_DIR_TEST_FILES =
            getResourcePath(SpiderSitemapXmlParserUnitTest.class, "sitemapxml");

    @Test
    void shouldFailToCreateParserWithUndefinedSpiderOptions() {
        // Given
        SpiderParam undefinedSpiderOptions = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new SpiderSitemapXmlParser(undefinedSpiderOptions));
    }

    @Test
    void shouldNotFailToEvaluateAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        // When
        boolean canParse = spiderParser.canParseResource(undefinedMessage, ROOT_PATH, false);
        // Then
        assertThat(canParse, is(equalTo(false)));
    }

    @Test
    void shouldFailToEvaluateAnUndefinedPath() {
        // Given
        String undefinedPath = null;
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> spiderParser.canParseResource(new HttpMessage(), undefinedPath, false));
    }

    @Test
    void shouldParsePathThatEndsWithSitemapXml() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        boolean parsed = false;
        // When
        boolean canParse = spiderParser.canParseResource(new HttpMessage(), "/sitemap.xml", parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    void shouldParseMessageEvenIfAlreadyParsed() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        boolean parsed = true;
        // When
        boolean canParse = spiderParser.canParseResource(new HttpMessage(), "/sitemap.xml", parsed);
        // Then
        assertThat(canParse, is(equalTo(true)));
    }

    @Test
    void shouldNotParseAnUndefinedMessage() {
        // Given
        HttpMessage undefinedMessage = null;
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        // When
        boolean completelyParsed =
                spiderParser.parseResource(undefinedMessage, new Source(""), BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotRequireSourceToParseMessage() {
        // Given
        Source undefinedSource = null;
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        HttpMessage message = createMessageWith("NoUrlsSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, undefinedSource, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(true)));
    }

    @Test
    void shouldNotParseMessageIfParseOfSitemapXmlIsDisabled() {
        // Given
        SpiderParam params = createSpiderParamWithConfig();
        params.setParseSitemapXml(false);
        SpiderSitemapXmlParser spiderParser = new SpiderSitemapXmlParser(params);
        HttpMessage message = createMessageWith("NoUrlsSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotParseNonXmlMessage() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        HttpMessage message = createMessageWith("text/html", "NoUrlsSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotParseXmlMessageIfClientError() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        HttpMessage message = createMessageWith("404 Not Found", "text/xml", "NoUrlsSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotParseXmlMessageIfServerError() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        HttpMessage message =
                createMessageWith("500 Internal Server Error", "text/xml", "NoUrlsSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotParseEmptyXmlMessage() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        HttpMessage message = createMessageWith("EmptyFile.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotParseMalformedXmlMessage() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        HttpMessage message = createMessageWith("MalformedSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotParseXmlMessageWithDoctype() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        HttpMessage message = createMessageWith("DoctypeSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(false)));
    }

    @Test
    void shouldNotFindUrlsIfNoneDefinedInSitemap() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        spiderParser.addSpiderParserListener(listener);
        HttpMessage message = createMessageWith("NoUrlsSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(true)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
    }

    @Test
    void shouldNotFindUrlsIfUrlHasNoLocationIsEmptyInSitemap() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        spiderParser.addSpiderParserListener(listener);
        HttpMessage message = createMessageWith("UrlNoLocationSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(true)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
    }

    @Test
    void shouldNotFindUrlsIfUrlLocationIsEmptyInSitemap() {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        spiderParser.addSpiderParserListener(listener);
        HttpMessage message = createMessageWith("UrlEmptyLocationSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(true)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(0)));
    }

    @Test
    void shouldFindUrlsInValidSitemapXml() throws Exception {
        // Given
        SpiderSitemapXmlParser spiderParser = createSpiderSitemapXMLParser();
        TestSpiderParserListener listener = createTestSpiderParserListener();
        spiderParser.addSpiderParserListener(listener);
        HttpMessage message = createMessageWith("MultipleUrlsSitemap.xml");
        // When
        boolean completelyParsed = spiderParser.parseResource(message, null, BASE_DEPTH);
        // Then
        assertThat(completelyParsed, is(equalTo(true)));
        assertThat(listener.getNumberOfUrlsFound(), is(equalTo(5)));
        assertThat(
                listener.getUrlsFound(),
                contains(
                        "https://example.org/",
                        "http://subdomain.example.com/",
                        "http://example.com/relative",
                        "ftp://example.com/",
                        "http://www.example.com/%C7"));
    }

    private static SpiderSitemapXmlParser createSpiderSitemapXMLParser() {
        SpiderParam params = createSpiderParamWithConfig();
        params.setParseSitemapXml(true);
        return new SpiderSitemapXmlParser(params);
    }

    private static HttpMessage createMessageWith(String filename) {
        return createMessageWith("text/xml", filename);
    }

    private static HttpMessage createMessageWith(String contentType, String filename) {
        return createMessageWith("200 OK", contentType, filename);
    }

    private static HttpMessage createMessageWith(
            String statusCodeMessage, String contentType, String filename) {
        HttpMessage message = new HttpMessage();
        try {
            String fileContents = readFile(BASE_DIR_TEST_FILES.resolve(filename));
            message.setRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n");
            message.setResponseHeader(
                    "HTTP/1.1 "
                            + statusCodeMessage
                            + "\r\n"
                            + "Content-Type: "
                            + contentType
                            + "; charset=UTF-8\r\n"
                            + "Content-Length: "
                            + fileContents.length());
            message.setResponseBody(fileContents);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }
}
