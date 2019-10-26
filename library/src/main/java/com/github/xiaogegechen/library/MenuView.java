package com.github.xiaogegechen.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 一个在侧边栏类似ListView的ViewGroup，可以展开和闭合并带动画效果，由于携带多个View对象，所以适用于比较少的View列表的
 * 展示。
 * @attr ref R.styleable.MenuView_menu_view_orientation 方向，纵向或者横向
 * @attr ref R.styleable.MenuView_menu_view_reverse 方向是否倒置
 * @attr ref R.styleable.MenuView_menu_view_animation_duration 动画时长
 *
 * @since v1.3.0
 */
public class MenuView extends FrameLayout {

    private static final String TAG = "MenuView";

    /**
     * Adapter类，负责子View的创建和点击事件等。和ListView的Adapter使用方式类似，但是需要注意getView()方法不要复用
     * 之前的View，因为在之后的打开和关闭动画中都要直接操作这些View。
     */
    public static abstract class Adapter {
        /**
         * 创建子View，这里需要每一个position处都返回一个新的View，因为之后的动画需要单独操作每一个View，因此不使用
         * View的复用机制
         * @param position 子View的位置
         * @param parent 这个View的父ViewGroup，如果没有特殊情况，一定是一个LinearLayout
         * @return 指定位置上的View，一定是一个新的View对象，不能复用之前的View
         */
        public abstract View getView(int position, ViewGroup parent);

        /**
         * 拿到子View的个数
         * @return 子View的个数
         */
        public abstract int getCount();

        /**
         * 将特定的子View设置为选中状态
         * @param position 子View的位置
         *
         * @since v1.3.2
         */
        public void makeViewSelected(int position){}
    }

    /**
     * 动画监听器
     */
    public interface AnimatorListener{
        /**
         * 动画开始前回调
         * @param animation 动画
         */
        void onAnimationStart(Animator animation);

        /**
         * 动画结束后回调
         * @param animation 动画
         */
        void onAnimationEnd(Animator animation);
    }

