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
    private String groupId;

    public BottomSheetPrizeEditor(List<String> initialList, OnPrizeSaveListener listener, String groupId) {
        this.listener = listener;
        this.prizeList = new ArrayList<>(initialList);
        this.groupId = groupId;
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

        adapter = new PrizeListAdapter(prizeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 加入項目
        btnAdd.setOnClickListener(v -> {
            String newPrize = inputPrize.getText().toString().trim();
            if (!newPrize.isEmpty()) {
                prizeList.add(newPrize);
                adapter.notifyItemInserted(prizeList.size() - 1);
                inputPrize.setText("");
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && groupId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .collection("groups")
                    .document(groupId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists() && snapshot.contains("title")) {
                            inputTitle.setText(snapshot.getString("title"));
                        }
                    });
        }

        // 儲存按鈕：寫入 prizeList + title
        btnSave.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPrizeSaved(new ArrayList<>(prizeList));
            }

            if (user != null && groupId != null) {
                String title = inputTitle.getText().toString().trim();
                Map<String, Object> data = new HashMap<>();
                data.put("prizes", prizeList);
                data.put("title", title);

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .collection("groups")
                        .document(groupId)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "已儲存", Toast.LENGTH_SHORT).show();
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "儲存失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                dismiss();
            }
        });

        return view;
    }
}

