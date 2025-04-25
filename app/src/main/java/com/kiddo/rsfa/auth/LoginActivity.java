package com.kiddo.rsfa.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.rsfa.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword;
    private MaterialButton buttonLogin;
    private TextView textResetPassword, textSignUp;
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textResetPassword = findViewById(R.id.textResetPassword);
        textSignUp = findViewById(R.id.textSignUp);
        buttonBack = findViewById(R.id.buttonBack);

        // Quay lại
        buttonBack.setOnClickListener(v -> finish());

        // Chuyển sang màn reset password
        textResetPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });

        // Chuyển sang màn đăng ký
        textSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        // Xử lý login (tạm thời mô phỏng login thành công)
        buttonLogin.setOnClickListener(v -> {
            // TODO: kiểm tra dữ liệu nhập
            // Nếu login thành công:
            // SessionManager.getInstance(this).setLoggedIn(true);
            finish(); // quay lại màn hình trước (more)
        });
    }
}
