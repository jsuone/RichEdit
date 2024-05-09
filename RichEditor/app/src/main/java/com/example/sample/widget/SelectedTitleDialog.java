package com.example.sample.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sample.Impl.OnNoteCompileSelectedTitleListener;
import com.example.sample.R;
import com.example.sample.adapter.SelectedTitleListAdapter;
import com.example.sample.model.TitleModel;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;

import java.util.Map;

public class SelectedTitleDialog extends Dialog {
    private Context context;
    private RecyclerView recyclerView;
    private Button cancel;
    private Button confirm;
    private TitleModel titleModel;
    private String pos_titleGUID;//当前笔记的所属主题
    private SelectedTitleListAdapter adapter;
    public SelectedTitleDialog(@NonNull Context context,String titleGUID) {
        super(context);
        this.context = context;
        titleModel = DataManager.getInstance().getTitleData(Constant.Title_KEY);//得到主题数据集合
        pos_titleGUID = titleGUID;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_selected_title);
        initView();
    }
    void initView(){
        recyclerView = findViewById(R.id.select_title_dialog_rv);
        cancel = findViewById(R.id.select_title_dialog_cancel);
        confirm = findViewById(R.id.select_title_dialog_confirm);
        adapter = new SelectedTitleListAdapter(context,titleModel,pos_titleGUID);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        cancel.setOnClickListener(v->{
            dismiss();
        });
        confirm.setOnClickListener(v->{
           OnNoteCompileSelectedTitleListener listener = (OnNoteCompileSelectedTitleListener) context;
           listener.getSelectedData(adapter.getSelectedData());
           dismiss();
        });
        //recyclerView.scrollToPosition(adapter.getInitPosition());
    }

}
