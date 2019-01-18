package com.bjx.router;

import com.bjx.annotation.bean.RouteMeta;
import com.bjx.router.template.IRouterGroup;
import com.bjx.router.template.IRouterRoot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yt on 2019/1/16.
 */

public class Warehouse {
    //运行时的根路由表
    public static Map<String, Class<? extends IRouterGroup>> rootMap = new HashMap<>();

    //运行时的分组内的路由信息
    public static Map<String, RouteMeta> groupMap = new HashMap<>();
}
