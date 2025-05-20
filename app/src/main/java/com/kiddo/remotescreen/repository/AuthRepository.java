package com.kiddo.remotescreen.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kiddo.remotescreen.model.ForgotPasswordRequest;
import com.kiddo.remotescreen.model.LoginRequest;
import com.kiddo.remotescreen.model.LoginResponse;
import com.kiddo.remotescreen.model.RegisterRequest;

import java.io.IOException;
import okhttp3.*;

public class AuthRepository {
    private static final String BASE_URL = "http://vanjustkiddo.click:8080/auth";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public void login(LoginRequest request, LoginCallback callback) {
        RequestBody body = RequestBody.create(
                gson.toJson(request),
                MediaType.parse("application/json")
        );

        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = gson.fromJson(response.body().string(), LoginResponse.class);
                    callback.onSuccess(loginResponse);
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        String message = json.has("description") ? json.get("description").getAsString() : "Wrong email or password";
                        callback.onError(message);
                    } catch (Exception e) {
                        callback.onError("Wrong email or password");
                    }
                }
            }
        });
    }

    public void signup(RegisterRequest request, SignupCallback callback) {
        RequestBody body = RequestBody.create(
                gson.toJson(request),
                MediaType.parse("application/json")
        );

        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/signup")
                .post(body)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
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
                        callback.onError("Signup failed: " + message);
                    } catch (Exception e) {
                        callback.onError("Signup failed: " + responseBody);
                    }
                }
            }
        });
    }

    public void forgotPassword(ForgotPasswordRequest request, ForgotPasswordCallback callback) {
        RequestBody body = RequestBody.create(
                gson.toJson(request),
                MediaType.parse("application/json")
        );

        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/forgot-password")
                .post(body)
                .build();

        client.newCall(httpRequest).enqueue(new Callback() {
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
                        callback.onError("Forgot password failed: " + message);
                    } catch (Exception e) {
                        callback.onError("Forgot password failed: " + responseBody);
                    }
                }
            }
        });
    }

    public interface SignupCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }

    public interface ForgotPasswordCallback {
        void onSuccess();
        void onError(String error);
    }
}
