package fcu.app.schoolApp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BottomSheetPrizeEditor extends BottomSheetDialogFragment {

    public interface OnPrizeSaveListener {
        void onPrizeSaved(List<String> currentPrizes);
    }

    private OnPrizeSaveListener listener;
    private List<String> prizeList = new ArrayList<>();
    private PrizeListAdapter adapter;

    public BottomSheetPrizeEditor(List<String> initialList, OnPrizeSaveListener listener) {
        this.listener = listener;
        this.prizeList = new ArrayList<>(initialList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_prize_editor, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerPrize);
        EditText inputPrize = view.findViewById(R.id.inputPrize);
        EditText inputTitle = view.findViewById(R.id.inputTitle);
        Button btnAdd = view.findViewById(R.id.btnAddPrize);
        Button btnSave = view.findViewById(R.id.btnSavePrize);

        //初始化 RecyclerView
        adapter = new PrizeListAdapter(prizeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 新增項目
        btnAdd.setOnClickListener(v -> {
            String newPrize = inputPrize.getText().toString().trim();
            if (!newPrize.isEmpty()) {
                prizeList.add(newPrize);
                adapter.notifyItemInserted(prizeList.size() - 1);
                inputPrize.setText("");
            }
        });
        //載入現有 title（從 Firebase）
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists() && snapshot.contains("title")) {
                            inputTitle.setText(snapshot.getString("title"));
                        }
                    });
        }

        //儲存按鈕：寫入 prizeList + title
        btnSave.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPrizeSaved(new ArrayList<>(prizeList));
            }

            if (user != null) {
                String title = inputTitle.getText().toString().trim();
                Map<String, Object> data = new HashMap<>();
                data.put("prizes", prizeList);
                data.put("title", title);
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .set(data, SetOptions.merge());
            } else {
                dismiss();
            }
        });

        return view;
    }
}
