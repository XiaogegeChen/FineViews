package com.github.xiaogegechen.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ColorTextView extends View {
    private static final String TAG = "ColorNumber";

    /**
     * 默认字体大小为300px
     */
    private static final int TEXT_SIZE_DEFAULT = 300;
    private static final int SIZE_DEFAULT = 150;

    private Paint mTextPaint;
    private String mText;
    private int mStartColor;
    private int mEndColor;
    private int mTextSize;

    public ColorTextView(Context context) {
        this (context, null);
    }

    public ColorTextView(Context context, AttributeSet attrs) {
        this (context, attrs, 0);
    }

    public ColorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super (context, attrs, defStyleAttr);

        // 属性
        TypedArray typedArray = context.obtainStyledAttributes (attrs, R.styleable.ColorTextView);
        mStartColor = typedArray.getColor (R.styleable.ColorTextView_start_color, getResources ().getColor (R.color.color_text_view_start_color));
        mEndColor = typedArray.getColor (R.styleable.ColorTextView_end_color, getResources ().getColor (R.color.color_text_view_end_color));
        mText = typedArray.getString (R.styleable.ColorTextView_text);
        mTextSize = typedArray.getDimensionPixelSize (R.styleable.ColorTextView_text_size, TEXT_SIZE_DEFAULT);
        typedArray.recycle ();

        // 初始化
        mTextPaint = new Paint ();
        mTextPaint.setStyle (Paint.Style.STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign (Paint.Align.CENTER);
        mTextPaint.setTextSize (mTextSize);

        mTextBound = new Rect ();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode (widthMeasureSpec);
        int widthSize = MeasureSpec.getSize (widthMeasureSpec);
        int heightMode = MeasureSpec.getMode (heightMeasureSpec);
        int heightSize = MeasureSpec.getSize (heightMeasureSpec);

        if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            // 使用默认大小
            setMeasuredDimension (SIZE_DEFAULT, SIZE_DEFAULT);
        }else if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY){
            setMeasuredDimension (SIZE_DEFAULT, heightSize);
        } else if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension (widthSize, SIZE_DEFAULT);
        }
    }

    private Rect mTextBound;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw (canvas);

        mTextPaint.getTextBounds (mText, 0, mText.length (), mTextBound);
        Log.d (TAG, "textBound is: " + mTextBound);
        Log.d (TAG, "width is: " + getWidth () + ", height is: " + getHeight ());

        float left = (getWidth () - mTextBound.width ()) >> 1;
        float right = getWidth () - left;
        float top = (getHeight () - mTextBound.height ()) >> 1;
        float bottom = getHeight () - top;
        Log.d (TAG, "left -> " + left + ", top -> " + top);
        LinearGradient gradient = new LinearGradient (left, top, right, bottom, mStartColor, mEndColor, Shader.TileMode.REPEAT);
        mTextPaint.setShader (gradient);
        // 居中显示
        float x = (float)(getWidth () * 1.0 / 2);
        float y = (float)(getHeight () * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2;
        canvas.drawText (mText, x, y, mTextPaint);
    }

    /**
     * 设置文字
     * @param text 文字
     */
    public void setText(String text) {
        mText = text;
        invalidate ();
    }

    /**
     * 设置渐变色开始的颜色
     * @param startColor 开始的颜色
     */
    public void setStartColor(int startColor) {
        mStartColor = startColor;
        invalidate ();
    }

    /**
     * 设置渐变色结束的颜色
     * @param endColor 结束的颜色
     */
    public void setEndColor(int endColor) {
        mEndColor = endColor;
        invalidate ();
    }

    /**
     * 设置字体大小
     * @param textSize 字体大小
     */
    public void setTextSize(int textSize){
        mTextSize = textSize;
        invalidate ();
    }
}
