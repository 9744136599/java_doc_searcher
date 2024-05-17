package com.example.demo.model;

import lombok.Data;

//表示搜索结果
public class Result {
    private String title;
    private String url;
    //desc是正文的一段摘要
    private String desc;

    @Override
    public String toString() {
        return "Result{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
