package com.rk.crawler.api;


public class Frequency {
    private String word;
    private Integer count;

    public Frequency(String word, Integer count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public Integer getCount() {
        return count;
    }
}
