package com.hogetvedt.crawler.spider;

import com.google.gson.Gson;
import com.hogetvedt.crawler.models.EntryPoint;
import com.hogetvedt.crawler.models.WebLink;
import com.hogetvedt.crawler.util.JsonReader;
import lombok.Data;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Logger;

@Data
public class Spider {
    private volatile Set<String> vistedLinks = Collections.synchronizedSet(new HashSet<>());
    private volatile Queue<WebLink> links  = new LinkedList<>();

    private Integer httpRequests;
    private Integer successfulRequests;
    private Integer failedRequests;

    private static final Logger LOGGER = Logger.getLogger(Spider.class.getSimpleName());

    public Spider() {
        this.httpRequests = 0;
        this.successfulRequests = 0;
        this.failedRequests = 0;
    }

    public void initiateCrawl(String entryEndpoint) throws Exception {
        // check initial URL for validity
        if(null == entryEndpoint || entryEndpoint.equals("")) {
            throw new Exception("Invalid Entry Endpoint");
        }

        // parse initial endpoints
        parseEntryEndpoint(entryEndpoint);

        // crawl all items while queue is not empty
        while(!links.isEmpty()) {

            WebLink link = links.poll();
            //String link = links.poll(); // take first url from queue

            if(link != null) {
                if(!vistedLinks.contains(link.getUrl())) {
                    crawl(link);
                    vistedLinks.add(link.getUrl());
                }
            }
        }

        printResults(); // displays the final results to the chat log
    }

    @Async
    public void crawl(WebLink link) throws SpiderException {
        if(link != null && (link.getUrl() == null || link.getUrl().equals(""))) {
            throw new SpiderException("Invalid URL");
        }

        httpRequests++;

        try {
            // make a connection then fetch the status code
            link.setResponseCode(Jsoup.connect(link.getUrl()).execute().statusCode());

            if(link.isSuccessfulResponse()) {
                LOGGER.info("Crawled [STATUS: " + link.getResponseCode() + "] ~ " + link.getUrl());
                successfulRequests++;

                Document document = Jsoup.connect(link.getUrl()).get();
                Elements parsedLinks = document.select("a[href]");

                parsedLinks.forEach(element -> {
                    enqueueIfUnique(element.absUrl("href"));
                });
            } else {
                LOGGER.warning("Crawled [STATUS: " + link.getResponseCode() + "] ~ " + link.getUrl());
                failedRequests++;
            }
        } catch(IOException e) {
            LOGGER.warning("Failed to crawl: " + link.getUrl());
            failedRequests++;
        }
    }

    // enqueues a new web for the spider to crawl on
    private void enqueueIfUnique(String url) {
        if(!vistedLinks.contains(url)) {   // if the url has not yet been seen
            WebLink link = new WebLink(url);
            links.add(link);
        }
    }

    // parses the entry point links
    private void parseEntryEndpoint(String entryEndpoint) throws SpiderException {
        JSONObject json = null;

        try {
            json = JsonReader.readJsonFromUrl(entryEndpoint);
        } catch (MalformedURLException e) {
            throw new SpiderException("Invalid entry point URL", e);
        } catch (IOException e) {   // update me
            throw new SpiderException("Error reading JSON from URL", e);
        } catch (JSONException e) {
            throw new SpiderException("Invalid or Malformed JSON", e);
        }


        if (json != null) {
            Gson gson = new Gson();
            EntryPoint endpoint = gson.fromJson(json.toString(), EntryPoint.class);
            List<String> linksList = endpoint.getLinks();

            linksList.forEach(link -> {
                enqueueIfUnique(link);
            });

            vistedLinks.clear();
        }
    }

    private void printResults() {
        LOGGER.info("Results: ");
        LOGGER.info("HTTP Requests: " + httpRequests);
        LOGGER.info("Successful Requests : " + successfulRequests);
        LOGGER.info("Failed Requests  : " + failedRequests);
    }
}
