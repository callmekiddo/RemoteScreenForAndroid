package com.kiddo.rsfa.app;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kiddo.rsfa.R;
import com.kiddo.rsfa.ui.connect.ConnectFragment;
import com.kiddo.rsfa.ui.control.ControlFragment;
import com.kiddo.rsfa.ui.layout.LayoutGrid;
import com.kiddo.rsfa.ui.more.MoreFragment;

public class MainActivity extends AppCompatActivity{

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Gắn fragment mặc định (Connect)
        if (savedInstanceState == null) {
            loadFragment(new ConnectFragment());
        }

        // Xử lý chuyển fragment khi chọn bottom nav
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_connect) {
                loadFragment(new ConnectFragment());
                return true;
            } else if (id == R.id.nav_control) {
                loadFragment(new ControlFragment());
                return true;
            } else if (id == R.id.nav_layout) {
                loadFragment(new LayoutGrid());
                return true;
            } else if (id == R.id.nav_more) {
                loadFragment(new MoreFragment());
                return true;
            }

            return false;
        });

        // Ẩn/hiện bottom nav khi mở bàn phím
        setupKeyboardVisibilityHandler();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setupKeyboardVisibilityHandler() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.1) {
                bottomNavigationView.setVisibility(View.GONE); // Bàn phím mở
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE); // Bàn phím đóng
            }
        });
    }
}
