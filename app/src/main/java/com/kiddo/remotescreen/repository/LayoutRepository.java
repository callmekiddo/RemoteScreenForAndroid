package com.kiddo.remotescreen.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.kiddo.remotescreen.model.LayoutInfo;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LayoutRepository {

    public static List<LayoutInfo> loadAll(Context context) {
        File layoutDir = new File(context.getFilesDir(), "layouts");
        if (!layoutDir.exists() || !layoutDir.isDirectory()) return new ArrayList<>();

        List<LayoutInfo> result = new ArrayList<>();
        Gson gson = new Gson();

        for (File file : layoutDir.listFiles()) {
            if (file.getName().endsWith(".json")) {
                try {
                    LayoutInfo layout = gson.fromJson(new FileReader(file), LayoutInfo.class);
                    result.add(layout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}
