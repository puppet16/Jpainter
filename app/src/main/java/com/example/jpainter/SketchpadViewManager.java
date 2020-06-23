package com.example.jpainter;

import com.example.sketchpad.option.BaseOpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/22
 * desc: 按题号管理草稿类
 * ============================================================
 **/
public class SketchpadViewManager {
    private HashMap<String, List<BaseOpt>> mSketchpadMap;

    private static class SketchpadViewManagerInstance {
        private static final SketchpadViewManager instance = new SketchpadViewManager();
    }

    public static SketchpadViewManager getInstance() {
        return SketchpadViewManagerInstance.instance;
    }

    public SketchpadViewManager() {
        mSketchpadMap = new HashMap<>();
    }

    /**
     * 根据题号获取之前保存的画板操作列表
     *
     * @param numberId
     * @return
     */
    public List<BaseOpt> getSketchpadView(String numberId) {
        if (mSketchpadMap == null) {
            mSketchpadMap = new HashMap<>();
        }
        if (mSketchpadMap.get(numberId) == null) {
            return new ArrayList<>();
        }
        return mSketchpadMap.get(numberId);
    }

    /**
     * 保存画板操作列表
     *
     * @param numberId
     * @param optList
     * @return
     */
    public void saveSketchpadView(String numberId, List<BaseOpt> optList) {
        if (mSketchpadMap == null) {
            mSketchpadMap = new HashMap<>();
        }
        mSketchpadMap.put(numberId, optList);
    }

    public void onDestroy() {
        if (mSketchpadMap != null) {
            mSketchpadMap.clear();
            mSketchpadMap = null;
        }
    }
}
