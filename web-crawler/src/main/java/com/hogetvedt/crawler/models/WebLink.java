package com.hogetvedt.crawler.models;

import lombok.Data;

/*
    WebLink - Data model that represents a URL and its status code
 */
@Data
public class WebLink {
    private String url;
    private Integer responseCode;

    public WebLink(String url) {
        this.url = url;
    }

    // successful requests
    public boolean isSuccessfulResponse() {
        if(responseCode != null) {
            return responseCode < 400;
        }
        return false;
    }
}
