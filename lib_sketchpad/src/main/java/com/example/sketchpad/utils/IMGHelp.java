package com.example.sketchpad.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;

import com.example.sketchpad.CanvasPath;
import com.example.sketchpad.CanvasView;
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
 * date: 2020/6/20
 * desc: 贴片文字操作
 * ============================================================
 **/
public class IMGHelp {

    private static final String TAG = "IMGHelp";

    private Bitmap mImage, mMosaicImage;

    /**
     * 完整图片边框
     */
    private RectF mFrame = new RectF();

    /**
     * 完整图片边框
     */
    private RectF firstFrame = new RectF();

    /**
     * 裁剪图片边框（显示的图片区域）
     */
    private RectF mClipFrame = new RectF();


    /**
     * 裁剪模式前状态备份
     */
    private RectF mBackupClipFrame = new RectF();

    private float mBackupClipRotate = 0;

    private float mRotate = 0, totalRotate=0;

    private Path mShade = new Path();

    /**
     * 编辑模式
     */
    private @IOptionMode.Mode int mMode = IOptionMode.NONE;

    private boolean isFreezing = mMode == IOptionMode.ROTATE;

    /**
     * 可视区域，无Scroll 偏移区域
     */
    private RectF mWindow = new RectF();

    /**
     * 是否初始位置
     */
    private boolean isInitialHoming = false;

    /**
     * 所有操作记录
     */
    private List<BaseOpt> optItemList = new ArrayList<BaseOpt>();

    /**
     * 回退保存的所有操作
     */
    private List<BaseOpt> optClearList = new ArrayList<BaseOpt>();


    /*
     * 所有后退的操作记录
     */
    private Stack<BaseOpt> optItemStack = new Stack<>();

    private static final int MIN_SIZE = 500;

    private static final int MAX_SIZE = 10000;

    private Paint mPaint, mMosaicPaint, mShadePaint;

    private Matrix M = new Matrix();

    private static final boolean DEBUG = false;

    private final Bitmap DEFAULT_IMAGE;

    private static final int COLOR_SHADE = 0xCC000000;


