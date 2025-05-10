package com.kiddo.remotescreen.model;

public class ButtonData {
    public String name;
    public String keyFunction;
    public float leftRatio;
    public float topRatio;
    public float widthRatio;
    public float heightRatio;

    public ButtonData(String name, float left, float top, float width, float height, String keyFunction) {
        this.name = name;
        this.leftRatio = left;
        this.topRatio = top;
        this.widthRatio = width;
        this.heightRatio = height;
        this.keyFunction = keyFunction;
    }

    public String getName() {
        return name;
    }

    public String getKeyFunction() {
        return keyFunction;
    }

    public float getTopRatio() {
        return topRatio;
    }

    public float getLeftRatio() {
        return leftRatio;
    }

    public float getWidthRatio() {
        return widthRatio;
    }

    public float getHeightRatio() {
        return heightRatio;
    }
}
