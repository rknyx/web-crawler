package com.rk.crawler.api;


import com.rk.crawler.core.ParsingResult;

import java.net.URL;

public interface ParsingStrategy {
    ParsingResult parse(URL url);
}
