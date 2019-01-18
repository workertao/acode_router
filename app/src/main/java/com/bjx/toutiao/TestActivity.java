package com.bjx.toutiao;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bjx.annotation.BjxRouter;


/**
 * Created by Administrator on 2019/1/14.
 */
@BjxRouter(path = "/app/TestActivity")
public class TestActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
