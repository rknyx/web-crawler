package com.rk.crawler;


import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.rk.crawler.api.Frequency;
import com.rk.crawler.api.ParsingStrategy;
import com.rk.crawler.api.WebCrawler;
import com.rk.crawler.api.WebCrawlerConfig;
import com.rk.crawler.core.ParsingResult;
import com.rk.crawler.core.levelsyncbfsstrategy.LevelSyncBFSTraversal;
import com.rk.crawler.core.parsing.JsoupParser;
import com.rk.crawler.util.URLFilter;
import com.rk.crawler.util.WordsCounter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BasicTest {

//    @Test
//    public void wordsCounterTest() throws IOException {
//        URL url = Resources.getResource("words_count.txt");
//        String text = Resources.toString(url, Charsets.UTF_8);
//        WordsCounter wordsCounter = new WordsCounter(text);
//        Map<String, Integer> actual = wordsCounter.count();
//        Map<String, Integer> expected = ImmutableMap.of(
//                "слово", 4,
//                "word", 3,
//                "电脑", 4,
//                "45", 3);
//        Assert.assertEquals("Incorrect word counts", expected, actual);
//    }
//
//    @Test
//    public void levelSyncBFSTraversalTest() {
//        WebCrawlerConfig config = new WebCrawlerConfig("http://a.com", 100, 3);
//        Map<String, Set<String>> graph = ImmutableMap.<String, Set<String>>builder()
//                .put("http://a.com", Sets.newHashSet("http://b.com", "http://a.com", "http://c.com"))
//                .put("http://b.com", Sets.newHashSet("http://ba.com", "http://bb.com", "http://bc.com", "http://a.com"))
//                .put("http://c.com", Sets.newHashSet("http://ca.com", "http://cb.com", "http://cc.com", "http://a.com", "http://b.com"))
//                .put("http://ba.com", Sets.newHashSet("http://baa.com"))
//                .put("http://baa.com", Sets.newHashSet("http://baaa.com"))
//                .put("http://baaa.com", Sets.newHashSet("http://baaaa.com"))
//                .build();
//        ParsingStrategy parsingStrategy = Mockito.mock(ParsingStrategy.class);
//
//        graph.keySet().forEach(key -> {
//            Mockito.when(parsingStrategy.parse(key)).thenReturn(new ParsingResult(graph.get(key), ImmutableMap.of(key, 1)));
//        });
//
//        //two exceptions cases
//        Mockito.when(parsingStrategy.parse("http://b.com"))
//                .thenReturn(new ParsingResult(graph.get("http://b.com"),
//                        ImmutableMap.of("magic", 101, "http://b.com", 1)));
//        Mockito.when(parsingStrategy.parse("http://c.com"))
//                .thenReturn(new ParsingResult(graph.get("http://c.com"),
//                        ImmutableMap.of("magic", 99, "http://c.com", 1)));
//
//
//        LevelSyncBFSTraversal traversal = new LevelSyncBFSTraversal(config, parsingStrategy,
//                Executors.newSingleThreadExecutor());
//        List<Frequency> result = traversal.getTopPopularWords();
//        Map<String, Integer> expected = ImmutableMap.of(
//                "http://a.com", 1,
//                "http://b.com", 1,
//                "http://c.com", 1,
//                "magic", 200);
//
//        Map<String, Integer> actual = new HashMap<>();
//        result.forEach(frequency -> actual.put(frequency.getWord(), frequency.getCount()));
//        Assert.assertEquals("Incorrect traversal result", expected, actual);
//    }
//
//    @Test
//    public void testUrlFilter() {
//        URLFilter basicUrl = new URLFilter("https://example.com");
//        Set<String> goodUrls = Sets.newHashSet(
//                "https://example.com/a",
//                "https://EXAMPLE.com/b",
//                "http://example.com/c",
//                "https://example.com/c?param=value&param=nextvalue",
//                "https://example.com/index.php");
//        Set<String> badURls = Sets.newHashSet(
//                "", "url",
//                "https://anotherexample.com",
//                "https://example.com/a#anchor",
//                "https://example.com/c?param=value&param=nextvalue#anchor",
//                "https://example.com/photo.jpeg",
//                "https://example.com/script.js",
//                "https://example.com/style.css");
//        List<String> allUrls = new ArrayList<>();
//        allUrls.addAll(goodUrls);
//        allUrls.addAll(badURls);
//
//
//        Set<String> actualUrls = basicUrl.filterLinks(allUrls);
//        goodUrls = goodUrls.stream().map(String::toLowerCase).collect(Collectors.toSet());
//
//        Assert.assertEquals("Incorrect links filtration", goodUrls, actualUrls);
//    }

    //test strongly relies on particular wiki page version. Need to actualize on demand.
    @Test
    public void realResourceTest() throws MalformedURLException {
//        URL url = new URL("https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%B8%D1%81%D0%BA%D0%BE%D0%B2%D1%8B%D0%B9_%D1%80%D0%BE%D0%B1%D0%BE%D1%82");

        URL url = new URL("https://raw.githubusercontent.com/rknyx/money_transfer_api/master/src/main/java/com/rk/utils/JDBCConnectionPoolBuilder.java");
        WebCrawler crawler = new WebCrawler(url, 2);
        List<Frequency> frequencyList = crawler.scanTop100Words();
        int a = 5;
    }
}
