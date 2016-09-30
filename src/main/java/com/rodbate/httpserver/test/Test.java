package com.rodbate.httpserver.test;


import com.rodbate.httpserver.http.RBHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test {


    public void get(Integer a){
        System.out.println(a + " ==== ");
    }

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {


        Class testClass = RequestTest.class;

        Method get = testClass.getDeclaredMethod("get", RBHttpRequest.class);


        get.invoke(testClass.newInstance(), new RBHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/GET"));

    }
}
