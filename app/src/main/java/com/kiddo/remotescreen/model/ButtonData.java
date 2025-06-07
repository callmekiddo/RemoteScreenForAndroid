package com.kiddo.remotescreen.model;

import java.util.Collections;
import java.util.List;

public class ButtonData {
    private String name;
    private float leftRatio;
    private float topRatio;
    private float widthRatio;
    private float heightRatio;
    private List<KeyFunction> functions;

    public ButtonData(String name, float left, float top, float width, float height, List<KeyFunction> functions) {
        this.name = name;
        this.leftRatio = left;
        this.topRatio = top;
        this.widthRatio = width;
        this.heightRatio = height;
        this.functions = functions;
    }

    // Constructor chỉ với 1 function
    public ButtonData(String name, float left, float top, float width, float height, KeyFunction function) {
        this.name = name;
        this.leftRatio = left;
        this.topRatio = top;
        this.widthRatio = width;
        this.heightRatio = height;
        this.functions = function != null ? Collections.singletonList(function) : null;
    }

    public String getName() {
        return name;
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

    public List<KeyFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(List<KeyFunction> functions) {
        this.functions = functions;
    }

    public KeyFunction getFunction() {
        return (functions != null && !functions.isEmpty()) ? functions.get(0) : null;
    }
}
