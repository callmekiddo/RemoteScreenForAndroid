package com.kiddo.remotescreen.auth;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.RegisterRequest;
import com.kiddo.remotescreen.repository.AuthRepository;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText inputUsername, inputEmail, inputPassword, inputConfirmPassword;
    private MaterialButton buttonSignUp;
    private TextView textLogin;

    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        inputUsername = findViewById(R.id.inputUsername);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textLogin = findViewById(R.id.textLogin);

        textLogin.setOnClickListener(v -> finish());

        buttonSignUp.setOnClickListener(v -> handleSignup());
    }

    private void handleSignup() {
        String name = inputUsername.getText() != null ? inputUsername.getText().toString().trim() : "";
        String email = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";
        String password = inputPassword.getText() != null ? inputPassword.getText().toString().trim() : "";
        String confirmPassword = inputConfirmPassword.getText() != null ? inputConfirmPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(name, email, password);

        authRepository.signup(request, new AuthRepository.SignupCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(SignupActivity.this,
                            "Registered successfully! Please check your email to activate your account.",
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(SignupActivity.this, error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
