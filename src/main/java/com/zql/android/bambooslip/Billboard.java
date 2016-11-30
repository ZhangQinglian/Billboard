package com.zql.android.bambooslip;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.sql.Time;


/**
 * @author qinglian.zhang
 */

public class Billboard extends FrameLayout {

    public static final int BAMBOO_SLIP_ORIENTATION_HORIZONTAL = 1;
    public static final int BAMBOO_SLIP_ORIENTATION_VERTICAL = 2;

    private BaseView mBaseView;
    private int mSlipColumns;
    private int mSlipRows;
    private long mAnimationDuration;
    private long mAnimationDelay;
    private long mFlipDuration;
    private int mOrientation;
    private Handler mHandler = new Handler();
    private int mCount = -1;

    private BillboardCallback mCallback;
    private Bitmap mCurrentBitmap;
    private Bitmap mNextBitmap;
    private TimeInterpolator mTimeInterpolator = new AnticipateOvershootInterpolator();
    private int mLastSlip = -1;
    private long mTmpDelayFactor = -1;

    /**
     * {@link Billboard}'s callback
     */
    public interface BillboardCallback {
        /**
         * get a bitmap to show
         *
         * @param count {0,1,2,3,4,5,6,7,8, ... ,Integer.MAX_VALUE}
         * @return the bitmap to show
         */
        Bitmap getBitmap(int count);

        /**
         * custom the delay time of every slip
         * @param index slip index
         * @param delay the time you define in layout xml
         * @param slipSize the size of slip
         * @return delayfactor
         */
        long getDelayFactor(int index,int slipSize,long delay);
    }

    public Billboard(Context context, int slipColumns, int slipRows, long animationDuratin, long animationDelay, long flipDuration, int orientaion) {
        super(context);
        initView(slipColumns, slipRows, animationDuratin, animationDelay, flipDuration, orientaion);

    }

