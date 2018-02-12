package com.rk.crawler.util;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;


/**
 * Specialized set based on hashset.
 * Ignores URL protocol during operations.
 * HTTP protocol is more preferable. In choice between http and https version -> http is preferable.
 */
public class URLSet extends HashSet<URL> {
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";

    @Override
    public boolean add(URL url) {
        return super.contains(url) || super.contains(toOppositeProtocol(url)) || super.add(url);
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o) || (o instanceof URL && super.remove(toOppositeProtocol((URL)o)));
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o) || (o instanceof URL && super.contains(toOppositeProtocol((URL)o)));

    }

    private URL toProtocol(URL from, String protocol) {
        try {
            return new URL(protocol, from.getHost(), from.getPort(), from.getFile());
        } catch (MalformedURLException e) {
            return from; //seems should never happen but ok, save same protocol.
        }
    }

    private URL toOppositeProtocol(URL from) {
        return HTTP_PROTOCOL.equals(from.getProtocol())
                ? toProtocol(from, HTTP_PROTOCOL)
                : toProtocol(from, HTTPS_PROTOCOL);
    }
}
