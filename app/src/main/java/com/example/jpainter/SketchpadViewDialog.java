package com.example.jpainter;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sketchpad.SketchpadView;
import com.example.sketchpad.utils.CommonUtils;

import java.util.ArrayList;

/**
 * ============================================================
 * Author: ltt
 * date: 2020/6/22
 * desc:
 * ============================================================
 **/
public class SketchpadViewDialog extends DialogFragment {
    public static final String NUMBER_ID = "numberId";

    private SketchpadView mView;
    private String mNumberId;

    public static SketchpadViewDialog instance(String numberId) {
        SketchpadViewDialog dialog = new SketchpadViewDialog();
        Bundle bundle = new Bundle();
        bundle.putString(NUMBER_ID, numberId);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.App_Dialog);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        mNumberId = args.getString(NUMBER_ID, "");
        View v = inflater.inflate(R.layout.view_dialog_sketchpad, container, false);
        mView = v.findViewById(R.id.sketchpad_view);
        mView.setCanvasSize(CommonUtils.getScreenWidth(getContext()), CommonUtils.getScreenHeight(getContext()) - CommonUtils.dip2px(getContext(), 106));
        initListener();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (SketchpadViewManager.getInstance().getSketchpadView(mNumberId).size() <= 0) {
            return;
        }
        mView.setAllOptList(SketchpadViewManager.getInstance().getSketchpadView(mNumberId));
    }

    public void initListener() {
        mView.setOnItemClickInterface(new SketchpadView.OnItemClickInterface() {
            @Override
            public void OnCancelClick(boolean isEmpty) {
                mView.saveAllBackAllOpt();
                SketchpadViewManager.getInstance().saveSketchpadView(mNumberId, mView.getAllBackOptList());
                mView.resetImage();
                mView.clear();
                dismissAllowingStateLoss();
            }


            @Override
            public void OnClearAllClick() {
                if (!mView.getOptItemList().isEmpty() || !mView.getOptItemStack().isEmpty()) {
                    mView.clearAllBackAllOpt();
                    mView.resetImage();
                    mView.clear();
                    mView.changeModeToDoodle();
                }
            }
        });
    }

    private void toastShow(String content) {
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
