package com.example.myappocr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class spleshActivity extends AppCompatActivity {

    int spleshtime=3000;  //milisecond

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splesh);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent intent=new Intent(spleshActivity.this,MainActivity.class);
                startActivity(intent);
                finish();




            }
        },spleshtime);

    }
}