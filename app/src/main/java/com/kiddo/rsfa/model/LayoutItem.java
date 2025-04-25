package com.kiddo.rsfa.model;

public class LayoutItem {
    private final String name;
    private final int iconRes;

    public LayoutItem(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getName() {
        return name;
    }

    public int getIconRes() {
        return iconRes;
    }
}
