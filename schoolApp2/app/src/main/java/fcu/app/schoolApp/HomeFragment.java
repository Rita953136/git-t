package fcu.app.schoolApp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    String[] defaultPrizes = {
            "炒飯", "乾麵", "麥當勞", "便當", "鹹酥雞", "滷味", "鐵板燒"
    };
    private boolean isDataLoaded = false;
    private List<String> loadedPrizes = new ArrayList<>();

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
        FrameLayout loadingOverlay = view.findViewById(R.id.loadingOverlay);
        TextView titleText = view.findViewById(R.id.textTitle);
        TextView resultText = view.findViewById(R.id.textResult);
        Button resetButton = view.findViewById(R.id.btnResetWheel);

        loadingOverlay.setVisibility(View.VISIBLE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //reset 轉盤（從 Firebase 再載入）
        resetButton.setOnClickListener(v -> {
            FirebaseUser userNow = FirebaseAuth.getInstance().getCurrentUser();
            if (userNow != null) {
                loadPrizes(userNow.getUid(), wheelView, loadingOverlay, titleText);
                resultText.setText("👉  ？？？ ");
            }
        });

        //監聽轉動過程更新顯示
        wheelView.setOnSpinUpdateListener(currentPrize -> {
            if (currentPrize != null) {
                resultText.setText("👉 " + currentPrize);
                resetButton.setEnabled(false);
                resetButton.setAlpha(0.3f);
                editPrizeButton.setEnabled(false);
                editPrizeButton.setAlpha(0.3f);
            } else {
                resetButton.setEnabled(true);
                resetButton.setAlpha(1f);
                editPrizeButton.setEnabled(true);
                editPrizeButton.setAlpha(1f);
            }
        });

        // 載入資料（登入或匿名登入）
        if (user == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(result ->
                            loadPrizes(auth.getCurrentUser().getUid(), wheelView, loadingOverlay, titleText)
                    )
                    .addOnFailureListener(e -> {
                        // 🔁 匿名登入失敗才 fallback 預設
                        loadedPrizes = Arrays.asList(defaultPrizes);
                        wheelView.setPrizes(defaultPrizes);
                        titleText.setText("今晚吃什麼？");
                        loadingOverlay.setVisibility(View.GONE);
                        isDataLoaded = true;
                        Toast.makeText(getContext(), "匿名登入失敗，使用預設資料", Toast.LENGTH_SHORT).show();
                    });
        } else {
            loadPrizes(user.getUid(), wheelView, loadingOverlay, titleText);
        }

        // 編輯按鈕開啟 BottomSheet
        editPrizeButton.setOnClickListener(v -> {
            if (!isDataLoaded) {
                Toast.makeText(getContext(), "資料尚未載入完成，請稍候", Toast.LENGTH_SHORT).show();
                return;
            }

            BottomSheetPrizeEditor editor = new BottomSheetPrizeEditor(
                    new ArrayList<>(loadedPrizes),
                    currentPrizes -> {
                        wheelView.setPrizes(currentPrizes.toArray(new String[0]));
                        loadedPrizes = currentPrizes;

                        // ⬅️ 更新標題
                        FirebaseUser userReload = FirebaseAuth.getInstance().getCurrentUser();
                        if (userReload != null) {
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userReload.getUid())
                                    .get()
                                    .addOnSuccessListener(snapshot -> {
                                        if (snapshot.exists() && snapshot.contains("title")) {
                                            String newTitle = snapshot.getString("title");
                                            titleText.setText(newTitle);
                                        }
                                    });
                        }
                    });
            editor.show(getParentFragmentManager(), editor.getTag());
        });

        return view;
    }

    // 載入 Firestore 中的使用者資料
    private void loadPrizes(String uid, LuckyWheelView wheelView, FrameLayout loadingOverlay, TextView titleText) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        if (snapshot.contains("prizes")) {
                            loadedPrizes = (List<String>) snapshot.get("prizes");
                            wheelView.setPrizes(loadedPrizes.toArray(new String[0]));
                        }

                        if (snapshot.contains("title")) {
                            String title = snapshot.getString("title");
                            if (title != null && !title.isEmpty()) {
                                titleText.setText(title);
                            }
                        }

                        isDataLoaded = true;
                    }
                    loadingOverlay.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ 載入失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingOverlay.setVisibility(View.GONE);
                });
    }
}
