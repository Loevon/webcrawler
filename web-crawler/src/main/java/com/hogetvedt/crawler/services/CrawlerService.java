package com.hogetvedt.crawler.services;

import com.hogetvedt.crawler.spider.Spider;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Data
@Service
public class CrawlerService {
    private Spider spider;

    @Value("${crawler.entryPoint}")
    private String entryEndpoint;

    public CrawlerService() {
        this.spider = new Spider();
        this.entryEndpoint = "";
    }

    // temp starter
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if(this.spider == null) {
            this.spider = new Spider();
        }

        try {
            this.spider.initiateCrawl(entryEndpoint);
        } catch (Exception e) {
            e.printStackTrace();    // entry point failed
        }
    }

    public void resetSpider() {
        this.spider = new Spider();
    }
}
