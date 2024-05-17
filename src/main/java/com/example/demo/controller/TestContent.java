package com.example.demo.controller;

import com.example.demo.common.Parser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class TestContent {
    public static void main(String[] args) {
        Parser parser = new Parser();
        File file = new File("D:\\JAVA\\java_api\\docs\\api\\java\\util\\ArrayList.html");
        String ret = parser.parseContent(file);
        System.out.println(ret);
    }
}
