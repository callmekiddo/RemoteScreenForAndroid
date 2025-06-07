package com.kiddo.remotescreen.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.kiddo.remotescreen.R;

import java.io.File;
import java.util.List;

public class LayoutInfo {
    public String name;
    public String iconPath;
    public List<ButtonData> buttons;

    public LayoutInfo() {
    }

    public LayoutInfo(String name, String iconPath, List<ButtonData> buttons) {
        this.name = name;
        this.iconPath = iconPath;
        this.buttons = buttons;
    }

    public String getName() {
        return name;
    }

    public String getIconPath() {
        return iconPath;
    }

    public List<ButtonData> getButtons() {
        return buttons;
    }

    public Bitmap getIconBitmap(Context context) {
        if (iconPath == null || iconPath.isEmpty()) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.layout);
        }

        File iconFile = new File(context.getFilesDir(), "icons/" + iconPath);
        if (!iconFile.exists()) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.layout);
        }

        return BitmapFactory.decodeFile(iconFile.getAbsolutePath());
    }
}
