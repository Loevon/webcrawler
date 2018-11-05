package com.hogetvedt.crawler.models;

import lombok.Data;

import java.util.List;


/*
    EntryPoint - Basic class that represents the collection of links found here:
    https://raw.githubusercontent.com/OnAssignment/compass-interview/master/data.json
 */
@Data
public class EntryPoint {
    private List<String> links;
}
