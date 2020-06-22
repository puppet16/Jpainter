package com.example.jpainter;

import android.app.Application;
import android.content.Context;


import java.lang.ref.WeakReference;


public class IApplication extends Application {

    public static final String TAG = "painter";
    private static WeakReference<Context> mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<>(getApplicationContext());
    }

    /**
     * 全局获取 Context
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        return mContext.get();
    }
}
