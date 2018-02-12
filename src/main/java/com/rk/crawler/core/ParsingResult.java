package com.rk.crawler.core;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents result of parsing
 * mapping word -> frequency
 * At first point it seems reasonable to keep words in structure like Trie, but according to words count
 * in popular languages (100-500k of words, not even millions) HashMap will work faster.
 */
public class ParsingResult {
    private Set<URL> links;
    private Map<String, Integer> wordsCount;
    public static final ParsingResult EMPTY_RESULT = new ParsingResult(Collections.emptySet(), Collections.emptyMap());

    public ParsingResult(Set<URL> links, Map<String, Integer> wordsCount) {
        this.links = links;
        this.wordsCount = wordsCount;
    }

    public Set<URL> getLinks() {
        return links;
    }

    public Map<String, Integer> getWordsCount() {
        return wordsCount;
    }
}
