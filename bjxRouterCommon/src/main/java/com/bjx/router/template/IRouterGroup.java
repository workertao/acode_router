package com.bjx.router.template;

import com.bjx.annotation.bean.RouteMeta;

import java.util.List;
import java.util.Map;

/**
 * Created by yt on 2019/1/14.
 * 保存路由信息
 */

public interface IRouterGroup {
    //存放路由实体信息
    void loadGroupMap(Map<String, RouteMeta> atlas);
}
