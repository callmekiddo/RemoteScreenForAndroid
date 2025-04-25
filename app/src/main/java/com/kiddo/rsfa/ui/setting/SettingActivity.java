package com.kiddo.rsfa.ui.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kiddo.rsfa.R;

public class SettingActivity extends AppCompatActivity {

    private MaterialButton buttonChangeTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView textCurrentTheme = findViewById(R.id.textCurrentTheme);
        View btnTheme = findViewById(R.id.btnTheme);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        // Set current theme text
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                textCurrentTheme.setText(R.string.light);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                textCurrentTheme.setText(R.string.dark);
                break;
            default:
                textCurrentTheme.setText(R.string.follow_system);
        }

        // Handle theme change
        btnTheme.setOnClickListener(v -> {
            String[] themes = {getString(R.string.light), getString(R.string.dark), getString(R.string.follow_system)};

            int checked;

            switch (AppCompatDelegate.getDefaultNightMode()) {
                case AppCompatDelegate.MODE_NIGHT_NO:
                    checked = 0;
                    break;
                case AppCompatDelegate.MODE_NIGHT_YES:
                    checked = 1;
                    break;
                default:
                    checked = 2;
                    break;
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.choose_theme)
                    .setSingleChoiceItems(themes, checked, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                break;
                            case 1:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                break;
                            case 2:
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                break;
                        }
                        recreate();
                        dialog.dismiss();
                    }).show();
        });

        // Back
        buttonBack.setOnClickListener(v -> finish());
    }
}
