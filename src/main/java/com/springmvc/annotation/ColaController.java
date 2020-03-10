package com.springmvc.annotation;


import java.lang.annotation.*;

@Target(ElementType.TYPE)  //只能够再类上使用
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColaController {
    String value() default "";
}
