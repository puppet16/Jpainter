package com.example.sketchpad.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;

import com.example.sketchpad.CanvasPath;
import com.example.sketchpad.option.BaseOpt;
import com.example.sketchpad.option.IOptionMode;
import com.example.sketchpad.option.PathOpt;
import com.example.sketchpad.option.RubberOpt;

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
public class CanvasManager {
    /**
     * 所有操作记录
     */
    protected List<BaseOpt> mOptItemList = new ArrayList<BaseOpt>();

    /**
     * 回退保存的所有操作
     */
    protected List<BaseOpt> mOptClearList = new ArrayList<BaseOpt>();
    /*
     * 所有undo的操作记录，也即为可进行redo操作的数据栈
     */
    private Stack<BaseOpt> optUndoStack = new Stack<>();

    /**
     * 完整图片边框
     */
    private RectF mFrame = new RectF();
    /**
     * 可视区域，无Scroll 偏移区域
     */
    private RectF mWindow = new RectF();
    /**
     * 涂鸦画笔
     */
    private Paint mDoodlePaint;
    /**
     * 画布上图像
     */
    private Bitmap mImage;
    /**
     * 画布矩阵
     */
    private Matrix M = new Matrix();
    /**
     * 编辑模式
     */
    private @IOptionMode.Mode int mMode = IOptionMode.NONE;

    private final Bitmap DEFAULT_IMAGE = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    /**
     * 是否初始位置
     */
    private boolean isInitialHoming = false;

