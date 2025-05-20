package com.kiddo.remotescreen.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.LoginRequest;
import com.kiddo.remotescreen.model.LoginResponse;
import com.kiddo.remotescreen.repository.AuthRepository;
import com.kiddo.remotescreen.util.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword;
    private MaterialButton buttonLogin;
    private TextView textResetPassword, textSignUp;
    private ImageButton buttonBack;

    private final AuthRepository authRepository = new AuthRepository();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = SessionManager.getInstance(this);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textResetPassword = findViewById(R.id.textResetPassword);
        textSignUp = findViewById(R.id.textSignUp);
        buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> finish());

        textResetPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class))
        );

        textSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );

        buttonLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);
        authRepository.login(request, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                session.saveUserSession(
                        response.token(),
                        response.fullName(),
                        response.expiresIn(),
                        email
                );

                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    finish(); // back to MoreFragment
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
