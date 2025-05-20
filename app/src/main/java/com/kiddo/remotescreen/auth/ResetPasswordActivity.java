package com.kiddo.remotescreen.auth;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.ForgotPasswordRequest;
import com.kiddo.remotescreen.repository.AuthRepository;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText inputEmail;
    private MaterialButton buttonReset;
    private TextView textLogin;
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        inputEmail = findViewById(R.id.inputEmail);
        buttonReset = findViewById(R.id.buttonResetPassword);
        textLogin = findViewById(R.id.textLogin);

        textLogin.setOnClickListener(v -> finish());

        buttonReset.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            if (email.isEmpty()) {
                inputEmail.setError("Email is required");
                return;
            }

            ForgotPasswordRequest request = new ForgotPasswordRequest(email);
            authRepository.forgotPassword(request, new AuthRepository.ForgotPasswordCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(ResetPasswordActivity.this, "Check your email for reset instructions", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_LONG).show());
                }
            });
        });
    }
}