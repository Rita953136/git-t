
package fcu.app.schoolApp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PrizeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_INPUT = 1;

    private List<String> prizeList;

    public PrizeListAdapter(List<String> prizeList) {
        this.prizeList = prizeList;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == prizeList.size()) ? VIEW_TYPE_INPUT : VIEW_TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return prizeList.size() + 1; // +1 for input
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_INPUT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_prize_input, parent, false);
            return new InputViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_prize, parent, false);
            return new PrizeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PrizeViewHolder) {
            String prize = prizeList.get(position);
            ((PrizeViewHolder) holder).textPrize.setText(prize);

            ((PrizeViewHolder) holder).btnDelete.setOnClickListener(v -> {
                prizeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, prizeList.size());
            });
        }
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

    class InputViewHolder extends RecyclerView.ViewHolder {
        EditText editText;
        Button btnAdd;

        public InputViewHolder(@NonNull View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.editTextNewPrize);
            btnAdd = itemView.findViewById(R.id.btnConfirmAdd);

            btnAdd.setOnClickListener(v -> {
                String newItem = editText.getText().toString().trim();
                if (!newItem.isEmpty()) {
                    prizeList.add(newItem);
                    notifyItemInserted(prizeList.size() - 1);
                    editText.setText("");
                }
            });
        }
    }
}
