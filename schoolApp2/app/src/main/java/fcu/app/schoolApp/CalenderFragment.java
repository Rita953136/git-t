package fcu.app.schoolApp;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CalenderFragment extends Fragment {

    private FrameLayout numberContainer;
    private ConstraintLayout rootLayout;
    private MaterialButton buttonStart, buttonSetting;
    private Handler handler = new Handler();
    private boolean isAnimating = false;
    private List<View> previewViews = new ArrayList<>();

    private int min = 1, max = 100, count = 1;
    private int originalMin, originalMax, originalCount;

    private final int animationDuration = 2000;
    private final int updateInterval = 40;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calender, container, false);

        rootLayout = view.findViewById(R.id.layout_root);
        numberContainer = view.findViewById(R.id.number_container);
        buttonStart = view.findViewById(R.id.button_start);
        buttonSetting = view.findViewById(R.id.button_setting);

        buttonStart.setOnClickListener(v -> {
            if (!isAnimating) startRandomAnimation(count);
        });

        buttonSetting.setOnClickListener(v -> openSettingDialog());

        view.post(() -> previewNumberViews(count));

        return view;
    }

    private void startRandomAnimation(int targetCount) {
        isAnimating = true;
        Random random = new Random();
        long startTime = System.currentTimeMillis();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float fraction = (float) elapsed / animationDuration;
                float speedFactor = (float) (1 - Math.pow(2 * fraction - 1, 2));

                for (View view : previewViews) {
                    TextView tv = (TextView) ((CardView) view).getChildAt(0);
                    int num = random.nextInt(max - min + 1) + min;
                    tv.setText(String.valueOf(num));
                }

                if (elapsed < animationDuration) {
                    handler.postDelayed(this, (int) (updateInterval * (1.2 - speedFactor)));
                } else {
                    for (View view : previewViews) {
                        TextView tv = (TextView) ((CardView) view).getChildAt(0);
                        int finalNum = random.nextInt(max - min + 1) + min;
                        tv.setText(String.valueOf(finalNum));
                    }
                    isAnimating = false;
                }
            }
        });
    }

    private void previewNumberViews(int previewCount) {
        numberContainer.removeAllViews();
        previewViews.clear();

        int containerHeight = numberContainer.getHeight();
        int availableHeight = containerHeight - 40;
        if (availableHeight <= 0) availableHeight = 1000;

        if (previewCount == 1) {
            CardView card = createCardView();
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, 180);
            lp.topMargin = 100;
            lp.leftMargin = 40;
            lp.rightMargin = 40;
            card.setLayoutParams(lp);
            numberContainer.addView(card);
            previewViews.add(card);
        } else {
            int columns = (previewCount > 5) ? 2 : 1;
            int rows = (int) Math.ceil(previewCount / (double) columns);
            int heightPerItem = availableHeight / rows;

            for (int i = 0; i < previewCount; i++) {
                CardView card = createCardView();
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, heightPerItem - 20);
                lp.topMargin = (i % rows) * heightPerItem + 20;
                lp.leftMargin = (columns == 2 && i / rows == 1) ? rootLayout.getWidth() / 2 + 10 : 40;
                lp.rightMargin = 40;
                card.setLayoutParams(lp);
                numberContainer.addView(card);
                previewViews.add(card);
            }
        }
    }

    private CardView createCardView() {
        CardView card = new CardView(getContext());
        card.setCardBackgroundColor(Color.parseColor("#1F1F1F"));
        card.setRadius(16);
        card.setCardElevation(6);

        TextView tv = new TextView(getContext());
        tv.setText("?");
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(42);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        card.addView(tv);
        return card;
    }

    private void openSettingDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_random_setting);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            window.setAttributes(wlp);
        }

        EditText inputMin = dialog.findViewById(R.id.editMin);
        EditText inputMax = dialog.findViewById(R.id.editMax);
        EditText inputCount = dialog.findViewById(R.id.editCount);
        MaterialButton btnConfirm = dialog.findViewById(R.id.button_confirm);

        originalMin = min;
        originalMax = max;
        originalCount = count;

        inputMin.setText(String.valueOf(min));
        inputMax.setText(String.valueOf(max));
        inputCount.setText(String.valueOf(count));

        inputCount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int after) {
                try {
                    int tempCount = Integer.parseInt(s.toString());
                    if (tempCount > 0 && tempCount <= 10) {
                        previewNumberViews(tempCount);
                    }
                } catch (Exception ignored) {}
            }
        });

        btnConfirm.setOnClickListener(v -> {
            try {
                int newMin = Integer.parseInt(inputMin.getText().toString());
                int newMax = Integer.parseInt(inputMax.getText().toString());
                int newCount = Integer.parseInt(inputCount.getText().toString());

                if (newMin >= newMax || newCount <= 0 || newCount > 10) {
                    Toast.makeText(getContext(), "請輸入有效數值（最大值需大於最小值，組數1~10）", Toast.LENGTH_SHORT).show();
                } else {
                    min = newMin;
                    max = newMax;
                    count = newCount;
                    dialog.dismiss();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "請正確填寫欄位", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setOnDismissListener(d -> previewNumberViews(count));
        dialog.show();
    }
}
