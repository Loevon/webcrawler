package com.hogetvedt.crawler.spider;

public class SpiderException extends Exception {
    public SpiderException(String message, Throwable throwable) { super(message, throwable); }
    public SpiderException(String message) { super(message); }
}
