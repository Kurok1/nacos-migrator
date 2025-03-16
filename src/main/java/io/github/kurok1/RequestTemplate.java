package io.github.kurok1;

import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:khanc.dev@gmail.com">韩超</a>
 * @since 1.0
 */
public class RequestTemplate {

    private final String host;
    private final int port;
    private final boolean secure;

    private final OkHttpClient client = new OkHttpClient();

    private final Map<String, String> headers = new HashMap<>();

    public RequestTemplate(String host, int port) {
        this(host, port, false);
    }

    public RequestTemplate(String host, int port, boolean secure) {
        this.host = host;
        this.port = port;
        this.secure = secure;
    }

    public void addHeader(String headerKey, String headerValue) {
        this.headers.put(headerKey, headerValue);
    }

    public String getUrl(String path, Map<String, String> params) {
        String baseUrl = "";
        if (secure) {
            baseUrl = String.format("https://%s:%d/%s", host, port, path);
        } else baseUrl = String.format("http://%s:%d/%s", host, port, path);
        if (params != null && !params.isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);  // 删除最后一个 &
            return urlBuilder.toString();
        } else {
            return baseUrl;
        }
    }

    public Response get(String path) throws IOException {
        return get(path, Collections.emptyMap());
    }

    public Response get(String path, Map<String, String> params) throws IOException {
        String url = getUrl(path, params);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return client.newCall(request).execute();
    }

    public String postForm(String path, Map<String, String> form) {
        String url = getUrl(path, new HashMap<>());

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        form.forEach(bodyBuilder::addFormDataPart);

        RequestBody requestBody = bodyBuilder.build();
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        headers.forEach(requestBuilder::addHeader);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void postFormAndFile(String path, Map<String, String> form, Path file) throws IOException {
        String url = getUrl(path, form);
        RequestBody fileBody = RequestBody.create(Files.readAllBytes(file), MediaType.parse("application/zip"));

        // 构建 Multipart 请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)  // multipart/form-data
                .addFormDataPart("file", file.getFileName().toString(), fileBody) // 参数名 "file"，文件名，文件体
                .build();
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        headers.forEach(requestBuilder::addHeader);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            System.out.println(response.body().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void post(String path, Object body) throws IOException {
        post(path, body, Collections.emptyMap());
    }

    public void post(String path, Object body, Map<String, String> params) throws IOException {
        String url = getUrl(path, params);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        }
    }

}
