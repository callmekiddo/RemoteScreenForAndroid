package com.kiddo.remotescreen.model;

public class DeviceStatus {
    private final boolean allowRemote;
    private final String connectedAndroid;

    public DeviceStatus(boolean allowRemote, String connectedAndroid) {
        this.allowRemote = allowRemote;
        this.connectedAndroid = connectedAndroid;
    }

    public boolean getAllowRemote() {
        return allowRemote;
    }

    public String getConnectedAndroid() {
        return connectedAndroid;
    }
}
