package com.example.demo.model;

import lombok.Data;

@Data
public class DocInfo {
    private int docId;
    private String title;
    private String url;
    private String content;
}
