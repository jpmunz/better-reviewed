package com.jpmunz.betterreviewed;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

public class RottenTomatoesAutoCompleteTextView extends AutoCompleteTextView {

    private static final int TEXT_CHANGED_MESSAGE_CODE = 100;
    private static final int AUTO_COMPLETE_DELAY = 750;


    private ProgressBar mLoadingIndicator;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            RottenTomatoesAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    public RottenTomatoesAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLoadingIndicator(ProgressBar progressBar) {
        mLoadingIndicator = progressBar;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        mHandler.removeMessages(TEXT_CHANGED_MESSAGE_CODE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(TEXT_CHANGED_MESSAGE_CODE, text), AUTO_COMPLETE_DELAY);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.GONE);
        }

        super.onFilterComplete(count);
    }

}
