package fcu.app.schoolApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeFragment extends Fragment {

    private String[] defaultPrizes = {
            "炒飯", "乾麵", "麥當勞", "便當", "鹹酥雞", "滷味", "鐵板燒"
    };

    private boolean isDataLoaded = false;
    private List<String> loadedPrizes = new ArrayList<>();
    private String currentGroup = "default";
    private ActivityResultLauncher<Intent> selectGroupLauncher;

    public HomeFragment() {}

    public static HomeFragment newInstance(String param1, String param2) {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LuckyWheelView wheelView = view.findViewById(R.id.wheelView);
        ImageButton editPrizeButton = view.findViewById(R.id.btnEditPrize);
        ImageButton groupButton = view.findViewById(R.id.btnEditPrize2);
        FrameLayout loadingOverlay = view.findViewById(R.id.loadingOverlay);
        TextView titleText = view.findViewById(R.id.textTitle);
        TextView resultText = view.findViewById(R.id.textResult);
        Button resetButton = view.findViewById(R.id.btnResetWheel);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // 群組切換回傳
        selectGroupLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String selectedGroupId = result.getData().getStringExtra("selectedGroupId");
                        if (selectedGroupId != null) {
                            currentGroup = selectedGroupId;
                            if (user != null) {
                                loadPrizes(user.getUid(), currentGroup, wheelView, loadingOverlay, titleText);
                                resultText.setText("👉  ？？？ ");
                            }
                        }
                    }
                });

        loadingOverlay.setVisibility(View.VISIBLE);

        resetButton.setOnClickListener(v -> {
            if (user != null)
                loadPrizes(user.getUid(), currentGroup, wheelView, loadingOverlay, titleText);
            resultText.setText("👉  ？？？ ");
        });

        wheelView.setOnSpinUpdateListener(currentPrize -> {
            if (currentPrize != null) {
                resultText.setText("👉 " + currentPrize);
                resetButton.setEnabled(false);
                editPrizeButton.setEnabled(false);
                resetButton.setAlpha(0.3f);
                editPrizeButton.setAlpha(0.3f);
            } else {
                resetButton.setEnabled(true);
                editPrizeButton.setEnabled(true);
                resetButton.setAlpha(1f);
                editPrizeButton.setAlpha(1f);
            }
        });

        // 登入流程＋初始化群組
        if (user == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(result -> {
                        FirebaseUser newUser = auth.getCurrentUser();
                        if (newUser != null) {
                            checkOrCreateDefaultGroup(newUser.getUid(), wheelView, loadingOverlay, titleText);
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadedPrizes = Arrays.asList(defaultPrizes);
                        wheelView.setPrizes(defaultPrizes);
                        titleText.setText("今晚吃什麼？");
                        loadingOverlay.setVisibility(View.GONE);
                        isDataLoaded = true;
                    });
        } else {
            checkOrCreateDefaultGroup(user.getUid(), wheelView, loadingOverlay, titleText);
        }

        editPrizeButton.setOnClickListener(v -> {
            if (!isDataLoaded) {
                Toast.makeText(getContext(), "資料尚未載入完成", Toast.LENGTH_SHORT).show();
                return;
            }

            BottomSheetPrizeEditor editor = new BottomSheetPrizeEditor(
                    loadedPrizes,
                    updatedPrizes -> {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(currentUser.getUid())
                                    .collection("groups")  // ✅ 改為正確 collection
                                    .document(currentGroup)
                                    .set(Collections.singletonMap("prizes", updatedPrizes), SetOptions.merge())
                                    .addOnSuccessListener(unused -> {
                                        loadedPrizes = updatedPrizes;
                                        wheelView.setPrizes(loadedPrizes.toArray(new String[0]));
                                        Toast.makeText(getContext(), "已更新獎項", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "更新失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
            );
            editor.show(getParentFragmentManager(), editor.getTag());
        });

        groupButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SelectGroupActivity.class);
            selectGroupLauncher.launch(intent);
        });

        return view;
    }

    private void loadPrizes(String uid, String group, LuckyWheelView wheelView, FrameLayout overlay, TextView titleText) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("groups") // ✅ 改為正確 collection
                .document(group)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        loadedPrizes = (List<String>) snapshot.get("prizes");
                        wheelView.setPrizes(loadedPrizes.toArray(new String[0]));
                        String title = snapshot.getString("title");
                        titleText.setText(title != null ? title : "今晚吃什麼？");
                    } else {
                        loadedPrizes = Arrays.asList(defaultPrizes);
                        wheelView.setPrizes(defaultPrizes);
                        titleText.setText("今晚吃什麼？");
                    }
                    isDataLoaded = true;
                    overlay.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "載入失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    overlay.setVisibility(View.GONE);
                });
    }

    private void checkOrCreateDefaultGroup(String uid, LuckyWheelView wheelView, FrameLayout overlay, TextView titleText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(uid)
                .collection("groups")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // 建立預設群組
                        String groupId = UUID.randomUUID().toString();
                        Map<String, Object> defaultGroup = new HashMap<>();
                        defaultGroup.put("title", "預設群組");
                        defaultGroup.put("prizes", Arrays.asList("項目1", "項目2", "項目3"));

                        db.collection("users")
                                .document(uid)
                                .collection("groups")
                                .document(groupId)
                                .set(defaultGroup)
                                .addOnSuccessListener(unused -> {
                                    currentGroup = groupId;
                                    loadPrizes(uid, currentGroup, wheelView, overlay, titleText);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "建立預設群組失敗", Toast.LENGTH_SHORT).show();
                                    overlay.setVisibility(View.GONE);
                                });
                    } else {
                        // 有群組 → 用第一筆
                        DocumentSnapshot firstGroup = querySnapshot.getDocuments().get(0);
                        currentGroup = firstGroup.getId();
                        loadPrizes(uid, currentGroup, wheelView, overlay, titleText);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "檢查群組失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    overlay.setVisibility(View.GONE);
                });
    }

    private void updateTitle(FirebaseUser user, String group, TextView titleText) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("groups")
                .document(group)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && snapshot.contains("title")) {
                        titleText.setText(snapshot.getString("title"));
                    }
                });
    }
}
