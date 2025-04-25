package com.kiddo.rsfa.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kiddo.rsfa.R;
import com.kiddo.rsfa.auth.LoginActivity;
import com.kiddo.rsfa.ui.account.AccountActivity;
import com.kiddo.rsfa.ui.setting.SettingActivity;
import com.kiddo.rsfa.util.SessionManager;

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

        buttonAccount.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AccountActivity.class)));
    }

    private void updateUI() {
        boolean isLoggedIn = SessionManager.getInstance(requireContext()).isLoggedIn();
        buttonLoginAccount.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        buttonAccount.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}
