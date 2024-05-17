package com.example.demo.model;

import lombok.Data;

@Data
public class Weight {
    private int docId;
    //weight表示 文档 和 词 之间的相关性，值越大相关性越强
    private int weight;
}
