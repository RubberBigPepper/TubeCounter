package com.albakm.tubecounter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class ExtendedSpinnerHelper implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        TextWatcher {
    private SeekBar mSeekBarValue;
    private EditText mEditValue;
    private Button mBtnMinus;
    private Button mBtnPlus;
    private TextView mTextViewDescription;

    public ExtendedSpinnerHelper(View includeView, int nResIdDescription, int nValue, int nMaxValue){
        mSeekBarValue=(SeekBar)includeView.findViewById(R.id.seekValue);
        mEditValue=(EditText)includeView.findViewById(R.id.editValue);
        mTextViewDescription=(TextView)includeView.findViewById(R.id.textViewDesrciption);
        mBtnMinus=(Button)includeView.findViewById(R.id.btnMinus);
        mBtnPlus=(Button)includeView.findViewById(R.id.btnPlus);

        mSeekBarValue.setMax(nMaxValue);
        mTextViewDescription.setText(nResIdDescription);

        mBtnMinus.setOnClickListener(this);
        mBtnPlus.setOnClickListener(this);
        mEditValue.addTextChangedListener(this);
        mSeekBarValue.setOnSeekBarChangeListener(this);
        UpdateEditText();
        setValue(nValue);
    }

    public void setValue(int nValue){
        nValue--;
        if(nValue<0||nValue>mSeekBarValue.getMax())
            return;
        mSeekBarValue.setProgress(nValue);
        UpdateEditText();
    }

    public int getValue(){
        return mSeekBarValue.getProgress()+1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnMinus:
                if(mSeekBarValue.getProgress()>0)
                    mSeekBarValue.setProgress(mSeekBarValue.getProgress()-1);
                UpdateEditText();
                break;
            case R.id.btnPlus:
                if(mSeekBarValue.getProgress()<mSeekBarValue.getMax())
                    mSeekBarValue.setProgress(mSeekBarValue.getProgress()+1);
                UpdateEditText();
                break;
        }
    }

    private void UpdateEditText(){
        if(mEditValue.isFocused())
            return;
        mEditValue.removeTextChangedListener(this);
        mEditValue.setText(String.format(Locale.US,"%d",getValue()));
        mEditValue.addTextChangedListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
           UpdateEditText();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try{
            int nNewValue=Integer.valueOf(s.toString());
            setValue(nNewValue);
        }
        catch (Exception ex){}
    }
}
