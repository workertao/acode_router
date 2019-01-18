package com.bjx.toutiao;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import com.bjx.annotation.BjxRouter;
import com.bjx.community.activity.BjxCommunityActivity;
import com.bjx.news.activity.BjxNewActivity;



@BjxRouter(path = "/main/MainActivity")
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btn1;
    private Button btn2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn1:
                startActivity(new Intent(this,BjxNewActivity.class));
                break;
            case R.id.btn2:
                startActivity(new Intent(this,BjxCommunityActivity.class));
                break;
        }
    }
}
