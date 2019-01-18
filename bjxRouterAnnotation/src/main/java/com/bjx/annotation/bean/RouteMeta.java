package com.bjx.annotation.bean;

import com.bjx.annotation.BjxRouter;

import javax.lang.model.element.Element;


/**
 * Created by yt on 2019/1/14.
 */

public class RouteMeta {
    public enum Type {
        ACTIVITY,
        ISERVICE
    }

    //类型
    private Type type;
    private Element element;
    //路由地址
    private String path;
    //路由分组
    private String group;
    //类信息
    private Class<?> destination;

    public RouteMeta() {
    }

    public RouteMeta(Type type, Element element, BjxRouter bjxRouter) {
        this(type, element, bjxRouter.path(), bjxRouter.group(), null);
    }

    public static RouteMeta build(Type type, Class<?> destination, String path, String group) {
        return new RouteMeta(type, null, path, group, destination);
    }

    public RouteMeta(Type type, Element element, String path, String group, Class<?> destination) {
        this.type = type;
        this.element = element;
        this.path = path;
        this.group = group;
        this.destination = destination;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Class<?> getDestination() {
        return destination;
    }

    public void setDestination(Class<?> destination) {
        this.destination = destination;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return "RouteMeta{" +
                "type=" + type +
                ", element=" + element +
                ", path='" + path + '\'' +
                ", group='" + group + '\'' +
                ", destination=" + destination +
                '}';
    }
}
