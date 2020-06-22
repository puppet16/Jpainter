package com.example.sketchpad.utils;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import static java.lang.Math.abs;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/20
 * desc:
 * ============================================================
 **/
public class CommonUtils {

    /**
     * 判断坐标上两条直线是否相交
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param x4
     * @param y4
     * @return
     */
    public static boolean isIntersect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        boolean result = false;
        try {

            double temp = 0.0000000000000000001;

            //共线的情况单独考虑 
            if (abs((x2 - x1) * (y4 - y3) - (x4 - x3) * (y2 - y1)) < temp) {
                if ((x1 == x3) && ((y3 - y1) * (y3 - y2) <= temp || (y4 - y1) * (y4 - y2) <= temp)) {
                    result = true;
                } else {
                    result = false;
                }
            } else {
                double m = (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);
                double n = (x2 - x1) * (y4 - y1) - (x4 - x1) * (y2 - y1);
                double p = (x4 - x3) * (y1 - y3) - (x1 - x3) * (y4 - y3);
                double q = (x4 - x3) * (y2 - y3) - (x2 - x3) * (y4 - y3);
                if (m * n <= temp && p * q <= temp) {
                    result = true;
                } else {
                    result = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
