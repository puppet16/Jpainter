package com.example.sketchpad;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import com.example.sketchpad.option.IOptionMode;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/20
 * desc: 画板的轨迹
 * ============================================================
 **/
public class CanvasPath {

    protected Path path;
    //轨迹颜色
    private int color;
    //轨迹宽度
    private float width;
    //轨迹的操作类型
    private @IOptionMode.Mode int mode;

    protected List<PathPoint> pathPointList = new ArrayList<PathPoint>();

    public static final float BASE_DOODLE_WIDTH = 5f;

    public static final float BASE_MOSAIC_WIDTH = 5f;

    public CanvasPath() {
        this(new Path());
    }

    public CanvasPath(Path path) {
        this(path, IOptionMode.DOODLE);
    }

    public CanvasPath(Path path, @IOptionMode.Mode int mode) {
        this(path, mode, Color.parseColor("#FFFA5151"));
    }

    public CanvasPath(Path path, @IOptionMode.Mode int mode, int color) {
        this(path, mode, color, BASE_MOSAIC_WIDTH,null);
    }

    public CanvasPath(Path path, @IOptionMode.Mode int mode, int color, float width, List<PathPoint> pathPointList) {
        this.path = path;
        this.mode = mode;
        this.color = color;
        this.width = width;

        this.pathPointList = pathPointList;
        if (mode == IOptionMode.RUBBER) {
            path.setFillType(Path.FillType.EVEN_ODD);
        }
    }


    public void addPathPoint(float x,float y){
        //涂鸦模式才记录点
        if(pathPointList!=null && getMode() == IOptionMode.DOODLE){
            pathPointList.add(new PathPoint(x,y));
        }
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public @IOptionMode.Mode int getMode() {
        return mode;
    }

    public void setMode(@IOptionMode.Mode int mode) {
        this.mode = mode;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public void transform(Matrix matrix) {
        path.transform(matrix);
    }

    public List<PathPoint> getPathPointList() {
        return pathPointList;
    }

    @Deprecated
    public void scale(float centerX,float centerY,float scale){
        if(!pathPointList.isEmpty()){
            for (PathPoint pathPoint:pathPointList){
                float x1 = pathPoint.x;
                float y1 = pathPoint.y;
                float[] point = {x1, y1};

                Matrix matrix = new Matrix();
                matrix.setScale(scale,scale,centerX,centerY);
                matrix.mapPoints(point);

                pathPoint.x = point[0];
                pathPoint.y = point[1];
            }
        }
    }

    public static class PathPoint{
        float x;
        float y;

        public PathPoint() {
        }

        public PathPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
}
