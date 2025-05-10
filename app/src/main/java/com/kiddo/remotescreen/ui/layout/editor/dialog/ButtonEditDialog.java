package com.kiddo.remotescreen.ui.layout.editor.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.kiddo.remotescreen.R;
import com.kiddo.remotescreen.ui.layout.editor.popup.KeyboardFunctionPopup;
import com.kiddo.remotescreen.ui.layout.editor.popup.MouseFunctionPopup;

import java.util.Locale;

public class ButtonEditDialog extends DialogFragment {

    private Button targetButton;
    private View buttonView;

    public ButtonEditDialog(Button targetButton, View buttonView) {
        this.targetButton = targetButton;
        this.buttonView = buttonView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(R.drawable.background_dialog);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        View view = inflater.inflate(R.layout.dialog_button_edit, container, false);

        EditText inputName = view.findViewById(R.id.editButtonName);
        TextView textTitle = view.findViewById(R.id.textTitle);
        ImageButton btnClose = view.findViewById(R.id.btnCloseEditor);
        FrameLayout functionSlot = view.findViewById(R.id.functionSlot);

        textTitle.setText("Button");
        inputName.setText(targetButton.getText().toString());
        inputName.requestFocus();

        inputName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                targetButton.setText(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClose.setOnClickListener(v -> dismiss());

        String keyFunction = (String) buttonView.getTag(R.id.keyFunction);

        if (keyFunction != null) {
            String[] parts = keyFunction.split("_", 2);
            if (parts.length == 2) {
                String type = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase();
                String value = parts[1];
                setFunctionView(type, value, functionSlot);
            } else {
                functionSlot.addView(createAddButton(functionSlot));
            }
        } else {
            functionSlot.addView(createAddButton(functionSlot));
        }

        setupDimension(view, R.id.controlLeft, "Left");
        setupDimension(view, R.id.controlTop, "Top");
        setupDimension(view, R.id.controlWidth, "Width");
        setupDimension(view, R.id.controlHeight, "Height");

        return view;
    }

    private void setFunctionView(String type, String value, FrameLayout slot) {
        slot.removeAllViews();

        View functionView = View.inflate(getContext(), R.layout.item_key_function, null);
        TextView text = functionView.findViewById(R.id.textFunctionName);
        ImageButton remove = functionView.findViewById(R.id.btnRemoveFunction);

        String keyFunctionValue = (type + "_" + value).toUpperCase();
        String label = type + " " + value;
        text.setText(label);

        buttonView.setTag(R.id.keyFunction, keyFunctionValue);

        remove.setOnClickListener(v -> {
            slot.removeView(functionView);
            slot.addView(createAddButton(slot));
            buttonView.setTag(R.id.keyFunction, null);
        });

        slot.addView(functionView);
    }

    private View createAddButton(FrameLayout slot) {
        ImageButton add = new ImageButton(requireContext());
        add.setImageResource(R.drawable.ic_add);
        add.setBackgroundResource(android.R.color.transparent);
        add.setContentDescription("Add function");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        add.setLayoutParams(params);

        add.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_key_function, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_mouse) {
                    new MouseFunctionPopup(requireActivity(), selected ->
                            setFunctionView("Mouse", selected, slot)
                    ).show();
                } else if (item.getItemId() == R.id.menu_keyboard) {
                    new KeyboardFunctionPopup(requireActivity(), selected ->
                            setFunctionView("Keyboard", selected, slot)
                    ).show();
                }
                return true;
            });

            popup.show();
        });

        return add;
    }

    private void setupDimension(View parent, int controlId, String label) {
        View control = parent.findViewById(controlId);
        if (control == null) return;

        TextView textLabel = control.findViewById(R.id.textLabel);
        EditText editValue = control.findViewById(R.id.editValue);
        SeekBar seekBar = control.findViewById(R.id.seekBar);
        textLabel.setText(label);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) buttonView.getLayoutParams();
        ViewGroup canvas = (ViewGroup) buttonView.getParent();
        if (canvas == null) return;

        float canvasWidth = canvas.getWidth();
        float canvasHeight = canvas.getHeight();

        float ratio = switch (label) {
            case "Left" -> params.leftMargin / canvasWidth;
            case "Top" -> params.topMargin / canvasHeight;
            case "Width" -> buttonView.getWidth() / canvasWidth;
            case "Height" -> buttonView.getHeight() / canvasHeight;
            default -> 0f;
        };

        int progress = (int) (ratio * 100);
        seekBar.setProgress(progress);
        editValue.setText(String.format(Locale.US, "%.2f", ratio));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (label.equals("Width") || label.equals("Height")) {
                    progress = Math.max(progress, 6);
                }

                float newRatio = progress / 100f;

                switch (label) {
                    case "Left" -> {
                        int maxLeft = canvas.getWidth() - buttonView.getWidth();
                        params.leftMargin = Math.min((int) (canvasWidth * newRatio), maxLeft);
                    }
                    case "Top" -> {
                        int maxTop = canvas.getHeight() - buttonView.getHeight();
                        params.topMargin = Math.min((int) (canvasHeight * newRatio), maxTop);
                    }
                    case "Width" -> buttonView.getLayoutParams().width = (int) (canvasWidth * newRatio);
                    case "Height" -> buttonView.getLayoutParams().height = (int) (canvasHeight * newRatio);
                }

                buttonView.setLayoutParams(buttonView.getLayoutParams());
                editValue.setText(String.format(Locale.US, "%.2f", newRatio));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        editValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    float inputRatio = Float.parseFloat(editValue.getText().toString());
                    if (label.equals("Width") || label.equals("Height")) {
                        inputRatio = Math.max(0.06f, Math.min(1f, inputRatio));
                    } else {
                        inputRatio = Math.max(0f, Math.min(1f, inputRatio));
                    }
                    int progressFromInput = (int) (inputRatio * 100);
                    seekBar.setProgress(progressFromInput);
                } catch (NumberFormatException ignored) {}
            }
        });
    }
}
