package com.rodbate.httpserver.common;


import com.rodbate.httpserver.http.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * 请求元数据
 *
 */
public class RequestMeta {


    //请求url路径
    private String url;

    //方法调用类
    private Class target;


    //请求映射的调用方法
    private Method method;


    //请求http方法
    private List<RequestMethod> requestMethod;

    //返回响应的Content-Type
    private String responseContentType;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Class getTarget() {
        return target;
    }

    public void setTarget(Class target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<RequestMethod> getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(List<RequestMethod> requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }
}
