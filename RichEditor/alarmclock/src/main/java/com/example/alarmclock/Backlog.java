package com.example.alarmclock;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

public class Backlog extends LinearLayout implements View.OnClickListener{
    private CheckBox clockIsEndView;
    private TextView clockRemarkView;
    private TextView clockTimeView;
    private ImageButton clockMoreOptionView;
    private OnCheckedClickListener onCheckedClickListener;
    private OnClickListener moreOptionListener;
    public Backlog(Context context) {
        this(context,null);
    }

    public Backlog(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Backlog(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    void initView(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.module_clock_wiget,null);
        clockIsEndView = view.findViewById(R.id.clock_isEnd);
        clockRemarkView = view.findViewById(R.id.clock_remark);
        clockTimeView = view.findViewById(R.id.clock_time);
        clockMoreOptionView = view.findViewById(R.id.backlog_more_option);

        clockIsEndView.setOnClickListener(this);
        clockMoreOptionView.setOnClickListener(this);
        addView(view);
    }
    public void setClockRemarkView(String text){
        clockRemarkView.setText(text);
    }
    public void setClockTimeView(String time){
        clockTimeView.setText(time);
    }
    public void setClockIsEndView(boolean isChecked){
        clockIsEndView.setChecked(isChecked);
    }
    public interface OnCheckedClickListener{
        void onChecked(boolean isChecked);
    }
    public void setOnCheckedClickListener(OnCheckedClickListener onCheckedClickListener){
        this.onCheckedClickListener = onCheckedClickListener;
    }
    public void setMoreOptionListener(OnClickListener listener){
        this.moreOptionListener = listener;
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
         if(id == R.id.clock_isEnd){
            if(onCheckedClickListener!=null){
                onCheckedClickListener.onChecked(clockIsEndView.isChecked());
            }
        }else if(id==R.id.backlog_more_option){
             if(moreOptionListener!=null){
                 moreOptionListener.onClick(v);
             }
         }
    }
}
