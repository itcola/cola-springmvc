package com.springmvc.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD}) //能够再类上  方法上使用
@Retention(RetentionPolicy.RUNTIME) //运行期间可以通过反射获取
@Documented
public @interface ColaRequestMapping {
    String value() default "";
}
