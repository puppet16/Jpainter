package com.example.sketchpad.option;

import com.example.sketchpad.CanvasPath;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/20
 * desc: 涂鸦操作
 * ============================================================
 **/

public class PathOpt extends BaseOpt{

    private CanvasPath mPath;

    public CanvasPath getPath() {
        return mPath;
    }

    public void setPath(CanvasPath path) {
        this.mPath = path;
    }
}
