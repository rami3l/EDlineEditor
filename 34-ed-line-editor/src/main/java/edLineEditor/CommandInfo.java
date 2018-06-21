package edLineEditor;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandInfo {
    private int beginIndex;
    private int endIndex;
    private int toIndex;
    private char commandMark;
    private boolean isDefaultLoc;
    private String fileName;
    private String originStr;
    private String changeToStr;
    private int replaceCount;
    private char markChara;
    private int currentLine;

    public CommandInfo(String command, Page page) throws FalseInputFormatException{
        currentLine = page.getCurrLine();
        if (command.length() == 0) throw new FalseInputFormatException();
        String originStr = command;
        command = dealReplace(command);                            // 检查是否为替换,进行特殊处理
        check$AndStr(command);                          // 检查是否含有 $-/str/
        if (Character.isAlphabetic(command.charAt(0))
                || command.charAt(0) == '=') {              // 检查是否为默认地址
            isDefaultLoc = true;
            beginIndex = currentLine;
            endIndex = currentLine;
            commandMark = command.charAt(0);
        }
        else if (command.charAt(0) == ';'){
            beginIndex = currentLine;
            endIndex = page.getSize() - 1;
            commandMark = command.charAt(1);
        }
        else if (command.charAt(0) == ','){
            beginIndex = 0;
            endIndex = page.getSize() - 1;
            try {
                commandMark = command.charAt(1);
            }catch (StringIndexOutOfBoundsException e){
                throw new FalseInputFormatException();
            }
        }
        else {
            if (times(command, "/") % 2 != 0){              // 判断？/？
                while (command.contains("?")){
                    int i1 = command.indexOf("?");
                    int i2 = command.indexOf("?", i1 + 1);
                    String s = command.substring(i1, i2 + 1);
                    int lineNumber = page.findUpLineNumber(s.substring(1, s.length() - 1));
                    command = command.replace(s, Integer.toString(lineNumber + 1));
                }
                while (command.contains("/")){              // 转化匹配字符
                    int i1 = command.indexOf("/");
                    int i2 = command.indexOf("/", i1 + 1);
                    String s = command.substring(i1, i2 + 1);
                    int lineNumber = page.findDownLineNumber(s.substring(1, s.length() - 1));
                    command = command.replace(s, Integer.toString(lineNumber + 1));
                }
            }
            else {
                while (command.contains("/")){              // 转化匹配字符
                    int i1 = command.indexOf("/");
                    int i2 = command.indexOf("/", i1 + 1);
                    String s = command.substring(i1, i2 + 1);
                    int lineNumber = page.findDownLineNumber(s.substring(1, s.length() - 1));
                    command = command.replace(s, Integer.toString(lineNumber + 1));
                }
                while (command.contains("?")){
                    int i1 = command.indexOf("?");
                    int i2 = command.indexOf("?", i1 + 1);
                    String s = command.substring(i1, i2 + 1);
                    int lineNumber = page.findUpLineNumber(s.substring(1, s.length() - 1));
                    command = command.replace(s, Integer.toString(lineNumber + 1));
                }
            }
            while (command.contains("'")){                // 转化标记符
                int i = command.indexOf("'");
                String o = command.substring(i, i + 2);
                int lineNumber =page.getMark(o.charAt(1));
                command = command.replace(o, Integer.toString(lineNumber + 1));
            }
            if (command.contains(".")){                // 替换默认地址
                command = command.replace(".", Integer.toString(currentLine + 1));
            }
            if (command.contains("$")){
                command = command.replace("$", Integer.toString(page.getSize()));
            }
            if (command.contains("-")) {
                command = dealOperation(command, "-");
            }
            if (command.contains("+")){
                command = dealOperation(command, "+");
            }
            if (command.charAt(0) == ',' &&
                    Character.isDigit(command.charAt(command.indexOf(",") + 1))){ // 逗号右边有数字，左边没有
                int i;
                for (i = 0; i < command.length(); i++){
                    if (Character.isAlphabetic(command.charAt(i)) || command.charAt(i) == '=') break;
                }
                String old = command.substring(0, i);
                String New = Integer.toString(currentLine + 1) + old;
                command = command.replace(old, New);
            }
            if (command.contains(",") &&
                    Character.isDigit(command.charAt(command.indexOf(",") - 1))
                    && (Character.isAlphabetic(command.charAt(command.indexOf(",") + 1))
                    || command.charAt(command.indexOf(",") + 1) == '=')){  // 逗号左边有数字，右边没有
                String old = command.substring(0, command.indexOf(",") + 1);
                String New = old + Integer.toString(currentLine + 1);
                command = command.replace(old, New);
            }
            if (!command.contains(",")){
                int i;
                for (i = 0; i < command.length(); i++){
                    if (Character.isAlphabetic(command.charAt(i)) || command.charAt(i) == '='){
                        break;
                    }
                }
                String num = command.substring(0, i);
                String New = num + "," + num;
                command = New + command.substring(i, command.length());
            }

            int i;
            for (i = 0; i < command.length(); i++){
                if (Character.isAlphabetic(command.charAt(i)) || command.charAt(i) == '='){
                    break;
                }
            }
            command = command.substring(0, i) + " " + command.substring(i, command.length());

            String[] loc = command.split(" ")[0].split(",");
            beginIndex = Integer.parseInt(loc[0]) - 1;
            endIndex = Integer.parseInt(loc[1]) - 1;
            commandMark = command.split(" ")[1].charAt(0);
        }
        // 分别对某些指令进行特殊处理
        if (commandMark == 'm' || commandMark == 't'){
            dealMoveAndCopy(command, commandMark, page);
        }
        else if (commandMark == 'z'){
            if (originStr.charAt(originStr.length() - 1) == 'z'){
                toIndex = -1;
            }
            else {
                toIndex = Integer.parseInt(originStr.split("z")[1]);
            }
        }
        else if (commandMark == 'w' || commandMark == 'W' || commandMark == 'f'){
            if (originStr.split(" ").length == 2){
                fileName = originStr.split(" ")[1];
            }
            else fileName = "";
        }
        else if (commandMark == 'k'){
            int i = command.indexOf("k");
            markChara = command.charAt(i+1);
        }
        check(page);
    }

    private void check(Page page) throws FalseInputFormatException {          // 检查开始和结束地址
        if (beginIndex < -1 || endIndex < beginIndex || endIndex >= page.getSize()){
            throw new FalseInputFormatException();
        }
    }

    private static void check$AndStr(String command) throws FalseInputFormatException {         // 初始检查输入是否合法
        Pattern p1 = Pattern.compile("\\$([+-])(/.+/|\\?.+\\?)");          // 不合法 $(+-)(/str/|?str?)
        Pattern p2 = Pattern.compile("\\$([+-])\\$");                      // 不合法 $(+-)$
        Pattern p3 = Pattern.compile("(/.+/|\\?.+\\?)([+-])(/.+/|\\?.+\\?)");  // 不合法 /str/+-/str/
        Pattern p4 = Pattern.compile("(^[/])*[a-z],[a-z](^[/])+");
        Pattern p5 = Pattern.compile("/.*\\?.*/|\\?.*/.*\\?");          // 合法的模式/?/ or ?/?
        Matcher m1 = p1.matcher(command);
        Matcher m2 = p2.matcher(command);
        Matcher m3 = p3.matcher(command);
        Matcher m4 = p4.matcher(command);
        Matcher m5 = p5.matcher(command);
        if (m1.find() || m2.find() || m3.find() || m4.find())
            throw new FalseInputFormatException();
        if ((times(command, "?") == 1 || times(command, "/") == 1) && !m5.find()){
            throw new FalseInputFormatException();
        }
    }

    private void dealMoveAndCopy(String command, char commandMark, Page page) throws FalseInputFormatException {
        // 处理剪切和复制
        if (command.indexOf(commandMark) == command.length() - 1){
            toIndex = currentLine;
        }
        else {
            String to = command.substring(command.indexOf(commandMark) + 1, command.length());
            CommandInfo toInfo = new CommandInfo(to+"p", page);
            toIndex = toInfo.getBeginIndex();
        }
    }

    private String dealReplace(String command) throws FalseInputFormatException {       // 处理替换指令
        Pattern p = Pattern.compile("s/.+/.*/(g|\\d*)");
        Pattern p1 = Pattern.compile(".+//(g|\\d*)");                 // 匹配/str//模式
        Matcher m = p.matcher(command);
        Matcher m1 = p1.matcher(command);
        if (!m.find()) return command;
        String cutStr = m.group();
        String s = cutStr.substring(2, cutStr.length());
        if (m1.find()){
            originStr = s.split("/")[0];
            changeToStr = "";
            s = s.replace(originStr + "//", originStr + "/null/");
        }
        else {
            originStr = s.split("/")[0];
            changeToStr = s.split("/")[1];
        }
        if (s.split("/").length == 3){
            if (s.split("/")[2].equals("g")){
                replaceCount = -1;
            }
            else {
                try {
                    replaceCount = Integer.parseInt(s.split("/")[2]);
                }catch (Exception e){
                    throw new FalseInputFormatException();
                }
            }
        }
        else {
            replaceCount = 1;
        }
        int i = command.indexOf(cutStr);
        return command.substring(0, i + 1);
    }

    private String extractNumber(String s, String operator){          // 处理两个数的加法和减法
        int beginNumber;
        int endNumber;
        if (s.indexOf(operator) == 0) {
            beginNumber = currentLine + 1;
        }
        else {
            beginNumber = Integer.parseInt(s.split("[+-]")[0]);
        }
        endNumber = Integer.parseInt(s.split("[+-]")[1]);
        if (operator.equals("-")){
            s = Integer.toString(beginNumber - endNumber);
        }else {
            s = Integer.toString(beginNumber + endNumber);
        }
        return s;
    }

    private String dealOperation(String commandLine, String operator){             // 处理加减号
        int index = 0;
        for (int i = 0; i < commandLine.length(); i++){
            if (Character.isAlphabetic(commandLine.charAt(i)) || commandLine.charAt(i) == '=') break;
            index++;
        }
        String loc = commandLine.substring(0, index);
        String s1 = loc.split(",")[0];
        if (s1.contains(operator)){
            s1 = extractNumber(s1, operator);
        }
        if (loc.contains(",")){
            String s2 = loc.split(",")[1];
            if (s2.contains(operator)){
                s2 = extractNumber(s2, operator);
            }
            commandLine = s1 + "," + s2 + commandLine.substring(index, commandLine.length());
        }
        else {
            commandLine = s1 + commandLine.substring(index, commandLine.length());
        }
        return commandLine;
    }

    void set_S_Param(String old, String New, int count){
        originStr = old;
        changeToStr = New;
        replaceCount = count;
    }

    public static int times(String line, String sub){
        if (sub.equals("?")) sub = "\\?";
        Pattern p = Pattern.compile(sub);
        Matcher m = p.matcher(line);
        int times = 0;
        while (m.find()){
            times++;
        }
        return times;
    }

    public int getBeginIndex(){
        return this.beginIndex;
    }

    public int getEndIndex(){
        return this.endIndex;
    }

    public int getToIndex() {
        return this.toIndex;
    }

    public char getCommand() {
        return this.commandMark;
    }

    public boolean isDefaultLoc() {
        return isDefaultLoc;
    }

    public String fileName(){
        return fileName;
    }

    public String originStr(){
        return originStr;
    }

    public String changeToStr(){
        return changeToStr;
    }

    public int replaceCount() {
        return replaceCount;
    }

    public char markChara() {
        return markChara;
    }
}
