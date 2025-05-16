package com.kiddo.remotescreen.ui.connect.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.kiddo.remotescreen.R;

public class PasswordDialog extends DialogFragment {

    public interface PasswordDialogListener {
        void onPasswordConfirmed(String pcId, String password);
    }

    private PasswordDialogListener listener;

    public static PasswordDialog newInstance(String pcId, PasswordDialogListener listener) {
        PasswordDialog dialog = new PasswordDialog();
        dialog.listener = listener;

        Bundle args = new Bundle();
        args.putString("pcId", pcId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String pcId = getArguments() != null ? getArguments().getString("pcId") : null;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_password_device, null);
        EditText input = view.findViewById(R.id.editTextPassword);

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("Connect", (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (!password.isEmpty()) {
                        if (listener != null) {
                            listener.onPasswordConfirmed(pcId, password);
                        }
                    } else {
                        Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}
