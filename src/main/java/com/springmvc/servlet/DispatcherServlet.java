package com.springmvc.servlet;

import com.springmvc.annotation.*;
import com.springmvc.controller.UserController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DispatcherServlet extends HttpServlet {

    //将扫描得到的class类路径都放到此集合中
    List<String> classNames = new ArrayList<String>();
    //创建ioc容器（将反射实例化的对象添加到容器中,实际开发中考虑到线程安全应该使用CurrentHashMap）
    Map<String, Object> beans = new HashMap<String, Object>();
    //创建url映射方法的容器
    Map<String, Object> handlerMap = new HashMap<String, Object>();


    /**
     * 初始化！
     *
     * @param config
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.包扫描（找到带有注解的类）
        executeScanPackage("com.springmvc");
        //2.反射实例化
        executeInstance();
        //3.依赖注入（装配bean）
        executeAutowired();
        //4.方法映射（通过浏览器输入路径找到对应的方法）
        executeUrlHanding();
    }

    /**
     * 4.方法映射（通过浏览器输入路径找到对应的方法）
     */
    private void executeUrlHanding() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) { //遍历所有的对象
            Object instance = entry.getValue();//得到所有对象的实例
            Class<?> clazz = instance.getClass();//得到对象的class
            if (clazz.isAnnotationPresent(ColaController.class)) { //判断这个对象是否是controller对象
                ColaRequestMapping crmClass = clazz.getAnnotation(ColaRequestMapping.class);//得到这个对象上边的注解
                String classUrl = crmClass.value();//得到这个注解中的类路径

                Method[] methods = clazz.getDeclaredMethods();//得到这个对象的所有方法
                for (Method method : methods) {//遍历所有方法
                    if (method.isAnnotationPresent(ColaRequestMapping.class)) {
                        ColaRequestMapping crmMethod = method.getAnnotation(ColaRequestMapping.class);//找到带有ColaRequestMapping注解的方法
                        String methodUrl = crmMethod.value(); //得到这个注解中的方法路径
                        handlerMap.put(classUrl + methodUrl, method);//将url和方法放到集合 中
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }
        }
    }

    /**
     * 3.依赖注入（装配bean）
     */
    private void executeAutowired() {
        for (Map.Entry entry : beans.entrySet()) { //遍历整个ioc容器
            Object instance = entry.getValue();//得到所有的实例对象
            Class<?> clazz = instance.getClass(); //得到实例对象的class
            if (clazz.isAnnotationPresent(ColaController.class)) { //判断class对象是否是controller对象
                Field[] fields = clazz.getDeclaredFields(); //得到controller对象下的所有成员变量
                for (Field field : fields) { //遍历成员变量
                    if (field.isAnnotationPresent(ColaAutowired.class)) { //判断成员变量是否有ColaAutowired的注解
                        ColaAutowired ca = field.getAnnotation(ColaAutowired.class);
                        String keyService = ca.value();
                        Object serviceInstance = beans.get(keyService);
                        field.setAccessible(true); //将这个私有变量设为能够赋值的
                        try {
                            field.set(instance, serviceInstance); //第一个参数是这个变量属于的controller对象，第二个参数是要注入的service对象
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }
        }
    }

    /**
     * 2.反射实例化
     */
    private void executeInstance() {
        for (String className : classNames) {
            String cn = className.replace(".class", "");
            try {
                Class<?> clazz = Class.forName(cn);
                //判断带注解的.class文件
                if (clazz.isAnnotationPresent(ColaController.class)) {
                    Object instanceController = clazz.newInstance();//反射创建实例
                    ColaRequestMapping rm = clazz.getAnnotation(ColaRequestMapping.class);//得到这个controller类上边的注解
                    String keyController = rm.value();// 这个key是ColaRequestMapping中的值
                    beans.put(keyController, instanceController); //  将ColaController类作为value存入ioc容器中 使用ColaRequestMapping中的值cola作为key
                } else if (clazz.isAnnotationPresent(ColaService.class)) {
                    Object instanceService = clazz.newInstance();
                    ColaService cs = clazz.getAnnotation(ColaService.class);
                    String keyService = cs.value();
                    beans.put(keyService, instanceService);
                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 1.包扫描（找到带有注解的类）
     *
     * @param scanPackage
     */
    private void executeScanPackage(String scanPackage) {
        //url = file:/C:/z-app/code/springmvc_cola/target/classes/com/springmvc
        //URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.", "/"));//根据debug这样写得到的是url为null 把前边/去掉即可
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));//通过com/springmvc得到url

        //fileStr = /C:/z-app/code/springmvc_cola/target/classes/com/springmvc
        String fileStr = url.getFile();

        File file = new File(fileStr); // file = C:\z-app\code\springmvc_cola\target\classes\com\springmvc
        String[] fileList = file.list();
        for (String path : fileList) { //fileList=[ annotation,controller,service,sevlet ]
            File filePath = new File(fileStr + "/" + path);
            if (filePath.isDirectory()) {
                //不是.class文件递归循环
                executeScanPackage(scanPackage + "." + path);
            } else {
                //进证明是.class文件 (这里是将com.springmvc包下的所有class文件都添加到了集合中)
                classNames.add(scanPackage + "." + filePath.getName()); //com.springmvc.xxx.class
            }
        }
    }

    private static Object[] hand(HttpServletRequest request, HttpServletResponse response, Method method) {
        //拿到当前方法都有哪些参数
        Class<?>[] paramClazzs = method.getParameterTypes();

        //根据参数的个数 new一个参数的数组 将方法里的所有参数复制到args
        Object[] args = new Object[paramClazzs.length];

        int args_i = 0;
        int index = 0;
        for (Class<?> paramClazz : paramClazzs) {
            if (ServletRequest.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = response;
            }
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if (paramAns.length > 0) {
                for (Annotation paramAn : paramAns) {
                    if (ColaParam.class.isAssignableFrom(paramAn.getClass())) {
                        ColaParam cp = (ColaParam) paramAn;
                        args[args_i++] = request.getParameter(cp.value());
                    }
                }
            }
            index++;
        }
        return args;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获得到请求url
        String requestURI = req.getRequestURI(); // springmvc_cola/cola/write
        //String context = req.getContextPath(); // springmvc_cola   //这里有一个问题 得不到项目名 根据debug得到的是一个"" 所以下边直接手动写入
        String path = requestURI.replace("/springmvc_cola", "");//   cola/write
        Method method = (Method) handlerMap.get(path);
        UserController userController = (UserController) beans.get("/" + path.split("/")[1]);
        Object[] args = hand(req, resp, method);
        try {
            method.invoke(userController,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }
}
