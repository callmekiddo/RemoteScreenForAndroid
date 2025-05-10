package com.kiddo.remotescreen.model;

import java.util.List;

public class LayoutInfo {
    public String name;
    public String iconPath;
    public List<ButtonData> buttons;

    public LayoutInfo(String name, String iconPath, List<ButtonData> buttons) {
        this.name = name;
        this.iconPath = iconPath;
        this.buttons = buttons;
    }
}
