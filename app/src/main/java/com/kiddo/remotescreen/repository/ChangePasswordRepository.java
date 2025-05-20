package com.kiddo.remotescreen.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kiddo.remotescreen.model.UpdatePasswordRequest;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordRepository {

    private static final String BASE_URL = "http://vanjustkiddo.click:8080/api/v1/user/update-password";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public void changePassword(UpdatePasswordRequest requestObject, String token, ChangePasswordCallback callback) {
        RequestBody body = RequestBody.create(
                gson.toJson(requestObject),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .put(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        String message = json.has("description") ? json.get("description").getAsString() : "Unexpected error";
                        callback.onError("Change password failed: " + message);
                    } catch (Exception e) {
                        callback.onError("Change password failed: " + responseBody);
                    }
                }
            }
        });
    }

    public interface ChangePasswordCallback {
        void onSuccess();
        void onError(String error);
    }
}