    /**
     * 方向
     */
    @IntDef({
            HORIZONTAL,
            VERTICAL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {}
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    /**
     * 状态
     */
    @IntDef({
            OPEN,
            CLOSE,
            ANIMATING
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatusMode {}
    public static final int OPEN = 100;
    public static final int CLOSE = 101;
    public static final int ANIMATING = 102;

    // 默认时长
    private static final int DURATION_DEFAULT = 40;
    private static final Interpolator INTERPOLATOR_DEFAULT = new AccelerateDecelerateInterpolator();
    // scrollView的子view
    private LinearLayout mLinearLayout;
    // 方向
    private @OrientationMode int mOrientation;
    // 是否反向动画，正常情况下，纵向展开从上向下，关闭从下向上；横向展开从左向右，关闭从右向左。如果这时反向将颠倒顺序
    private boolean mIsReverse;
    // 动画时长
    private int mAnimationDuration;
    // 动画插值器
    private Interpolator mInterpolator;
    // 开闭动画，各个view复用，减少内存消耗
    private ObjectAnimator mOpenAnimator;
    private ObjectAnimator mCloseAnimator;
    // 动画监听
    private AnimatorListener mOpenAnimatorListener;
    private AnimatorListener mCloseAnimatorListener;
    // 当前动画迭代到的view
    private int mCurrentIndex = 0;
    // 当前状态
    private @StatusMode int mStatus = OPEN;
    // 是否已经设置过中心点
    private boolean mHasSetPivot = false;
    // adapter
    private Adapter mAdapter;

    public MenuView(Context context) {
        this(context, null);
    }

    public MenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 属性值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuView);
        mOrientation = typedArray.getInt(R.styleable.MenuView_menu_view_orientation, VERTICAL);
        mIsReverse = typedArray.getBoolean(R.styleable.MenuView_menu_view_reverse, false);
        mAnimationDuration = typedArray.getInteger(R.styleable.MenuView_menu_view_animation_duration, DURATION_DEFAULT);
        typedArray.recycle();
        mInterpolator = INTERPOLATOR_DEFAULT;
        // 手动添加 ScrollView 进去
        FrameLayout scrollView;
        mLinearLayout = new LinearLayout(context);
        if(mOrientation == VERTICAL){
            scrollView = new ScrollView(context);
            mLinearLayout.setOrientation(VERTICAL);
        }else{
            scrollView = new HorizontalScrollView(context);
            mLinearLayout.setOrientation(HORIZONTAL);
        }
        scrollView.addView(mLinearLayout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); // ScrollView 内部添加 LinearLayout
        addView(scrollView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    /**
     * 设置适配器，子View同时通过适配器拿到的
     * @param adapter 适配器
     */
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            View view = adapter.getView(i, mLinearLayout);
            mLinearLayout.addView(view);
        }
    }

    /**
     * 设置展开动画的监听器
     * @param openAnimatorListener 动画监听器
     *
     * @since v1.3.1
     */
    public void setOpenAnimatorListener(AnimatorListener openAnimatorListener) {
        mOpenAnimatorListener = openAnimatorListener;
    }

    /**
     * 设置闭合动画的监听器
     * @param closeAnimatorListener 动画监听器
     *
     * @since v1.3.1
     */
    public void setCloseAnimatorListener(AnimatorListener closeAnimatorListener) {
        mCloseAnimatorListener = closeAnimatorListener;
    }

    /**
     * 展开整个menuView，如果已经处于展开状态或者正在动画，将不处理。这个方法展开menuView中所有的子View
     *
     * @see #open(int, int)
     */
    public void open(){
        int startIndex = 0;
        int endIndex = mLinearLayout.getChildCount() - 1;
        open(startIndex, endIndex);
    }

    /**
     * 展开局部的menuView，如果已经处于展开状态或者正在动画，将不处理。这个方法展开局部的子View，通过指定开始位置和结束
     * 位置来指定展开区间
     * @param startIndex 开始位置下标
     * @param endIndex 结束位置下标
     *
     * @see #open()
     *
     * @since v1.3.1
     */
    public void open(int startIndex, int endIndex){
        // 调整下标范围，保证不会越界
        if(startIndex < 0){
            startIndex = 0;
        }
        int count = mLinearLayout.getChildCount();
        if(endIndex > (count - 1)){
            endIndex = count - 1;
        }
        openInternal(startIndex, endIndex);
    }

    /**
     * 展开动画，如果已经处于展开状态或者正在动画，将不处理。内部使用，调用时需要保证不会发生数组越界
     * @param startIndex 开始位置下标
     * @param endIndex 结束位置下标
     *
     * @see #open()
     * @see #open(int, int)
     *
     * @since v1.3.1
     */
    private void openInternal(final int startIndex, final int endIndex){
        // 设置旋转中心
        setPivot();
        // 正在动画或者已经打开不处理
        if(mStatus == ANIMATING || mStatus == OPEN){
            if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "skip open animation");
            return;
        }
        // 确定要做动画，更新状态
        mStatus = ANIMATING;
        // 根据方向不同，改变不同的属性
        Property<View, Float> property;
        if(mOrientation == VERTICAL){
            property = View.ROTATION_Y;
        }else{
            property = View.ROTATION_X;
        }
        // 从第一个view开始执行动画，当这个view动画结束后将动画实例（mOpenAnimator）的target设置为下一个view，并开启这个动画，直到最后一个
        // view执行完动画，将动画实例（mOpenAnimator）清空，以便下一次展开动画使用
        final int count = mLinearLayout.getChildCount();
        // 第一个执行动画的child
        View firstAnimatedChild;
        if(mIsReverse){
            // 反向从最后一个开始
            mCurrentIndex = count - 1 - startIndex;
            firstAnimatedChild = mLinearLayout.getChildAt(mCurrentIndex);
        }else{
            // 正常从第一个开始
            mCurrentIndex = startIndex;
            firstAnimatedChild = mLinearLayout.getChildAt(mCurrentIndex);
        }
        mOpenAnimator = ObjectAnimator.ofFloat(firstAnimatedChild, property, 90, 0);
        mOpenAnimator.setInterpolator(mInterpolator);
        mOpenAnimator.setDuration(mAnimationDuration);
        mOpenAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 指向下一个子View
                if(mIsReverse){
                    // 反向要向上迭代
                    mCurrentIndex --;
                    if(mCurrentIndex > (count - 1 - endIndex - 1)){
                        mOpenAnimator.setTarget(mLinearLayout.getChildAt(mCurrentIndex));
                        mOpenAnimator.start();
                    }else{
                        if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "open animation finish");
                        // 监听动画结束
                        notifyOpenAnimationEnd();
                        // 清空
                        mOpenAnimator = null;
                        mStatus = OPEN;
                    }
                }else{
                    // 正常向下迭代
                    mCurrentIndex ++;
                    if(mCurrentIndex < (endIndex + 1)){
                        mOpenAnimator.setTarget(mLinearLayout.getChildAt(mCurrentIndex));
                        mOpenAnimator.start();
                    }else{
                        if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "open animation finish");
                        // 监听动画结束
                        notifyOpenAnimationEnd();
                        // 清空
                        mOpenAnimator = null;
                        mStatus = OPEN;
                    }
                }
            }
        });
        // 触发动画
        if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "begin to do open animation");
        // 监听动画开始
        notifyOpenAnimationStart();
        mOpenAnimator.start();
    }

    private void notifyOpenAnimationStart(){
        if(mOpenAnimatorListener != null){
            mOpenAnimatorListener.onAnimationStart(mOpenAnimator);
        }
    }

    private void notifyOpenAnimationEnd(){
        if(mOpenAnimatorListener != null){
            mOpenAnimatorListener.onAnimationEnd(mOpenAnimator);
        }
    }

    /**
     * 关闭整个menuView，如果已经处于关闭状态或者正在动画，将不处理。这个方法将关闭整个menuView
     *
     * @see #close(int, int)
     */
    public void close(){
        int startIndex = 0;
        int endIndex = mLinearLayout.getChildCount() - 1;
        close(startIndex, endIndex);
    }

    /**
     * 关闭局部menuView，如果已经处于关闭状态或者正在动画，将不处理。这个方法将关闭指定区域的子View，通过指定起始下标和
     * 终止下标来指定区域
     * @param startIndex 其实下标
     * @param endIndex 终止下标
     *
     * @see #close()
     *
     * @since v1.3.1
     */
    public void close(int startIndex, int endIndex){
        // 调整下标范围，保证不会越界
        if(startIndex < 0){
            startIndex = 0;
        }
        int count = mLinearLayout.getChildCount();
        if(endIndex > (count - 1)){
            endIndex = count - 1;
        }
        closeInternal(startIndex, endIndex);
    }

    /**
     * 关闭动画，内部使用，调用时需要保证不会发生数组越界
     * @param startIndex 开始位置下标
     * @param endIndex 结束位置下标
     *
     * @see #close()
     * @see #close(int, int)
     *
     * @since v1.3.1
     */
    private void closeInternal(final int startIndex, final int endIndex){
        // 设置旋转中心
        setPivot();
        // 正在动画或者已经关闭不处理
        if(mStatus == ANIMATING || mStatus == CLOSE){
            if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "skip close animation");
            return;
        }
        // 确定要做动画，更新状态
        mStatus = ANIMATING;
        // 根据方向不同，改变不同的属性
        Property<View, Float> property;
        if(mOrientation == VERTICAL){
            property = View.ROTATION_Y;
        }else{
            property = View.ROTATION_X;
        }
        final int count = mLinearLayout.getChildCount();
        // 第一个执行动画的child
        View firstAnimatedChild;
        if(mIsReverse){
            // 反向从第一个开始关闭
            mCurrentIndex = count - 1- endIndex;
            firstAnimatedChild = mLinearLayout.getChildAt(mCurrentIndex);
        }else{
            // 正常从最后一个开始关闭
            mCurrentIndex = endIndex;
            firstAnimatedChild = mLinearLayout.getChildAt(mCurrentIndex);
        }
        mCloseAnimator = ObjectAnimator.ofFloat(firstAnimatedChild, property, 0, 90);
        mCloseAnimator.setInterpolator(mInterpolator);
        mCloseAnimator.setDuration(mAnimationDuration);
        mCloseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 迭代到下一个view
                if(mIsReverse){
                    // 反向向下迭代
                    mCurrentIndex ++;
                    if(mCurrentIndex < (count - 1 - startIndex + 1)){
                        mCloseAnimator.setTarget(mLinearLayout.getChildAt(mCurrentIndex));
                        mCloseAnimator.start();
                    }else{
                        if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "close animation finish");
                        // 监听动画结束
                        notifyCloseAnimationEnd();
                        // 清空
                        mCloseAnimator = null;
                        mStatus = CLOSE;
                    }
                }else{
                    // 正常向上迭代
                    mCurrentIndex --;
                    if(mCurrentIndex == (startIndex - 1)){
                        if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "close animation finish");
                        // 监听动画结束
                        notifyCloseAnimationEnd();
                        mCloseAnimator = null;
                        mStatus = CLOSE;
                    }else{
                        mCloseAnimator.setTarget(mLinearLayout.getChildAt(mCurrentIndex));
                        mCloseAnimator.start();
                    }
                }
            }
        });
        if(Consts.MENU_VIEW_DEBUG) Log.d(TAG, "begin to do close animation");
        // 监听动画开始
        notifyCloseAnimationStart();
        mCloseAnimator.start();
    }

    private void notifyCloseAnimationStart(){
        if (mCloseAnimatorListener != null) {
            mCloseAnimatorListener.onAnimationStart(mCloseAnimator);
        }
    }

    private void notifyCloseAnimationEnd(){
        if (mCloseAnimatorListener != null) {
            mCloseAnimatorListener.onAnimationEnd(mCloseAnimator);
        }
    }

    /**
     * 为动画设置插值器
     * @param interpolator 插值器
     */
    public void setAnimationInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
     * 为动画设置时长，这个时长是单个View的动画时长，不是总时长
     * @param animationDuration 单个View的动画时长
     */
    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    /**
     * 使某指定的子View处于选中状态
     * @param position 子View位置
     *
     * @since v1.3.2
     */
    public void makeViewSelected(int position){
        if (mAdapter != null) {
            mAdapter.makeViewSelected(position);
        }
    }

    // 设置子view的旋转中心点
    private void setPivot(){
        // 设置过就不再设置了
        if(!mHasSetPivot){
            int count = mLinearLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                View item = mLinearLayout.getChildAt(i);
                if(mOrientation == VERTICAL){
                    item.setPivotX(0);
                    item.setPivotY(0);
                }else{
                    item.setPivotX(0);
                    item.setPivotY(item.getHeight());
                }
            }
            // 标记为已经设置
            mHasSetPivot = true;
        }
    }

    /**
     * 获取当前状态
     * @return {@link #OPEN}、{@link #CLOSE}、{@link #ANIMATING}
     */
    public @StatusMode int getStatus() {
        return mStatus;
    }
}
