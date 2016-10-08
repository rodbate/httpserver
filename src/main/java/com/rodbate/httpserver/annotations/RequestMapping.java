package com.rodbate.httpserver.annotations;


import com.rodbate.httpserver.http.RequestMethod;

import java.lang.annotation.*;




@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {


    String value();


    RequestMethod[] method() default {RequestMethod.ALL};


    String responseContentType() default "application/json; charset=UTF-8";


}
