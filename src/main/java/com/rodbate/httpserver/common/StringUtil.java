package com.rodbate.httpserver.common;


import java.util.Objects;

/**
 *
 * 字符串工具
 *
 */
public class StringUtil {





    /**
     * 去除空格
     *
     * @param str str
     * @return str
     */
    public static String removeBlankSpace(String str){

        Objects.requireNonNull(str);

        char[] array = str.toCharArray();

        int p1 = 0;

        int p2 = 0;

        while(p2 < array.length){

            array[p1] = array[p2++];

            if (array[p1] != ' '){
                p1++;
            }
        }

        return new String(array, 0, p1);
    }




    public static boolean isNotNull(String str) {

        return str != null && str.trim().length() > 0;
    }

    public static boolean isNull(String s) {
        return !isNotNull(s);
    }


}
