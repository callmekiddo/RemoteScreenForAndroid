package com.kiddo.remotescreen.ui.layout.editor.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.model.LayoutItem;

import java.io.File;

public class LayoutOptionDialog extends Dialog {

    public LayoutOptionDialog(@NonNull Context context, LayoutItem item, Runnable onEdit, Runnable onDelete) {
        super(context);
        setContentView(R.layout.dialog_layout_option);

        TextView name = findViewById(R.id.textLayoutName);
        ImageView icon = findViewById(R.id.iconLayout);
        ImageButton btnEdit = findViewById(R.id.btnEdit);
        ImageButton btnDelete = findViewById(R.id.btnDelete);
        ImageButton btnClose = findViewById(R.id.btnClose);

        name.setText(item.getName());

        File iconFile = new File(context.getFilesDir(), "icons/" + item.getIconPath());
        if (iconFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
            icon.setImageBitmap(bitmap);
        } else {
            icon.setImageResource(R.drawable.layout);
        }

        btnEdit.setOnClickListener(v -> {
            dismiss();
            onEdit.run();
        });

        btnDelete.setOnClickListener(v -> {
            dismiss();
            onDelete.run();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }
}