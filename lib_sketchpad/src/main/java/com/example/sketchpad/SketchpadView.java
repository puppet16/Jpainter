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
public class SketchpadView extends FrameLayout implements View.OnClickListener {

    private CanvasView mCanvasView;
    private TextView mTvCancel;
    private Button mBtnClearAll;
    private Button mBtnRubber;
    private Button mBtnDoodle;
    private CanvasManager mHelp;
    private boolean mIsRubberSelected;

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
        mCanvasView = rootView.findViewById(R.id.doodleView);
        mTvCancel = rootView.findViewById(R.id.tv_cancel);
        mBtnRubber = rootView.findViewById(R.id.btn_rubber);
        mBtnClearAll = rootView.findViewById(R.id.btn_claer_all);
        mBtnDoodle = rootView.findViewById(R.id.btn_doodle);
        mBtnDoodle.setOnClickListener(this);
        mBtnClearAll.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
        mBtnRubber.setOnClickListener(this);
        mHelp = mCanvasView.getCanvasManager();
        mIsRubberSelected = false;
        onModeClick(IOptionMode.DOODLE);
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        if (vid == R.id.btn_rubber) {
            if (mIsRubberSelected) {
                onModeClick(IOptionMode.DOODLE);
            } else {
                onModeClick(IOptionMode.RUBBER);
            }
            mIsRubberSelected = !mIsRubberSelected;
            updateRubberUI();
        } else if (vid == R.id.tv_cancel) {
            onCancelClick();
        } else if (vid == R.id.btn_claer_all) {
            if (onItemClickInterface != null) {
                mIsRubberSelected = false;
                updateRubberUI();
                onItemClickInterface.OnClearAllClick();
            }
        }
    }

    private void updateRubberUI() {
        mBtnRubber.setBackgroundResource(mIsRubberSelected ? R.mipmap.icon_sketchpad_opt_rubber_select : R.mipmap.icon_sketchpad_opt_rubber_normal);
    }

    public List<BaseOpt> getOptItemList() {
        if (mHelp != null) {
            return mHelp.getOptItemList();
        }
        return new ArrayList<>();
    }

    public Stack<BaseOpt> getOptItemStack() {
        if (mHelp != null) {
            return mHelp.getOptUndoStack();
        }
        return new Stack<BaseOpt>();
    }

    public void setAllOptList(List<BaseOpt> optList) {
        mCanvasView.clearAllOpt();
        mCanvasView.clearAllBackOPt();
        mCanvasView.setAllOptList(optList);
    }

    private OnItemClickInterface onItemClickInterface;


    public interface OnItemClickInterface {
        void OnCancelClick(boolean isEmpty);

        void OnClearAllClick();
    }

    public List<BaseOpt> getAllBackOptList() {
        return mHelp.getAllBackOptList();
    }

    /**
     * 保存所有回退操作
     */
    public void saveAllBackAllOpt() {
        mCanvasView.saveAllBackOpt();
    }

    public void setBitmap(Bitmap image) {
        mCanvasView.clearAllOpt();
        mCanvasView.clearAllBackOPt();
        mCanvasView.setBitmap(image);
    }

    /**
     * 清空所有回退操作
     */
    public void clearAllBackAllOpt() {
        mCanvasView.clearAllBackOPt();
    }

    /**
     * 点击取消
     */
    private void onCancelClick() {
        if (onItemClickInterface != null) {
            onItemClickInterface.OnCancelClick(mHelp.getOptItemList().isEmpty());
        }
    }

    public void setCanvasSize(int width, int height) {
        mHelp.setCanvasSize(width, height);
    }

    private void onModeClick(@IOptionMode.Mode int mode) {
        int cm = mCanvasView.getMode();
        if (cm == mode) {
            mode = IOptionMode.NONE;
        }
        mCanvasView.setMode(mode);
    }

    /**
     * 切换为涂鸦模式
     */
    public void changeModeToDoodle() {
        onModeClick(IOptionMode.DOODLE);
    }

    public void clear() {
        mCanvasView.clearAllOpt();
        onModeClick(IOptionMode.NONE);
    }

    public void setOnItemClickInterface(OnItemClickInterface onItemClickInterface) {
        this.onItemClickInterface = onItemClickInterface;
    }

    public void resetImage() {
        mCanvasView.reset();
    }
}
