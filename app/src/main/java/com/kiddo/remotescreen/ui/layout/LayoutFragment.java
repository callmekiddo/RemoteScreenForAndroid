package com.kiddo.remotescreen.ui.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kiddo.remotescreen.R;

public class LayoutFragment extends Fragment {

    public LayoutFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton buttonAdd = view.findViewById(R.id.buttonLayoutAction);

        buttonAdd.setOnClickListener(v -> {
            showAddLayoutDialog();
        });
    }

    private void showAddLayoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setItems(new String[]{"New Layout", "Import Layout"}, (dialog, which) -> {
                    if (which == 0) {
                        // TODO: xử lý tạo layout mới
                        // Gợi ý: mở giao diện tạo phím ảo
                    } else if (which == 1) {
                        // TODO: xử lý import layout từ file
                        // Gợi ý: mở FileChooser để chọn file .json
                    }
                })
                .show();
    }
}

