package com.rk.crawler.core;

import com.rk.crawler.api.Frequency;
import com.rk.crawler.api.ParsingStrategy;
import com.rk.crawler.api.WebCrawlerConfig;

import java.util.List;
import java.util.concurrent.ExecutorService;

abstract public class AbstractTraversal {
    protected WebCrawlerConfig webCrawlerConfig;
    protected ParsingStrategy parsingStrategy;
    protected ExecutorService executorService;

    public AbstractTraversal(WebCrawlerConfig webCrawlerConfig, ParsingStrategy parsingStrategy,
                             ExecutorService executorService) {
        this.webCrawlerConfig = webCrawlerConfig;
        this.parsingStrategy = parsingStrategy;
        this.executorService = executorService;
    }

    public abstract List<Frequency> getTopPopularWords();

}
