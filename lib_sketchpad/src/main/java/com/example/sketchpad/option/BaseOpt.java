package com.example.sketchpad.option;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/20
 * desc: 操作类型基类
 * ============================================================
 **/
public abstract class BaseOpt {

    //记录添加操作的时候当前画布角度
    protected float curAngle;

    // 这次操作被删了
    protected boolean removed = false;

    public float getCurAngle() {
        return curAngle;
    }

    public void setCurAngle(float curAngle) {
        this.curAngle = curAngle;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
