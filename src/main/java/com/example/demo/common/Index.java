package com.example.demo.common;

import com.example.demo.model.DocInfo;
import com.example.demo.model.Weight;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//构造出的索引结构
//要实现5个方法：
public class Index {

    private static final String INDEX_PATH = "/home/lyl/install/";
    private ObjectMapper objectMapper = new ObjectMapper();

    //正排索引
    private ArrayList<DocInfo> forwardIndex = new ArrayList<>();
    //使用哈希表表示
    //倒排索引,key是词,value是一组文档
    private HashMap<String, ArrayList<Weight>> invertedIndex = new HashMap<>();

    //1.给定一个docId，在正排索引中，查询文档详细信息
    public DocInfo getDocInfo(int docId){
        return forwardIndex.get(docId);
    }
    //2.给定一个词，在倒排索引中，查哪些文档和这个词关联
    public List<Weight> getInverted(String term){
        return invertedIndex.get(term);
    }
    //3.往索引中新加一个文档
    public void addDoc(String title, String url, String content){
        DocInfo docInfo = buildForward(title, url, content);
        buildInverted(docInfo);
    }

    private void buildInverted(DocInfo docInfo) {
        class WordCnt{
            public int titleCount;
            public int contentCount;
        }
        HashMap<String, WordCnt> wordCntHashMap = new HashMap<>();
        //1)对标题分词，遍历标题
        List<Term> terms = ToAnalysis.parse(docInfo.getTitle()).getTerms();
        //2)统计标题中出现的次数
        for (Term term : terms){
            // 判断是否存在，不存在则创建新键值对加入HashMap;
            // 存在找到之前的值，把对应cnt+1
            String word = term.getName();
            WordCnt wordCnt = wordCntHashMap.get(word);
            if (wordCnt == null){
                WordCnt newWordCnt = new WordCnt();
                newWordCnt.titleCount = 1;
                newWordCnt.contentCount = 0;
                wordCntHashMap.put(word, newWordCnt);
            }else {
                wordCnt.titleCount += 1;
            }
        }
        //3)对正文分词，遍历正文
        terms = ToAnalysis.parse(docInfo.getContent()).getTerms();
        //4)统计正文中出现的次数
        for (Term term : terms){
            // 判断是否存在，不存在则创建新键值对加入HashMap;
            // 存在找到之前的值，把对应cnt+1
            String word = term.getName();
            WordCnt wordCnt = wordCntHashMap.get(word);
            if (wordCnt == null){
                WordCnt newWordCnt = new WordCnt();
                newWordCnt.titleCount = 0;
                newWordCnt.contentCount = 1;
                wordCntHashMap.put(word, newWordCnt);
            }else {
                wordCnt.contentCount += 1;
            }
        }
        //5)汇总到HashMap中，并计算权重(weight)：标题次数*10+正文次数
        //6)遍历HashMap，更新倒排索引结构
        for (Map.Entry<String, WordCnt> entry : wordCntHashMap.entrySet()){
            //先根据词去倒排索引中去查
            synchronized (invertedIndex){
                List<Weight> invertedList = invertedIndex.get(entry.getKey());
                if (invertedList == null){
                    //如果为空，插入新的键值对
                    ArrayList<Weight> newInvertedList = new ArrayList<>();
                    Weight weight = new Weight();
                    //把新的文档，构造成Weight对象，插入进来
                    weight.setDocId(docInfo.getDocId());
                    //计算权重
                    weight.setWeight(entry.getValue().titleCount*10 + entry.getValue().contentCount);
                    newInvertedList.add(weight);
                    invertedIndex.put(entry.getKey(), newInvertedList);
                }else {
                    Weight weight = new Weight();
                    //如果非空，构造一个weight对象，插入倒排索引后
                    weight.setDocId(docInfo.getDocId());
                    weight.setWeight(entry.getValue().titleCount*10 + entry.getValue().contentCount);
                    invertedList.add(weight);
                }
            }
        }
    }

    private DocInfo buildForward(String title, String url, String content) {
        DocInfo docInfo = new DocInfo();
        docInfo.setTitle(title);
        docInfo.setUrl(url);
        docInfo.setContent(content);
        synchronized (forwardIndex ){
            docInfo.setDocId(forwardIndex.size());
            forwardIndex.add(docInfo);
        }
        return docInfo;
    }

    //4.把内存中的索引结构保存到磁盘
    public void save(){
        //用两个文件保存正排和倒排
        System.out.println("保存索引开始");
        //判断索引文件是否存在，不存在就创建
        File indexPathFile = new File(INDEX_PATH);
        if (!indexPathFile.exists()){
            indexPathFile.mkdir();
        }
        File forwardIndexFile = new File(INDEX_PATH + "forward.txt");
        File invertIndexFile = new File(INDEX_PATH + "invert.txt");
        try {
            objectMapper.writeValue(forwardIndexFile, forwardIndex);
            objectMapper.writeValue(invertIndexFile, invertedIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("保存索引完成");
    }

    //5.把磁盘中的索引数据加载到内存
    public void load(){
        System.out.println("开始加载");
        //先确定加载路径
        File forwardIndexFile = new File(INDEX_PATH + "forward.txt");
        File invertIndexFile = new File(INDEX_PATH + "invert.txt");
        //从磁盘读取到内存
        try {
            forwardIndex = objectMapper.readValue(forwardIndexFile, new TypeReference<ArrayList<DocInfo>>() {});
            invertedIndex = objectMapper.readValue(invertIndexFile, new TypeReference<HashMap<String, ArrayList<Weight>>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("加载完成");
    }

    public static void main(String[] args) {
        Index index = new Index();
        index.load();
        System.out.println("索引加载完成");
    }
}
