package com.rodbate.httpserver.test;


import com.rodbate.httpserver.Bootstrap;
import com.rodbate.httpserver.annotations.RequestMapping;
import com.rodbate.httpserver.common.ClassReflection;
import com.rodbate.httpserver.common.RequestMappers;

import static com.rodbate.httpserver.common.ClassReflection.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class T {


    public static void main(String[] args) {

        Bootstrap.main();
    }
}
