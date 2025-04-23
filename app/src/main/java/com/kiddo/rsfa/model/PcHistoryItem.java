package com.kiddo.rsfa.model;

public class PcHistoryItem {
    private final String name;
    private final boolean isOnline;

    public PcHistoryItem(String name, boolean isOnline) {
        this.name = name;
        this.isOnline = isOnline;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return isOnline;
    }
}
