package com.example.demo.common;


import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Parser {
    Index index = new Index();
    private static final String PATH = "D:/JAVA/java_api/docs/api";

    //单线程制作索引
    public void run(){

        long beg = System.currentTimeMillis();
        //1.枚举出api中的所有文件
        ArrayList<File> fileList = new ArrayList<>();
        enumFile(PATH, fileList);
//        System.out.println(fileList);
//        System.out.println(fileList.size());

        //2.针对文件路径，打开文件，读取文件，进行解析-------占消耗时间的大头
        for (File file : fileList){
            System.out.println("开始解析：" + file.getAbsolutePath());
            parseHTML(file);
        }

        //3.把内存中构造好的索引数据结构，保存到指定文件
        index.save();
    }
    //多线程制作索引
    public void runByThread() throws InterruptedException {
        //1.枚举出所有文件
        ArrayList<File> fileList = new ArrayList<>();
        enumFile(PATH, fileList);
        //2.遍历文件.使用线程池
        CountDownLatch countDownLatch = new CountDownLatch(fileList.size());
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        for (File file : fileList){
//          使用submit将文件提交到线程池中，借助new Runnable()描述具体任务的内容
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("开始解析：" + file.getAbsolutePath());
                    parseHTML(file);
                    countDownLatch.countDown();
                }
            });
        }
        //await 方法会阻塞, 直到所有任务都countDown执行完
        countDownLatch.await();
        //把线程池的线程手动结束(非守护线程影响进程结束，需要手动关闭)
        threadPool.shutdown();
        //3.保存索引(保证所有线程任务都执行完，才能开始保存--->CountDownLatch)
        index.save();
    }

    private void parseHTML(File file) {
        //解析html标题
        String title = parseTitle(file);
        //解析html对应url
        String url = parseUrl(file);
        //解析正文
        //String content = parseContent(file);
        String content = parseContentByRegex(file);
        //把解析出的标题、url、正文组成的内容加入索引
        index.addDoc(title, url, content);
    }

    public String parseContent(File file) {
        //字符流读取，用 < 和 > 控制是否读取内容
        try {
            //FileReader fileReader = new FileReader(file);
            //搭配BufferedReader优化读取速率,将缓冲区设为1MB
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file), 1024 * 1024);
            //加上是否拷贝的开关
            boolean isCopy = true;
            //StringBuilder用来保存结果
            StringBuilder content = new StringBuilder();
            while (true){
                //读到文件末尾会返回-1，所以用int表示读完
                int ret = bufferedReader.read();
                if (ret == -1){
                    break;
                }
                char c = (char) ret;
                if (isCopy){
                    //开关是打开状态，拷贝
                    if (c == '<'){
                        isCopy = false;
                        continue;
                    }
                    //去掉换行
                    if (c == '\n' || c == '\r'){
                        c = ' ';
                    }
                    content.append(c);
                }else{
                    //开关是关闭状态，不拷贝，直到遇到 >
                    if (c == '>'){
                        isCopy = true;
                    }
                }
            }
            bufferedReader.close();
            return String.valueOf(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String readFile(File file){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file), 1024 * 1024);
            StringBuilder stringBuilder = new StringBuilder();
            while (true){
                int ret = bufferedReader.read();
                if (ret == -1){
                    break;
                }
                char c = (char) ret;
                if (c == '\n' || c == '\t'){
                    c = ' ';
                }
                stringBuilder.append(c);
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    //正则实现去标签和script
    public String parseContentByRegex(File file){
        //把文件都读到String里
        String content = readFile(file);
        //替换掉script标签
        content = content.replaceAll("<script.*?>(.*?)</script>", " ");
        //替换掉普通的html标签
        content = content.replaceAll("<.*?>", " ");
        //合并空格
        content = content.replaceAll("\\s+", " ");
        return content;
    }
    private String parseUrl(File file) {
        String part1 = "https://docs.oracle.com/javase/8/docs/api/";
        String part2 = file.getAbsolutePath().substring(PATH.length());
        return part1 + part2;
    }

    private String parseTitle(File file) {
        return file.getName().substring(0, file.getName().length() - 5);
    }

    /**
     *
     * @param path 表示从哪个目录开始递归查找
     * @param fileList 递归后的结果
     */
    private void enumFile(String path, ArrayList<File> fileList) {
        //转成文件
        File rootPath = new File(path);
        //把文件分成数组
        File[] files = rootPath.listFiles();
        //System.out.println(Arrays.toString(files));
        for(File file : files){
            //如果file是普通文件，加入fileList.如果是目录，继续递归
            if (file.isDirectory()){
                enumFile(file.getAbsolutePath(), fileList);
            }else {
                if (file.getAbsolutePath().endsWith(".html")){
                    fileList.add(file);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser();
        //parser.run();
        parser.runByThread();
    }
}
