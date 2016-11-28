package com.zql.android.bambooslip;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;


/**
 * @author qinglian.zhang
 */

public class Billboard extends FrameLayout {

    public static final int BAMBOO_SLIP_ORIENTATION_VERTICAL = 1;
    public static final int BAMBOO_SLIP_ORIENTATION_HORIZONTAL = 2;

    private BaseView mBaseView;
    private int mSlipColumns;
    private int mSlipRows;
    private long mAnimationDuration;
    private long mAnimationDelay;
    private Handler mHandler = new Handler();
    private int mCount = -1;

    private BillboardCallback mCallback;
    public interface BillboardCallback{
        Bitmap getBitmap(int count);
    }

    public Billboard(Context context,int slipColumns,int slipRows,long animationDuratin,long animationDelay) {
        super(context);
        initView(slipColumns,slipRows,animationDuratin,animationDelay);

    }

    public Billboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        if(attrs != null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.Billboard);
            int columns = typedArray.getInt(R.styleable.Billboard_billboard_columns,10);
            int rows = typedArray.getInt(R.styleable.Billboard_billboard_rows,1);
            int duration = typedArray.getInt(R.styleable.Billboard_billboard_duration,1000);
            int delay = typedArray.getInt(R.styleable.Billboard_billboard_delay,3000);
            initView(columns,rows,duration,delay);
            typedArray.recycle();
        }
    }

    public void setCallback(BillboardCallback callback){
        mCallback = callback;
    }

    public void clearCallback(){
        mCallback = null;
    }
    private void initView(int slipColumns,int slipRows,long animationDuration,long animationDelay){
        setDrawingCacheEnabled(true);
        mSlipColumns = slipColumns;
        mSlipRows = slipRows;
        mAnimationDuration = animationDuration;
        mAnimationDelay = animationDelay;
        mBaseView = new BaseView(this);
        Log.d("scott","columns = " + mSlipColumns + "  row = " + mSlipRows + "  duration = " + mAnimationDuration + " delay = " + mAnimationDelay);
    }

    private int getCount(){
        if(mCount == Integer.MAX_VALUE){
            mCount = -1;
        }
        mCount += 1;
        return mCount;
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            public boolean onPreDraw() {

                for(int i = 0;i<mSlipRows*mSlipColumns;i++){
                    Slip slip = new Slip(Billboard.this,BAMBOO_SLIP_ORIENTATION_HORIZONTAL,mSlipColumns,mSlipRows);
                }
                if(mCallback != null){
                    Bitmap bitmap = mCallback.getBitmap(getCount());
                    mBaseView.setBitmap(bitmap);
                }
                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    public void prepareFlip(){
        Bitmap bitmap = getDrawingCache();
        for(int i = 0;i<mSlipColumns*mSlipRows;i++){
            Slip slip = (Slip) getChildAt(i+1);
            slip.setBitmap(bitmap);
            slip.invalidate();
            slip.setVisibility(VISIBLE);
        }
        mBaseView.setVisibility(INVISIBLE);
        startFlip();
    }

    private void startFlip(){
        for(int i = 0;i<mSlipColumns*mSlipRows;i++){
            final Slip slip = (Slip) getChildAt(i+1);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    slip.doAnimation();
                }
            },i*mAnimationDelay);
        }
    }

    private class BaseView extends ImageView{

        Bitmap bitmap ;
        public BaseView(Billboard billboard) {
            super(billboard.getContext());
            FrameLayout.LayoutParams FLP = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            setLayoutParams(FLP);
            billboard.addView(this);
            setScaleType(ScaleType.FIT_CENTER);
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            setImageBitmap(bitmap);
        }

    }
    private class Slip extends View {

        private int orientation;
        private Rect boundsSrc ;
        private Rect boundsDes ;
        private Paint bitmapPaint;
        private Bitmap bitmap;
        private int index;

        public Slip(Billboard bambooSlips, int orientation, int slipColumns, int slipRows) {
            super(bambooSlips.getContext());
            //setBackgroundColor(getRandomColor());
            setVisibility(INVISIBLE);
            setCameraDistance(50*getResources().getDisplayMetrics().densityDpi);
            bitmapPaint = new Paint();
            bitmapPaint.setDither(true);
            bitmapPaint.setFilterBitmap(true);

            this.orientation = orientation;
            index = bambooSlips.getChildCount() - 1;
            int w = bambooSlips.getWidth();
            int h = bambooSlips.getHeight();


            int layoutW = (w / slipColumns);
            if((index+1)%mSlipColumns == 0){
                layoutW = layoutW + (w%slipColumns);
            }
            int layoutH = (h / slipRows);
            FrameLayout.LayoutParams FLP = new LayoutParams(layoutW, layoutH);
            FLP.leftMargin = (w / slipColumns) * (index % slipColumns);
            FLP.topMargin = layoutH * (index / slipColumns);
            this.setLayoutParams(FLP);
            Log.d("scott","" + index + "   w = " + layoutW + "  h = " + layoutH + "   l = " + FLP.leftMargin + "  t = " + FLP.topMargin);
            boundsSrc = new Rect(FLP.leftMargin,FLP.topMargin,FLP.leftMargin+layoutW,FLP.topMargin+layoutH);
            boundsDes = new Rect(0,0,layoutW,layoutH);
            bambooSlips.addView(this);
        }


        public void setBitmap(Bitmap bitmap){
            this.bitmap = bitmap;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if(bitmap != null){
                Log.d("scott","src : " + boundsSrc.flattenToString());
                Log.d("scott","des : " + boundsDes.flattenToString());
                canvas.drawBitmap(bitmap,boundsSrc,boundsDes,bitmapPaint);
            }
        }
        public void doAnimation(){
            ValueAnimator animator = ValueAnimator.ofFloat(0.0f,180.0f);
            animator.setDuration(mAnimationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    applyAnimationValue(v);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationStop(index);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
        private void applyAnimationValue(float v){
            setRotationY(v);
        }
    }

    private void animationStop(int index){

    }
}
