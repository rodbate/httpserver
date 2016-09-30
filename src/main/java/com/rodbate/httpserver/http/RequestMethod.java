package com.rodbate.httpserver.http;




public enum RequestMethod {

    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"), HEAD("HEAD");

    private final String desc;

    public String getDesc() {
        return desc;
    }

    RequestMethod(String desc){
        this.desc = desc;
    }
}
