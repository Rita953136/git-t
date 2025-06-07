
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView textUid = view.findViewById(R.id.textUid);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnEditName = view.findViewById(R.id.btnEditName);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (user != null) {
            String uid = user.getUid();

            firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String name = snapshot.contains("name") ? snapshot.getString("name") : "(未命名)";
                        textUid.setText(name);
                    })
                    .addOnFailureListener(e -> textUid.setText("(讀取失敗)"));

            btnEditName.setOnClickListener(v -> {
                EditText input = new EditText(getContext());
                input.setHint("輸入新名稱");

                new AlertDialog.Builder(getContext())
                        .setTitle("修改名稱")
                        .setView(input)
                        .setPositiveButton("儲存", (dialog, which) -> {
                            String newName = input.getText().toString();
                            if (!newName.isEmpty()) {
                                firestore.collection("users")
                                        .document(uid)
                                        .update("name", newName)
                                        .addOnSuccessListener(unused -> {
                                            textUid.setText(newName);
                                            Toast.makeText(getContext(), "✅ 名稱已更新", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "❌ 名稱更新失敗", Toast.LENGTH_SHORT).show());
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });

            btnChangePassword.setOnClickListener(v -> {
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
                EditText inputOld = dialogView.findViewById(R.id.etOldPassword);
                EditText inputNew = dialogView.findViewById(R.id.etNewPassword);
                EditText inputConfirm = dialogView.findViewById(R.id.etConfirmPassword);

                new AlertDialog.Builder(getContext())
                        .setTitle("修改密碼")
                        .setView(dialogView)
                        .setPositiveButton("確認", (dialog, which) -> {
                            String oldPass = inputOld.getText().toString();
                            String newPass = inputNew.getText().toString();
                            String confirmPass = inputConfirm.getText().toString();

                            if (!newPass.equals(confirmPass)) {
                                Toast.makeText(getContext(), "❌ 新密碼不一致", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
                            user.reauthenticate(credential).addOnSuccessListener(unused -> {
                                user.updatePassword(newPass).addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "✅ 密碼已更新", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "❌ 密碼更新失敗", Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "❌ 驗證失敗：舊密碼錯誤", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });

            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            });
        }

        return view;
    }
}
