package com.github.xiaogegechen.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 圆角button，右侧带有一个图标，padding仿照button的设计
 * 内容在水平居中，竖直方向的padding为内容的padding，不包括边框
 */
public class CornerButton extends View {

    private static final String TAG = "CornerButton";

    private static final int BORDER_WIDTH_DEFAULT = 0;
    private static final int TEXT_SIZE_DEFAULT = 40;
    private static final int DISTANCE_DEFAULT = 0;
    // 默认的padding
    private static final int PADDING_DEFAULT = 10;

    private int mStartColor;
    private int mEndColor;
    private int mBorderColor;
    private int mTextColor;
    private int mBorderWidth;
    private String mText;
    private int mTextSize;
    private Drawable mIcon;
    private int mDistance;

    private Paint mTextPaint;
    private Paint mBgPaint;
    private Paint mBorderPaint;

    // 记录测量模式，如果是精确模式，那么所有的动态更改都
    // 不需要重新布局的，如果不是精确模式，那么有的属性动态更改之后
    // 需要重新布局。这样可以避免不需要的重新布局，提高性能
    private boolean mIsSizeExactly = false;

    public CornerButton(Context context) {
        this (context, null);
    }

    public CornerButton(Context context, AttributeSet attrs) {
        this (context, attrs, 0);
    }

    public CornerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super (context, attrs, defStyleAttr);
        // 属性值
        TypedArray a = context.obtainStyledAttributes (attrs, R.styleable.CornerButton);
        mStartColor = a.getColor (R.styleable.CornerButton_corner_button_start_color, getResources ().getColor (R.color.corner_button_start_color));
        mEndColor = a.getColor (R.styleable.CornerButton_corner_button_end_color, getResources ().getColor (R.color.corner_button_end_color));
        mBorderColor = a.getColor (R.styleable.CornerButton_corner_button_border_color, getResources ().getColor (R.color.corner_button_border_color));
        mTextColor = a.getColor (R.styleable.CornerButton_corner_button_text_color, getResources ().getColor (R.color.corner_button_text_color));
        mBorderWidth = a.getDimensionPixelSize (R.styleable.CornerButton_corner_button_border_width, BORDER_WIDTH_DEFAULT);
        mText = a.getString (R.styleable.CornerButton_corner_button_text);
        mTextSize = a.getDimensionPixelSize (R.styleable.CornerButton_corner_button_text_size, TEXT_SIZE_DEFAULT);
        mIcon = a.getDrawable (R.styleable.CornerButton_corner_button_icon);
        mDistance = a.getDimensionPixelSize (R.styleable.CornerButton_corner_button_distance, DISTANCE_DEFAULT);
        a.recycle ();

        // 可点击
        setClickable (true);

        mTextPaint = new Paint ();
        mTextPaint.setTextSize (mTextSize);
        mTextPaint.setStyle (Paint.Style.STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor (mTextColor);
        mTextPaint.setTextAlign (Paint.Align.CENTER);

        mBorderPaint = new Paint ();
        mBorderPaint.setStyle (Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth (mBorderWidth);
        mBorderPaint.setColor (mBorderColor);

        mBgPaint = new Paint ();
        mBgPaint.setStyle (Paint.Style.FILL);
        mBgPaint.setAntiAlias(true);

        mBorderPath = new Path ();
        mLeftRectF = new RectF ();
        mRightRectF = new RectF ();
        mTextBound = new Rect ();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode (widthMeasureSpec);
        int widthSize = MeasureSpec.getSize (widthMeasureSpec);
        int heightMode = MeasureSpec.getMode (heightMeasureSpec);
        int heightSize = MeasureSpec.getSize (heightMeasureSpec);

        //测量文字
        mTextPaint.getTextBounds (mText, 0, mText.length (), mTextBound);

        // 如果是精确测量模式，就不需要下面的操作
        if(widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
            if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "Measure exactly");
            //更新标记位
            mIsSizeExactly = true;
            setMeasuredDimension(widthSize, heightSize);
            return;
        }

        int width;
        int height;

        // 文字宽高
        int textHeight = mTextBound.height ();
        int textWidth = mTextBound.width ();
        // 图标原始宽高
        int iconWidth = mIcon.getIntrinsicWidth();
        int iconHeight = mIcon.getIntrinsicHeight ();
        // 缩放比
        float scale = (float) (textHeight * 1.0 / iconHeight);
        // 图标宽高
        iconWidth = (int) (iconWidth * scale);
        // 内容宽
        int contentWidth = textWidth + iconWidth + mDistance;

        // 高
        if(heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }else{
            // 如果没有设置padding，那就使用默认值
            if(getPaddingTop() == 0 && getPaddingBottom() == 0){
                height = mTextBound.height() + PADDING_DEFAULT * 2;
            }else{
                // 如果有设置padding，那么也要再加上默认值，美观
                height = mTextBound.height() + getPaddingBottom() + getPaddingTop() + PADDING_DEFAULT * 2;
            }
        }

        // 宽，忽略使用者设置的padding
        if(widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }else{
            // 默认的宽是内容刚好不在圆内
            width = contentWidth + height;
        }

        setMeasuredDimension(width, height);
    }

