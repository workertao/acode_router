package com.bjx.router.template;

import com.bjx.router.template.IRouterGroup;

import java.util.Map;

/**
 * Created by yt on 2019/1/16.
 */

public interface IRouterRoot {
    ////存放路分组信息
    void loadRootMap(Map<String, Class<? extends IRouterGroup>> rootMap);
}
