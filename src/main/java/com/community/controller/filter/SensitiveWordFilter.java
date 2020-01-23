package com.community.controller.filter;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/17/12:34
 * @Description:
 */
@Component
public class SensitiveWordFilter {

    private static Logger logger = LoggerFactory.getLogger(SensitiveWordFilter.class);

    //敏感词的替换词
    private static final String sensitiveWordReplace = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    /**
     * 容器创建此类时就会调用此方法
     * 构造敏感词前缀树
     */
    @PostConstruct
    public void init() {
        //根节点
        TrieNode root = new TrieNode();
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-word.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))
        ) {
            String keyWord;
            while ((keyWord = bufferedReader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyWord);
            }
        } catch (IOException e) {
            logger.error("敏感词前缀树生成失败：" + e.getMessage());
        }
    }

    /**
     * 将一个敏感词添加到前缀树中
     *
     * @param keyWord
     */
    private void addKeyword(String keyWord) {
        TrieNode tmpNode = rootNode;
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TrieNode subNode = tmpNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tmpNode.addSubNode(c, subNode);
            }
            // 指向子节点,进入下一轮循环
            tmpNode = subNode;
            // 设置结束标识
            if (i == keyWord.length() - 1) {
                tmpNode.setWordEnd(true);
            }
        }
    }

    /**
     * 敏感词替换
     * @param text
     * @return
     */
    public String filter(String text){
        //前缀树指针
        TrieNode tmpNode=rootNode;
        //文本记录指针
        int begin=0;
        //在文本根据前缀树移动的指针，判断是否有敏感词Node
        int position=0;
        StringBuffer sb=new StringBuffer();
        while(position<text.length()){
            char c=text.charAt(position);
            //如果当前字符是特殊字符，先记录
            if(isSymbol(c)){
                //如果从前缀树根节点开始，begin指针移动下一位
               if(tmpNode==rootNode){
                   begin++;
                   sb.append(c);
               }
               //position指针向后移动判断是否有敏感词下一节点
                position++;
                continue;
            }
            // 检查下级节点
            tmpNode = tmpNode.getSubNode(c);
            //如果等于Null 说明无敏感词
            if(tmpNode==null){
                //非敏感词的加入StringBuffer
                sb.append(text.charAt(begin));
                //进入下一个位置
                position=++begin;
                // 重新指向根节点
                tmpNode = rootNode;
            }else if(tmpNode.isWordEnd()){
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(sensitiveWordReplace);
                begin=++position;
                tmpNode=rootNode;
            }else {
                position++;
            }
        }
        // 将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     * 判断是否为特殊符号
     * @param c
     * @return
     */
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    class TrieNode {

        //子节点
        private Map<Character, TrieNode> subNode = new HashMap<>();

        //是否是敏感词树的最后一个字符
        private boolean isWordEnd = false;


        private void setWordEnd(boolean isWordEnd) {
            this.isWordEnd = true;
        }

        private boolean isWordEnd() {
            return isWordEnd;
        }

        private void addSubNode(Character c, TrieNode trieNode) {
            subNode.put(c, trieNode);
        }

        private TrieNode getSubNode(Character c) {
            return subNode.get(c);
        }
    }

}
