package com.github.xiaogegechen.fineviews;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.xiaogegechen.library.ColorTextView;
import com.github.xiaogegechen.library.CornerButton;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CornerButton mShareButton;
    private ColorTextView mColorTextView;
    private Button mButton;

    private static final String[] TEST = {"h", "he", "hel", "hell", "hello"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        mShareButton = findViewById (R.id.share_button);
        mColorTextView = findViewById(R.id.color_text_view);
        mButton = findViewById(R.id.button);
        mColorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int random = new Random().nextInt(TEST.length);
                String content = TEST[random];
                mColorTextView.setText(content);
            }
        });
        mShareButton.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Log.d (TAG, "onClick: ");
                mShareButton.setIcon (R.drawable.change);
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButton.setText("hello world");
            }
        });
    }
}
