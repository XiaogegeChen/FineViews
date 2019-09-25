package com.github.xiaogegechen.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;

/**
 * 圆角button，右侧带有一个图标，因为边界的线条有可能显示不完全
 * 所以使用时候要注意设置padding
 * 同时提供内容与边界的上下padding,左右强制居中,保证美观
 */
public class CornerButton extends View {

    private static final String TAG = "CornerButton";

    private static final int BORDER_WIDTH_DEFAULT = 0;
    private static final int TEXT_SIZE_DEFAULT = 40;
    private static final int DISTANCE_DEFAULT = 0;
    private static final int INNER_PADDING_TOP_DEFAULT = 10;
    private static final int INNER_PADDING_BOTTOM_DEFAULT = 10;

    private int mStartColor;
    private int mEndColor;
    private int mBorderColor;
    private int mTextColor;
    private int mBorderWidth;
    private String mText;
    private int mTextSize;
    private Drawable mIcon;
    private int mDistance;
    private int mInnerPaddingTop;
    private int mInnerPaddingBottom;

    private Paint mTextPaint;
    private Paint mBgPaint;
    private Paint mBorderPaint;

    // 记录测量模式，如果是精确模式，那么所有的动态更改都
    // 不需要重新布局的，如果不是精确模式，那么有的属性动态更改之后
    // 需要重新布局。这样可以避免不需要的重新布局，提高性能
    private boolean mIsSizeExactly = false;
    private boolean mIsWidthExactly = false;
    private boolean mIsHeightExactly = false;

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
        mInnerPaddingTop = a.getDimensionPixelSize(R.styleable.CornerButton_corner_button_padding_top, INNER_PADDING_TOP_DEFAULT);
        mInnerPaddingBottom = a.getDimensionPixelSize(R.styleable.CornerButton_corner_button_padding_bottom, INNER_PADDING_BOTTOM_DEFAULT);
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
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "perform measure");
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
            mIsHeightExactly = true;
            mIsWidthExactly = true;
            setMeasuredDimension(widthSize, heightSize);
            return;
        }
        mIsSizeExactly = false;

        int width;
        int height;

        // padding
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

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
            mIsHeightExactly = true;
            height = heightSize;
        }else{
            mIsHeightExactly = false;
            height = paddingBottom + paddingTop + textHeight + mInnerPaddingBottom + mInnerPaddingTop;
        }

        // 宽，忽略使用者设置的padding
        if(widthMode == MeasureSpec.EXACTLY){
            mIsWidthExactly = true;
            width = widthSize;
        }else{
            // 默认的宽是内容刚好不在圆内
            mIsWidthExactly = false;
            width = contentWidth + (textHeight + mInnerPaddingBottom + mInnerPaddingTop) + paddingLeft + paddingRight;
        }
        Log.d(TAG, "view size, width is : " + width + ", height is : " + height);
        setMeasuredDimension(width, height);
    }

    private Path mBorderPath;
    private RectF mLeftRectF;
    private RectF mRightRectF;
    private LinearGradient mBgLinearGradient;
    private Rect mTextBound;

    @Override
    protected void onDraw(Canvas canvas) {
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "perform draw");
        super.onDraw (canvas);

        // 重新测量文本范围，因为如果是动态更改而且还是精确测量模式
        // 那么不执行onMeasure()，也就没有测量新的文本范围
        mTextPaint.getTextBounds (mText, 0, mText.length (), mTextBound);

        // 宽高和内边距
        int width = getWidth ();
        int height = getHeight ();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop ();
        int paddingBottom = getPaddingBottom ();
        int realWidth = width - paddingLeft - paddingRight;
        int realHeight = height - paddingBottom - paddingTop;

        // 着色器的顶点坐标,让view居中，即使使用者设置的四周padding不一样也要居中，美观！
        float left = (float) ((paddingLeft + paddingRight) * 1.0 / 2);
        float right = width - (float) ((paddingTop + paddingBottom) * 1.0 / 2);
        float top = (float) ((paddingTop + paddingBottom) * 1.0 / 2);
        float bottom = height - (float) ((paddingTop + paddingBottom) * 1.0 / 2);

        // (1)画背景
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw background");
        // 定位圆角button的位置
        if (mBgLinearGradient == null) {
            mBgLinearGradient = new LinearGradient (left, top, right, bottom, mStartColor, mEndColor, Shader.TileMode.REPEAT);
        }
        mBgPaint.setShader (mBgLinearGradient);
        if(realWidth <= realHeight){
            // 背景是圆形
            canvas.drawCircle ((float) ((left + right) * 1.0 / 2), (float) ((top + bottom) * 1.0 / 2), (float) (realWidth * 1.0 / 2), mBgPaint);
        } else {
            // 背景是圆角矩形
            float r = (float) (realHeight * 1.0 / 2);
            mLeftRectF.set (left, top, left + r * 2, top + r * 2);
            mRightRectF.set (right - r * 2, bottom - r * 2, right, bottom);
            mBorderPath.moveTo (right - r , bottom);
            mBorderPath.lineTo (left + r, bottom);
            mBorderPath.arcTo (mLeftRectF, 90, 180, false);
            mBorderPath.lineTo (right - r , top);
            mBorderPath.arcTo (mRightRectF, 270, 180, false);
            canvas.drawPath (mBorderPath, mBgPaint);
        }

        // (2)画边框
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw border");
        if(realWidth <= realHeight){
            // 边框是圆形
            canvas.drawCircle ((float) ((left + right) * 1.0 / 2), (float) ((top + bottom) * 1.0 / 2), (float) (realWidth * 1.0 / 2), mBorderPaint);
        } else {
            // 边框是圆角矩形
            canvas.drawPath (mBorderPath, mBorderPaint);
        }

        // (3)确定文字位置，画文字
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw text");
        mTextPaint.getTextBounds (mText, 0, mText.length (), mTextBound);
        // 文字、图标、内容宽高
        int textHeight;
        int textWidth;
        int iconWidth;
        int iconHeight;
        int contentWidth;

        // 绘制的文字位置, textPositionX是文本重点横坐标,textPositionY是文本基线纵坐标
        float textPositionX;
        float textPositionY;

        textHeight = mTextBound.height ();
        textWidth = mTextBound.width ();
        iconWidth = mIcon.getIntrinsicWidth();
        iconHeight = mIcon.getIntrinsicHeight ();
        // 为了保证文字和图标等高，需要对图标缩放
        float scale = (float) (textHeight * 1.0 / iconHeight);
        iconHeight = textHeight;
        iconWidth = (int) (iconWidth * scale);
        contentWidth = textWidth + iconWidth + mDistance;

        // 如果圆角button装不下文字和图标，那就不显示内容
        // 这种情况只会发生在使用者指定了宽高的情况下，没指定的情况下
        // 不会发生
        if(mIsWidthExactly && contentWidth > realWidth)return;
        if(mIsHeightExactly && textHeight > realHeight)return;

        textPositionX = (float) ((left + right + textWidth - contentWidth) * 1.0 / 2);
        textPositionY = top + (float)(realHeight * 1.0 / 2) - (mTextPaint.getFontMetrics ().top + mTextPaint.getFontMetrics ().bottom) / 2 + (float) (((mInnerPaddingTop - mInnerPaddingBottom) * 1.0) / 2);
        canvas.drawText (mText, textPositionX, textPositionY, mTextPaint);

        // (4)确定图标位置,画图标
        if(Consts.CORNER_BUTTON_DEBUG)Log.d(TAG, "draw icon");
        int iconLeft = (int) (textPositionX + (textWidth * 1.0) / 2 + mDistance);
        int iconTop = (int) (top + mInnerPaddingTop);
        int iconRight = iconLeft + iconWidth;
        int iconBottom = iconTop + iconHeight;
        mIcon.setBounds (iconLeft, iconTop, iconRight, iconBottom);
        mIcon.draw (canvas);
        canvas.save();
        canvas.restore();
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
//            // 不知道为什么图标更改不会导致requestLayout()执行measure流程
//            // 先手动设置
//            invalidate ();
        }
    }

    public void setIcon(@DrawableRes int resourceId){
        Bitmap bitmap = BitmapFactory.decodeResource (getResources (), resourceId);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        setIcon(drawable);
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