    public Billboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Billboard);
            int columns = typedArray.getInt(R.styleable.Billboard_billboard_columns, 10);
            int rows = typedArray.getInt(R.styleable.Billboard_billboard_rows, 1);
            int duration = typedArray.getInt(R.styleable.Billboard_billboard_duration, 1000);
            int delay = typedArray.getInt(R.styleable.Billboard_billboard_delay, 300);
            int flipDuration = typedArray.getInt(R.styleable.Billboard_billboard_refresh, 3000);
            int orientation = typedArray.getInt(R.styleable.Billboard_billboard_orientation, BAMBOO_SLIP_ORIENTATION_HORIZONTAL);
            initView(columns, rows, duration, delay, flipDuration, orientation);
            typedArray.recycle();
        }
    }

    /**
     * set the callback
     *
     * @param callback {@link BillboardCallback}
     */
    public void setCallback(BillboardCallback callback) {
        mCallback = callback;
    }

    public void clearCallback() {
        mCallback = null;
    }

    private void initView(int slipColumns, int slipRows, long animationDuration, long animationDelay, long flipDuration, int orientation) {
        setDrawingCacheEnabled(true);
        if (slipColumns < 1) {
            mSlipColumns = 10;
        } else {
            mSlipColumns = slipColumns;
        }
        if (slipRows < 1) {
            mSlipRows = 1;
        } else {
            mSlipRows = slipRows;
        }
        if (animationDuration < 100) {
            mAnimationDuration = 1000;
        } else {
            mAnimationDuration = animationDuration;
        }
        if (animationDelay < 0) {
            mAnimationDelay = 300;
        } else {
            mAnimationDelay = animationDelay;
        }
        if (flipDuration < 0) {
            mFlipDuration = 3000;
        } else {
            mFlipDuration = flipDuration;
        }
        if (orientation != BAMBOO_SLIP_ORIENTATION_HORIZONTAL && orientation != BAMBOO_SLIP_ORIENTATION_VERTICAL) {
            mOrientation = BAMBOO_SLIP_ORIENTATION_HORIZONTAL;
        } else {
            mOrientation = orientation;
        }
        mBaseView = new BaseView(this);
        //Log.d("scott", "columns = " + mSlipColumns + "  row = " + mSlipRows + "  duration = " + mAnimationDuration + " delay = " + mAnimationDelay);
    }

    private int getAutoCount() {
        if (mCount == Integer.MAX_VALUE) {
            mCount = -1;
        }
        mCount += 1;
        return mCount;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                if (mCallback == null) {
                    throw new RuntimeException("you must call 'Billboard.setCallback'");
                }
                for (int i = 0; i < mSlipRows * mSlipColumns; i++) {
                    new Slip(Billboard.this, mOrientation, mSlipColumns, mSlipRows);
                }
                if (mCallback != null) {
                    mCurrentBitmap = fitBitmap(mCallback.getBitmap(getAutoCount()), getWidth(), getHeight());
                    mNextBitmap = fitBitmap(mCallback.getBitmap(getAutoCount()), getWidth(), getHeight());
                    mBaseView.setBitmap(mCurrentBitmap);
                }
                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }


    /**
     * start flip
     */
    public void go() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                prepareFlip();
            }
        }, 1000);
    }

    /**
     * set timeInterpolator of flip animation
     *
     * @param timeInterpolator timeInterpolator of flip animation
     */
    public void setTimeInterpolator(TimeInterpolator timeInterpolator) {
        mTimeInterpolator = timeInterpolator;
    }

    /**
     * end flip
     */
    public void endFlip() {
        mIsEnd = true;
    }

    private void prepareFlip() {
        for (int i = 0; i < mSlipColumns * mSlipRows; i++) {
            Slip slip = (Slip) getChildAt(i + 1);
            slip.invalidate();
            slip.setVisibility(VISIBLE);
        }
        mBaseView.setVisibility(INVISIBLE);
        startFlip();
    }


    private void startFlip() {
        for (int i = 0; i < mSlipColumns * mSlipRows; i++) {
            final Slip slip = (Slip) getChildAt(i + 1);
            long delayFactor =  mCallback.getDelayFactor(i,mSlipColumns*mSlipRows,mAnimationDelay);
            if(mLastSlip == -1){
                mLastSlip = i;
            }
            if(mTmpDelayFactor == -1){
                mTmpDelayFactor = delayFactor;
            }
            if(delayFactor>=mTmpDelayFactor){
                mLastSlip = i;
                mTmpDelayFactor = delayFactor;
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    slip.doAnimation();
                }
            },delayFactor);
        }
    }

    private Runnable mRunFlip = new Runnable() {
        @Override
        public void run() {
            prepareFlip();
        }
    };
    private boolean mIsEnd = false;

    private void stopFlip() {
        Log.d("scott", "stop flip");
        mCurrentBitmap = mNextBitmap;
        mNextBitmap = fitBitmap(mCallback.getBitmap(getAutoCount()), getWidth(), getHeight());
        for (int i = 0; i < mSlipColumns * mSlipRows; i++) {
            Slip slip = (Slip) getChildAt(i + 1);
            slip.initNext();
            slip.setVisibility(GONE);
        }
        mBaseView.setBitmap(mCurrentBitmap);
        mBaseView.setVisibility(VISIBLE);
        if (!mIsEnd) {
            mHandler.postDelayed(mRunFlip, mFlipDuration);
        }
    }

    private Bitmap fitBitmap(Bitmap bitmap, int w, int h) {
        Bitmap des = Bitmap.createScaledBitmap(bitmap, w, h, false);
        bitmap.recycle();
        return des;
    }

    private class BaseView extends ImageView {

        Bitmap bitmap;

        public BaseView(Billboard billboard) {
            super(billboard.getContext());
            FrameLayout.LayoutParams FLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            setLayoutParams(FLP);

            billboard.addView(this);

            setScaleType(ScaleType.FIT_XY);
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            setImageBitmap(bitmap);
        }

    }

    private class Slip extends View {

        private int orientation;
        private Rect boundsSrc;
        private Rect boundsDes;
        private Paint bitmapPaint;
        private int index;
        private boolean next = false;

        public Slip(Billboard bambooSlips, int orientation, int slipColumns, int slipRows) {
            super(bambooSlips.getContext());
            //setBackgroundColor(getRandomColor());
            setVisibility(INVISIBLE);
            setCameraDistance(50 * getResources().getDisplayMetrics().densityDpi);
            bitmapPaint = new Paint();
            bitmapPaint.setDither(true);
            bitmapPaint.setFilterBitmap(true);

            this.orientation = orientation;
            index = bambooSlips.getChildCount() - 1;
            int w = bambooSlips.getWidth();
            int h = bambooSlips.getHeight();


            int layoutW = (w / slipColumns);
            if ((index + 1) % mSlipColumns == 0) {
                layoutW = layoutW + (w % slipColumns);
            }
            int layoutH = (h / slipRows);
            if ((index + mSlipColumns) >= (mSlipColumns * mSlipRows)) {
                layoutH = layoutH + (h % slipRows);
            }
            FrameLayout.LayoutParams FLP = new LayoutParams(layoutW, layoutH);
            FLP.leftMargin = (w / slipColumns) * (index % slipColumns);
            FLP.topMargin = (h / slipRows) * (index / slipColumns);
            this.setLayoutParams(FLP);
            //Log.d("scott", "" + index + "   w = " + layoutW + "  h = " + layoutH + "   l = " + FLP.leftMargin + "  t = " + FLP.topMargin);
            boundsSrc = new Rect(FLP.leftMargin, FLP.topMargin, FLP.leftMargin + layoutW, FLP.topMargin + layoutH);
            boundsDes = new Rect(0, 0, layoutW, layoutH);
            bambooSlips.addView(this);
        }

        public void initNext() {
            next = false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (next) {
                if (mNextBitmap != null) {
                    canvas.drawBitmap(mNextBitmap, boundsSrc, boundsDes, bitmapPaint);
                }
            } else {
                if (mCurrentBitmap != null) {
                    canvas.drawBitmap(mCurrentBitmap, boundsSrc, boundsDes, bitmapPaint);
                }
            }

        }

        public void doAnimation() {
            ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 180.0f);
            animator.setInterpolator(mTimeInterpolator);
            animator.setDuration(mAnimationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    if (v > 85 && !next) {
                        next = true;
                        invalidate();
                    }
                    if (v > 90) {
                        applyAnimationValue(v + 180);
                    } else {
                        applyAnimationValue(v);
                    }
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    animationStart(index);
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

        private void applyAnimationValue(float v) {
            if (orientation == BAMBOO_SLIP_ORIENTATION_VERTICAL) {
                setRotationX(-v);
            }
            if (orientation == BAMBOO_SLIP_ORIENTATION_HORIZONTAL) {
                setRotationY(v);
            }
        }

    }

    private void animationStop(int index) {
        if (index == mLastSlip) {
            stopFlip();
        }
    }

    private void animationStart(int index) {

    }
}
