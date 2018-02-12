package com.rk.crawler.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class WordsCounter {
    private static final String NOT_UNICODE_LETTER_NOT_NUMBER_REGEXP = "[^\\p{L}\\d]";
    private static final String UNICODE_LETTER_OR_NUMBER_REGEXP = "[\\p{L}\\d]";
    private static final Logger log = LoggerFactory.getLogger(WordsCounter.class);
    private static final int MAX_EXCEPTIONS = 1000;
    private final String text;

    public WordsCounter(String text) {
        this.text = text;
    }

    public Map<String, Integer> count() {
        if (StringUtils.isEmpty(text)) {
            return Collections.emptyMap();
        }
        int exceptionCounter = 0;
        Map<String, Integer> countsResult = new HashMap<>();

        Scanner scanner = new Scanner(text).useDelimiter(NOT_UNICODE_LETTER_NOT_NUMBER_REGEXP);
        while (scanner.hasNext()) {
            try {
                String element = scanner.next().toLowerCase();
                if (!element.isEmpty()) {
                    Integer currCount = Optional.ofNullable(countsResult.get(element)).orElse(0);
                    countsResult.put(element, currCount + 1);
                }
            } catch (InputMismatchException e) {
                ++exceptionCounter;
                if (exceptionCounter > MAX_EXCEPTIONS) {
                    log.error(">{} InputMismatchException exceptions on Scanner.next(). Text is broken or encoding is strange. Skip entire text");
                    return Collections.emptyMap();
                }
                scanner.skip(UNICODE_LETTER_OR_NUMBER_REGEXP);
            }

        }
        return countsResult;

    }
}
