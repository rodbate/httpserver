package com.rodbate.httpserver.common;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ListSerializer;
import com.rodbate.httpserver.annotations.RequestMapping;
import com.rodbate.httpserver.http.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;


import static com.rodbate.httpserver.common.ClassReflection.*;

/**
 *
 * 请求url映射器
 *
 *
 */
public class RequestMappers {


    public static final Map<String, RequestMeta> urlMappers = new HashMap<>();


    private static final Logger LOG = LoggerFactory.getLogger(RequestMappers.class);


    public static void init(){
        LOG.info(" >>>>>>>>>>>  URL Mapper Begin =============>>>>> ");
        initMappers();
        printMappers();
        LOG.info(" >>>>>>>>>>>  URL Mapper Finish =============>>>>> ");
    }


    public static RequestMeta getMeta(String uri){
        return urlMappers.get(uri);
    }


    private static void printMappers() {

        List<PrintUrl> printUrls = new ArrayList<>();

        //[{"url":"/add","method":"com.rodbate.add","requestMethod":["GET","POST"],"response":"text/html"}]
        for (Map.Entry<String, RequestMeta> entry : urlMappers.entrySet()) {

            RequestMeta value = entry.getValue();

            List<String> list = new ArrayList<>();

            for(RequestMethod m : value.getRequestMethod()) {
                list.add(m.getDesc());
            }

            if (list.size() == 0){
                list.add("ALL");
            }

            PrintUrl print = new PrintUrl(entry.getKey(),
                                          value.getTarget().getName() + "." +value.getMethod().getName(),
                                          list,
                                          value.getResponseContentType());

            printUrls.add(print);
        }

        LOG.info(JSON.toJSONString(printUrls));
    }


    private static class PrintUrl{

        String url;

        String method;

        List<String> requestMethod;

        String response;

        public PrintUrl() {
        }

        public PrintUrl(String url, String method, List<String> requestMethod, String response) {
            this.url = url;
            this.method = method;
            this.requestMethod = requestMethod;
            this.response = response;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<String> getRequestMethod() {
            return requestMethod;
        }

        public void setRequestMethod(List<String> requestMethod) {
            this.requestMethod = requestMethod;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }



    /**
     *
     * 初始化url映射
     *
     */
    private static void initMappers(){

        Map<Class<?>, String> urlClass = new HashMap<>();

        Map<String, Method> urlMethod = new HashMap<>();

        //1 获取有RequestMapping 注解的类
        Set<Class<?>> classes = getClassesByAnnotation(RequestMapping.class);

        for(Class<?> c : classes) {

            RequestMapping rm = c.getDeclaredAnnotation(RequestMapping.class);

            urlClass.put(c, rm.value());
        }


        //2 获取有RequestMapping 注解的方法

        Set<Method> methods = getMethodsByAnnotation(RequestMapping.class);

        for (Method m : methods) {

            RequestMapping rm = m.getDeclaredAnnotation(RequestMapping.class);

            String methodUrl = rm.value();

            if (!methodUrl.startsWith("/")) {
                methodUrl = "/" + methodUrl;
            }

            //请求完整路径
            String url = methodUrl;

            //获取该方法所在的类
            Class<?> clazz = m.getDeclaringClass();

            String prefixUrl = urlClass.get(clazz);

            if (prefixUrl != null) {

                if (prefixUrl.endsWith("/")) {
                    prefixUrl = prefixUrl.substring(0, prefixUrl.length());
                }

                url = prefixUrl + methodUrl;

            }

            RequestMeta meta = new RequestMeta();

            meta.setUrl(url);
            meta.setMethod(m);

            List<RequestMethod> requestMethods = new ArrayList<>();
            requestMethods.addAll(Arrays.asList(rm.method()));

            meta.setRequestMethod(requestMethods);
            meta.setResponseContentType(rm.responseContentType());
            meta.setTarget(clazz);

            urlMappers.put(url, meta);

        }

    }
}
