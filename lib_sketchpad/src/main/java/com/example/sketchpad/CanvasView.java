package com.example.sketchpad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.example.sketchpad.option.BaseOpt;
import com.example.sketchpad.utils.CommonUtils;
import com.example.sketchpad.option.IOptionMode;
import com.example.sketchpad.utils.IMGHelp;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/20
 * desc:
 * ============================================================
 **/
public class CanvasView extends FrameLayout {

    private static final String TAG = "SketchpadView";

    private IMGHelp mImage;

    private Pen mPen = new Pen();

    private int mPointerCount = 0;

    private Paint mDoodlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mRubberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final boolean DEBUG = true;

    {
        // 涂鸦画刷
        mDoodlePaint.setStyle(Paint.Style.STROKE);
        mDoodlePaint.setStrokeWidth(mPen.getWidth());
        mDoodlePaint.setColor(Color.RED);
        mDoodlePaint.setPathEffect(new CornerPathEffect(CanvasPath.BASE_DOODLE_WIDTH));
        mDoodlePaint.setStrokeCap(Paint.Cap.ROUND);
        mDoodlePaint.setStrokeJoin(Paint.Join.ROUND);

        // 橡皮擦画刷
        mRubberPaint.setStyle(Paint.Style.STROKE);
        mRubberPaint.setStrokeWidth(CanvasPath.BASE_MOSAIC_WIDTH);
        mRubberPaint.setColor(Color.BLACK);
        mRubberPaint.setPathEffect(new CornerPathEffect(CanvasPath.BASE_MOSAIC_WIDTH));
        mRubberPaint.setStrokeCap(Paint.Cap.ROUND);
        mRubberPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public CanvasView(Context context) {
        this(context, null, 0);
        initialize(context);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initialize(context);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void initialize(Context context) {
        mImage = new IMGHelp();
        mPen.setMode(mImage.getMode());
    }

    public void setImageBitmap(Bitmap image) {
        mImage.setBitmap(image);
        invalidate();
    }

    public void setMode(@IOptionMode.Mode int mode) {
        mPen.reset();
        // 设置新的编辑模式
        mImage.setMode(mode);
        mPen.setMode(mode);
        invalidate();
    }


    /**
     * 获取所有的操作
     * @return
     */
    public List<BaseOpt> getAllOptList(){
        return mImage.getOptItemList();
    }


    /**
     * 获取所有的操作
     * @return
     */
    public List<BaseOpt> getAllBackOptList(){
        return mImage.getAllBackOptList();
    }

    /**
     * 清空所有回退操作
     */
    public void clearAllBackOPt(){
        mImage.clearAllBackOPt();

    }



    /**
     * 保存所有回退操作
     */
    public void saveAllBackOpt(){
        mImage.saveAllBackOPt();
        invalidate();
    }

    public void setPenColor(int color) {
        mPen.setColor(color);
    }

    public int getPenColor() {
        return mPen.getColor();
    }

    public @IOptionMode.Mode int getMode() {
        return mImage.getMode();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onDrawImages(canvas,true,false);
    }

    private void onDrawImages(Canvas canvas, boolean isSaveBitmap,boolean isShowClear) {
        canvas.save();

        /**
         * clip 中心旋转
         */
        RectF clipFrame = mImage.getClipFrame();
        canvas.rotate(mImage.getRotate(), clipFrame.centerX(), clipFrame.centerY());

        /**
         * 背景图片
         */
        if(isSaveBitmap){
            mImage.onDrawImage(canvas);
        }

        /**
         * 按层级顺序显示item
         */
         mImage.onDrawItemList(canvas,isShowClear);

        /**
         * 绘制的时候实时显示涂鸦路径
         */
        if (mImage.getMode() == IOptionMode.DOODLE && !mPen.isEmpty()) {
            mDoodlePaint.setColor(mPen.getColor());
            mDoodlePaint.setStrokeWidth(mPen.getWidth());
            canvas.save();
            RectF frame = mImage.getClipFrame();
            canvas.rotate(-mImage.getRotate(), frame.centerX(), frame.centerY());
            canvas.translate(getScrollX(), getScrollY());
            canvas.drawPath(mPen.getPath(), mDoodlePaint);
            canvas.restore();
        }

        canvas.restore();
    }

    public Bitmap saveBitmap(boolean isSaveBg,boolean isShowClear) {

        float scale = 1f / mImage.getScale();

        RectF frame = new RectF(mImage.getClipFrame());

        // 旋转基画布
        Matrix m = new Matrix();
        m.setRotate(mImage.getRotate(), frame.centerX(), frame.centerY());
        m.mapRect(frame);

        // 缩放基画布
        m.setScale(scale, scale, frame.left, frame.top);
        m.mapRect(frame);

        Bitmap bitmap = Bitmap.createBitmap(Math.round(frame.width()),
                Math.round(frame.height()), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        // 平移到基画布原点&缩放到原尺寸
        canvas.translate(-frame.left, -frame.top);
        canvas.scale(scale, scale, frame.left, frame.top);

        onDrawImages(canvas,isSaveBg,isShowClear);

        return bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mImage.onWindowChanged(right - left, bottom - top);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return onInterceptTouch(ev) || super.onInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    boolean onInterceptTouch(MotionEvent event) {
        if (mImage.getMode() == IOptionMode.ROTATE) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onTouch(event);
    }

    float preX = 0;
    float preY = 0;

    boolean onTouch(MotionEvent event) {
        boolean handled = true;
        mPointerCount = event.getPointerCount();


        int mode = mImage.getMode();

        if (mode != IOptionMode.NONE && mode != IOptionMode.TEXT && mPointerCount > 1) {
            boolean isPath = event.getEventTime() - event.getDownTime() > ViewConfiguration.getDoubleTapTimeout();
            handled |= onPathDone(event, isPath);
        }  else {
            handled |= onTouchPath(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float creX = event.getX();
                float creY = event.getY();
                if(mode == IOptionMode.RUBBER){
                    List<CanvasPath> CanvasPaths =  mImage.getmDoodles();
                    if(CanvasPaths!=null && CanvasPaths.size()>0 && preX!=0 && preY!=0){
                        float scrollX = getScrollX();
                        float scrollY = getScrollY();

                        for (int j =0;j<CanvasPaths.size();j++){
                            CanvasPath CanvasPath = CanvasPaths.get(j);
                            List<CanvasPath.PathPoint> pathPointList = CanvasPath.getPathPointList();
                            if(pathPointList!=null && pathPointList.size()>1){
                                for (int i=1;i<pathPointList.size();i++){

                                    CanvasPath.PathPoint prePoint = pathPointList.get(i-1);
                                    CanvasPath.PathPoint crePoint = pathPointList.get(i);

                                    float x1 = prePoint.getX();
                                    float y1 = prePoint.getY();
                                    float x2 = crePoint.getX();
                                    float y2 = crePoint.getY();

                                    //如果图形整移动的话就要把移动距离去掉
                                    float x3 = preX+scrollX;
                                    float y3 = preY+scrollY;
                                    float x4 = creX+scrollX;
                                    float y4 = creY+scrollY;

                                    float[] point = {x3, y3, x4, y4};
                                    Matrix matrix = new Matrix();
                                    RectF frame = mImage.getClipFrame();
                                    matrix.preRotate(-mImage.getRotate(), frame.centerX(), frame.centerY());
                                    matrix.mapPoints(point);

                                    boolean isClear = CommonUtils.isIntersect(x1, y1, x2, y2,
                                            point[0], point[1], point[2], point[3]);
                                    if(isClear){
                                        mImage.undoDoodleByItem(CanvasPath);
                                    }
                                }
                            }
                        }
                    }
                    preX = event.getX();
                    preY = event.getY();
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                preX = 0;
                preY = 0;
                break;
        }

        recordPoint(event);

        return handled;
    }/**/

    private boolean pointInPath(Path path, int x,int y) {
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        Region region = new Region();
        region.setPath(path, new Region((int) bounds.left-5, (int) bounds.top-5, (int) bounds.right+5, (int) bounds.bottom+5));
        return region.contains(x, y);
    }

    /**
     * 记录定位
     * @param event
     */
    private void recordPoint(MotionEvent event){
        @IOptionMode.Mode int mode = mImage.getMode();
        if(mode == IOptionMode.DOODLE){
            //涂鸦模式
            float x = event.getX();
            float y = event.getY();
        }
    }

    private boolean onTouchPath(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return onPathBegin(event);
            case MotionEvent.ACTION_MOVE:
                return onPathMove(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(getMode()==IOptionMode.DOODLE){
                    return mPen.isIdentity(event.getPointerId(0)) && onPathDone(event, true);
                }
        }
        return false;
    }


    /**
     * 画线开始
     * @param event
     * @return
     */
    private boolean onPathBegin(MotionEvent event) {
        mPen.reset(event.getX(), event.getY());
        //添加路径点，但是如果图像已经移动，需要把移动的位置偏移算上
        mPen.addPointItem(event.getX()+getScrollX(), event.getY()+getScrollY());

        mPen.setIdentity(event.getPointerId(0));
        return true;
    }

    /**
     * 记录画线路径
     * @param event
     * @return
     */
    private boolean onPathMove(MotionEvent event) {
        if (mPen.isIdentity(event.getPointerId(0))) {
            mPen.lineTo(event.getX(), event.getY());
            //添加路径点，但是如果图像已经移动，需要把移动的位置偏移算上
            mPen.addPointItem(event.getX()+getScrollX(), event.getY()+getScrollY());
            invalidate();
            return true;
        }
        return false;
    }

    /**
     * 画线结束
     * @return
     */
    private boolean onPathDone(MotionEvent event, boolean addOpt) {
        if (mPen.isEmpty()) {
            return false;
        }
        //记录画线路径点
        mPen.addPathPoint(event.getX()+getScrollX(), event.getY()+getScrollY());
        mImage.addPath(mPen.toPath(), getScrollX(), getScrollY(), addOpt);

        mPen.reset();
        invalidate();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mImage.release();
    }

    private static boolean isAnima = true;

    /**
     * 清除所有操作
     */
    public void clearAllOpt(boolean isAnima){
        this.isAnima = isAnima;
        mImage.clearAllOpt();
    }

    public IMGHelp getmImage() {
        return mImage;
    }

    public void setBitmap(Bitmap bitmap) {
        //切换图片的时候不让旋转
        mImage.setRotate(0);

        mImage.setBitmap(bitmap);
        scrollTo(0, 0);
        invalidate();
    }

    public void reset() {
        mImage.setRotate(0);
        mImage.setInitialHoming(false);
        mImage.onWindowChanged(mImage.getWindow().width(), mImage.getWindow().height());
        scrollTo(0, 0);
        invalidate();
    }

    public static class Pen extends CanvasPath {

        private int identity = Integer.MIN_VALUE;

        void reset() {
            this.pathPointList = new ArrayList<>();
            this.path.reset();
            this.identity = Integer.MIN_VALUE;
        }

        void reset(float x, float y) {
            this.path.reset();
            this.path.moveTo(x, y);
            this.identity = Integer.MIN_VALUE;
            //记录画线路径点
            this.pathPointList = new ArrayList<>();
        }

        void addPointItem(float x, float y){
            this.addPathPoint(x,y);
        }

        void setIdentity(int identity) {
            this.identity = identity;
        }

        boolean isIdentity(int identity) {
            return this.identity == identity;
        }

        void lineTo(float x, float y) {

            this.path.lineTo(x, y);
        }

        boolean isEmpty() {
            return this.path.isEmpty();
        }

        CanvasPath toPath() {
            return new CanvasPath(new Path(this.path), getMode(), getColor(), getWidth(),getPathPointList());
        }
    }
}
