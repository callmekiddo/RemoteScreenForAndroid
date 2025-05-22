package com.kiddo.remotescreen.ui.connect;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiddo.remotescreen.model.PcHistoryItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PcHistoryStorage {

    private static final String PREF_NAME = "pc_history";
    private static final String KEY_LIST = "list";

    public static void add(Context context, PcHistoryItem item) {
        List<PcHistoryItem> list = load(context);

        // Nếu đã có → cập nhật trạng thái
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(item.getId())) {
                list.remove(i);
                break;
            }
        }

        list.add(0, item); // thêm vào đầu
        save(context, list);
    }

    public static List<PcHistoryItem> load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LIST, null);
        if (json != null) {
            Type type = new TypeToken<List<PcHistoryItem>>(){}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }

    private static void save(Context context, List<PcHistoryItem> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(list);
        prefs.edit().putString(KEY_LIST, json).apply();
    }
}
