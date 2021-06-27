package com.example.downloadingdemov1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {
    Button button;
    TextView textView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textView = findViewById(R.id.text20);
        button = findViewById(R.id.button11);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        textView.setText("接收到的数据为："+name);
        button.setOnClickListener(v -> {
            intent.putExtra("result","Hello，依然范特西稀，我是回传的数据！");
            setResult(Activity.RESULT_OK,intent);
            finish();
        });
    }
}