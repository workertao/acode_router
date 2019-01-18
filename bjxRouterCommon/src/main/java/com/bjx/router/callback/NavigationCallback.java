package com.bjx.router.callback;

import com.bjx.router.PostCard;

/**
 * Created by yt on 2019/1/17.
 */

public interface NavigationCallback {

    /**
     * Callback when find the destination.
     *
     * @param postcard meta
     */
    void onFound(PostCard postcard);

    /**
     * Callback after lose your way.
     *
     * @param postcard meta
     */
    void onLost(PostCard postcard);

    /**
     * Callback after navigation.
     *
     * @param postcard meta
     */
    void onArrival(PostCard postcard);

}
