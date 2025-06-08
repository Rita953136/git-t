package fcu.app.schoolApp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupSelectorAdapter extends RecyclerView.Adapter<GroupSelectorAdapter.ViewHolder> {

    public interface OnGroupSelectedListener {
        void onGroupSelected(String groupId);
    }

    public interface OnGroupDeleteListener {
        void onDelete(String groupId);
    }

    private OnGroupDeleteListener deleteListener;

    public void setOnGroupDeleteListener(OnGroupDeleteListener listener) {
        this.deleteListener = listener;
    }

    public static class GroupItem {
        public String groupId;
        public String title;
        public int optionCount;

        public GroupItem(String groupId, String title, int optionCount) {
            this.groupId = groupId;
            this.title = title;
            this.optionCount = optionCount;
        }
    }

    private final List<GroupItem> groupList;
    private final OnGroupSelectedListener listener;

    public GroupSelectorAdapter(List<GroupItem> groupList, OnGroupSelectedListener listener) {
        this.groupList = groupList;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textGroupName, textOptionCount;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textGroupName = itemView.findViewById(R.id.textGroupName);
            textOptionCount = itemView.findViewById(R.id.textOptionCount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(GroupItem item) {
            textGroupName.setText(item.title);
            textOptionCount.setText("共 " + item.optionCount + " 項");

            itemView.setOnClickListener(v -> listener.onGroupSelected(item.groupId));

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(item.groupId);
                }
            });
        }
    }

    @NonNull
    @Override
    public GroupSelectorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupSelectorAdapter.ViewHolder holder, int position) {
        holder.bind(groupList.get(position));
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }
}
