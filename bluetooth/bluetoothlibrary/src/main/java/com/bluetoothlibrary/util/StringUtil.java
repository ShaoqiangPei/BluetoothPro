package com.bluetoothlibrary.util;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {


    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || input.trim().length() == 0 || input.equals("null"))
            return true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotEmpty(String input) {
        return !isEmpty(input);
    }

    /***
     * 判断是否为数字(整数或小数)
     *
     * 一般用于判断输入框中要输入Double类型数据
     * @param msg
     * @return
     */
    public static boolean isDoubleFormat(String msg){
        String regex="0|^[1-9][0-9]*$|^[1-9][0-9]*\\.[0-9]+$|0\\.[0-9]+$";
        return isRegex(msg,regex);
    }

    /**字符串是否全为字母**/
    public static boolean isAllLetter(String msg){
        String regex="^[a-zA-Z]+$";
        return isRegex(msg,regex);
    }

    /**
     * 根据规则匹配字符串
     *
     * @param msg 字符串
     * @param regex 设置的规则，如:"^[a-zA-Z0-9]+$"
     * @return
     */
    public static boolean isRegex(String msg, String regex){
        if(StringUtil.isEmpty(msg)){
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(msg);
        return matcher.matches();
    }

}