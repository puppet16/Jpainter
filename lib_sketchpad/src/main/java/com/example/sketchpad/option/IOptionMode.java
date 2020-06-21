package com.example.sketchpad.option;

import androidx.annotation.IntDef;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/20
 * desc:用户的编辑类型定义
 * ============================================================
 **/
public interface IOptionMode {

    int NONE = 100;//无
    int DOODLE = 101;//涂鸦，目前只有这一个功能
    int TEXT = 102;//文本
    int RUBBER = 103;//橡皮擦
    int ROTATE = 103;//旋转
    int GRAPH = 104;//图形，如三角形

    @IntDef({NONE, DOODLE, TEXT, ROTATE, GRAPH, RUBBER})
    public @interface Mode {
    }
}
