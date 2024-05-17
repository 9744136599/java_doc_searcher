package com.example.demo.controller;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class TestAnsj {
    public static void main(String[] args) {
        String str = "我是计算机专业的学生，喜欢打篮球";
        List<Term> terms = ToAnalysis.parse(str).getTerms();
        for (Term term : terms){
            System.out.println(term.getName());
        }
    }

}
