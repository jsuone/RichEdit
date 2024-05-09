/*
package com.example.alarmclock;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacklogListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
*/
/*        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backlog_list);
        ArrayList<Group> groupList = new ArrayList<>();
        ArrayList<Group.Item> item1 = new ArrayList<>();
        item1.add(new Group.Item("备忘录","2023年11月19号 8：00",false));
        item1.add(new Group.Item("备忘录不要忘记相信你还真的封口机阿斯顿和福克斯大黄蜂卡商对接范德萨开发计划卡商对接","2023年11月29号 9：00",false));
        groupList.add(new Group("已过期",item1));

        ArrayList<Group.Item> items = new ArrayList<>();
        items.add(new Group.Item("每日任务","2024年3月9号 8：00",false));
        items.add(new Group.Item("每日任务","2024年3月9号 8：00",false));
        groupList.add(new Group("待办",items));
        ArrayList<Group.Item> item2 = new ArrayList<>();
        item2.add(new Group.Item("备忘录gegui","2023年11月29号 8：00",true));
        item2.add(new Group.Item("备忘录不要忘记相信你还真的封口机阿斯顿和福克斯大黄蜂卡商对接范德萨开发计划卡商对接","2024年12月19号 8：00",true));
        groupList.add(new Group("已完成",item2));
        GroupedListAdapter groupAdapter = new GroupedListAdapter(this,groupList);
        ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent intent = result.getData();
                    groupAdapter.updateListData(intent);
                }
            }
        });
        groupAdapter.setOnChildClickListener(new GroupedRecyclerViewAdapter.OnChildClickListener() {
            @Override
            public void onChildClick(GroupedRecyclerViewAdapter adapter, BaseViewHolder holder, int groupPosition, int childPosition) {
                Intent intent = new Intent(BacklogListActivity.this,BacklogCompileActivity.class);
                GroupedListAdapter groupedListAdapter = (GroupedListAdapter) adapter;
                Group.Item item = groupedListAdapter.getGroupForPosition(groupPosition).getItems().get(childPosition);
                intent.putExtra("backlog_isChecked",Boolean.valueOf(item.isChecked()));
                intent.putExtra("backlog_title",item.getBacklog_name());
                intent.putExtra("UUID",childPosition);
                intent.putExtra("GROUPID",groupPosition);
                //还要加入时间
                //startActivity(intent);//采用隐式启动反而报错，可能是libarary的问题
                intentActivityResultLauncher.launch(intent);
            }
        });
        recyclerView = findViewById(R.id.clock_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        recyclerView.setAdapter(groupAdapter);*//*

    }
}*/
