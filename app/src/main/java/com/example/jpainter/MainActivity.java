package com.example.jpainter;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import com.example.sketchpad.SketchpadView;
import com.example.sketchpad.utils.CommonUtils;

public class MainActivity extends AppCompatActivity {

    SketchpadView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mView = findViewById(R.id.iv_image);
        mView.setBitmap(Bitmap.createBitmap(CommonUtils.getScreenWidth(this), CommonUtils.getScreenHeight(this) - CommonUtils.dip2px(this, 106), Bitmap.Config.ARGB_8888));
        mView.setOnItemClickInterface(new SketchpadView.OnItemClickInterface() {
            @Override
            public void OnCancelClick(boolean isEmpty) {
                if (isEmpty) {
                    mView.resetImage();
                    mView.clear();
                } else {
                    toastShow("弹窗提示");
                    mView.saveAllBackAllOpt();
                    mView.resetImage();
                    mView.clear();
                }
            }


            @Override
            public void OnClearAllClick() {
                if (!mView.getOptItemList().isEmpty() || !mView.getOptItemStack().isEmpty()) {
                    mView.clearAllBackAllOpt();
                    mView.resetImage();
                    mView.clear();
                }
            }
        });
    }

    private void toastShow(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }
}