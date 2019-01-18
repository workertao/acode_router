package com.bjx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yt on 2019/1/14.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface BjxRouter {
    //路由地址
    String path();

    //将路由节点进行分组，可以实现按组动态加载
    String group() default "";
}
