package com.github.xiaogegechen.fineviews;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.xiaogegechen.library.ColorTextView;
import com.github.xiaogegechen.library.CornerButton;
import com.github.xiaogegechen.library.MenuView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String[] TEST = {"h", "he", "hel", "hell", "hello"};
    private static List<Item> sItemList = new ArrayList<>();

    static {
        for (int i = 0; i < 7; i++) {
            Item item = new Item(R.drawable.ic_cancel, Color.parseColor("#33334c"), Color.parseColor("#c74870"));
            sItemList.add(item);
        }
    }

    private CornerButton mShareButton;
    private ColorTextView mColorTextView;
    private MenuView mMenuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        mShareButton = findViewById (R.id.share_button);
        mColorTextView = findViewById(R.id.color_text_view);
        mMenuView = findViewById(R.id.menu_view);

        mColorTextView.setOnClickListener(v -> {
            int random = new Random().nextInt(TEST.length);
            String content = TEST[random];
            mColorTextView.setText(content);
        });

        mShareButton.setOnClickListener (v -> {
            Log.d (TAG, "onClick: ");
            int random = new Random().nextInt(TEST.length);
            String content = TEST[random];
            mShareButton.setText(content);
        });

        mMenuView.setAdapter(new Adapter(sItemList, mMenuView));

        findViewById(R.id.cons_button).setOnClickListener(v -> {
            mMenuView.open();
        });

        mMenuView.setOpenAnimatorListener(new MenuView.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "start open");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "end open");
            }
        });

        mMenuView.setCloseAnimatorListener(new MenuView.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "start close");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "end close");
            }
        });

        mMenuView.makeViewSelected(3);
    }

    static class Adapter extends MenuView.Adapter {

        private List<Item> mItemList;
        private MenuView mMenuView;

        private Map<Integer, View> mViewMap = new HashMap<>();

        // 目前被选中的位置
        private int mCurrentSelectedPosition = -1;
        // 上一个被选中的位置
        private int mLastSelectedPosition = -1;

        public Adapter(List<Item> itemList, MenuView menuView) {
            mItemList = itemList;
            mMenuView = menuView;
        }

        @Override
        public View getView(int position, ViewGroup parent) {
            final Item item = mItemList.get(position);
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

            ImageView imageView = view.findViewById(R.id.item_image);
            imageView.setBackgroundColor(item.getNormalColor());
            imageView.setImageResource(item.getIconId());
            imageView.setOnClickListener(v -> {
                // 更新两个position
                if(position != mCurrentSelectedPosition){
                    mLastSelectedPosition = mCurrentSelectedPosition;
                }
                mCurrentSelectedPosition = position;
                Log.d(TAG, "onClick: " + position + ", mLastSelectedPosition is : " + mLastSelectedPosition);
                v.setBackgroundColor(item.getClickedColor());
                // 还原上一个view
                if(mLastSelectedPosition != -1){
                    View lastSelectedView = mViewMap.get(mLastSelectedPosition);
                    if(lastSelectedView != null){
                        Log.d(TAG, "reset last view: ");
                        lastSelectedView.findViewById(R.id.item_image).setBackgroundColor(item.getNormalColor());
                    }
                }
                // 响应点击
                mMenuView.close(1, 3);
            });
            mViewMap.put(position, view);
            return view;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public void makeViewSelected(int position) {
            super.makeViewSelected(position);
            mCurrentSelectedPosition = position;
            final Item item = mItemList.get(position);
            final View view = mViewMap.get(position);
            if (view != null) {
                view.findViewById(R.id.item_image).setBackgroundColor(item.getClickedColor());
            }
        }
    }
}
