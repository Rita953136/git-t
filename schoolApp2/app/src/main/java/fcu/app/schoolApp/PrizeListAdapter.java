package fcu.app.schoolApp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PrizeListAdapter extends RecyclerView.Adapter<PrizeListAdapter.PrizeViewHolder> {

    private List<String> prizeList;

    public PrizeListAdapter(List<String> prizeList) {
        this.prizeList = prizeList;
    }

    @NonNull
    @Override
    public PrizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prize, parent, false);
        return new PrizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrizeViewHolder holder, int position) {
        String prize = prizeList.get(position);
        holder.textPrize.setText(prize);

        holder.btnDelete.setOnClickListener(v -> {
            prizeList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, prizeList.size());
        });
    }

    @Override
    public int getItemCount() {
        return prizeList.size();
    }

    static class PrizeViewHolder extends RecyclerView.ViewHolder {
        TextView textPrize;
        ImageButton btnDelete;

        PrizeViewHolder(@NonNull View itemView) {
            super(itemView);
            textPrize = itemView.findViewById(R.id.textPrize);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
