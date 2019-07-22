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
import android.view.View;

public class CornerButton extends View {

    private static final int BORDER_WIDTH_DEFAULT = 0;
    private static final int TEXT_SIZE_DEFAULT = 40;
    private static final int DISTANCE_DEFAULT = 0;

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
        if(width <= height){
            // 边框是圆形
            canvas.drawCircle ((float) (realWidth * 1.0 / 2 + paddingLeft), (float) (realHeight * 1.0 / 2 + paddingTop), (float) (realWidth * 1.0 / 2), mBorderPaint);
        } else {
            // 边框是圆角矩形
            canvas.drawPath (mBorderPath, mBorderPaint);
        }

        // (3)确定文字位置，画文字
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
        invalidate ();
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        invalidate ();
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
        invalidate ();
    }

    public void setIcon(int resourceId){
        Bitmap bitmap = BitmapFactory.decodeResource (getResources (), resourceId);
        mIcon = new BitmapDrawable (bitmap);
        invalidate ();
    }

    public void setDistance(int distance) {
        mDistance = distance;
        invalidate ();
    }
}
