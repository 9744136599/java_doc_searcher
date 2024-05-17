package com.example.demo.controller;

import com.example.demo.common.AjaxResult;
import com.example.demo.common.DocSearcher;
import com.example.demo.common.ResponseAdvice;
import com.example.demo.model.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/searcher")
public class SearchController {
    @Autowired
    private DocSearcher docSearcher;
    private ResponseAdvice responseAdvice;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping("/search")
    public List<Result> search(@RequestParam("query") String query) throws JsonProcessingException {
        //1.参数为空或非法
        if (query == null || query.equals("")){
            AjaxResult.fail(404, "参数非法");
        }
        //2.打印记录query的值
        System.out.println("query = " + query);
        //3.调用搜索模块
        List<Result> results = docSearcher.search(query);
        return results;
    }
}
