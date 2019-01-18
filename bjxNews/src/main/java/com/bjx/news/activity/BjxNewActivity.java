package com.bjx.news.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.bjx.annotation.BjxRouter;
import com.bjx.news.BuildConfig;
import com.bjx.news.R;


/**
 * Created by yt on 2019/1/4.
 */
@BjxRouter(path = "/bjxNews/BjxNewActivity")
public class BjxNewActivity extends Activity {
    private TextView tvNews;
    private TextView tvBundleData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bjx_news);
        initView();
        Intent intent = new Intent();
        intent.putExtra("result", "返回success");
        setResult(RESULT_OK, intent);
    }
    //接收数据
    private void getData() {
        if (getIntent().getExtras()==null) {
            return;
        }
        String name = getIntent().getStringExtra("name");
        int age = getIntent().getIntExtra("age", -1);
        tvBundleData.setText("接收到的参数：name:" + name + "    age:" + age);
    }

    private void initView() {
        tvNews = findViewById(R.id.tvNews);
        tvBundleData = findViewById(R.id.tvBundleData);
        if (BuildConfig.isMoudle) {
            tvNews.setText("单独运行--新闻");
        } else {
            tvNews.setText("libary--新闻");
        }
        getData();
    }
}
