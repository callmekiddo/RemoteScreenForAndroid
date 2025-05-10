package com.kiddo.remotescreen.ui.layout.editor;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.ButtonData;
import com.kiddo.remotescreen.model.LayoutInfo;
import com.kiddo.remotescreen.ui.layout.editor.component.ButtonComponent;
import com.kiddo.remotescreen.ui.layout.editor.dialog.SaveLayoutDialog;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LayoutEditor extends AppCompatActivity {

    private boolean isEditMode = true;
    private final List<ButtonComponent> components = new ArrayList<>();
    private SaveLayoutDialog saveLayoutDialog;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String originalJson = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_layout_editor);
        hideSystemUI();
        setupOptionsMenu();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (saveLayoutDialog != null) {
                        saveLayoutDialog.onImagePicked(result);
                    }
                });

        if (getIntent().hasExtra("layout_name")) {
            String layoutName = getIntent().getStringExtra("layout_name");

            View canvas = findViewById(R.id.layoutEditorCanvas);
            canvas.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    loadLayoutFromFile(layoutName); // ⬅️ delay việc load đến khi canvas có kích thước thật
                }
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    new AlertDialog.Builder(LayoutEditor.this)
                            .setTitle("Unsaved changes")
                            .setMessage("You have unsaved changes. Do you want to save them?")
                            .setPositiveButton("Save", (dialog, which) -> {
                                saveLayoutDialog = new SaveLayoutDialog(LayoutEditor.this);
                                saveLayoutDialog.show();
                            })
                            .setNegativeButton("Don't Save", (dialog, which) -> finish())
                            .setNeutralButton("Cancel", null)
                            .show();
                } else {
                    finish(); // xử lý mặc định
                }
            }
        });
    }

    public ActivityResultLauncher<Intent> getImagePickerLauncher() {
        return imagePickerLauncher;
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void setupOptionsMenu() {
        ImageButton buttonSetup = findViewById(R.id.buttonSetup);
        buttonSetup.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(LayoutEditor.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_layout_editor_options, popup.getMenu());

            MenuItem modeItem = popup.getMenu().findItem(R.id.menu_switch_mode);
            if (modeItem != null) {
                modeItem.setTitle(isEditMode ? "Switch to test mode" : "Switch to edit mode");
            }

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (!isEditMode && (itemId == R.id.menu_add_button || itemId == R.id.menu_add_joystick || itemId == R.id.menu_add_touchpad)) {
                    Toast.makeText(this, "Cannot add in test mode", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (itemId == R.id.menu_add_button) {
                    ButtonComponent button = new ButtonComponent(this);
                    button.addToCanvas();
                    components.add(button);
                } else if (itemId == R.id.menu_add_joystick) {
                    Toast.makeText(this, "Add joystick", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.menu_add_touchpad) {
                    Toast.makeText(this, "Add touchpad", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.menu_save) {
                    saveLayoutDialog = new SaveLayoutDialog(this);
                    saveLayoutDialog.show();
                } else if (itemId == R.id.menu_switch_mode) {
                    isEditMode = !isEditMode;
                    updateEditModeUI();
                }

                return true;
            });

            popup.show();
        });
    }

    private boolean hasUnsavedChanges() {
        List<ButtonData> current = getAllButtonData();
        LayoutInfo currentInfo = new LayoutInfo("temp", "layout.png", current);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String currentJson = gson.toJson(currentInfo);

        return originalJson == null || !originalJson.equals(currentJson);
    }

    private void updateEditModeUI() {
        View canvas = findViewById(R.id.layoutEditorCanvas);
        if (!(canvas instanceof ViewGroup group)) return;
        int count = group.getChildCount();

        for (int i = 0; i < count; i++) {
            View buttonWrapper = group.getChildAt(i);
            View editOverlay = buttonWrapper.findViewById(R.id.editOverlayButton);
            View settingIcon = buttonWrapper.findViewById(R.id.btnEditSetup);

            if (isEditMode) {
                if (editOverlay != null) {
                    editOverlay.setVisibility(View.VISIBLE);
                    editOverlay.setBackgroundResource(R.drawable.background_editable_button);
                }
                if (settingIcon != null) settingIcon.setVisibility(View.VISIBLE);
            } else {
                if (editOverlay != null) editOverlay.setBackground(null);
                if (settingIcon != null) settingIcon.setVisibility(View.GONE);
            }
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public List<ButtonData> getAllButtonData() {
        List<ButtonData> list = new ArrayList<>();
        ViewGroup canvas = findViewById(R.id.layoutEditorCanvas);

        float canvasWidth = canvas.getWidth();
        float canvasHeight = canvas.getHeight();

        for (int i = 0; i < canvas.getChildCount(); i++) {
            View wrapper = canvas.getChildAt(i);
            Button button = wrapper.findViewById(R.id.coreButton);

            if (button == null) continue;

            String name = button.getText().toString();
            float leftRatio = wrapper.getX() / canvasWidth;
            float topRatio = wrapper.getY() / canvasHeight;
            float widthRatio = wrapper.getWidth() / canvasWidth;
            float heightRatio = wrapper.getHeight() / canvasHeight;

            Object keyObj = wrapper.getTag(R.id.keyFunction);
            String keyFunction = keyObj instanceof String ? (String) keyObj : null;

            ButtonData data = new ButtonData(name, leftRatio, topRatio, widthRatio, heightRatio, keyFunction);
            list.add(data);
        }

        return list;
    }

    private void loadLayoutFromFile(String layoutName) {
        try {
            File file = new File(getFilesDir(), "layouts/" + layoutName + ".json");
            if (!file.exists()) return;

            Gson gson = new Gson();
            LayoutInfo info = gson.fromJson(new FileReader(file), LayoutInfo.class);
            originalJson = gson.toJson(info); // ✅ lưu trạng thái ban đầu
            ViewGroup canvas = findViewById(R.id.layoutEditorCanvas);

            float canvasWidth = canvas.getWidth();
            float canvasHeight = canvas.getHeight();

            for (ButtonData data : info.buttons) {
                ButtonComponent button = new ButtonComponent(this);
                View view = button.getContainerView();

                ViewGroup.LayoutParams rawParams = view.getLayoutParams();
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(rawParams);

                params.width = (int) (canvasWidth * data.getWidthRatio());
                params.height = (int) (canvasHeight * data.getHeightRatio());
                params.leftMargin = (int) (canvasWidth * data.getLeftRatio());
                params.topMargin = (int) (canvasHeight * data.getTopRatio());

                view.setLayoutParams(params);
                button.getTargetButton().setText(data.getName());
                button.getContainerView().setTag(R.id.keyFunction, data.getKeyFunction());
                button.addToCanvas();
                components.add(button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
