package com.kiddo.remotescreen.ui.connect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.adapter.connect.PcHistoryAdapter;
import com.kiddo.remotescreen.model.PcHistoryItem;

import java.util.Arrays;
import java.util.List;

public class ConnectFragment extends Fragment {

    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Danh sách mẫu - sau này bạn có thể thay bằng dữ liệu thật
        List<PcHistoryItem> pcList = Arrays.asList(
                new PcHistoryItem("DESKTOP-1", true),
                new PcHistoryItem("DESKTOP-2", false),
                new PcHistoryItem("DESKTOP-3", false),
                new PcHistoryItem("DESKTOP-4", false),
                new PcHistoryItem("DESKTOP-5", false),
                new PcHistoryItem("DESKTOP-6", false),
                new PcHistoryItem("DESKTOP-7", false)
        );

        PcHistoryAdapter adapter = new PcHistoryAdapter(pcList, item -> {
            Toast.makeText(getContext(), "Connecting to: " + item.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Thực hiện kết nối thực sự tại đây
        });

        recyclerView.setAdapter(adapter);
    }

}

