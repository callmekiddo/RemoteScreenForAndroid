package com.kiddo.remotescreen.repository;

import com.kiddo.remotescreen.model.DeviceStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceRepository {
    private static final String BASE_URL = "http://vanjustkiddo.click:8080/device";

    public static void connectToPc(String pcId, String pcPassword, String androidDeviceName, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("deviceId", pcId);
            json.put("password", pcPassword);
            json.put("androidDeviceName", androidDeviceName);
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(
                json.toString(), MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/connect-android")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(response.body().string());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    String deviceId = jsonResponse.getString("deviceId");
                    String deviceName = jsonResponse.getString("deviceName");

                    callback.onSuccess(deviceId, deviceName);
                } catch (Exception e) {
                    callback.onError("Invalid response: " + e.getMessage());
                }
            }
        });
    }

    public static void getDeviceStatus(String pcId, DeviceStatusCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL + "/status/" + pcId)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code());
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    boolean allowRemote = json.getBoolean("allowRemote");
                    String connectedAndroid = json.optString("connectedAndroid", null);
                    callback.onSuccess(new DeviceStatus(allowRemote, connectedAndroid));
                } catch (JSONException e) {
                    callback.onError("Invalid JSON: " + e.getMessage());
                }
            }
        });
    }

    public static void disconnectAndroid(String deviceId, SimpleCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL + "/disconnect-android?deviceId=" + deviceId)
                .post(RequestBody.create(new byte[0], null)) // POST rỗng
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("HTTP " + response.code() + ": " + response.body().string());
                }
            }
        });
    }


    public interface DeviceStatusCallback {
        void onSuccess(DeviceStatus status);
        void onError(String error);
    }

    public interface Callback {
        void onSuccess(String deviceId, String deviceName);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
