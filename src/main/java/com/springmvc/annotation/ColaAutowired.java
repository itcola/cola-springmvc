package com.springmvc.annotation;


import java.lang.annotation.*;

@Target(ElementType.FIELD) //只能再类成员变量上使用
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColaAutowired {
    String value() default "";
}
