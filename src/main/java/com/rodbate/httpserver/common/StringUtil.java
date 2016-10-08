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
     * @param src str
     * @return str
     */
    public static String removeBlankSpace(String src){

        Objects.requireNonNull(src);

        src = src.trim();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < src.length(); i++) {

            char c = src.charAt(i);

            if (c != ' ') {
                sb.append(c);
            }
        }

        return sb.toString();
    }



    public static boolean isNotNull(String str) {

        return str != null && str.trim().length() > 0;
    }

    public static boolean isNull(String s) {
        return !isNotNull(s);
    }


    public static void main(String[] args) {

        System.out.println(removeBlankSpace("Content-Type = multipart/form-data; boundary=----WebKitFormBoundaryGzGFIXxIloOfAyym"));

    }
}
