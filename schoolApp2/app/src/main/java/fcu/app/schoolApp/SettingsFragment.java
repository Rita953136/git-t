// ✅ SettingsFragment.java - 登入/登出自動切換按鈕邏輯

package fcu.app.schoolApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {}

    public static SettingsFragment newInstance(String param1, String param2) {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView textUid = view.findViewById(R.id.textUid);
        Button btnReset = view.findViewById(R.id.btnResetDefault);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Button btnEditName = null;
        if (user != null) {
            String uid = user.getUid();
            firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String name = snapshot.contains("name") ? snapshot.getString("name") : "(未命名)";
                        textUid.setText(name);
                    })
                    .addOnFailureListener(e -> {
                        textUid.setText("(讀取失敗) ");
                    });

            btnLogout.setText("登出並重設身份");
            btnLogout.setText("登出");

            // ✅ 恢復預設獎項
            btnReset.setOnClickListener(v -> {
                List<String> defaultList = Arrays.asList(
                        "炒飯", "乾麵", "麥當勞", "便當", "鹹酥雞", "滷味", "鐵板燒"
                );
                Map<String, Object> data = new HashMap<>();
                data.put("prizes", defaultList);

                firestore.collection("users")
                        .document(uid)
                        .set(data)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(getContext(), "✅ 已重設為預設獎項", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "❌ 重設失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            // ✅ 登出
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                textUid.setText("(尚未登入)");
                btnLogout.setText("登入");
                Toast.makeText(getContext(), "已登出", Toast.LENGTH_SHORT).show();
            });

            btnEditName = view.findViewById(R.id.btnEditName);
            btnEditName.setVisibility(View.VISIBLE);

            btnEditName.setOnClickListener(v -> {
                EditText input = new EditText(getContext());
                input.setHint("輸入新名稱");

                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("修改名稱")
                        .setView(input)
                        .setPositiveButton("儲存", (dialog, which) -> {
                            String newName = input.getText().toString().trim();
                            if (!newName.isEmpty()) {
                                firestore.collection("users")
                                        .document(user.getUid())
                                        .update("name", newName)
                                        .addOnSuccessListener(unused -> {
                                            textUid.setText(newName);
                                            Toast.makeText(getContext(), "✅ 名稱已更新", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(getContext(), "❌ 名稱更新失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });

        } else {
            btnEditName.setVisibility(View.GONE);
            textUid.setText("(尚未登入)");
            btnLogout.setText("登入");

            btnLogout.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

            });
            btnReset.setEnabled(false);
        }

        return view;
    }
} // end of SettingsFragment