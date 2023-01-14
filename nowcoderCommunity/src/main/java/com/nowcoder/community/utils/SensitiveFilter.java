package com.nowcoder.community.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
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

@Component
public class SensitiveFilter {

   private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

   private static final String REPLACEMENT = "***";

   //根节点
   private TrieNode rootNode = new TrieNode();

   //初始化字典树
   @PostConstruct
   public void init(){
      try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");//从类目录下加载文件资源
          BufferedReader reader = new BufferedReader(new InputStreamReader(is));){

         String keyword;
         while((keyword=reader.readLine())!=null){
            this.addKeyWord(keyword);
         }

      }catch (IOException e){
         LOGGER.error("加载敏感词文件失败： " + e.getMessage());
      }
   }

   //添加敏感词,字典树插入操作
   private void addKeyWord(String keyword){
      TrieNode node = rootNode;
      for(int i = 0; i < keyword.length(); i++){
         char c = keyword.charAt(i);
         TrieNode subNode = node.getSubNode(c);
         if(subNode == null){
            subNode = new TrieNode();
            node.addSubNode(c,subNode);
         }
         node = subNode;
         if(i == keyword.length()-1){
            node.setKeywordEnd(true);  //设置结束位置为敏感词
         }
      }
   }

   /**
    * 过滤敏感词
    * @param text 待过滤文本
    * @return
    */
   public String filter(String text){
      if(StringUtils.isBlank(text)){
         return null;
      }
      // 指针1
      TrieNode tempNode = rootNode;
      // 指针2
      int begin = 0;
      // 指针3
      int position = 0;
      // 结果
      StringBuilder sb = new StringBuilder();

      while(begin < text.length()){
         if(position < text.length()) {
            Character c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
               if (tempNode == rootNode) {
                  begin++;
                  sb.append(c);
               }
               position++;
               continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
               // 以begin开头的字符串不是敏感词
               sb.append(text.charAt(begin));
               // 进入下一个位置
               position = ++begin;
               // 重新指向根节点
               tempNode = rootNode;
            }
            // 发现敏感词
            else if (tempNode.isKeywordEnd()) {
               sb.append(REPLACEMENT);
               begin = ++position;
            }
            // 检查下一个字符
            else {
               position++;
            }
         }
         // position遍历越界仍未匹配到敏感词
         else{
            sb.append(text.charAt(begin));
            position = ++begin;
            tempNode = rootNode;
         }
      }
      return sb.toString();
   }

   //判断是否为符号
   private boolean isSymbol(char c){
      //0x9fff~0x2e80 东亚文字范围
      return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
   }
   /**
    * 字典树
    */
   private class TrieNode{

      //关键字结束标识
      private boolean isKeywordEnd = false;

      //子节点
      private Map<Character,TrieNode> subNodes = new HashMap<>();

      public boolean isKeywordEnd() {
         return isKeywordEnd;
      }

      public void setKeywordEnd(boolean keywordEnd) {
         isKeywordEnd = keywordEnd;
      }

      //添加子节点
      public void addSubNode(Character c, TrieNode node){
         subNodes.put(c,node);
      }

      //获取子节点
      public TrieNode getSubNode(Character c){
         return subNodes.get(c);
      }
   }
}