    /**
     * 设置画布大小
     */
    public void setCanvasSize(int width, int height) {
        setBitmap(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
    }

    /**
     * 设置涂鸦画笔
     */
    public void setDoodlePaint( Paint doodlePaint) {
        mDoodlePaint = doodlePaint;
    }

    /**
     * 设置显示图像
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        //如果传入的bitmap为null，那么创建一个空的纯色bitmap
        if (bitmap == null || bitmap.isRecycled()) {
            ColorDrawable drawable = new ColorDrawable(Color.parseColor("#000000"));
            bitmap = Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
        }

        mImage = bitmap;
        onImageChanged();
    }

    private void onImageChanged() {
        isInitialHoming = false;
        onWindowChanged(mWindow.width(), mWindow.height());
    }

    public void onWindowChanged(float width, float height) {
        if (width == 0 || height == 0) {
            return;
        }
        mWindow.set(0, 0, width, height);
        if (!isInitialHoming) {
            mFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());
            isInitialHoming = true;
        } else {
            M.mapRect(mFrame);
        }
    }

    public RectF getWindow() {
        return mWindow;
    }

    public void setInitialHoming(boolean initialHoming) {
        this.isInitialHoming = initialHoming;
    }

    public List<BaseOpt> getAllBackOptList(){
        return mOptClearList;
    }

    /**
     * 清空所有回退的操作
     */
    public void clearAllBackOPt(){
        if(mOptClearList!=null && !mOptClearList.isEmpty()){
            mOptClearList.clear();
        }
    }

    /**
     * 保存所有回退的操作
     */
    public void saveAllBackOPt(){
        if(mOptClearList==null){
            mOptClearList = new ArrayList<>();
        }
        if(mOptItemList!=null && !mOptItemList.isEmpty()){
            mOptClearList.addAll(mOptItemList);
        }
    }

    public @IOptionMode.Mode int getMode() {
        return mMode;
    }

    public void setMode(@IOptionMode.Mode int mode) {
        if (this.mMode == mode) {
            return;
        }
        //只要切换模式就把redo栈清空，即只允许一个模式下的redo操作
        clearRedoStack();
        //更新redo和undo按钮的UI状态
        updateUndoOpt();
        this.mMode = mode;
    }

    /**
     * 清空redo栈，即清空回退操作保存栈，也就不能回退“回退(undo)操作”
     */
    private void clearRedoStack() {
        optUndoStack.clear();
    }

    /**
     * 更新undo和redo的按钮UI
     */
    public void updateUndoOpt(){
        if(mUpdateUndoViewInterface !=null){
            mUpdateUndoViewInterface.onUpdateUndoView(isUndoEnabled());
            mUpdateUndoViewInterface.onUpdateRedoView(!optUndoStack.isEmpty());
        }
    }

    /**
     * 判断是否可以进行undo操作，若可以，则undoView应置为可用状态
     * 规则：只要操作列表里有一个操作的isRemoved为false，则，undo就可用
     * @return true|false
     */
    private boolean isUndoEnabled() {
        if (null == mOptItemList || 0 == mOptItemList.size()) {
            return false;
        }
        for (BaseOpt opt : mOptItemList) {
            if (!opt.isRemoved()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取涂鸦路径，在橡皮擦模式下调用
     * @return
     */
    public List<CanvasPath> getDoodles(){
        List<CanvasPath> CanvasPaths = new ArrayList<>();
        for (BaseOpt baseOpt:mOptItemList){
            if(baseOpt instanceof PathOpt && !baseOpt.isRemoved()){
                CanvasPaths.add(((PathOpt) baseOpt).getPath());
            }
        }

        return CanvasPaths;
    }

    /**
     * 删除其中某一个涂鸦线条
     * @param CanvasPath
     */
    public void undoDoodleByItem(CanvasPath CanvasPath) {
        BaseOpt removed = null;
        if(!mOptItemList.isEmpty()){
            for (BaseOpt opt : mOptItemList) {
                if (opt instanceof PathOpt && ((PathOpt) opt).getPath() == CanvasPath) {
                    removed = opt;
                    opt.setRemoved(true);
                    break;
                }
            }
        }

        // 删除操作添加记录
        if (null == removed) {
            return;
        }
        RubberOpt removeOpt = new RubberOpt();
        removeOpt.setCurAngle(0);
        removeOpt.setRemovedOpt(removed);
        mOptItemList.add(removeOpt);
        clearRedoStack();
        updateUndoOpt();
    }

    /**
     * 添加路径
     * @param path
     * @param sx
     * @param sy
     * @param addOpt
     */
    public void addPath(CanvasPath path, float sx, float sy, boolean addOpt) {
        if (path == null) return;

        path.transform(M);
        Matrix m1 = new Matrix();
        for (CanvasPath.PathPoint point : path.getPathPointList()) {
            float[] p = {point.getX(), point.getY()};
            m1.mapPoints(p);
            point.setX(p[0]);
            point.setY(p[1]);
        }

        if (addOpt) {
            //画线操作添加记录
            PathOpt pathOpt = new PathOpt();
            pathOpt.setCurAngle(0);
            pathOpt.setPath(path);
            mOptItemList.add(pathOpt);
            clearRedoStack();
            updateUndoOpt();
        }
    }

    /**
     * 清除所有操作
     */
    public void clearAllOpt(){
        if(mOptItemList!=null){
            mOptItemList.clear();
        }
        if(optUndoStack !=null){
            optUndoStack.clear();
        }
        updateUndoOpt();
    }

    /**
     * 后退操作undo
     */
    public float undoViewClick(){
        float result = 0;
        if(mOptItemList!=null && mOptItemList.size()>0){
            BaseOpt baseOpt = mOptItemList.get(mOptItemList.size()-1);

            optUndoStack.push(baseOpt);
            mOptItemList.remove(mOptItemList.size()-1);
            if (baseOpt instanceof RubberOpt){
                ((RubberOpt) baseOpt).getRemovedOpt().setRemoved(false);
            }
        }
        updateUndoOpt();
        return result;
    }

    /**
     * redo操作
     */
    public float redoViewClick(){
        float result = 0;
        if(optUndoStack !=null && optUndoStack.size()>0){
            BaseOpt baseOpt = optUndoStack.pop();
            mOptItemList.add(baseOpt);
            if (baseOpt instanceof RubberOpt){
                ((RubberOpt) baseOpt).getRemovedOpt().setRemoved(true);
            }
        }
        updateUndoOpt();
        return result;
    }


    /**
     * 根据添加顺序绘制不同操作
     * @param canvas
     */
    public void onDrawItemList(Canvas canvas,boolean isShowClear){
        //如果所有回退操作不为空先画回退操作
        if(isShowClear){
            if(mOptClearList!=null && !mOptClearList.isEmpty()){
                for (BaseOpt baseOpt:mOptClearList){
                    if (baseOpt.isRemoved()) {
                        continue;
                    }

                    if(baseOpt instanceof PathOpt){
                        //绘制涂鸦
                        //onDrawDoodles(canvas);
                        onDrawDoodles(baseOpt,canvas);
                    }
                }
            }
        }

        for (BaseOpt baseOpt:mOptItemList){
            if (baseOpt.isRemoved()) {
                continue;
            }

            if(baseOpt instanceof PathOpt){
                //绘制涂鸦
                //onDrawDoodles(canvas);
                onDrawDoodles(baseOpt,canvas);
            }
        }
    }

    /**
     * 涂鸦绘制
     * @param baseOpt
     * @param canvas
     */
    private void onDrawDoodles(BaseOpt baseOpt, Canvas canvas){
        if(baseOpt instanceof PathOpt){
            CanvasPath path = ((PathOpt) baseOpt).getPath();
            canvas.save();
            canvas.translate(mFrame.left, mFrame.top);
            try {
                mDoodlePaint.setColor(path.getColor());
                mDoodlePaint.setStrokeWidth(path.getWidth());
                // rewind
                canvas.drawPath(path.getPath(), mDoodlePaint);
            } catch (Exception e) {
                e.printStackTrace();
            }
            canvas.restore();
        }
    }
    /**
     * 绘制图片
     * @param canvas
     */
    public void onDrawImage(Canvas canvas) {
        // 绘制图片
        canvas.drawBitmap(mImage, null, mFrame, null);
    }

    public List<BaseOpt> getOptItemList() {
        return mOptItemList;
    }

    public Stack<BaseOpt> getOptUndoStack() {
        return optUndoStack;
    }

    public void release() {
        if (mImage != null && !mImage.isRecycled()) {
            mImage.recycle();
        }
    }

    public interface UpdateUndoViewInterface {
        void onUpdateUndoView(boolean isSelected);
        void onUpdateRedoView(boolean isSelected);
    }

    private UpdateUndoViewInterface mUpdateUndoViewInterface;

    public void setUpdateUndoViewInterface(UpdateUndoViewInterface mUpdateUndoViewInterface){
        this.mUpdateUndoViewInterface = mUpdateUndoViewInterface;
    }
}
