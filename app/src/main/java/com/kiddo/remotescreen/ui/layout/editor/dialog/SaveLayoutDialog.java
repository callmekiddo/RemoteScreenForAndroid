package com.kiddo.remotescreen.ui.layout.editor.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.ButtonData;
import com.kiddo.remotescreen.model.LayoutInfo;
import com.kiddo.remotescreen.ui.layout.editor.LayoutEditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class SaveLayoutDialog extends Dialog {

    private final LayoutEditor activity;
    private String currentIconFileName = null;
    private EditText inputName;

    public SaveLayoutDialog(LayoutEditor activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_save_layout);

        ImageButton btnClose = findViewById(R.id.btnCloseSave);
        Button btnSave = findViewById(R.id.btnSaveLayout);
        ImageButton btnPickIcon = findViewById(R.id.btnPickIcon);
        inputName = findViewById(R.id.inputLayoutName);

        btnClose.setOnClickListener(v -> dismiss());

        btnPickIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activity.getImagePickerLauncher().launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String layoutName = inputName.getText().toString().trim();
            if (layoutName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter layout name", Toast.LENGTH_SHORT).show();
                return;
            }

            File dir = new File(getContext().getFilesDir(), "layouts");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, layoutName + ".json");

            if (file.exists()) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Overwrite layout?")
                        .setMessage("A layout with the same name already exists. Do you want to overwrite it?")
                        .setPositiveButton("Overwrite", (dialog, which) -> saveLayout(file, layoutName))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                saveLayout(file, layoutName);
            }
        });
    }

    public void onImagePicked(ActivityResult result) {
        if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
            Uri selectedImage = result.getData().getData();
            try (InputStream in = getContext().getContentResolver().openInputStream(selectedImage)) {
                File iconsDir = new File(getContext().getFilesDir(), "icons");
                if (!iconsDir.exists()) iconsDir.mkdirs();

                String fileName = "icon_" + System.currentTimeMillis() + ".png";
                File outFile = new File(iconsDir, fileName);

                try (OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                currentIconFileName = fileName;
                Toast.makeText(getContext(), "Icon selected", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to save icon", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveLayout(File file, String layoutName) {
        List<ButtonData> layoutData = activity.getAllButtonData();
        String iconPath = (currentIconFileName != null) ? currentIconFileName : "layout.png";
        LayoutInfo info = new LayoutInfo(layoutName, iconPath, layoutData);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(info);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            Toast.makeText(getContext(), "Saved to " + file.getName(), Toast.LENGTH_SHORT).show();
            dismiss();
            activity.finish(); // trở lại layout list
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save layout", Toast.LENGTH_LONG).show();
        }
    }
}
