package com.albakm.tubecounter;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PlusMinusCounterHelper implements View.OnClickListener{
    private int mCount=0;
    private Button mBtnMinus;
    private Button mBtnPlus;
    private TextView mTextViewCount;
    private int mMinValue=0;
    private int mMaxValue=100;

    public PlusMinusCounterHelper(View includeView, int nResIdDescription, int nValue, int nMinValue, int nMaxValue){
        mBtnMinus=(Button)includeView.findViewById(R.id.btnMinus);
        mBtnPlus=(Button)includeView.findViewById(R.id.btnPlus);
        ((TextView)includeView.findViewById(R.id.textViewDesrciption)).setText(nResIdDescription);
        mTextViewCount=(TextView)includeView.findViewById(R.id.textViewCountedItems);

        mMaxValue=nMaxValue;
        mMinValue=nMinValue;
        mCount=nValue;

        mTextViewCount.setText(String.valueOf(mCount));

        mBtnMinus.setOnClickListener(this);
        mBtnPlus.setOnClickListener(this);
    }

    public int getCount(){
        return mCount;
    }

    public void setCount(int nCount){
        if(nCount<mMinValue||nCount>mMaxValue)
            return;
        mCount=nCount;
        mTextViewCount.setText(String.valueOf(mCount));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnMinus:
                setCount(mCount-1);
                break;
            case R.id.btnPlus:
                setCount(mCount+1);
                break;
        }
    }
}
