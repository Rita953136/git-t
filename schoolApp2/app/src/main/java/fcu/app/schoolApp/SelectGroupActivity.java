package fcu.app.schoolApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SelectGroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar loading;
    private GroupSelectorAdapter adapter;
    private List<GroupSelectorAdapter.GroupItem> groupList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_selector);

        recyclerView = findViewById(R.id.recyclerGroupList);
        loading = findViewById(R.id.progressBar);

        adapter = new GroupSelectorAdapter(groupList, groupId -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedGroupId", groupId);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadGroups(user.getUid());
        } else {
            Toast.makeText(this, "尚未登入", Toast.LENGTH_SHORT).show();
            finish();
        }
        Button btnAddGroup = findViewById(R.id.btnAddGroup);
        btnAddGroup.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("新增群組");

            final EditText input = new EditText(this);
            input.setHint("輸入群組名稱");
            builder.setView(input);

            builder.setPositiveButton("新增", (dialog, which) -> {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {
                    FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        String groupId = UUID.randomUUID().toString();

                        Map<String, Object> newGroup = new HashMap<>();
                        newGroup.put("title", title);
                        newGroup.put("prizes", Arrays.asList("項目1", "項目2", "項目3"));

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .collection("groups")
                                .document(groupId)
                                .set(newGroup)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "已新增群組", Toast.LENGTH_SHORT).show();
                                    loadGroups(uid); // 重新載入群組
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "新增失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(this, "請輸入群組名稱", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        });

    }

    private void loadGroups(String uid) {
        loading.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("groups")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("LoadGroups", "筆數：" + querySnapshot.size()); // ✅ 加這行

                    groupList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String groupId = doc.getId();
                        String title = doc.getString("title");
                        List<?> prizesRaw = (List<?>) doc.get("prizes");
                        int count = prizesRaw != null ? prizesRaw.size() : 0;

                        groupList.add(new GroupSelectorAdapter.GroupItem(groupId, title, count));

                    }

                    adapter.notifyDataSetChanged();
                    loading.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "載入群組失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loading.setVisibility(View.GONE);
                });
    }

}
