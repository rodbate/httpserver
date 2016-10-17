package com.rodbate.httpserver.common;


import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * 类反射通用类
 *
 */
public class ClassReflection {



    /**
     *
     * 获取指定包下的所有类
     *
     * @param classLoader 类加载器
     * @param pack 包路径  com.rodbate
     * @return set
     */
    public static Set<Class<?>> getClassesFromPackage(ClassLoader classLoader, String pack) {

        Objects.requireNonNull(pack, "pack must not be null");

        Set<Class<?>> classSet = new HashSet<>();

        if (classLoader == null) {

            classLoader = Thread.currentThread().getContextClassLoader();

        }

        String packagePath = "";
        //com/rodbate/httpserver
        if (pack.length() > 0) {
            packagePath = pack.replace(".", "/");
        }


        try {

            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {

                URL url = resources.nextElement();

                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {

                    String fileName = URLDecoder.decode(url.getFile(), "UTF-8");

                    findClass(pack, fileName, classSet, classLoader);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return classSet;
    }


    /**
     *
     * 加载class
     *
     * @param pack 包名
     * @param fileName class文件名
     * @param classSet class集合
     * @param classLoader 类加载器
     */
    private static void findClass(String pack, String fileName, Set<Class<?>> classSet, ClassLoader classLoader) {


        File dir = new File(fileName);

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles(pathname -> (pathname.isDirectory() || pathname.getName().endsWith(".class")));


        for (File f : files) {

            if (f.isDirectory()) {

                findClass(pack.length() == 0 ? f.getName() : pack + "." + f.getName(), f.getAbsolutePath(), classSet, classLoader);

            } else {

                String className = f.getName().substring(0, f.getName().length() - 6);

                try {

                    classSet.add(classLoader.loadClass(pack.length() == 0 ? className : pack + "." + className));

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation){

        Set<Class<?>> classes = new HashSet<>();

        Set<Class<?>> classSet = getClassesFromPackage(null, "");

        for (Class<?> clazz : classSet) {
            if (clazz.isAnnotationPresent(annotation)) {
                classes.add(clazz);
            }
        }

        return classes;
    }


    public static Set<Method> getMethodsByClassAndAnnotation(Class<?> clazz, Class<? extends Annotation> annotation){

        Set<Method> methods = new HashSet<>();

        Method[] declaredMethods = clazz.getDeclaredMethods();

        for (Method m : declaredMethods) {
            if (m.isAnnotationPresent(annotation)) {

                methods.add(m);
            }
        }

        return methods;
    }

    public static Set<Method> getMethodsByAnnotation(Class<? extends Annotation> annotation){
        Set<Method> methods = new HashSet<>();

        Set<Class<?>> classSet = getClassesFromPackage(null, "");

        for (Class<?> clazz : classSet) {
            Method[] declaredMethods = clazz.getDeclaredMethods();

            for (Method m : declaredMethods) {
                if (m.isAnnotationPresent(annotation)) {

                    methods.add(m);
                }
            }
        }

        return methods;
    }

}
