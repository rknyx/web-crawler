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
import com.rk.crawler.util.URLFilter;
import com.rk.crawler.util.WordsCounter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicTest {

    @Test
    public void wordsCounterTest() throws IOException {
        URL url = Resources.getResource("words_count.txt");
        String text = Resources.toString(url, Charsets.UTF_8);
        WordsCounter wordsCounter = new WordsCounter(text);
        Map<String, Integer> actual = wordsCounter.count();
        Map<String, Integer> expected = ImmutableMap.of(
                "слово", 4,
                "word", 3,
                "电脑", 4,
                "45", 3);
        Assert.assertEquals("Incorrect word counts", expected, actual);
    }

    @Test
    public void levelSyncBFSTraversalTest() throws MalformedURLException {
        WebCrawlerConfig config = new WebCrawlerConfig(url("http://a.com"), 100, 2);
        final URL A = url("http://a.com");
        final URL B = url("http://b.com");
        final URL C = url("http://c.com");
        final URL BA = url("http://ba.com");
        Map<URL, Set<URL>> graph = ImmutableMap.<URL, Set<URL>>builder()
                .put(A, Sets.newHashSet(B, A, C))
                .put(B, Stream.of("http://ba.com", "http://bb.com", "http://bc.com", "http://a.com").map(this::url).collect(Collectors.toSet()))
                .put(C, Stream.of("http://ca.com", "http://cb.com", "http://cc.com", "http://a.com", "http://b.com").map(this::url).collect(Collectors.toSet()))
                .put(BA, Stream.of("http://baa.com").map(this::url).collect(Collectors.toSet()))
                .put(url("http://baa.com"), Stream.of("http://baaa.com").map(this::url).collect(Collectors.toSet()))
                .put(url("http://baaa.com"), Stream.of("http://baaaa.com").map(this::url).collect(Collectors.toSet()))
                .build();
        ParsingStrategy parsingStrategy = Mockito.mock(ParsingStrategy.class);

        graph.keySet().forEach(key ->
            Mockito.when(parsingStrategy.parse(key)).thenReturn(new ParsingResult(graph.get(key), ImmutableMap.of(key.toExternalForm(), 1)))
        );

        //two exceptions cases
        Mockito.when(parsingStrategy.parse(B))
                .thenReturn(new ParsingResult(graph.get(B),
                        ImmutableMap.of("magic", 101, B.toExternalForm(), 1)));
        Mockito.when(parsingStrategy.parse(C))
                .thenReturn(new ParsingResult(graph.get(C),
                        ImmutableMap.of("magic", 99, C.toExternalForm(), 1)));


        LevelSyncBFSTraversal traversal = new LevelSyncBFSTraversal(config, parsingStrategy,
                Executors.newSingleThreadExecutor());
        List<Frequency> result = traversal.getTopPopularWords();
        Map<String, Integer> expected = ImmutableMap.of(
                A.toExternalForm(), 1,
                B.toExternalForm(), 1,
                C.toExternalForm(), 1,
                BA.toExternalForm(), 1,
                "magic", 200);

        Map<String, Integer> actual = new HashMap<>();
        result.forEach(frequency -> actual.put(frequency.getWord(), frequency.getCount()));
        Assert.assertEquals("Incorrect traversal result", expected, actual);
    }

    @Test
    public void testUrlFilter() throws MalformedURLException {
        URLFilter basicUrl = new URLFilter(new URL("https://example.com"));
        Set<String> goodUrls = Sets.newHashSet(
                "https://example.com/a",
                "https://EXAMPLE.com/b",
                "http://example.com/c",
                "https://example.com/c?param=value&param=nextvalue",
                "https://example.com/index.php");
        Set<String> badURls = Sets.newHashSet(
                "", "url",
                "https://anotherexample.com",
                "https://example.com/a#anchor",
                "https://example.com/c?param=value&param=nextvalue#anchor",
                "https://example.com/photo.jpeg",
                "https://example.com/script.js",
                "https://example.com/style.css");
        List<String> allUrls = new ArrayList<>();
        allUrls.addAll(goodUrls);
        allUrls.addAll(badURls);


        Set<String> actualUrls = basicUrl.filterLinks(allUrls).stream().map(URL::toExternalForm).collect(Collectors.toSet());
        goodUrls = goodUrls.stream().map(String::toLowerCase).collect(Collectors.toSet());

        Assert.assertEquals("Incorrect links filtration", goodUrls, actualUrls);
    }

    //test strongly relies on particular wiki page version. Need to actualize on demand.
    @Test
    public void realResourceTest() throws MalformedURLException {
        URL url = new URL("https://en.wikipedia.org/wiki/Main_Page");
        WebCrawler crawler = new WebCrawler(url, 1);
        List<Frequency> frequencyList = crawler.scanTop100Words();
        Map<String, Integer> mapToCheck = new HashMap<>();
        frequencyList.forEach(frequency -> mapToCheck.put(frequency.getWord(), frequency.getCount()));

        List<String> mandatoryWords = Arrays.asList("the", "of", "and", "to");
        for (String mandatoryWord : mandatoryWords) {
            Assert.assertTrue(mapToCheck.containsKey(mandatoryWord));

            Assert.assertTrue(frequencyList.size() == 100);
        }
    }

    private URL url(String stringUrl) {
        try {
            return new URL(stringUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
