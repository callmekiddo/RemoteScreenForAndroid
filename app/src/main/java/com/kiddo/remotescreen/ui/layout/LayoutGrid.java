package com.kiddo.remotescreen.ui.layout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.adapter.layout.LayoutItemAdapter;
import com.kiddo.remotescreen.model.LayoutInfo;
import com.kiddo.remotescreen.model.LayoutItem;
import com.kiddo.remotescreen.ui.layout.editor.LayoutEditor;
import com.kiddo.remotescreen.ui.layout.editor.dialog.LayoutOptionDialog;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LayoutGrid extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewLayouts);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        List<LayoutItem> layoutItems = loadLayouts();
        LayoutItemAdapter adapter = new LayoutItemAdapter(layoutItems, item -> {
            new LayoutOptionDialog(requireContext(), item, () -> {
                Intent intent = new Intent(requireContext(), LayoutEditor.class);
                intent.putExtra("layout_name", item.getName());
                startActivity(intent);
            }, () -> {
                // Delete → xoá file
                File file = new File(requireContext().getFilesDir(), "layouts/" + item.getName() + ".json");
                if (file.exists()) {
                    file.delete();
                    Toast.makeText(requireContext(), "Deleted " + item.getName(), Toast.LENGTH_SHORT).show();
                    // Gợi ý: reload fragment để cập nhật danh sách
                    requireActivity().recreate(); // hoặc adapter.notifyDataSetChanged()
                }
            }).show();
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.buttonLayoutAction);
        fab.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_button_add_option, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_new_layout) {
                    startActivity(new Intent(requireContext(), LayoutEditor.class));
                    return true;
                } else if (id == R.id.menu_import_layout) {
                    Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadLayoutList(); // cập nhật lại danh sách sau khi quay về
    }

    private List<LayoutItem> loadLayouts() {
        List<LayoutItem> layoutItems = new ArrayList<>();
        File layoutDir = new File(requireContext().getFilesDir(), "layouts");
        File[] files = layoutDir.listFiles((dir, name) -> name.endsWith(".json"));

        Gson gson = new Gson();
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    LayoutInfo info = gson.fromJson(reader, LayoutInfo.class);
                    layoutItems.add(new LayoutItem(info.name, info.iconPath));
                } catch (Exception e) {
                    Log.e("LayoutGrid", "Failed to load layout: " + file.getName(), e);
                }
            }
        }
        return layoutItems;
    }

    private void reloadLayoutList() {
        List<LayoutItem> layoutItems = loadLayouts();
        LayoutItemAdapter adapter = new LayoutItemAdapter(layoutItems, item -> {
            new LayoutOptionDialog(requireContext(), item, () -> {
                Intent intent = new Intent(requireContext(), LayoutEditor.class);
                intent.putExtra("layout_name", item.getName());
                startActivity(intent);
            }, () -> {
                File file = new File(requireContext().getFilesDir(), "layouts/" + item.getName() + ".json");
                if (file.exists()) {
                    file.delete();
                    Toast.makeText(requireContext(), "Deleted " + item.getName(), Toast.LENGTH_SHORT).show();
                    reloadLayoutList(); // cập nhật sau khi xoá
                }
            }).show();
        });
        RecyclerView recyclerView = requireView().findViewById(R.id.recyclerViewLayouts);
        recyclerView.setAdapter(adapter);
    }

}
