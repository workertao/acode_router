package com.bjx.community.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bjx.annotation.BjxRouter;
import com.bjx.community.BuildConfig;
import com.bjx.community.R;
import com.bjx.router.PostCard;
import com.bjx.router.callback.NavigationCallback;
import com.bjx.router.router.BjxSuperRouter;


/**
 * Created by yt on 2019/1/4.
 */
@BjxRouter(path = "/bjxCommunity/BjxCommunityActivity")
public class BjxCommunityActivity extends Activity {
    private TextView tvText;
    private Button btnGotoNews;
    private Button btnGotoNewsForResult;
    private Button btnError;
    private TextView btnBackNews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bjx_community);
        initView();
    }

    private void initView() {
        tvText = findViewById(R.id.tvText);
        btnGotoNews = findViewById(R.id.btnGotoNews);
        btnGotoNewsForResult = findViewById(R.id.btnGotoNewsForResult);
        btnError = findViewById(R.id.btnError);
        btnBackNews = findViewById(R.id.btnBackNews);
        if (BuildConfig.isMoudle) {
            tvText.setText("单独运行--社区");
        } else {
            tvText.setText("libary--社区");
        }
        btnGotoNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BjxSuperRouter.getInstance().build("/bjxNews/BjxNewActivity").navigation(BjxCommunityActivity.this);
            }
        });
        btnGotoNewsForResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("name", "古力娜扎-巴扎黑");
                bundle.putInt("age", 458853);
                BjxSuperRouter.getInstance().build("/bjxNews/BjxNewActivity").withBundle(bundle).navigation(BjxCommunityActivity.this, 203, new NavigationCallback() {
                    @Override
                    public void onFound(PostCard postcard) {
                        Log.d("router","onFound:"+postcard.toString());
                    }

                    @Override
                    public void onLost(PostCard postcard) {
                        Log.d("router","onLost:"+postcard.toString());
                    }

                    @Override
                    public void onArrival(PostCard postcard) {
                        Log.d("router","onArrival:"+postcard.toString());
                    }
                });
            }
        });
        btnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BjxSuperRouter.getInstance().build("/a/b").navigation(BjxCommunityActivity.this, -1, new NavigationCallback() {
                    @Override
                    public void onFound(PostCard postcard) {
                        Log.d("router","onFound:"+postcard.toString());
                    }

                    @Override
                    public void onLost(PostCard postcard) {
                        Log.d("router","onLost:"+postcard.toString());
                    }

                    @Override
                    public void onArrival(PostCard postcard) {
                        Log.d("router","onArrival:"+postcard.toString());
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String result = data.getStringExtra("result");
            btnBackNews.setVisibility(View.VISIBLE);
            btnBackNews.setText(result);
        }
    }
}
