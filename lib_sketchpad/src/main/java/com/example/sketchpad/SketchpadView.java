package com.example.sketchpad;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sketchpad.option.BaseOpt;
import com.example.sketchpad.option.IOptionMode;
import com.example.sketchpad.utils.CanvasManager;
import com.example.sketchpad.utils.IMGHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/22
 * desc:
 * ============================================================
 **/
public class SketchpadView extends FrameLayout implements View.OnClickListener  {

    private CanvasView imgView;
    private TextView tvCancel;
    private Button tvClearAll, tvRubber, tvDoodle;
    private CanvasManager mHelp;


    public SketchpadView(@NonNull Context context) {
        this(context, null);
    }

    public SketchpadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SketchpadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.view_sketchpad, this, true);
        imgView = rootView.findViewById(R.id.dooleView2);
        tvCancel = rootView.findViewById(R.id.tv_cancel);
        tvRubber = rootView.findViewById(R.id.tv_rubber);
        tvClearAll = rootView.findViewById(R.id.tv_claer_all);
        tvDoodle = rootView.findViewById(R.id.tv_doodle);
        tvClearAll.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvRubber.setOnClickListener(this);
        tvDoodle.setOnClickListener(this);
        mHelp = imgView.getCanvasManager();

    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        if (vid == R.id.tv_rubber) {
            onModeClick(IOptionMode.RUBBER);
        }else if (vid == R.id.tv_cancel) {
            onCancelClick();
        }else if (vid == R.id.tv_doodle) {
            onModeClick(IOptionMode.DOODLE);
        }else if (vid == R.id.tv_claer_all) {
            if(onItemClickInterface!=null){
                onItemClickInterface.OnClearAllClick();
            }
        }
    }

    public List<BaseOpt> getOptItemList(){
        if(mHelp!=null){
            return mHelp.getOptItemList();
        }
        return new ArrayList<>();
    }

    public Stack<BaseOpt> getOptItemStack(){
        if(mHelp !=null ){
            return mHelp.getOptUndoStack();
        }
        return new Stack<BaseOpt>();
    }


    private OnItemClickInterface onItemClickInterface;


    public interface OnItemClickInterface {
        void OnCancelClick(boolean isEmpty);
        void OnClearAllClick();
    }

    public List<BaseOpt> getAllBackOptList(){
        return mHelp.getAllBackOptList();
    }

    /**
     * 保存所有回退操作
     */
    public void saveAllBackAllOpt(){
        imgView.saveAllBackOpt();
    }

    public void setBitmap(Bitmap image) {
        imgView.clearAllOpt();
        imgView.clearAllBackOPt();
        imgView.setBitmap(image);
    }
    /**
     * 清空所有回退操作
     */
    public void clearAllBackAllOpt(){
        imgView.clearAllBackOPt();
    }

    /**
     * 点击取消
     */
    private void onCancelClick() {
        if (onItemClickInterface != null) {
            onItemClickInterface.OnCancelClick(mHelp.getOptItemList().isEmpty() && mHelp.getOptItemList().isEmpty());
        }
    }

    private void onModeClick(@IOptionMode.Mode int mode) {
        int cm = imgView.getMode();
        if (cm == mode) {
            mode = IOptionMode.NONE;
        }
        imgView.setMode(mode);
    }

    public void clear(boolean isAnima) {
        imgView.clearAllOpt();
        onModeClick(IOptionMode.NONE);
    }

    public void setOnItemClickInterface(OnItemClickInterface onItemClickInterface) {
        this.onItemClickInterface = onItemClickInterface;
    }

    public void resetImage() {
        imgView.reset();
    }
}
