package com.rk.crawler.api;

import java.net.URL;

public class WebCrawlerConfig {
    private URL seedUrl;
    private int wordsCount;
    private int maxDepth;

    public WebCrawlerConfig(URL seedUrl, int wordsCount, int maxDepth) {
        this.seedUrl = seedUrl;
        this.wordsCount = wordsCount;
        this.maxDepth = maxDepth;
    }

    public URL getSeedUrl() {
        return seedUrl;
    }

    public int getWordsCount() {
        return wordsCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
