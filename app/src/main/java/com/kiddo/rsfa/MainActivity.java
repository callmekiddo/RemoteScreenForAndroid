package com.kiddo.rsfa;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kiddo.rsfa.ui.connect.ConnectFragment;
import com.kiddo.rsfa.ui.control.ControlFragment;

public class MainActivity extends AppCompatActivity {

    private final Fragment connectFragment = new ConnectFragment();
    private final Fragment controlFragment = new ControlFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_connect) {
                loadFragment(new ConnectFragment());
                return true;
            } else if (id == R.id.nav_control) {
                loadFragment(new ControlFragment());
                return true;
            }

            return false;
        });

        // Load mặc định là ConnectFragment
        bottomNav.setSelectedItemId(R.id.nav_connect);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