    private Path mBorderPath;
    private RectF mLeftRectF;
    private RectF mRightRectF;
    private LinearGradient mBgLinearGradient;
    private Rect mTextBound;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw (canvas);
        // 宽高和内边距
        int width = getWidth ();
        int height = getHeight ();
        int paddingLeft = getPaddingLeft ();
        int paddingRight = getPaddingRight ();
        int paddingTop = getPaddingTop ();
        int paddingBottom = getPaddingBottom ();
        int realWidth = width - paddingLeft - paddingRight;
        int realHeight = height - paddingTop - paddingBottom;
        // (1)画背景
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw background");
        if (mBgLinearGradient == null) {
            mBgLinearGradient = new LinearGradient (paddingLeft, paddingTop, paddingLeft + realWidth, paddingTop + realHeight, mStartColor, mEndColor, Shader.TileMode.REPEAT);
        }
        mBgPaint.setShader (mBgLinearGradient);
        if(realWidth <= realHeight){
            // 背景是圆形
            canvas.drawCircle ((float) (realWidth * 1.0 / 2 + paddingLeft), (float) (realHeight * 1.0 / 2 + paddingTop), (float) (realWidth * 1.0 / 2), mBgPaint);
        } else {
            // 背景是圆角矩形
            float r = (float) (realHeight * 1.0 / 2);
            mLeftRectF.set (paddingLeft, paddingTop, paddingLeft + r * 2, paddingTop + r * 2);
            mRightRectF.set (paddingLeft + realWidth - r * 2, paddingTop, paddingLeft + realWidth, paddingTop + r * 2);
            mBorderPath.moveTo (paddingLeft + realWidth - r , paddingTop + realHeight);
            mBorderPath.lineTo (paddingLeft + r, paddingTop + realHeight);
            mBorderPath.arcTo (mLeftRectF, 90, 180, false);
            mBorderPath.lineTo (paddingLeft + realWidth - r , paddingTop);
            mBorderPath.arcTo (mRightRectF, 270, 180, false);
            canvas.drawPath (mBorderPath, mBgPaint);
        }

        // (2)画边框
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw border");
        if(realWidth <= realHeight){
            // 边框是圆形
            canvas.drawCircle ((float) (realWidth * 1.0 / 2 + paddingLeft), (float) (realHeight * 1.0 / 2 + paddingTop), (float) (realWidth * 1.0 / 2), mBorderPaint);
        } else {
            // 边框是圆角矩形
            canvas.drawPath (mBorderPath, mBorderPaint);
        }

        // (3)确定文字位置，画文字
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw text");
        mTextPaint.getTextBounds (mText, 0, mText.length (), mTextBound);
        // 文字宽高
        int textHeight = mTextBound.height ();
        int textWidth = mTextBound.width ();
        // 图标原始宽高
        int iconWidth = mIcon.getIntrinsicWidth();
        int iconHeight = mIcon.getIntrinsicHeight ();
        // 缩放比
        float scale = (float) (textHeight * 1.0 / iconHeight);
        // 图标宽高
        iconHeight = textHeight;
        iconWidth = (int) (iconWidth * scale);
        // 内容宽
        int contentWidth = textWidth + iconWidth + mDistance;
        float textPositionX = (float) (paddingLeft + (realWidth - contentWidth + textWidth) * 1.0 / 2);
        float textPositionY = paddingTop + (float)(realHeight * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2;
        canvas.drawText (mText, textPositionX, textPositionY, mTextPaint);

        // (4)确定图标位置,画图标
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw icon");
        int iconLeft = paddingLeft + (realWidth - contentWidth) / 2 + textWidth + mDistance;
        int iconTop = paddingTop + (realHeight - iconHeight) / 2;
        int iconRight = iconLeft + iconWidth;
        int iconBottom = iconTop + iconHeight;
        mIcon.setBounds (iconLeft, iconTop, iconRight, iconBottom);
        canvas.save ();
        mIcon.draw (canvas);
    }

    // 动态设置属性
    public void setStartColor(int startColor) {
        mStartColor = startColor;
        invalidate ();
    }

    public void setEndColor(int endColor){
        mEndColor = endColor;
        invalidate ();
    }

    public void setBorderColor(int borderColor) {
        mBorderColor = borderColor;
        invalidate ();
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        invalidate ();
    }

    public void setBorderWidth(int borderWidth) {
        mBorderWidth = borderWidth;
        invalidate ();
    }

    public void setText(String text) {
        mText = text;
        // 文字改变会改变view的尺寸，因此应该重新布局
        if(mIsSizeExactly){
            invalidate();
        }else{
            requestLayout();
        }
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        // 文字改变会改变view的尺寸，因此应该重新布局
        if(mIsSizeExactly){
            invalidate();
        }else{
            requestLayout();
        }
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
        // 图标改变会改变view的尺寸，因此应该重新布局
        if(mIsSizeExactly){
            invalidate();
        }else{
            requestLayout();
        }
    }

    public void setIcon(int resourceId){
        Bitmap bitmap = BitmapFactory.decodeResource (getResources (), resourceId);
        mIcon = new BitmapDrawable (bitmap);
        // 文字改变会改变view的尺寸，因此应该重新布局
        if(mIsSizeExactly){
            invalidate();
        }else{
            requestLayout();
        }
    }

    public void setDistance(int distance) {
        mDistance = distance;
        // 文字改变会改变view的尺寸，因此应该重新布局
        if(mIsSizeExactly){
            invalidate();
        }else{
            requestLayout();
        }
    }
}
