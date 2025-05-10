package com.kiddo.remotescreen.ui.layout.editor.component;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.ui.layout.editor.LayoutEditor;
import com.kiddo.remotescreen.ui.layout.editor.dialog.ButtonEditDialog;

public class ButtonComponent {

    private final Activity activity;
    private final FrameLayout canvas;
    private final View buttonContainer;
    private final Button coreButton;

    public ButtonComponent(Activity activity) {
        this.activity = activity;
        this.canvas = activity.findViewById(R.id.layoutEditorCanvas);
        LayoutInflater inflater = LayoutInflater.from(activity);
        this.buttonContainer = inflater.inflate(R.layout.view_editable_button, canvas, false);
        this.coreButton = buttonContainer.findViewById(R.id.coreButton);

        setup();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setup() {
        if (coreButton != null) {
            coreButton.setText("Button");
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(250, 150);
        params.leftMargin = 100;
        params.topMargin = 100;
        buttonContainer.setLayoutParams(params);

        ImageView setupIcon = buttonContainer.findViewById(R.id.btnEditSetup);
        if (setupIcon != null) {
            setupIcon.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(activity, v);
                menu.getMenuInflater().inflate(R.menu.menu_editable_button, menu.getMenu());

                menu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.item_edit) {
                        if (activity instanceof AppCompatActivity compatActivity) {
                            new ButtonEditDialog(coreButton, buttonContainer)
                                    .show(compatActivity.getSupportFragmentManager(), "EditDialog");
                        }
                    } else if (id == R.id.item_delete) {
                        canvas.removeView(buttonContainer);
                    }
                    return true;
                });

                menu.show();
            });
        }

        buttonContainer.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private int lastAction;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!(activity instanceof LayoutEditor layoutEditor) || !layoutEditor.isEditMode())
                    return false;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = event.getRawX() - view.getX();
                        dY = event.getRawY() - view.getY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() - dX;
                        float newY = event.getRawY() - dY;

                        newX = Math.max(0, Math.min(newX, canvas.getWidth() - view.getWidth()));
                        newY = Math.max(0, Math.min(newY, canvas.getHeight() - view.getHeight()));

                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                        params.leftMargin = (int) newX;
                        params.topMargin = (int) newY;
                        view.setLayoutParams(params);
                        lastAction = MotionEvent.ACTION_MOVE;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            view.performClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });

        coreButton.setOnTouchListener((v, event) -> {
            if (((LayoutEditor) activity).isEditMode()) return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Toast.makeText(activity, "Test button clicked!", Toast.LENGTH_SHORT).show();
                v.performClick();
            }
            return false;
        });
    }

    public void addToCanvas() {
        canvas.addView(buttonContainer);
    }

    public Button getTargetButton() {
        return coreButton;
    }

    public View getContainerView() {
        return buttonContainer;
    }
}
