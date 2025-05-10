package com.kiddo.remotescreen.auth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.remotescreen.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText inputEmail;
    private MaterialButton buttonReset;
    private TextView textLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        inputEmail = findViewById(R.id.inputEmail);
        buttonReset = findViewById(R.id.buttonResetPassword);
        textLogin = findViewById(R.id.textLogin);

        textLogin.setOnClickListener(v -> finish());

        buttonReset.setOnClickListener(v -> {
            // TODO: thực hiện gửi yêu cầu reset mật khẩu
            finish(); // giả lập thành công
        });
    }
}
