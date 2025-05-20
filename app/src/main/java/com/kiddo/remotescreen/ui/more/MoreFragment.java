package com.kiddo.remotescreen.ui.more;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.auth.LoginActivity;
import com.kiddo.remotescreen.model.UpdatePasswordRequest;
import com.kiddo.remotescreen.repository.ChangePasswordRepository;
import com.kiddo.remotescreen.repository.LogoutRepository;
import com.kiddo.remotescreen.ui.more.setting.SettingActivity;
import com.kiddo.remotescreen.util.SessionManager;

public class MoreFragment extends Fragment {

    private View buttonLoginAccount, buttonAccount, buttonSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonLoginAccount = view.findViewById(R.id.btnLoginAccount);
        buttonAccount = view.findViewById(R.id.btnAccount);
        buttonSettings = view.findViewById(R.id.btnSettings);

        updateUI();

        buttonLoginAccount.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
        });

        buttonSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingActivity.class)));

        buttonAccount.setOnClickListener(v -> showProfileDialog());
    }

    private void updateUI() {
        SessionManager session = SessionManager.getInstance(requireContext());
        boolean isLoggedIn = session.isLoggedIn();

        buttonLoginAccount.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        buttonAccount.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);

        if (isLoggedIn) {
            if (buttonAccount instanceof ViewGroup && ((ViewGroup) buttonAccount).getChildCount() > 1) {
                View view = ((ViewGroup) buttonAccount).getChildAt(1);
                if (view instanceof TextView) {
                    ((TextView) view).setText("Hello, " + session.getUserFullName());
                }
            }
        }
    }

    private void showProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile, null);
        TextView textFullName = dialogView.findViewById(R.id.textFullName);
        TextView textEmail = dialogView.findViewById(R.id.textEmail);
        TextView textLogout = dialogView.findViewById(R.id.textLogout);
        TextView textChangePassword = dialogView.findViewById(R.id.textChangePassword);

        SessionManager session = SessionManager.getInstance(requireContext());
        textFullName.setText(session.getUserFullName());
        textEmail.setText(session.getUserEmail());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        LogoutRepository logoutRepository = new LogoutRepository();

        textLogout.setOnClickListener(v -> {
            logoutRepository.logout(
                    session.getUserEmail(),
                    session.getAuthToken(), // ✅ gửi token vào header Authorization
                    new LogoutRepository.LogoutCallback() {
                        @Override
                        public void onSuccess() {
                            session.clearSession();
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                updateUI();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Logout failed: " + error, Toast.LENGTH_LONG).show()
                            );
                        }
                    }
            );
        });

        textChangePassword.setOnClickListener(v -> {
            View changePassView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
            TextInputEditText inputNew = changePassView.findViewById(R.id.inputNewPassword);
            TextInputEditText inputConfirm = changePassView.findViewById(R.id.inputConfirmPassword);
            MaterialButton buttonChange = changePassView.findViewById(R.id.buttonChangePassword);

            AlertDialog changeDialog = new AlertDialog.Builder(requireContext())
                    .setView(changePassView)
                    .create();

            buttonChange.setOnClickListener(bv -> {
                String newPass = inputNew.getText() != null ? inputNew.getText().toString().trim() : "";
                String confirmPass = inputConfirm.getText() != null ? inputConfirm.getText().toString().trim() : "";

                if (newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                UpdatePasswordRequest request = new UpdatePasswordRequest(
                        session.getUserEmail(), newPass, confirmPass
                );

                new ChangePasswordRepository().changePassword(request, session.getAuthToken(), new ChangePasswordRepository.ChangePasswordCallback() {
                    @Override
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Password changed successfully. You have been logged out.", Toast.LENGTH_SHORT).show();

                            // Clear session local
                            session.clearSession();

                            // Dismiss cả 2 dialog
                            changeDialog.dismiss();
                            dialog.dismiss();

                            // Cập nhật giao diện MoreFragment
                            updateUI();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Change password failed: " + error, Toast.LENGTH_LONG).show()
                        );
                    }
                });
            });

            changeDialog.show();
        });

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}
