package fcu.app.schoolApp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private TextView tvGoLogin, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_signin_email);
        etPassword = findViewById(R.id.et_signin_password);
        btnLogin = findViewById(R.id.btn_signin);
        tvGoLogin = findViewById(R.id.tv_go_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password); // 👈 請確保 layout 有這個 ID

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.getEmail() != null) {
                                Toast.makeText(LoginActivity.this, "登入成功：" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "登入成功，但無法取得使用者資訊", Toast.LENGTH_SHORT).show();
                            }

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            String errorMessage = "登入失敗，請再試一次";
                            if (task.getException() != null) {
                                errorMessage = "登入失敗：" + task.getException().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 註冊跳轉
        SpannableString spannable = new SpannableString("尚未有帳號？點我註冊");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ds.linkColor);
            }
        };
        spannable.setSpan(clickableSpan, 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvGoLogin.setText(spannable);
        tvGoLogin.setMovementMethod(LinkMovementMethod.getInstance());
        1
        SpannableString forgotSpan = new SpannableString("忘記密碼？");
        ClickableSpan forgotClickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showForgotPasswordDialog();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ds.linkColor);
            }
        };
        forgotSpan.setSpan(forgotClickable, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvForgotPassword.setText(forgotSpan);
        tvForgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("重設密碼");

        final EditText input = new EditText(this);
        input.setHint("請輸入註冊 Email");
        builder.setView(input);

        builder.setPositiveButton("送出", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "已寄出重設密碼連結", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "錯誤：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(LoginActivity.this, "請輸入 Email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
