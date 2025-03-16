package io.github.kurok1;

import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestTemplateTest {

    private final RequestTemplate requestTemplate = new RequestTemplate("localhost", 8848, false);

    @org.junit.jupiter.api.Test
    void postForm() throws Exception {
        requestTemplate.postForm("nacos/v1/auth/users/login", Map.of("username", "nacos", "password", "nacos"));
    }

    @Test
    void testGetToken() throws Exception {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username","nacos")
                .addFormDataPart("password","nacos")
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:8848/nacos/v1/auth/users/login")
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }
}