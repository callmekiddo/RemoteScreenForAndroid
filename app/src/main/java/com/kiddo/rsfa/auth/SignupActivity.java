package com.kiddo.rsfa.auth;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.rsfa.R;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword, inputConfirmPassword;
    private MaterialButton buttonSignUp;
    private TextView textLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textLogin = findViewById(R.id.textLogin);

        // Quay về màn Login nếu đã có tài khoản
        textLogin.setOnClickListener(v -> finish());

        // Xử lý đăng ký (giả lập)
        buttonSignUp.setOnClickListener(v -> {
            // TODO: kiểm tra hợp lệ, gửi dữ liệu đăng ký
            finish(); // giả sử đăng ký thành công
        });
    }
}
