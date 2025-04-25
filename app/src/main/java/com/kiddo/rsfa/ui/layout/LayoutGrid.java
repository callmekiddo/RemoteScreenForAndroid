package com.kiddo.rsfa.ui.layout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kiddo.rsfa.R;
import com.kiddo.rsfa.adapter.layout.LayoutItemAdapter;
import com.kiddo.rsfa.model.LayoutItem;
import com.kiddo.rsfa.ui.layout.editor.LayoutEditor;

import java.util.Arrays;
import java.util.List;

public class LayoutGrid extends Fragment {

    public LayoutGrid() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton buttonAction = view.findViewById(R.id.buttonLayoutAction);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewLayouts);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        List<LayoutItem> layoutItems = Arrays.asList(
                new LayoutItem("Mouse", R.drawable.ic_launcher_background),
                new LayoutItem("Keyboard", R.drawable.ic_launcher_background),
                new LayoutItem("Keyboard+Mouse", R.drawable.ic_launcher_background)
        );

        LayoutItemAdapter adapter = new LayoutItemAdapter(layoutItems, item -> {
            Toast.makeText(getContext(), "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Chuyển sang màn LayoutFragment để chỉnh sửa nếu cần
        });

        recyclerView.setAdapter(adapter);

        buttonAction.setOnClickListener(this::showLayoutOptions);
    }

    private void showLayoutOptions(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_button_add_option, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_new_layout) {
                Intent intent = new Intent(requireContext(), LayoutEditor.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.menu_import_layout) {
                // TODO: Xử lý import layout từ file
                return true;
            }
            return false;
        });

        popup.show();
    }
}