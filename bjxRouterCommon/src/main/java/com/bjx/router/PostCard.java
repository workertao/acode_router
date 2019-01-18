package com.bjx.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;

import com.bjx.annotation.bean.RouteMeta;
import com.bjx.router.callback.NavigationCallback;
import com.bjx.router.router.BjxSuperRouter;

/**
 * Created by yt on 2019/1/16.
 */

public class PostCard extends RouteMeta {
    private Bundle bundle;
    //增加设置intent的action
    private String action;
    // Flags of route
    private int flags = -1;
    // The transition animation of activity
    private Bundle optionsCompat;

    public PostCard(String path, String group) {
        this(path, group, null);
    }

    public PostCard(String path, String group, Bundle bundle) {
        setPath(path);
        setGroup(group);
        this.bundle = bundle;
    }

    public Bundle getExtras() {
        return bundle;
    }

    public PostCard withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public String getAction() {
        return action;
    }

    public PostCard withAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Set special flags controlling how this intent is handled.  Most values
     * here depend on the type of component being executed by the Intent,
     * specifically the FLAG_ACTIVITY_* flags are all for use with
     * {@link Context#startActivity Context.startActivity()} and the
     * FLAG_RECEIVER_* flags are all for use with
     * {@link Context#sendBroadcast(Intent) Context.sendBroadcast()}.
     */
    public PostCard withFlags(int flag) {
        this.flags = flag;
        return this;
    }

    /**
     * Add additional flags to the intent (or with existing flags
     * value).
     *
     * @param flags The new flags to set.
     * @return Returns the same Intent object, for chaining multiple calls
     * into a single statement.
     * @see #withFlags
     */
    public PostCard addFlags(int flags) {
        this.flags |= flags;
        return this;
    }

    public int getFlags() {
        return flags;
    }

    public Bundle getOptionsBundle() {
        return optionsCompat;
    }

    @RequiresApi(16)
    public PostCard withOptionsCompat(ActivityOptionsCompat compat) {
        if (null != compat) {
            this.optionsCompat = compat.toBundle();
        }
        return this;
    }


    public Object navigation(Activity activity) {
       return navigation(activity, -1);
    }

    public Object navigation(Activity activity, int requsetCode) {
        return  navigation(activity, requsetCode, null);
    }

    public Object navigation(Activity activity, int requestCode, NavigationCallback callback) {
        return BjxSuperRouter.getInstance().navigation(activity, this, requestCode, callback);
    }

    @Override
    public String toString() {
        return "PostCard{" +
                "bundle=" + bundle +
                ", action='" + action + '\'' +
                ", flags=" + flags +
                ", optionsCompat=" + optionsCompat +
                '}';
    }
}
