package com.bjx.router.router;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bjx.annotation.bean.RouteMeta;
import com.bjx.router.PostCard;
import com.bjx.router.Warehouse;
import com.bjx.router.callback.NavigationCallback;
import com.bjx.router.data.Config;
import com.bjx.router.exception.NoRouteFoundException;
import com.bjx.router.template.IRouterGroup;
import com.bjx.router.template.IRouterRoot;
import com.bjx.router.utils.ClassUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Created by yt on 2019/1/16.
 */

public class BjxSuperRouter {
    private static BjxSuperRouter bjxSuperRouter;

    private static Application context;

    private static Handler mHandler;

    public static BjxSuperRouter getInstance() {
        if (bjxSuperRouter == null) {
            bjxSuperRouter = new BjxSuperRouter();
        }
        return bjxSuperRouter;
    }

    //初始化
    public static void init(Application va1) throws InterruptedException, IOException, PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        context = va1;
        mHandler = new Handler(Looper.getMainLooper());
        //获取所有apt生成的路由文件  group 和 root
        Set<String> routerMap = ClassUtils.getFileNameByPackageName(context, Config.APT_PACKAGE_NAME);
        Log.d("post", "路由表：" + routerMap.size());
        for (String className : routerMap) {
            if (className.startsWith(Config.APT_PACKAGE_NAME + "." + Config.APT_ROOT_CLASS_NAME)) {
                // root中注册的是分组信息 将分组信息加入仓库中
                ((IRouterRoot) (Class.forName(className).getConstructor().newInstance())).loadRootMap(Warehouse.rootMap);
            }
        }
    }

    /**
     * 路由跳转
     *
     * @param path
     * @return
     */
    public PostCard build(String path) {
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(context, "路由地址不能为空", Toast.LENGTH_LONG).show();
        }
        return build(path, extractGroup(path));
    }

    /**
     * 分组+路由地址跳转
     *
     * @param path
     * @param group
     * @return
     */
    public PostCard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            Toast.makeText(context, "路由地址为空||分组为空", Toast.LENGTH_LONG).show();
        }
        return new PostCard(path, group);
    }

    /**
     * 跳转到某一个页面
     *
     * @param va1         上下文
     * @param postCard    跳转的数据
     * @param requsetCode requsetCode
     * @param callback    callback
     * @return
     */
    public Object navigation(Context va1, final PostCard postCard, final int requsetCode, final NavigationCallback callback) {
        try {
            completion(postCard);
        } catch (NoRouteFoundException e) {
            //没找到
            Toast.makeText(va1, "没有找到路由地址:" + postCard.getPath(), Toast.LENGTH_LONG).show();
            if (null != callback) {
                callback.onLost(postCard);
            }
            return this;
        }
        if (null != callback) {
            callback.onFound(postCard);
        }
        final Context currentContext = null == va1 ? context : va1;
        switch (postCard.getType()) {
            case ACTIVITY:
                final Intent intent = new Intent(currentContext, postCard.getDestination());
                if (postCard.getExtras() != null) {
                    intent.putExtras(postCard.getExtras());
                }
                // 设置 flags.
                int flags = postCard.getFlags();
                if (-1 != flags) {
                    intent.setFlags(flags);
                } else if (!(currentContext instanceof Activity)) {    // Non activity, need less one flag.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                // 设置 Actions
                String action = postCard.getAction();
                if (!TextUtils.isEmpty(action)) {
                    intent.setAction(action);
                }
                // 进入主线程跳转
                runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(requsetCode, currentContext, intent, postCard, callback);
                    }
                });
                break;
        }

        return this;
    }


    /**
     * 数据处理
     *
     * @param postCard
     */
    public void completion(PostCard postCard) {
        if (null == postCard) {
            Toast.makeText(context, "postCard为空", Toast.LENGTH_LONG).show();
            return;
        }
        //获取路由表中对应的路由实体信息RouteMeta
        RouteMeta routeMeta = Warehouse.groupMap.get(postCard.getPath());
        if (routeMeta == null) {
            //先根据分组在根路由表中找到对应的class
            Class<? extends IRouterGroup> rootClazz = Warehouse.rootMap.get(postCard.getGroup());
            if (rootClazz == null) {
                throw new NoRouteFoundException("找不到根路由表中的class");
            }
            try {
                //找到对应的class后，构造出接口，然后将数据添加到Warehouse中
                IRouterGroup iRouterGroup = rootClazz.getConstructor().newInstance();
                iRouterGroup.loadGroupMap(Warehouse.groupMap);
                Warehouse.rootMap.remove(postCard.getGroup());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //再次调用并且赋值，走else
            completion(postCard);
        } else {
            postCard.setDestination(routeMeta.getDestination());
            postCard.setType(routeMeta.getType());
        }
    }

    /**
     * Be sure execute in main thread.
     *
     * @param runnable code
     */
    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * startActivity
     *
     * @param requestCode
     * @param currentContext
     * @param intent
     * @param postCard
     * @param callback
     */
    private void startActivity(int requestCode, Context currentContext, Intent intent, PostCard postCard, NavigationCallback callback) {
        if (requestCode >= 0) {  // Need start for result
            if (currentContext instanceof Activity) {
                ActivityCompat.startActivityForResult((Activity) currentContext, intent, requestCode, postCard.getOptionsBundle());
            } else {
                Toast.makeText(currentContext, "跳转的必须是activity", Toast.LENGTH_LONG).show();
            }
        } else {
            ActivityCompat.startActivity(currentContext, intent, postCard.getOptionsBundle());
        }
        if (null != callback) { // Navigation over.
            callback.onArrival(postCard);
        }
    }

    /**
     * 将组信息提取出来
     *
     * @param path
     * @return
     */
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException(path + " : 不能提取group.");
        }
        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new RuntimeException(path + " : 不能提取group.");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
