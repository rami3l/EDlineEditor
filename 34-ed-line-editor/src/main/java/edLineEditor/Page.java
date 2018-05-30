package edLineEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

// 缓存文件类，在未保存之前，所有文件操作均在Page类中操作
public class Page {
    LinkedList<String> currPage;        // 保存当前和上一个状态，用于撤销
    LinkedList<String> prevPage;
    String filename;
    int currLine;                        // 保存当前行
    HashMap<Character, Integer> mark;     // 保存标记符号
    boolean isSaved;

    public Page(){                       // 从ed直接进入
        currPage = new LinkedList<>();
        prevPage = new LinkedList<>();
        mark = new HashMap<>();
        filename = "";
        currLine = 0;
    }

    public Page(String filename){           // 从文件读入
        //TODO
        ArrayList<String> file = FileTool.readFile(filename);
        currPage = new LinkedList<>();
        prevPage = new LinkedList<>();
        mark = new HashMap<>();
        this.filename = filename;
        currLine = file.size();
        currPage.addAll(file);
        prevPage.addAll(file);
    }

    public void saveCurrent(){              // 在对文本操作前保存当前状态，修改前必须调用
        prevPage = (LinkedList<String>) currPage.clone();
    }

    public void unDo(){                    // 撤销操作，返回至saveCurrent的状态
        LinkedList<String> swap = (LinkedList<String>) currPage.clone();
        currPage = (LinkedList<String>) prevPage.clone();
        prevPage = (LinkedList<String>) swap.clone();
    }

    public int findDownLineNumber(String str){
        for (int i = currLine - 1; i < currPage.size(); i++){
            String s = currPage.get(i);
            if (s.contains(str)) return i + 1;
        }
        for (int i = 0; i < currLine - 1; i++){
            if (currPage.get(i).contains(str)) return i + 1;
        }
        return -1;
    }

    public int findUpLineNumber(String str){
        for (int i = currLine - 1; i >= 0; i--){
            String s = currPage.get(i);
            if (s.contains(str)) return i + 1;
        }
        for (int i = currPage.size() - 1; i > currLine - 1; i--){
            if (currPage.get(i).contains(str)) return i + 1;
        }
        return -1;
    }

    public static void main(String[] args){
        Page test = new Page();
        test.prevPage.add("GYS");
        test.currPage.add("ZWQ");
        test.saveCurrent();
        test.currPage.add("Love");
        System.out.println(test.currPage.toString());
        test.unDo();
    }
}