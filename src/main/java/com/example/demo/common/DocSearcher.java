package com.example.demo.common;

import com.example.demo.model.DocInfo;
import com.example.demo.model.Result;
import com.example.demo.model.Weight;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

//完成搜索过程
@Component
public class DocSearcher {
    private static final String STOP_WORD_PATH = "/home/lyl/install/stop_word.txt";
    //使用这个HashSet来保存停用词
    private HashSet<String> stopWords = new HashSet<>();

    private Index index = new Index();

    public DocSearcher(){
        index.load();
        loadStopWords();
    }
    //完成搜索过程的方法
    //query是用户查询词
    //返回值是搜索结果集合
    public List<Result> search(String query){
        //1.分词: 针对query进行分词
        List<Term> oldTerms = ToAnalysis.parse(query).getTerms();
        List<Term> terms = new ArrayList<>();
        //对分词结果进行过滤
        for (Term term : oldTerms){
            if (stopWords.contains(term.getName())){
                continue;
            }
            terms.add(term);
        }
        //2.触发: 针对分词结果查倒排
        List<Weight> allTermResult = new ArrayList<>();
        for(Term term : terms){
            String word = term.getName();
            List<Weight> invertedList = index.getInverted(word);
            if (invertedList == null){
                continue;
            }
            allTermResult.addAll(invertedList);
        }
        //3.排序: 针对触发结果按照权重降序排序
        allTermResult.sort(new Comparator<Weight>() {
            @Override
            public int compare(Weight o1, Weight o2) {
                return o2.getWeight() - o1.getWeight();
            }
        });
        //4.包装结果: 针对排序结果, 去查正排, 构造返回数据
        List<Result> results = new ArrayList<>();
        for (Weight weight : allTermResult){
            DocInfo docInfo = index.getDocInfo(weight.getDocId());
            Result result = new Result();
            result.setTitle(docInfo.getTitle());
            result.setUrl(docInfo.getUrl());
            result.setDesc(GenDesc(docInfo.getContent(), terms));
            results.add(result);
        }
        return results;
    }

    private String GenDesc(String content, List<Term> terms) {
        int firstPos = -1;
        for (Term term : terms){
            //分词库直接转小写了
            //所以需要把正文转小写，再查询
            String word = term.getName();
            //需要全字匹配，而不是一部分
            content = content.toLowerCase().replaceAll("\\b" + word + "\\b", " " + word + " ");
            firstPos = content.toLowerCase().indexOf(" "+ word + "");
            if (firstPos >= 0){
                //找到位置
                break;
            }
        }
        if (firstPos == -1){
            //只在title中有, 正文中不存在
            if (content.length() > 160){
                return content.substring(0, 160) + "...";
            }
            return content;
        }
        //从firstPos开始往前60个字符, 再从60个字符前往后截取160
        String desc = "";
        int descBeg = firstPos < 60 ? 0 : firstPos - 60;
        if (descBeg + 160 > content.length()){
            desc = content.substring(descBeg);
        }else{
            desc = content.substring(descBeg, descBeg + 160) + " ";
        }

        //替换操作, 把描述中和分词结果相同的部分, 加上一层<i>标签
        for(Term term : terms){
            String word = term.getName();
            desc = desc.replaceAll("(?i)" + word + " ", "<i>" + word + "</i>");
        }
        return desc;
    }

    public void loadStopWords(){
        try {
            BufferedReader bufferedReader =new BufferedReader(new FileReader(STOP_WORD_PATH));
            while (true){
                String line = bufferedReader.readLine();
                if (line == null){
                    //读取文件完毕
                    break;
                }
                stopWords.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        DocSearcher docSearcher = new DocSearcher();
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("->");
            String query = scanner.next();
            List<Result> results = docSearcher.search(query);
            for (Result result : results){
                System.out.println("================================");
                System.out.println(result);
            }
        }
    }
}
