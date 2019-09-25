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
import android.view.Gravity;
import android.view.View;

/**
 * 带渐变色的文字,当设置了padding时，如果尺寸不够显示文字
 * 将不会进行显示
 */
public class ColorTextView extends View {
    private static final String TAG = "ColorTextView";

    /**
     * 默认字体大小为300px
     */
    private static final int TEXT_SIZE_DEFAULT = 300;
    private static final int GRAVITY_DEFAULT = 0x11;

    private Paint mTextPaint;
    private String mText;
    private int mStartColor;
    private int mEndColor;
    private int mTextSize;
    private int mGravity;
    // 表示文字范围的矩形
    private Rect mTextBound;

    // 记录测量模式，如果是精确模式，那么所有的动态更改都
    // 不需要重新布局的，如果不是精确模式，那么有的属性动态更改之后
    // 需要重新布局。这样可以避免不需要的重新布局，提高性能
    private boolean mIsSizeExactly = false;
    private boolean mIsWidthExactly = false;
    private boolean mIsHeightExactly = false;

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
        mGravity = typedArray.getInt(R.styleable.ColorTextView_text_gravity, GRAVITY_DEFAULT);
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
        if(Consts.COLOR_TEXT_VIEW_DEBUG)Log.d(TAG, "perform measure");
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode (widthMeasureSpec);
        int widthSize = MeasureSpec.getSize (widthMeasureSpec);
        int heightMode = MeasureSpec.getMode (heightMeasureSpec);
        int heightSize = MeasureSpec.getSize (heightMeasureSpec);

        mTextPaint.getTextBounds (mText, 0, mText.length (), mTextBound);
        if(Consts.COLOR_TEXT_VIEW_DEBUG)Log.d (TAG, "textBound is: " + mTextBound);

        // 如果是精确测量模式，就不需要下面的操作
        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
            if(Consts.COLOR_TEXT_VIEW_DEBUG)Log.d(TAG, "Measure exactly");
            mIsWidthExactly = true;
            mIsHeightExactly = true;
            mIsSizeExactly = true;
            setMeasuredDimension(widthSize, heightSize);
            return;
        }
        // 更新标记位
        mIsSizeExactly = false;

        int width;
        int height;

        // 宽
        if(widthMode == MeasureSpec.EXACTLY){
            mIsWidthExactly = true;
            width = widthSize;
        }else{
            // 加上padding
            width = mTextBound.width () + getPaddingLeft() + getPaddingRight();
            mIsWidthExactly = false;
        }

        // 高
        if(heightMode == MeasureSpec.EXACTLY){
            mIsHeightExactly = true;
            height = heightSize;
        }else{
            mIsHeightExactly = false;
            height = mTextBound.height() + getPaddingBottom() + getPaddingTop();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(Consts.COLOR_TEXT_VIEW_DEBUG)Log.d(TAG, "perform draw");
        super.onDraw (canvas);
        if(Consts.COLOR_TEXT_VIEW_DEBUG)Log.d (TAG, "width is: " + getWidth () + ", height is: " + getHeight ());

        // 重新测量文本范围，因为如果是动态更改而且还是精确测量模式
        // 那么不执行onMeasure()，也就没有测量新的文本范围
        mTextPaint.getTextBounds (mText, 0, mText.length (), mTextBound);

        int width = getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int textWidth = mTextBound.width();
        int textHeight = mTextBound.height();

        // 着色器的顶点坐标
        float left;
        float right;
        float top;
        float bottom;

        // 绘制的文字位置, x是文本重点横坐标,y是文本基线纵坐标
        float x;
        float y;

        // 如果不够显示，那就不显示，直接返回
        if(mIsWidthExactly && width - paddingLeft - paddingRight < textWidth)return;
        if(mIsHeightExactly && height - paddingTop - paddingBottom < textHeight)return;

        // 竖直方向
        final int verticalGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
        if(verticalGravity == Gravity.TOP){
            top = paddingTop;
            bottom = paddingTop + textHeight;
            if(mIsHeightExactly){
                y = (float) (height * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2 + (float) (((paddingTop - (height - paddingTop - textHeight)) * 1.0) / 2);
            }else{
                y = (float) (height * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2 + (float) (((paddingTop - (paddingBottom)) * 1.0) / 2);
            }
        }else if(verticalGravity == Gravity.BOTTOM){
            top = height - paddingBottom - textHeight;
            bottom = height - textHeight;
            if(mIsHeightExactly){
                y = (float) (height * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2 + (float) ((((height - paddingBottom - textHeight) - (paddingBottom)) * 1.0) / 2);
            }else{
                y = (float) (height * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2 + (float) (((paddingTop - (paddingBottom)) * 1.0) / 2);
            }
        }else{ // verticalGravity == Gravity.CENTER_VERTICAL
            // 不考虑padding
            top = (float) ((height - textHeight) * 1.0 / 2);
            bottom = (float) ((height - textHeight) * 1.0 / 2) + textHeight;
            y = (float) (height * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2;
        }

        // 水平方向
        final int horizontalGravity = mGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        if(horizontalGravity == Gravity.LEFT){
            left = paddingLeft;
            right = textWidth + paddingLeft;
            x = paddingLeft + (float) (textWidth * 1.0 / 2);
        }else if(horizontalGravity == Gravity.RIGHT){
            left = width - textWidth - paddingRight;
            right = width - paddingRight;
            x = width - paddingRight - (float) (textWidth * 1.0 / 2);
        }else{ // horizontalGravity == Gravity.CENTER_HORIZONTAL
            // 不考虑padding
            left = (float) ((width - textWidth) * 1.0 / 2);
            right = (float) ((width - textWidth) * 1.0 / 2) + textWidth;
            x = (float) (width * 1.0 / 2);
        }

        if(Consts.COLOR_TEXT_VIEW_DEBUG)Log.d(TAG, "shader position, left is : " + left + ", right is : " + right + ", top is : " + top + ", bottom is : " + bottom);
        LinearGradient gradient = new LinearGradient (left, top, right, bottom, mStartColor, mEndColor, Shader.TileMode.REPEAT);
        mTextPaint.setShader (gradient);

        if(Consts.COLOR_TEXT_VIEW_DEBUG)Log.d(TAG, "text position, x is : " + x + ", y is : " + y);
        canvas.drawText (mText, x, y, mTextPaint);
    }

    /**
     * 设置文字
     * @param text 文字
     */
    public void setText(String text) {
        mText = text;
        // 文字改变会改变view的尺寸，因此应该重新布局
        if(mIsSizeExactly){
            invalidate();
        }else{
            requestLayout();
        }
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
        // 文字改变会改变view的尺寸，因此应该重新布局
        if(mIsSizeExactly){
            invalidate();
        }else{
            requestLayout();
        }
    }
}
