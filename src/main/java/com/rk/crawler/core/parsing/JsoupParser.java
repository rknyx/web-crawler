package com.rk.crawler.core.parsing;

import com.rk.crawler.core.ParsingResult;
import com.rk.crawler.api.ParsingStrategy;
import com.rk.crawler.util.CrawlerTimer;
import com.rk.crawler.util.URLFilter;
import com.rk.crawler.util.URLSet;
import com.rk.crawler.util.WordsCounter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsoupParser implements ParsingStrategy {
    public static final Logger log = LoggerFactory.getLogger(JsoupParser.class);
    private static final int CONNECT_TIMEOUT_MILLIS = 30000;
    private static final int MAX_BODY_SIZE_BYTES = 100 * 1024 * 1024; //100MB
    private static final String TEXT_HTML_CONTENT_TYPE = "text/html";
    private int politeness = 0;

    public JsoupParser(int politeness) {
        this.politeness = politeness;
    }

    public JsoupParser() {
        this(0);
    }

    @Override
    public ParsingResult parse(URL url) {
        ParsingResult result = ParsingResult.EMPTY_RESULT;

        try {
            if (politeness != 0) {
                log.info("Politeness enabled, wait '{}'ms", politeness);
                Thread.sleep(politeness);
            }
            log.debug("Download: '{}'", url);
            CrawlerTimer.start();
            Connection.Response response = Jsoup.connect(url.toExternalForm()).timeout(CONNECT_TIMEOUT_MILLIS)
                    .followRedirects(true)
                    .maxBodySize(MAX_BODY_SIZE_BYTES)
                    .execute();
            String contentType = StringUtils.lowerCase(response.contentType());
            if (!contentType.contains(TEXT_HTML_CONTENT_TYPE)) {
                log.trace("Ignore url: '{}' due to non-text content type");
                return ParsingResult.EMPTY_RESULT;
            }

            Document document = response.parse();
            log.debug("Downloaded: '{}', '{}'", url, CrawlerTimer.getTimeString());

            log.debug("Parsing links: '{}'", url);
            CrawlerTimer.start();
            Elements linksElements = document.select("a[href]");
            List<String> linksResult = linksElements.stream()
                    .map(link -> link.absUrl("href"))
                    .collect(Collectors.toList());
            URLSet linksFiltered = new URLFilter(url).filterLinks(linksResult);
            if (linksFiltered.isEmpty()) {
                log.debug("No links found, '{}'", CrawlerTimer.getTimeString());
            } else {
                log.debug("Links parsed: '{}', '{}'", url, CrawlerTimer.getTimeString());
            }

            log.debug("Start words counting: '{}'", url);
            CrawlerTimer.start();
            Map<String, Integer> countsResult = new WordsCounter(document.body().text()).count();
            log.debug("Words counted: '{}', '{}'", url, CrawlerTimer.getTimeString());

            result = new ParsingResult(linksFiltered, countsResult);
        } catch (InterruptedException e) {
            log.warn("Asked to shutdown, skip url fetch.");
        } catch (HttpStatusException e) {
            log.error("Got status code: '{}' while fetching: '{}'", e.getStatusCode(), e.getUrl());
        } catch (IOException e) {
            log.error("Exception during parsing links from url: '{}': {}", url, e.getMessage());
        }
        return result;
    }
}
