package com.zql.android.bambooslip;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * @author qinglian.zhang
 */

public class Craftsman {
    public Billboard make(ViewGroup parent, View view,int slipColumns,int slipRows,int animationDuration,int animationDelay) {
        Billboard bambooSlips = new Billboard(view.getContext(),slipColumns,slipRows,animationDuration,animationDelay);

        if(parent instanceof RelativeLayout){
            RelativeLayout.LayoutParams RLP = (RelativeLayout.LayoutParams) view.getLayoutParams();
            bambooSlips.setLayoutParams(RLP);
        }
        if(parent instanceof FrameLayout){
            FrameLayout.LayoutParams FLP = (FrameLayout.LayoutParams) view.getLayoutParams();
            bambooSlips.setLayoutParams(FLP);
        }
        FrameLayout.LayoutParams FLP = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        parent.removeView(view);
        view.setLayoutParams(FLP);
        parent.addView(bambooSlips);
        bambooSlips.addView(view);
        return bambooSlips;
    }
}
