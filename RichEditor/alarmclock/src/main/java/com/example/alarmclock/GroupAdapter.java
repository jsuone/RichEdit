//package com.example.alarmclock;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.recyclerview.widget.AsyncListDiffer;
//import androidx.recyclerview.widget.DiffUtil;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private List<Group> groupList;
//    private OnItemClickListener onItemClickListener;
//    private final AsyncListDiffer<Group> mDiffer = new AsyncListDiffer<Group>(this,)
//    public GroupAdapter(List<Group> groupList) {
//        this.groupList = groupList;
//    }
//
//    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
//        this.onItemClickListener = onItemClickListener;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//       if(viewType == 0){
//           View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.title_group,parent,false);
//           return new GroupHolder(view);
//       }else {
//           View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group,parent,false);
//           return new ItemHolder(view);
//       }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        int count = 0;
//        for (Group group : groupList) {
//            int size = 0;
//            size = group.getItems().size()+1;
//            if(position<count+size){
//                if(position == count){
//                    GroupHolder groupHolder = (GroupHolder) holder;
//                    groupHolder.title.setText(group.getName());
//                    return;
//                }else{
//                    ItemHolder itemHolder = (ItemHolder) holder;
//                    Group.Item item = group.getItems().get(position-count-1);
//                    itemHolder.backlog.setClockRemarkView(item.getBacklog_name());
//                    itemHolder.backlog.setClockTimeView(item.getBacklog_time());
//                    itemHolder.backlog.setClockIsEndView(item.isChecked());
//                    itemHolder.backlog.setOnClickListener(v->{
//                        onItemClickListener.OnItemClick(item,position);
//                    });
//                    return;//如果不加返回结束，再进行绑定hodler的时候会在此进行多余循环以至于holder类型转换错误
//                }
//            }
//            count += size;
//        }
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        int count = 0;
//        for (Group group : groupList) {
//            int size = group.getItems().size()+1;
//            if( position < count+size){
//                 return position==count ?0:1;
//            }
//            count+=size;
//        }
//        throw new IllegalArgumentException("Invalid position");
//
//    }
//
//    @Override
//    public int getItemCount() {
//        int count = 0;
//        for (Group group : groupList) {
//            count += group.getItems().size()+1;//每一个组还有一个标题，这也占一个元素所以加一
//        }
//        return count;
//    }
//
//   static class ItemHolder extends RecyclerView.ViewHolder {
//        private Backlog backlog;
//       public ItemHolder(@NonNull View itemView) {
//           super(itemView);
//           backlog = itemView.findViewById(R.id.backlog_item);
//       }
////
////       @Override
////       public void onClick(View v) {
////           if(v.getId() == R.id.backlog_item){
////               //进入设置界面 接收返回数据设置显示的数据
////               if(item!=null&&mOnItemClickListener!=null) {
////                   mOnItemClickListener.OnItemClick(item);
////               }
////           }
////       }
//   }
//   static class GroupHolder extends RecyclerView.ViewHolder{
//        private TextView title;
//       public GroupHolder(@NonNull View itemView) {
//           super(itemView);
//           title = itemView.findViewById(R.id.backlog_title);
//       }
//   }
//
//   static class  MyDiffUtilCallback extends DiffUtil.ItemCallback<Group>{
//
//       @Override
//       public boolean areItemsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
//           return false;
//       }
//
//       @Override
//       public boolean areContentsTheSame(@NonNull Group oldItem, @NonNull Group newItem) {
//           return false;
//       }
//
//       @Nullable
//       @Override
//       public Object getChangePayload(@NonNull Group oldItem, @NonNull Group newItem) {
//           return super.getChangePayload(oldItem, newItem);
//       }
//   }
//}
