package com.rk.crawler.api;

import com.rk.crawler.core.AbstractTraversal;
import com.rk.crawler.core.levelsyncbfsstrategy.LevelSyncBFSTraversal;
import com.rk.crawler.core.parsing.JsoupParser;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;

public class WebCrawler {
    private final int WORDS_COUNT = 100;
    private AbstractTraversal traversalStrategy;

    public WebCrawler(URL url, int scanDepth) {
        this(url, scanDepth, new JsoupParser(), Runtime.getRuntime().availableProcessors() * 2);
    }

    public WebCrawler(URL url, int scanDepth, ParsingStrategy parsingStrategy, int threadCount) {
        traversalStrategy = new LevelSyncBFSTraversal(
                new WebCrawlerConfig(url, WORDS_COUNT, scanDepth),
                parsingStrategy,
                Executors.newFixedThreadPool(threadCount)
        );
    }

    public List<Frequency> scanTop100Words() {
        return traversalStrategy.getTopPopularWords();
    }

}