    {
        DEFAULT_IMAGE = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        mShade.setFillType(Path.FillType.WINDING);

        // Doodle&Mosaic 's paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(CanvasPath.BASE_DOODLE_WIDTH);
        mPaint.setColor(Color.RED);
        mPaint.setPathEffect(new CornerPathEffect(CanvasPath.BASE_DOODLE_WIDTH));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public IMGHelp() {
        mImage = DEFAULT_IMAGE;

        if (mMode == IOptionMode.ROTATE) {
            initShadePaint();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        //如果传入的bitmap为null，那么创建一个空的纯色bitmap
        if (bitmap == null || bitmap.isRecycled()) {
            ColorDrawable drawable = new ColorDrawable(Color.parseColor("#000000"));
            bitmap = Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
        }

        this.mImage = bitmap;

        // 清空马赛克图层
        if (mMosaicImage != null) {
            mMosaicImage.recycle();
        }
        this.mMosaicImage = null;

        makeMosaicBitmap();

        onImageChanged();
    }


    public List<BaseOpt> getAllBackOptList(){
        return optClearList;
    }

    /**
     * 清空所有回退的操作
     */
    public void clearAllBackOPt(){
        if(optClearList!=null && !optClearList.isEmpty()){
            optClearList.clear();
        }

    }

    /**
     * 保存所有回退的操作
     */
    public void saveAllBackOPt(){
        if(optClearList==null){
            optClearList = new ArrayList<>();
        }
        if(optItemList!=null && !optItemList.isEmpty()){
            optClearList.addAll(optItemList);
        }
    }

    public @IOptionMode.Mode int getMode() {
        return mMode;
    }

    public void setMode(@IOptionMode.Mode int mode) {

        if (this.mMode == mode) {
            return;
        }

        //只要切换模式就把栈清空
        clearRedoStack();
        undateUndoOpt();

        setFreezing(true);

        this.mMode = mode;

        if (mMode == IOptionMode.ROTATE) {

            // 初始化Shade 画刷
            initShadePaint();

            // 备份裁剪前Clip 区域
            mBackupClipRotate = getRotate();
            mBackupClipFrame.set(mClipFrame);

            float scale = 1 / getScale();
            M.setTranslate(-mFrame.left, -mFrame.top);
            M.postScale(scale, scale);
            M.mapRect(mBackupClipFrame);
        } else if (mMode == IOptionMode.RUBBER) {
            makeMosaicBitmap();
        }
    }

    private void initShadePaint() {
        if (mShadePaint == null) {
            mShadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mShadePaint.setColor(COLOR_SHADE);
            mShadePaint.setStyle(Paint.Style.FILL);
        }
    }

    public List<CanvasPath> getmDoodles(){
        List<CanvasPath> CanvasPaths = new ArrayList<>();
        for (BaseOpt baseOpt:optItemList){
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
        if(!optItemList.isEmpty()){
            for (BaseOpt opt : optItemList) {
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
        removeOpt.setCurAngle(totalRotate);
        removeOpt.setRemovedOpt(removed);
        optItemList.add(removeOpt);
        clearRedoStack();
        undateUndoOpt();
    }

    public RectF getClipFrame() {
        return mClipFrame;
    }

    public RectF getWindow() {
        return mWindow;
    }

    private void makeMosaicBitmap() {
        if (mMosaicImage != null || mImage == null) {
            return;
        }

        if (mMode == IOptionMode.RUBBER) {

            int w = Math.round(mImage.getWidth() / 64f);
            int h = Math.round(mImage.getHeight() / 64f);

            w = Math.max(w, 8);
            h = Math.max(h, 8);

            // 马赛克画刷
            if (mMosaicPaint == null) {
                mMosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mMosaicPaint.setFilterBitmap(false);
                mMosaicPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            }

            mMosaicImage = Bitmap.createScaledBitmap(mImage, w, h, false);
        }
    }

    private void onImageChanged() {
        isInitialHoming = false;
        onWindowChanged(mWindow.width(), mWindow.height());
    }

    public void setInitialHoming(boolean initialHoming) {
        this.isInitialHoming = initialHoming;
    }
    private void onInitialHoming(float width, float height) {
        mFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());
        firstFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());

        float centerX = mFrame.centerX();
        float centerY = mFrame.centerY();
        Log.i("center","centerX=3="+centerX);
        Log.i("center","centerY=3="+centerY);

        mClipFrame.set(mFrame);

        if (mClipFrame.isEmpty()) {
            return;
        }

        toBaseHoming();

        isInitialHoming = true;
    }

    private void toBaseHoming() {
        if (mClipFrame.isEmpty()) {
            // Bitmap invalidate.
            return;
        }

        float scale = Math.min(
                mWindow.width() / mClipFrame.width(),
                mWindow.height() / mClipFrame.height()
        );

        // Scale to fit window.
        M.setScale(scale, scale, mClipFrame.centerX(), mClipFrame.centerY());
        M.postTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
        M.mapRect(mFrame);
        M.mapRect(firstFrame);
        M.mapRect(mClipFrame);

        setScale(scale);
    }

    public RectF getFrame() {
        return mFrame;
    }

    public void addPath(CanvasPath path, float sx, float sy, boolean addOpt) {
        if (path == null) return;

        float scale = 1f / getScale();

        M.setTranslate(sx, sy);
        M.postRotate(-getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        M.postTranslate(-mFrame.left, -mFrame.top);
        M.postScale(scale, scale);
        path.transform(M);

        Matrix m1 = new Matrix();
        m1.postRotate(-getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        for (CanvasPath.PathPoint point : path.getPathPointList()) {
            float[] p = {point.getX(), point.getY()};
            m1.mapPoints(p);
            point.setX(p[0]);
            point.setY(p[1]);
        }

        if (addOpt) {
            //画线操作添加记录
            PathOpt pathOpt = new PathOpt();
            pathOpt.setCurAngle(totalRotate);
            pathOpt.setPath(path);
            optItemList.add(pathOpt);
            clearRedoStack();
            undateUndoOpt();
        }
    }

    public interface UpdateUndoViewInterface {
        void backViewClick(boolean isSelected);
        void goViewClick(boolean isSelected);
    }

    private UpdateUndoViewInterface updateUndoViewInterface;

    public void setUpdateUndoViewInterface(UpdateUndoViewInterface updateUndoViewInterface){
        this.updateUndoViewInterface = updateUndoViewInterface;
    }

    public void undateUndoOpt(){
        if(updateUndoViewInterface !=null){
            if(isUndoEmpty()){
                updateUndoViewInterface.backViewClick(false);
            }else {
                updateUndoViewInterface.backViewClick(true);
            }

            if(optItemStack.isEmpty()){
                updateUndoViewInterface.goViewClick(false);
            }else {
                updateUndoViewInterface.goViewClick(true);
            }
        }
    }

    private boolean isUndoEmpty() {
        if (null == optItemList || 0 == optItemList.size()) {
            return true;
        }
        for (BaseOpt opt : optItemList) {
            if (!opt.isRemoved()) {
                return false;
            }
        }
        return true;
    }

    private void clearRedoStack() {
        optItemStack.clear();
    }

    /**
     * 清除所有操作
     */
    public void clearAllOpt(){
        if(optItemList!=null){
            optItemList.clear();
        }
        if(optItemStack!=null){
            optItemStack.clear();
        }
        undateUndoOpt();
    }

    /**
     * 后退操作
     */
    public float unBackClick(){
        float result = 0;
        if(optItemList!=null && optItemList.size()>0){
            BaseOpt baseOpt = optItemList.get(optItemList.size()-1);

            optItemStack.push(baseOpt);
            optItemList.remove(optItemList.size()-1);
            if (baseOpt instanceof RubberOpt){
                ((RubberOpt) baseOpt).getRemovedOpt().setRemoved(false);
            }
        }
        undateUndoOpt();
        return result;
    }

    /**
     * 前进操作
     */
    public float unGoClick(){
        float result = 0;
        if(optItemStack!=null && optItemStack.size()>0){
            BaseOpt baseOpt = optItemStack.pop();
            optItemList.add(baseOpt);
            if (baseOpt instanceof RubberOpt){
                ((RubberOpt) baseOpt).getRemovedOpt().setRemoved(true);
            }
        }
        undateUndoOpt();
        return result;
    }

    public void onWindowChanged(float width, float height) {
        if (width == 0 || height == 0) {
            return;
        }
        mWindow.set(0, 0, width, height);
        if (!isInitialHoming) {
            onInitialHoming(width, height);
        } else {
            M.setTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
            M.mapRect(mFrame);
            M.mapRect(mClipFrame);
        }
    }

    /**
     * 根据添加顺序绘制不同操作
     * @param canvas
     */
    public void onDrawItemList(Canvas canvas,boolean isShowClear){

        //如果所有回退操作不为空先画回退操作
        if(isShowClear){
            if(optClearList!=null && !optClearList.isEmpty()){
                for (BaseOpt baseOpt:optClearList){
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

        for (BaseOpt baseOpt:optItemList){
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


    private void onDrawDoodles(BaseOpt baseOpt, Canvas canvas){
        if(baseOpt instanceof PathOpt){
            CanvasPath path = ((PathOpt) baseOpt).getPath();
            canvas.save();
            float scale = getScale();
            canvas.translate(mFrame.left, mFrame.top);
            canvas.scale(scale, scale);
            path.onDrawDoodle(canvas, mPaint,scale);
            canvas.restore();
        }
    }

    /**
     * 绘制图片
     * @param canvas
     */
    public void onDrawImage(Canvas canvas) {
        // 裁剪区域
        canvas.clipRect(mClipFrame);
        // 绘制图片
        canvas.drawBitmap(mImage, null, mFrame, null);
        if (DEBUG) {
            // Clip 区域
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(6);
            canvas.drawRect(mFrame, mPaint);
            canvas.drawRect(mClipFrame, mPaint);
        }
    }

    public Bitmap getmImage() {
        return mImage;
    }

    public void setRotate(float rotate) {
        mRotate = rotate;
    }

    /**
     * 获取缩放比例
     * @return
     */
    public float getScale() {

        float fWidth = mFrame.width();
        float mWidth = mImage.getWidth();

        return 1f * fWidth / mWidth;
    }

    public void setScale(float scale) {
        setScale(scale, mClipFrame.centerX(), mClipFrame.centerY());
    }

    public void setScale(float scale, float focusX, float focusY) {
        onScale(scale / getScale(), focusX, focusY, null);
    }

    /**
     * 缩放到原始大小
     */
    public void onScaleOrigin(){

        M.setScale(firstFrame.width()/mClipFrame.width(), firstFrame.height()/mClipFrame.height(), firstFrame.centerX(), firstFrame.centerY());
        M.mapRect(mFrame);
        M.mapRect(mClipFrame);
    }

    /**
     * 缩放
     * @param factor
     * @param focusX
     * @param focusY
     * @param mPen
     */
    public void onScale(float factor, float focusX, float focusY, CanvasView.Pen mPen) {
        if (factor == 1f) return;


        if (Math.max(mClipFrame.width(), mClipFrame.height()) >= MAX_SIZE
                || Math.min(mClipFrame.width(), mClipFrame.height()) <= MIN_SIZE) {
            factor += (1 - factor) / 2;
        }

        M.setScale(factor, factor, focusX, focusY);
        M.mapRect(mFrame);
        M.mapRect(mClipFrame);

        // 修正clip 窗口
        if (!mFrame.contains(mClipFrame)) {
            // TODO
//            mClipFrame.intersect(mFrame);
        }

        for (BaseOpt baseOpt : optItemList) {
            if(baseOpt instanceof PathOpt){
                ((PathOpt) baseOpt).getPath().scale(focusX,focusY,factor);
            }
        }

        if(mPen!=null){

            float scale =  getScale();
            float width = getScale()<1?CanvasPath.BASE_DOODLE_WIDTH:mPen.getWidth()/getScale();

            if(width<CanvasPath.BASE_DOODLE_WIDTH){
                width = CanvasPath.BASE_DOODLE_WIDTH;
            }

            mPen.setWidth(width);
        }

    }

    public float getRotate() {
        return mRotate;
    }

    public boolean isFreezing() {
        return isFreezing;
    }

    public void setFreezing(boolean freezing) {
        if (freezing != isFreezing) {
            //rotateStickers(freezing ? -getRotate() : getTargetRotate());
            isFreezing = freezing;
        }
    }

    public void release() {
        if (mImage != null && !mImage.isRecycled()) {
            mImage.recycle();
        }
    }

    public List<BaseOpt> getOptItemList() {
        return optItemList;
    }

    public Stack<BaseOpt> getOptItemStack() {
        return optItemStack;
    }
}
