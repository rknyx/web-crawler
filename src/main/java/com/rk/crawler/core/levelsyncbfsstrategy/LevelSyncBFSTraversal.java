package com.rk.crawler.core.levelsyncbfsstrategy;

import com.rk.crawler.api.Frequency;
import com.rk.crawler.core.ParsingResult;
import com.rk.crawler.api.ParsingStrategy;
import com.rk.crawler.core.AbstractTraversal;
import com.rk.crawler.api.WebCrawlerConfig;
import com.rk.crawler.util.CrawlerTimer;
import com.rk.crawler.util.URLSet;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LevelSyncBFSTraversal extends AbstractTraversal {
    private static final Logger log = LoggerFactory.getLogger(LevelSyncBFSTraversal.class);
    private static final long TERMINATION_TIMEOUT_SEC = 30;
    private List<URL> currentQueue = new ArrayList<>();
    private List<URL> nextQueue = new ArrayList<>();
    private Set<URL> visited = new URLSet();
    private Map<String, Integer> wordCounts = new HashMap<>();
    private List<ParsingResult> parsingResults;
    private int currentDepth = -1;

    private class ParsingTask implements Callable<ParsingResult> {
        private URL url;

        public ParsingTask(URL url) {
            this.url = url;
        }

        public ParsingResult call() {
            ParsingResult parsingResult = parsingStrategy.parse(url);
            parsingResult.getLinks().removeAll(visited);
            return parsingResult;
        }
    }

    public LevelSyncBFSTraversal(WebCrawlerConfig webCrawlerConfig, ParsingStrategy parsingStrategy,
                                 ExecutorService executorService) {
        super(webCrawlerConfig, parsingStrategy, executorService);
        currentQueue.add(webCrawlerConfig.getSeedUrl());
    }

    @Override
    public List<Frequency> getTopPopularWords() {
        traverse();
        CrawlerTimer.start();
        log.debug("Start words sorting");
        List<Map.Entry<String, Integer>> entries = wordCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        boolean enoughEntries = entries.size() >= webCrawlerConfig.getWordsCount();

        List<Frequency> result = entries.subList(enoughEntries ? entries.size() - webCrawlerConfig.getWordsCount() : 0, entries.size())
                .stream()
                .map(entry -> new Frequency(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        log.debug("Words sotring complete: {}", CrawlerTimer.getTimeString());
        return result;
    }

    private void traverse() {
        try {
            while (!currentQueue.isEmpty() && currentDepth < webCrawlerConfig.getMaxDepth()) {
                visited.addAll(currentQueue);
                CrawlerTimer.start();
                log.debug("Submit '{}' parsing tasks", currentQueue.size());
                List<Future<ParsingResult>> futures = currentQueue.stream()
                        .map(ParsingTask::new)
                        .map(executorService::submit)
                        .collect(Collectors.toList());
                log.debug("Tasks submitted: {}", CrawlerTimer.getTimeString());

                //merge maps while parsing is working
                aggregateResults();

                parsingResults = Optional.ofNullable(parsingResults).orElse(new ArrayList<>(futures.size()));
                CrawlerTimer.start();
                log.info("Wait for Depth '{}' parse", currentDepth);
                for (Future<ParsingResult> future : futures) {
                    parsingResults.add(future.get());
                }
                log.info("Depth '{}' parsed: '{}', {} pages visited.", currentDepth, CrawlerTimer.getTimeString(), visited.size());
                log.info("Total different words: '{}'", wordCounts.size());
                parsingResults.forEach(parsingResult -> nextQueue.addAll(parsingResult.getLinks()));
                swapQueues();
            }

            executorService.shutdown();
            aggregateResults();
            executorService.awaitTermination(TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
            log.debug("Execution complete");
        } catch (ExecutionException e) {
            log.error("Execution exception. {}, {}", e.getMessage(), e.getStackTrace());
        } catch (InterruptedException e) {
            log.debug("Asked to shutdown. Throw away results");
            executorService.shutdown();
        }
    }

    private void aggregateResults() {
        if (CollectionUtils.isEmpty(parsingResults)) {
            return;
        }
        CrawlerTimer.start();
        log.debug("Start merging word counts");
        parsingResults.forEach(parsingResult -> aggregateMaps(parsingResult.getWordsCount()));
        log.debug("Merge counts complete: {}", CrawlerTimer.getTimeString());
        parsingResults.clear();
    }

    private void aggregateMaps(Map<String, Integer> other) {
        other.forEach((key, value) -> {
            Integer currValue = Optional.ofNullable(wordCounts.get(key)).orElse(0);
            wordCounts.put(key, currValue + value);
        });
    }

    private void swapQueues() {
        List<URL> temp = currentQueue;
        currentQueue = nextQueue;
        nextQueue = temp;
        nextQueue.clear();
        currentDepth += 1;
    }
}
