package com.example.richeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FontSizeAdapter extends RecyclerView.Adapter {

    private List<FontSizeBean> mData;
    private OnFontSizeSelectListener mOnFontSizeSelectListener;

    public FontSizeAdapter(List<FontSizeBean> data) {
        this.mData = data;
    }

    /**
     * 设置文字大小item点击监听
     *
     * @param onFontSizeSelectListener .
     */
    public void setOnFontSizeSelectListener(OnFontSizeSelectListener onFontSizeSelectListener) {
        this.mOnFontSizeSelectListener = onFontSizeSelectListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FontSizeHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.module_editor_item_font_size, parent, false));//attachtoroot是自动把layout加入view中，不用调用root.addview,但adapter中已经自动添加，所以不能为true再添加
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FontSizeHolder fontSizeHolder = (FontSizeHolder) holder;
        final FontSizeBean bean = mData.get(holder.getAdapterPosition());
        fontSizeHolder.mFontSize.setText(String.format("%spx", bean.getTextSize()));
        fontSizeHolder.mFontSizeSelect.setVisibility(bean.isSelect() ? View.VISIBLE : View.GONE);
        fontSizeHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelect(holder.getAdapterPosition());
                notifyDataSetChanged();
                //颜色回调
                if(mOnFontSizeSelectListener != null){
                    mOnFontSizeSelectListener.onFontSizeSelect(bean, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }
    /**
     * 设置只有一个为选中
     *
     * @param pos 选中的位置
     */
    private void setSelect(int pos){
        for(FontSizeBean bean: mData){
            bean.setSelect(false);
        }
        mData.get(pos).setSelect(true);
    }
    /**
     * 文字大小holder
     */
    class FontSizeHolder extends RecyclerView.ViewHolder{

        TextView mFontSize;
        ImageView mFontSizeSelect;

        FontSizeHolder(@NonNull View itemView) {
            super(itemView);
            mFontSize = itemView.findViewById(R.id.tv_font_size);
            mFontSizeSelect = itemView.findViewById(R.id.iv_font_size_select);
        }
    }
}
