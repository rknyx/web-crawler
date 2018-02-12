package com.rk.crawler.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class URLFilter {
    private static final Logger log = LoggerFactory.getLogger(URLFilter.class);
    private static final Set<String> PROHIBITED_URLS = new HashSet<>(Arrays.asList(
            ".jpeg", ".jpg", ".png", ".tiff", ".tif", ".css", ".js", ".zip", ".gz", ".gif", ".avi", ".exe", ".mpg", ".mpeg", ".pdf", ".wav",
            ".doc", ".exe", ".sh", ".midi", ".mp3", ".rar", ".jar", ".rpm", ".text", ".txt", ".xls", ".xml"));
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final Set<String> ALLOWED_SCHEMES = new HashSet<>(Arrays.asList(
            "http", "https"));

    private URL baseUrl;

    public URLFilter(URL url) {
        baseUrl = url;
    }

    public URLSet filterLinks(List<String> rawLinks) {
        URLSet urlSet = new URLSet();
        rawLinks.stream()
                .map(this::stringToUrl)
                .filter(Objects::nonNull)
                .filter(this::filterUndesiredExtensions)
                .filter(this::filterExternalLinks)
                .filter(this::filterNonHttpHttpsSchemes)
                .map(this::cloneWithoutAnchor)
                .forEach(urlSet::add);
        return urlSet;
    }

    private boolean filterNonHttpHttpsSchemes(URL url) {
        return ALLOWED_SCHEMES.contains(StringUtils.lowerCase(url.getProtocol()));
    }

    private boolean filterExternalLinks(URL url) {
        return StringUtils.defaultIfEmpty(url.getHost(), "").equals(baseUrl.getHost());
    }

    private boolean filterUndesiredExtensions(URL url) {
        String path = url.getPath().toLowerCase();
        int lastDotPosition = path.lastIndexOf('.');
        return -1 == lastDotPosition || !PROHIBITED_URLS.contains(path.substring(lastDotPosition));
    }

    private URL stringToUrl(String string) {
        try {
            if (!StringUtils.isEmpty(string)) {
                return new URL(string.toLowerCase());
            }
        } catch (MalformedURLException e) {
            log.error("Unparsable url: {} is discarded", string);
        }
        return null;
    }

    private URL cloneWithoutAnchor(URL url) {
        try {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
