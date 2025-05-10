package com.kiddo.remotescreen.model;

public class LayoutItem {
    private final String name;
    private final String iconPath;

    public LayoutItem(String name, String iconPath) {
        this.name = name;
        this.iconPath = iconPath;
    }

    public String getName() {
        return name;
    }

    public String getIconPath() {
        return iconPath;
    }
}

