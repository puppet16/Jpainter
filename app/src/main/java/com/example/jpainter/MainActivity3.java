package com.example.jpainter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sketchpad.SketchpadView;
import com.example.sketchpad.utils.CommonUtils;

public class MainActivity3 extends AppCompatActivity implements View.OnClickListener {

    private int mNumberId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                startActivity(new Intent(MainActivity3.this, MainActivity.class));
                break;
            case R.id.btn2:
                SketchpadViewDialog dialog = SketchpadViewDialog.instance(String.valueOf(mNumberId));
                dialog.show(getSupportFragmentManager(), "sketchpad");
                break;
            case R.id.btn3:
                mNumberId++;
                break;
            case R.id.btn4:
                mNumberId--;
                break;
        }
    }
}