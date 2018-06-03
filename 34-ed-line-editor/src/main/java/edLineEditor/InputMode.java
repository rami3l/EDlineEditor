package edLineEditor;
import java.util.Scanner;

public class InputMode {
    private Page page;
    private int index;
    public InputMode(int beginIndex, int endIndex, Page page){
        // 替换模式
        beginIndex--;
        endIndex--;
        page.saveCurrent();
        for (int i = beginIndex; i <= endIndex; i++){
            page.currPage.remove(beginIndex);
        }
        index = beginIndex;
        this.page = page;
    }
    public InputMode(int beginIndex, char command, Page page){
        // 追加在指定行后面
        beginIndex--;
        page.saveCurrent();
        if (command == 'a'){
            index = beginIndex + 1;
        }
        // 追加在指定行前面
        else {
            if (beginIndex == -1) index = 0;
            else index = beginIndex;
        }
        this.page = page;
    }

    public void insert(Scanner in){
        page.saveCurrent();
        while (true){
            String line = in.nextLine();
            if (line.equals(".")) break;            // 检测到句号时退出
            page.currPage.add(index, line);
            index++;
        }
        page.setCurrLine(index);
        page.isSaved = false;
    }

    public static void main (String[] args){
    }
}
