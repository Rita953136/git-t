package fcu.app.schoolApp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WheelActivity extends AppCompatActivity {

    private LuckyWheelView wheelView;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);

        wheelView = findViewById(R.id.wheelView);
        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> wheelView.startSpin());
    }
}
