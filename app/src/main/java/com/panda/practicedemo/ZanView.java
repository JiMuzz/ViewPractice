package com.panda.practicedemo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ZanView extends View implements View.OnClickListener {

    Paint numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    //文本颜色
    private static final int TEXT_DEFAULT_COLOR = Color.parseColor("#cccccc");
    private static final int TEXT_DEFAULT_END_COLOR = Color.parseColor("#00cccccc");

    private Bitmap thumbUp;
    private Bitmap notThumbUp;
    private Bitmap shining;

    boolean isThumbUp;

    //为了保证居中绘制，这是绘制的起点坐标，减去这个值则为以原点为坐标开始绘制的
    private int startX;
    private int startY;

    //图标大小
    private static final float THUMB_WIDTH = 20f;
    private static final float THUMB_HEIGHT = 20f;
    private static final float SHINING_WIDTH = 16f;
    private static final float SHINING_HEIGHT = 16f;

    //文本的上下移动变化值
    private float OFFSET_MIN;
    private float OFFSET_MAX;

    private float textSize;
    private float textStartX;

    private float drawablePadding;

    private int count;

    private String[] nums;//num[0]是不变的部分，nums[1]原来的部分，nums[2]变化后的部分
    private boolean toBigger;

    //动画相关
    private float offsetY;
    private float mOldOffsetY;
    private float mNewOffsetY;


    public ZanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    {
        count = 800;

        thumbUp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_messages_like_selected);
        notThumbUp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_messages_like_unselected);
        shining = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_messages_like_selected_shining);

        textSize = 15;
        textStartX = dip2px(THUMB_WIDTH) + drawablePadding;
        nums = new String[]{String.valueOf(count), "", ""};
        numberPaint.setTextSize(sp2px(textSize));

        OFFSET_MIN = 0;
        OFFSET_MAX = 1.5f * sp2px(textSize);


        setOnClickListener(this);
    }

    public ZanView setCount(int count) {
        this.count = count;
        calculateChangeNum(0);
        requestLayout();
        return this;
    }

    public float getOffsetY() {
        return offsetY;
    }

    /**
     * 设置动画
     *
     * @param offsetY 数字变大，老数字往上移动，新数字从下滑入——【0，1】
     *                数字变小，老数字往下移动，新数字从上滑入——【0，-1】
     */
    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
        mOldOffsetY = offsetY * OFFSET_MAX;
        if (toBigger) {
            mNewOffsetY = OFFSET_MAX * (offsetY - 1);
        } else {
            mNewOffsetY = OFFSET_MAX * (offsetY + 1);
        }

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawIcon(canvas);
        drawText(canvas);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        startX = (int) (w - THUMB_WIDTH - drawablePadding - numberPaint.measureText(String.valueOf(count))) / 2;
        startY = (int) (h - Math.max(THUMB_HEIGHT + SHINING_HEIGHT, sp2px(textSize))) / 2;
    }

    /**
     * 画左边图片
     */
    public void drawIcon(Canvas canvas) {
        if (isThumbUp) {
            canvas.drawBitmap(thumbUp, startX, startY, imgPaint);
        } else {
            canvas.drawBitmap(notThumbUp, startX, startY, imgPaint);
        }
    }


    /**
     * 画右边文字
     */
    public void drawText(Canvas canvas) {
        numberPaint.setColor(TEXT_DEFAULT_COLOR);
        canvas.drawText(nums[0], startX + textStartX, startY, numberPaint);


        //计算每个字符宽度
        float textcharWidth = numberPaint.measureText(String.valueOf(count)) / String.valueOf(count).length();


        float fraction = offsetY;
        if (!isThumbUp) {
            fraction = -offsetY;
        }


        numberPaint.setColor((Integer) evaluate(fraction, TEXT_DEFAULT_COLOR, TEXT_DEFAULT_END_COLOR));
        canvas.drawText(nums[1], startX + textStartX + textcharWidth * nums[0].length(), startY - mOldOffsetY, numberPaint);

        numberPaint.setColor((Integer) evaluate(fraction, TEXT_DEFAULT_END_COLOR, TEXT_DEFAULT_COLOR));
        canvas.drawText(nums[2], startX + textStartX + textcharWidth * nums[0].length(), startY - mNewOffsetY, numberPaint);
    }

    /**
     * 计算动画数组数据，绘制需要
     *
     * @param change 0 不变数字
     *               1 增加1
     *               -1 减1
     */
    private void calculateChangeNum(int change) {
        if (change == 0) {
            nums[0] = String.valueOf(count);
            nums[1] = "";
            nums[2] = "";
            return;
        }

        toBigger = change > 0;

        String oldstr = String.valueOf(count);
        String newstr = String.valueOf(count + change);

        int newLength = newstr.length();
        int oldLength = oldstr.length();

        if (newLength != oldLength) {
            nums[0] = "";
            nums[1] = oldstr;
            nums[2] = newstr;
        } else {

            for (int i = 0; i < newLength; i++) {
                char oldc = oldstr.charAt(i);
                char newc = newstr.charAt(i);

                if (oldc != newc) {

                    if (i == 0) {
                        nums[0] = "";
                    } else {
                        nums[0] = newstr.substring(0, i);
                    }

                    nums[1] = oldstr.substring(i);
                    nums[2] = newstr.substring(i);
                    break;
                }

            }

        }


    }

    @Override
    public void onClick(View v) {
        if (isThumbUp) {
            //如果是点赞状态，就取消点赞
            calculateChangeNum(-1);
            count--;
            showThumbDownAnim();
        } else {
            //如果是没点赞状态，就点赞
            calculateChangeNum(1);
            count++;
            showThumbUpAnim();
        }

        isThumbUp = !isThumbUp;
    }

    /**
     * 取消点赞
     */
    public void showThumbDownAnim() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "offsetY", 0, -1);
        animator.setDuration(300);
        animator.start();
    }

    /**
     * 点赞
     */
    public void showThumbUpAnim() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "offsetY", 0, 1);
        animator.setDuration(300);
        animator.start();
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public Object evaluate(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        float startA = ((startInt >> 24) & 0xff) / 255.0f;
        float startR = ((startInt >> 16) & 0xff) / 255.0f;
        float startG = ((startInt >> 8) & 0xff) / 255.0f;
        float startB = (startInt & 0xff) / 255.0f;

        int endInt = (Integer) endValue;
        float endA = ((endInt >> 24) & 0xff) / 255.0f;
        float endR = ((endInt >> 16) & 0xff) / 255.0f;
        float endG = ((endInt >> 8) & 0xff) / 255.0f;
        float endB = (endInt & 0xff) / 255.0f;

        // convert from sRGB to linear
        startR = (float) Math.pow(startR, 2.2);
        startG = (float) Math.pow(startG, 2.2);
        startB = (float) Math.pow(startB, 2.2);

        endR = (float) Math.pow(endR, 2.2);
        endG = (float) Math.pow(endG, 2.2);
        endB = (float) Math.pow(endB, 2.2);

        // compute the interpolated color in linear space
        float a = startA + fraction * (endA - startA);
        float r = startR + fraction * (endR - startR);
        float g = startG + fraction * (endG - startG);
        float b = startB + fraction * (endB - startB);

        // convert back to sRGB in the [0..255] range
        a = a * 255.0f;
        r = (float) Math.pow(r, 1.0 / 2.2) * 255.0f;
        g = (float) Math.pow(g, 1.0 / 2.2) * 255.0f;
        b = (float) Math.pow(b, 1.0 / 2.2) * 255.0f;

        return Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b);
    }
}
