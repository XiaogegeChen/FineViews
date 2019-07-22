package com.github.xiaogegechen.fineviews;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.xiaogegechen.library.CornerButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CornerButton mShareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        mShareButton = findViewById (R.id.share_button);
        mShareButton.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Log.d (TAG, "onClick: ");
                mShareButton.setIcon (R.drawable.change);
            }
        });
    }
}
