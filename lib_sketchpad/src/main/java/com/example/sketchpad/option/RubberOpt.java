package com.example.sketchpad.option;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/20
 * desc: 橡皮擦操作
 * ============================================================
 **/
public class RubberOpt extends BaseOpt{
    private BaseOpt mRemovedOpt;

    public BaseOpt getRemovedOpt() {
        return mRemovedOpt;
    }

    public void setRemovedOpt(BaseOpt mRemovedOpt) {
        this.mRemovedOpt = mRemovedOpt;
    }
}
